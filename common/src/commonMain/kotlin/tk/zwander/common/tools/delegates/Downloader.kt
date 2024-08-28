package tk.zwander.common.tools.delegates

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.tools.CryptUtils
import tk.zwander.common.tools.FusClient
import tk.zwander.common.tools.Request
import tk.zwander.common.tools.VersionFetch
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.Event
import tk.zwander.common.util.FileManager
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.time.ExperimentalTime

object Downloader {
    interface DownloadErrorCallback {
        fun onError(info: DownloadErrorInfo)
    }

    data class DownloadErrorInfo(
        val message: String,
        val callback: DownloadErrorConfirmCallback,
    )

    data class DownloadErrorConfirmCallback(
        val onAccept: suspend () -> Unit,
        val onCancel: suspend () -> Unit,
    )

    suspend fun onDownload(
        model: DownloadModel,
        confirmCallback: DownloadErrorCallback
    ) {
        eventManager.sendEvent(Event.Download.Start)
        model.statusText.value = MR.strings.downloading()

        val info = Request.retrieveBinaryFileInfo(
            fw = model.fw.value,
            model = model.model.value,
            region = model.region.value,
            imeiSerial = model.imeiSerial.value,
            onVersionException = { exception, info ->
                confirmCallback.onError(
                    info = DownloadErrorInfo(
                        message = exception.message!!,
                        callback = DownloadErrorConfirmCallback(
                            onAccept = {
                                performDownload(info!!, model)
                            },
                            onCancel = {
                                model.endJob("")
                                eventManager.sendEvent(Event.Download.Finish)
                            },
                        )
                    ),
                )
            },
            onFinish = {
                model.endJob(it)
                eventManager.sendEvent(Event.Download.Finish)
            },
            shouldReportError = {
                !model.manual.value
            },
        )

        if (info != null) {
            performDownload(info, model)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun performDownload(info: BinaryFileInfo, model: DownloadModel) {
        try {
            val (path, fileName, size, crc32, v4Key) = info
            val request = Request.createBinaryInit(fileName, FusClient.getNonce())

            FusClient.makeReq(FusClient.Request.BINARY_INIT, request)

            val fullFileName = fileName.replace(
                ".zip",
                "_${model.fw.value.replace("/", "_")}_${model.region.value}.zip",
            )

            val decryptionKeyFileName = if (BifrostSettings.Keys.enableDecryptKeySave()) {
                "DecryptionKey_${fullFileName}.txt"
            } else {
                null
            }

            val downloadDirectory = FileManager.pickDirectory()
            val encFile = downloadDirectory?.child(fullFileName, false)
            val decFile = downloadDirectory?.child(
                fullFileName.replace(".enc2", "")
                .replace(".enc4", ""),
                false,
            )
            val decKeyFile = downloadDirectory?.let { dir ->
                decryptionKeyFileName?.let { dec ->
                    dir.child(dec, false)
                }
            }

            if (downloadDirectory != null) {
                decKeyFile?.openOutputStream(false)?.use { output ->
                    if (fullFileName.endsWith(".enc2")) {
                        output.write(
                            CryptUtils.getV2Key(
                                model.fw.value,
                                model.model.value,
                                model.region.value,
                            ).second.toByteArray(),
                        )
                    }

                    v4Key?.let {
                        output.write(v4Key.second.toByteArray())
                    }
                }

                val outputStream = encFile?.openOutputStream(true) ?: return
                val md5 = try {
                    FusClient.downloadFile(
                        path + fileName,
                        encFile.getLength(),
                        size,
                        outputStream,
                        encFile.getLength(),
                    ) { current, max, bps ->
                        model.progress.value = current to max
                        model.speed.value = bps

                        eventManager.sendEvent(
                            Event.Download.Progress(
                                MR.strings.downloading(),
                                current,
                                max,
                            )
                        )
                    }
                } finally {
                    outputStream.flush()
                    outputStream.close()
                }

                if (crc32 != null) {
                    model.speed.value = 0L
                    model.statusText.value = MR.strings.checkingCRC()
                    val result = CryptUtils.checkCrc32(
                        encFile.openInputStream() ?: return,
                        encFile.getLength(),
                        crc32,
                    ) { current, max, bps ->
                        model.progress.value = current to max
                        model.speed.value = bps

                        eventManager.sendEvent(
                            Event.Download.Progress(
                                MR.strings.checkingCRC(),
                                current,
                                max,
                            )
                        )
                    }

                    if (!result) {
                        model.endJob(MR.strings.crcCheckFailed())
                        return
                    }
                }

                if (md5 != null) {
                    model.speed.value = 0L
                    model.statusText.value = MR.strings.checkingMD5()

                    eventManager.sendEvent(
                        Event.Download.Progress(
                            MR.strings.checkingMD5(),
                            0,
                            1
                        )
                    )

                    val result = withContext(Dispatchers.Default) {
                        CryptUtils.checkMD5(
                            md5,
                            encFile.openInputStream(),
                        )
                    }

                    if (!result) {
                        model.endJob(MR.strings.md5CheckFailed())
                        return
                    }
                }

                model.speed.value = 0L
                model.statusText.value = MR.strings.decrypting()

                val key =
                    if (fullFileName.endsWith(".enc2")) {
                        CryptUtils.getV2Key(
                            model.fw.value,
                            model.model.value,
                            model.region.value,
                        ).first
                    } else {
                        info.v4Key?.first!!
                    }

                CryptUtils.decryptProgress(
                    encFile.openInputStream() ?: return,
                    decFile?.openOutputStream() ?: return,
                    key,
                    size,
                ) { current, max, bps ->
                    model.progress.value = current to max
                    model.speed.value = bps

                    eventManager.sendEvent(
                        Event.Download.Progress(
                            MR.strings.decrypting(),
                            current,
                            max
                        )
                    )
                }

                if (BifrostSettings.Keys.autoDeleteEncryptedFirmware()) {
                    encFile.delete()
                }

                model.endJob(MR.strings.done())
            } else {
                model.endJob("")
            }
        } catch (e: Throwable) {
            val message = if (e !is CancellationException) "${e.message}" else ""
            model.endJob(message)
        }

        eventManager.sendEvent(Event.Download.Finish)
    }

    suspend fun onFetch(model: DownloadModel) {
        model.statusText.value = ""
        model.changelog.value = null
        model.osCode.value = ""

        val (fw, os, error, output) = VersionFetch.getLatestVersion(
            model.model.value,
            model.region.value,
        )

        if (error != null) {
            model.endJob(
                MR.strings.firmwareCheckError(
                    error.message.toString(),
                    output.replace("\t", "  ")
                )
            )
            return
        }

        model.changelog.value = ChangelogHandler.getChangelog(
            model.model.value,
            model.region.value,
            fw.split("/")[0],
        )

        model.fw.value = fw
        model.osCode.value = os

        model.endJob("")
    }
}