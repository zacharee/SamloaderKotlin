package tk.zwander.common.tools.delegates

import io.ktor.utils.io.core.toByteArray
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.tools.CryptUtils
import tk.zwander.common.tools.Request
import tk.zwander.common.util.Event
import tk.zwander.common.util.FileManager
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.model.DecryptModel
import tk.zwander.samloaderkotlin.resources.MR

object Decrypter {
    suspend fun onDecrypt(model: DecryptModel) {
        eventManager.sendEvent(Event.Decrypt.Start)
        val info = model.fileToDecrypt.value!!
        val inputFile = info.encFile
        val outputFile = info.decFile
        val decKey = model.decryptionKey.value

        try {
            val key = when {
                decKey.isNotBlank() -> CryptUtils.md5Provider.hasher().hash(decKey.toByteArray())
                inputFile.getName().endsWith(".enc2") -> {
                    CryptUtils.getV2Key(
                        model.fw.value,
                        model.model.value,
                        model.region.value,
                    ).first
                }
                else -> {
                    try {
                        val binaryFileInfo = Request.retrieveBinaryFileInfo(
                            fw = model.fw.value,
                            model = model.model.value,
                            region = model.region.value,
                            imeiSerial = model.imeiSerial.value,
                            onFinish = {
                                model.endJob(it)
                                eventManager.sendEvent(Event.Decrypt.Finish)
                            },
                        )

                        if (binaryFileInfo != null) {
                            binaryFileInfo.v4Key?.first!!
                        } else {
                            return
                        }
                    } catch (e: Throwable) {
                        println("Unable to retrieve v4 key ${e.message}.")
                        model.endJob(MR.strings.decryptError(e.message.toString()))
                        return
                    }
                }
            }

            val inputStream = try {
                inputFile.openInputStream() ?: return
            } catch (e: Throwable) {
                println("Unable to open input file ${e.message}.")
                model.endJob(MR.strings.decryptError(e.message.toString()))
                return
            }

            val outputStream = try {
                outputFile.openOutputStream() ?: return
            } catch (e: Throwable) {
                println("Unable to open output file ${e.message}.")
                model.endJob(MR.strings.decryptError(e.message.toString()))
                return
            }

            CryptUtils.decryptProgress(
                inputStream,
                outputStream,
                key,
                inputFile.getLength()
            ) { current, max, bps ->
                model.progress.value = current to max
                model.speed.value = bps
                eventManager.sendEvent(Event.Decrypt.Progress(MR.strings.decrypting(), current, max))
            }

            eventManager.sendEvent(Event.Decrypt.Finish)
            model.endJob(MR.strings.done())
        } catch (e: Throwable) {
            eventManager.sendEvent(Event.Decrypt.Finish)
            model.endJob(MR.strings.decryptError((e.localizedMessage ?: e.message).toString()))
        }
    }

    suspend fun onOpenFile(model: DecryptModel) {
        val encFile = FileManager.pickFile()
        val decFile = encFile?.let {
            FileManager.saveFile(
                encFile.getName().replace(".enc2", "")
                    .replace(".enc4", ""),
            )
        }
        val info = decFile?.let { DecryptFileInfo(encFile, decFile) }

        handleFileInput(model, info)
    }

    fun handleFileInput(model: DecryptModel, info: DecryptFileInfo?) {
        if (info != null) {
            if (!info.encFile.getName().endsWith(".enc2") &&
                !info.encFile.getName().endsWith(".enc4")) {
                model.endJob(MR.strings.selectEncrypted())
            } else {
                model.endJob("")
                model.fileToDecrypt.value = info
            }
        } else {
            model.endJob("")
        }
    }
}