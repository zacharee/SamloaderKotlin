package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import tk.zwander.common.GradleConfig
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.exception.VersionException
import tk.zwander.common.exceptions.DownloadError
import tk.zwander.common.tools.*
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.Event
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.view.components.*
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.time.ExperimentalTime

/**
 * The FusClient for retrieving firmware.
 */
@OptIn(DangerousInternalIoApi::class)
val client = FusClient()

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

@OptIn(DangerousInternalIoApi::class)
private suspend fun onDownload(
    model: DownloadModel,
    client: FusClient,
    confirmCallback: DownloadErrorCallback
) {
    eventManager.sendEvent(Event.Download.Start)
    model.statusText.value = MR.strings.downloading()

    val (info, error, output, requestBody) = Request.getBinaryFile(
        client,
        model.fw.value ?: "",
        model.model.value ?: "",
        model.region.value ?: "",
        model.imeiSerial.value ?: "",
    )

    if (error != null && error !is VersionException) {
        Exception(error).printStackTrace()
        model.endJob("${error.message ?: MR.strings.error()}\n\n${output}")
        CrossPlatformBugsnag.notify(DownloadError(requestBody, output, error))
        eventManager.sendEvent(Event.Download.Finish)
    } else {
        if (error is VersionException) {
            confirmCallback.onError(
                info = DownloadErrorInfo(
                    message = error.message!!,
                    callback = DownloadErrorConfirmCallback(
                        onAccept = {
                            performDownload(info!!, model, client)
                        },
                        onCancel = {
                            model.endJob("")
                            eventManager.sendEvent(Event.Download.Finish)
                        }
                    )
                )
            )
        } else {
            performDownload(info!!, model, client)
        }
    }
}

@OptIn(DangerousInternalIoApi::class, ExperimentalTime::class)
private suspend fun performDownload(info: BinaryFileInfo, model: DownloadModel, client: FusClient) {
    coroutineScope {
        val (path, fileName, size, crc32, v4Key) = info
        val request = Request.createBinaryInit(fileName, client.getNonce())

        client.makeReq(FusClient.Request.BINARY_INIT, request)

        val fullFileName = fileName.replace(
            ".zip",
            "_${model.fw.value?.replace("/", "_")}_${model.region.value}.zip",
        )

        eventManager.sendEvent(
            Event.Download.GetInput(fullFileName) { inputInfo ->
                try {
                    if (inputInfo != null) {
                        val (response, md5) = client.downloadFile(
                            path + fileName,
                            inputInfo.downloadFile.getLength(),
                        )

                        Downloader.download(
                            response,
                            size,
                            inputInfo.downloadFile.openOutputStream(true),
                            inputInfo.downloadFile.getLength(),
                        ) { current, max, bps ->
                            model.progress.value = current to max
                            model.speed.value = bps

                            eventManager.sendEvent(Event.Download.Progress(MR.strings.downloading(), current, max))
                        }

                        if (crc32 != null) {
                            model.speed.value = 0L
                            model.statusText.value = MR.strings.checkingCRC()
                            val result = CryptUtils.checkCrc32(
                                inputInfo.downloadFile.openInputStream(),
                                size,
                                crc32,
                            ) { current, max, bps ->
                                model.progress.value = current to max
                                model.speed.value = bps

                                eventManager.sendEvent(Event.Download.Progress(MR.strings.checkingCRC(), current, max))
                            }

                            if (!result) {
                                model.endJob(MR.strings.crcCheckFailed())
                                return@GetInput
                            }
                        }

                        if (md5 != null) {
                            model.speed.value = 0L
                            model.statusText.value = MR.strings.checkingMD5()

                            eventManager.sendEvent(Event.Download.Progress(MR.strings.checkingMD5(), 0, 1))

                            val result = withContext(Dispatchers.Default) {
                                CryptUtils.checkMD5(
                                    md5,
                                    inputInfo.downloadFile.openInputStream(),
                                )
                            }

                            if (!result) {
                                model.endJob(MR.strings.md5CheckFailed())
                                return@GetInput
                            }
                        }

                        model.speed.value = 0L
                        model.statusText.value = MR.strings.decrypting()

                        val key =
                            if (fullFileName.endsWith(".enc2")) CryptUtils.getV2Key(
                                model.fw.value ?: "",
                                model.model.value ?: "",
                                model.region.value ?: "",
                            ) else {
                                v4Key ?: CryptUtils.getV4Key(
                                    client,
                                    model.fw.value ?: "",
                                    model.model.value ?: "",
                                    model.region.value ?: "",
                                    model.imeiSerial.value ?: "",
                                )
                            }

                        CryptUtils.decryptProgress(
                            inputInfo.downloadFile.openInputStream(),
                            inputInfo.decryptFile.openOutputStream(),
                            key,
                            size,
                        ) { current, max, bps ->
                            model.progress.value = current to max
                            model.speed.value = bps

                            eventManager.sendEvent(Event.Download.Progress(MR.strings.decrypting(), current, max))
                        }

                        if (BifrostSettings.Keys.autoDeleteEncryptedFirmware() == true) {
                            inputInfo.downloadFile.delete()
                        }

                        model.endJob(MR.strings.done())
                    } else {
                        model.endJob("")
                    }
                } catch (e: Throwable) {
                    if (e is CancellationException) {
                        return@GetInput
                    }

                    model.endJob("${e.message}")
                }
            }
        )

        eventManager.sendEvent(Event.Download.Finish)
    }
}

private suspend fun onFetch(model: DownloadModel) {
    val (fw, os, error, output) = VersionFetch.getLatestVersion(model.model.value ?: "", model.region.value ?: "")

    if (error != null) {
        model.endJob(MR.strings.firmwareCheckError(error.message.toString(), output))
        return
    }

    model.changelog.value = ChangelogHandler.getChangelog(model.model.value ?: "", model.region.value ?: "", fw.split("/")[0])

    model.fw.value = fw
    model.osCode.value = os

    model.endJob("")
}

/**
 * The Downloader View.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
internal fun DownloadView() {
    val model = LocalDownloadModel.current

    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
    val manual by model.manual.collectAsState()
    val modelModel by model.model.collectAsState()
    val region by model.region.collectAsState()
    val fw by model.fw.collectAsState()
    val osCode by model.osCode.collectAsState()
    val progress by model.progress.collectAsState()
    val statusText by model.statusText.collectAsState()
    val changelog by model.changelog.collectAsState()
    val changelogExpanded by model.changelogExpanded.collectAsState()

    val canCheckVersion = manual == false && !modelModel.isNullOrBlank()
            && !region.isNullOrBlank() && !hasRunningJobs

    val canDownload = !modelModel.isNullOrBlank() && !region.isNullOrBlank() && !fw.isNullOrBlank()
            && !hasRunningJobs

    val canChangeOption = !hasRunningJobs

    val scope = rememberCoroutineScope()

    var downloadErrorInfo by remember {
        mutableStateOf<DownloadErrorInfo?>(null)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val constraints = constraints
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HybridButton(
                        onClick = {
                            model.launchJob {
                                onDownload(
                                    model, client,
                                    confirmCallback = object : DownloadErrorCallback {
                                        override fun onError(info: DownloadErrorInfo) {
                                            downloadErrorInfo = info
                                        }
                                    },
                                )
                            }
                        },
                        enabled = canDownload,
                        vectorIcon = painterResource(MR.images.download),
                        text = MR.strings.download(),
                        description = MR.strings.downloadFirmware(),
                        parentSize = constraints.maxWidth
                    )

                    Spacer(Modifier.width(8.dp))

                    HybridButton(
                        onClick = {
                            model.launchJob {
                                onFetch(model)
                            }
                        },
                        enabled = canCheckVersion,
                        text = MR.strings.checkForUpdates(),
                        vectorIcon = painterResource(MR.images.refresh),
                        description = MR.strings.checkForUpdatesDesc(),
                        parentSize = constraints.maxWidth
                    )

                    Spacer(Modifier.weight(1f))

                    HybridButton(
                        onClick = {
                            scope.launch {
                                eventManager.sendEvent(Event.Download.Finish)
                            }
                            model.endJob("")
                        },
                        enabled = hasRunningJobs,
                        text = MR.strings.cancel(),
                        description = MR.strings.cancel(),
                        vectorIcon = painterResource(MR.images.cancel),
                        parentSize = constraints.maxWidth
                    )
                }
            }
        }

        item {
            val boxSource = remember { MutableInteractionSource() }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize()
            ) {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = boxSource,
                        indication = null,
                        enabled = canChangeOption,
                    ) {
                        model.manual.value = model.manual.value?.let { !it }
                    }
                        .padding(4.dp)
                ) {
                    Checkbox(
                        checked = manual == true,
                        onCheckedChange = {
                            model.manual.value = it
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        enabled = canChangeOption,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                        ),
                        interactionSource = boxSource
                    )

                    Spacer(Modifier.width(8.dp))


                    Text(
                        text = MR.strings.manual(),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                AnimatedVisibility(
                    visible = manual == true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    var showingRequestWarningDialog by remember { mutableStateOf(false) }

                    val textStyle =
                        LocalTextStyle.current.copy(LocalContentColor.current)

                    Row {
                        Spacer(Modifier.width(32.dp))

                        val info = buildAnnotatedString {
                            pushStyle(SpanStyle(color = MaterialTheme.colorScheme.error))
                            append("${MR.strings.manualWarning()} ")
                            pushStringAnnotation(
                                "MoreInfo",
                                "MoreInfo",
                            )
                            pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                            append(MR.strings.moreInfo())
                            pop()
                            pop()
                        }

                        ClickableText(
                            text = info,
                            onClick = {
                                info.getStringAnnotations("MoreInfo", it, it)
                                    .firstOrNull()?.let {
                                        showingRequestWarningDialog = true
                                    }
                            },
                            style = textStyle,
                        )
                    }

                    AlertDialogDef(
                        showing = showingRequestWarningDialog,
                        title = {
                            Text(text = MR.strings.moreInfo())
                        },
                        text = {
                            val info = buildAnnotatedString {
                                append(MR.strings.manualWarningDetails(GradleConfig.appName, GradleConfig.appName))
                                append(" ")
                                pushStringAnnotation(
                                    "IssueLink",
                                    "https://github.com/zacharee/SamloaderKotlin/issues/10"
                                )
                                pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                                append(MR.strings.manualWarningDetails2())
                                pop()
                                pop()
                                append(" ")
                                append(MR.strings.manualWarningDetails3())
                            }

                            val scroll = rememberScrollState()

                            ClickableText(
                                text = info,
                                onClick = {
                                    info.getStringAnnotations("IssueLink", it, it)
                                        .firstOrNull()?.let { item ->
                                            UrlHandler.launchUrl(item.item)
                                        }
                                },
                                style = textStyle,
                                modifier = Modifier.verticalScroll(scroll)
                            )
                        },
                        buttons = {
                            TextButton(
                                onClick = {
                                    showingRequestWarningDialog = false
                                }
                            ) {
                                Text(MR.strings.ok())
                            }
                        },
                        onDismissRequest = {
                            showingRequestWarningDialog = false
                        }
                    )
                }
            }
        }

        item {
            MRFLayout(model, canChangeOption, manual == true && canChangeOption, showImeiSerial = true)
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedVisibility(
                    visible = manual == false && osCode.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = MR.strings.osVersion(osCode)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = hasRunningJobs || progress.first > 0 || progress.second > 0 || statusText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Spacer(Modifier.height(16.dp))

                        ProgressInfo(model)
                    }
                }

                val changelogCondition =
                    changelog != null && manual == false && !hasRunningJobs && !fw.isNullOrBlank()

                AnimatedVisibility(
                    visible = changelogCondition,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))

                        ExpandButton(
                            changelogExpanded,
                            MR.strings.changelog(),
                            modifier = Modifier.fillMaxWidth(),
                        ) { model.changelogExpanded.value = it }

                        Spacer(Modifier.height(8.dp))
                    }
                }

                AnimatedVisibility(
                    visible = changelogExpanded && changelogCondition,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ChangelogDisplay(changelog)
                }
            }
        }
    }

    AlertDialogDef(
        showing = downloadErrorInfo != null,
        onDismissRequest = {
            model.launchJob {
                downloadErrorInfo?.callback?.onCancel?.invoke()
                downloadErrorInfo = null
            }
        },
        title = {
            Text(text = MR.strings.warning())
        },
        text = {
            Text(text = downloadErrorInfo?.message ?: "")
        },
        buttons = {
            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = {
                    model.launchJob {
                        val info = downloadErrorInfo
                        downloadErrorInfo = null
                        info?.callback?.onCancel?.invoke()
                    }
                }
            ) {
                Text(text = MR.strings.no())
            }

            TextButton(
                onClick = {
                    model.launchJob {
                        val info = downloadErrorInfo
                        downloadErrorInfo = null
                        info?.callback?.onAccept?.invoke()
                    }
                }
            ) {
                Text(
                    text = MR.strings.yes(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
