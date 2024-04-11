package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.utils.io.core.internal.DangerousInternalIoApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.nanihadesuka.compose.ColumnScrollbarNew
import my.nanihadesuka.compose.ScrollbarSelectionMode
import tk.zwander.common.GradleConfig
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.exception.VersionException
import tk.zwander.common.exceptions.DownloadError
import tk.zwander.common.tools.CryptUtils
import tk.zwander.common.tools.FusClient
import tk.zwander.common.tools.Request
import tk.zwander.common.tools.VersionFetch
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.Event
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.invoke
import tk.zwander.common.util.isAccessoryModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.view.components.ChangelogDisplay
import tk.zwander.commonCompose.view.components.ExpandButton
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.InWindowAlertDialog
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
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

    val result = Request.getBinaryFile(
        client,
        model.fw.value ?: "",
        model.model.value ?: "",
        model.region.value ?: "",
        model.imeiSerial.value ?: "",
    )

    val (info, error, output, requestBody) = result

    if (error != null && error !is VersionException) {
        Exception(error).printStackTrace()
        model.endJob("${error.message ?: MR.strings.error()}\n\n${output}")
        if (result.isReportableCode() && !model.model.value.isAccessoryModel && !output.contains("Incapsula")) {
            CrossPlatformBugsnag.notify(DownloadError(requestBody, output, error))
        }
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
    try {
        val (path, fileName, size, crc32, v4Key) = info
        val request = Request.createBinaryInit(fileName, client.getNonce())

        client.makeReq(FusClient.Request.BINARY_INIT, request)

        val fullFileName = fileName.replace(
            ".zip",
            "_${model.fw.value?.replace("/", "_")}_${model.region.value}.zip",
        )

        eventManager.sendEvent(
            Event.Download.GetInput(fullFileName) { inputInfo ->
                if (inputInfo != null) {
                    val outputStream = inputInfo.downloadFile.openOutputStream(true) ?: return@GetInput
                    val md5 = try {
                        client.downloadFile(
                            path + fileName,
                            inputInfo.downloadFile.getLength(),
                            size,
                            outputStream,
                            inputInfo.downloadFile.getLength(),
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
                            inputInfo.downloadFile.openInputStream() ?: return@GetInput,
                            size,
                            crc32,
                        ) { current, max, bps ->
                            model.progress.value = current to max
                            model.speed.value = bps

                            eventManager.sendEvent(
                                Event.Download.Progress(
                                    MR.strings.checkingCRC(),
                                    current,
                                    max
                                )
                            )
                        }

                        if (!result) {
                            model.endJob(MR.strings.crcCheckFailed())
                            return@GetInput
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
                        inputInfo.downloadFile.openInputStream() ?: return@GetInput,
                        inputInfo.decryptFile.openOutputStream() ?: return@GetInput,
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

                    if (BifrostSettings.Keys.autoDeleteEncryptedFirmware() == true) {
                        inputInfo.downloadFile.delete()
                    }

                    model.endJob(MR.strings.done())
                } else {
                    model.endJob("")
                }
            }
        )
    } catch (e: Throwable) {
        val message = if (e !is CancellationException) "${e.message}" else ""
        model.endJob(message)
    }

    eventManager.sendEvent(Event.Download.Finish)
}

private suspend fun onFetch(model: DownloadModel) {
    model.statusText.value = ""
    model.changelog.value = null
    model.osCode.value = ""

    val (fw, os, error, output) = VersionFetch.getLatestVersion(
        model.model.value ?: "",
        model.region.value ?: ""
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
        model.model.value ?: "",
        model.region.value ?: "",
        fw.split("/")[0]
    )

    model.fw.value = fw
    model.osCode.value = os

    model.endJob("")
}

/**
 * The Downloader View.
 */
@OptIn(ExperimentalTextApi::class)
@DangerousInternalIoApi
@ExperimentalTime
@Composable
internal fun DownloadView() {
    val model = LocalDownloadModel.current

    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
    var manual by model.manual.collectAsMutableState()
    val modelModel by model.model.collectAsState()
    val region by model.region.collectAsState()
    val fw by model.fw.collectAsState()
    val osCode by model.osCode.collectAsState()
    val progress by model.progress.collectAsState()
    val statusText by model.statusText.collectAsState()
    val changelog by model.changelog.collectAsState()
    var changelogExpanded by model.changelogExpanded.collectAsMutableState()

    val canCheckVersion = manual == false && !modelModel.isNullOrBlank()
            && !region.isNullOrBlank() && !hasRunningJobs

    val canDownload = !modelModel.isNullOrBlank() && !region.isNullOrBlank() && !fw.isNullOrBlank()
            && !hasRunningJobs

    val canChangeOption = !hasRunningJobs

    val scope = rememberCoroutineScope()

    var downloadErrorInfo by remember {
        mutableStateOf<DownloadErrorInfo?>(null)
    }

    val scrollState = rememberScrollState()

    ColumnScrollbarNew(
        state = scrollState,
        thumbColor = ThemeConstants.Colors.scrollbarUnselected,
        thumbSelectedColor = ThemeConstants.Colors.scrollbarSelected,
        alwaysShowScrollBar = true,
        padding = ThemeConstants.Dimensions.scrollbarPadding,
        thickness = ThemeConstants.Dimensions.scrollbarThickness,
        selectionMode = ScrollbarSelectionMode.Disabled,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
                .verticalScroll(scrollState),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                        text = stringResource(MR.strings.download),
                        description = stringResource(MR.strings.downloadFirmware),
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
                        text = stringResource(MR.strings.checkForUpdates),
                        vectorIcon = painterResource(MR.images.refresh),
                        description = stringResource(MR.strings.checkForUpdatesDesc),
                        parentSize = constraints.maxWidth,
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
                        text = stringResource(MR.strings.cancel),
                        description = stringResource(MR.strings.cancel),
                        vectorIcon = painterResource(MR.images.cancel),
                        parentSize = constraints.maxWidth
                    )
                }
            }

            val boxSource = remember { MutableInteractionSource() }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize()
                    .padding(bottom = 8.dp),
            ) {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = boxSource,
                        indication = null,
                        enabled = canChangeOption,
                    ) {
                        manual = manual?.let { !it }
                    }
                        .padding(4.dp)
                ) {
                    Checkbox(
                        checked = manual == true,
                        onCheckedChange = {
                            manual = it
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
                        text = stringResource(MR.strings.manual),
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

                        val manualWarning = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                append(MR.strings.manualWarning())
                                append(" ")

                                withStyle(
                                    SpanStyle(textDecoration = TextDecoration.Underline),
                                ) {
                                    withAnnotation(UrlAnnotation("https://nothing.com")) {
                                        append(MR.strings.moreInfo())
                                    }
                                }
                            }
                        }

                        ClickableText(
                            text = manualWarning,
                            onClick = {
                                manualWarning.getUrlAnnotations(it, it)
                                    .firstOrNull()?.let {
                                        showingRequestWarningDialog = true
                                    }
                            },
                            style = LocalTextStyle.current,
                        )
                    }

                    InWindowAlertDialog(
                        showing = showingRequestWarningDialog,
                        title = {
                            Text(text = stringResource(MR.strings.moreInfo))
                        },
                        text = {
                            val info = buildAnnotatedString {
                                append(
                                    MR.strings.manualWarningDetails(
                                        GradleConfig.appName,
                                        GradleConfig.appName
                                    )
                                )
                                append(" ")
                                withAnnotation(UrlAnnotation("https://github.com/zacharee/SamloaderKotlin/issues/10")) {
                                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                        append(MR.strings.manualWarningDetails2())
                                    }
                                }
                                append(" ")
                                append(MR.strings.manualWarningDetails3())
                            }

                            val scroll = rememberScrollState()

                            ClickableText(
                                text = info,
                                onClick = {
                                    info.getUrlAnnotations(it, it)
                                        .firstOrNull()?.let { item ->
                                            UrlHandler.launchUrl(item.item.url)
                                        }
                                },
                                style = textStyle,
                                modifier = Modifier.verticalScroll(scroll),
                            )
                        },
                        buttons = {
                            TextButton(
                                onClick = {
                                    showingRequestWarningDialog = false
                                }
                            ) {
                                Text(stringResource(MR.strings.ok))
                            }
                        },
                        onDismissRequest = {
                            showingRequestWarningDialog = false
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = modelModel.isAccessoryModel,
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterStart),
            ) {
                Text(
                    text = stringResource(MR.strings.invalid_model),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            MRFLayout(
                model,
                canChangeOption,
                manual == true && canChangeOption,
                showImeiSerial = true
            )

            AnimatedVisibility(
                visible = manual == false && osCode.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val displayCode = remember {
                    osCode
                }

                Column {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = stringResource(MR.strings.osVersion, displayCode),
                    )
                }
            }

            AnimatedVisibility(
                visible = hasRunningJobs || progress.first > 0 || progress.second > 0 || statusText.isNotBlank(),
            ) {
                Column {
                    Spacer(modifier = Modifier.size(4.dp))

                    ProgressInfo(model)
                }
            }

            val changelogCondition =
                changelog != null && manual == false && !hasRunningJobs && !fw.isNullOrBlank()

            AnimatedVisibility(
                visible = changelogCondition,
            ) {
                ExpandButton(
                    changelogExpanded,
                    stringResource(MR.strings.changelog),
                    modifier = Modifier.fillMaxWidth(),
                ) { changelogExpanded = it }
            }

            AnimatedVisibility(
                visible = changelogExpanded && changelogCondition,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ChangelogDisplay(changelog)
            }
        }
    }

    InWindowAlertDialog(
        showing = downloadErrorInfo != null,
        onDismissRequest = {
            model.launchJob {
                downloadErrorInfo?.callback?.onCancel?.invoke()
                downloadErrorInfo = null
            }
        },
        title = {
            Text(text = stringResource(MR.strings.warning))
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
                Text(text = stringResource(MR.strings.no))
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
                    text = stringResource(MR.strings.yes),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
