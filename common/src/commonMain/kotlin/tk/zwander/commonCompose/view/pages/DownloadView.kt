package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.exception.VersionException
import tk.zwander.common.tools.*
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.view.components.*
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings
import kotlin.time.ExperimentalTime

/**
 * The FusClient for retrieving firmware.
 */
@OptIn(DangerousInternalIoApi::class)
val client = FusClient()

/**
 * Delegate retrieving the download location to the platform.
 */
expect object PlatformDownloadView {
    suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    )

    fun onStart()
    fun onFinish()
    fun onProgress(status: String, current: Long, max: Long)
}

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
    PlatformDownloadView.onStart()
    model.statusText.value = strings.downloading()

    val (info, error, output) = Request.getBinaryFile(
        client,
        model.fw.value,
        model.model.value,
        model.region.value
    )

    if (error != null && error !is VersionException) {
        Exception(error).printStackTrace()
        model.endJob("${error.message ?: strings.error()}\n\n${output}")
        PlatformDownloadView.onFinish()
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
                            PlatformDownloadView.onFinish()
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
    val (path, fileName, size, crc32, v4Key) = info
    val request = Request.createBinaryInit(fileName, client.getNonce())

    client.makeReq(FusClient.Request.BINARY_INIT, request)

    val fullFileName = fileName.replace(
        ".zip",
        "_${model.fw.value.replace("/", "_")}_${model.region.value}.zip"
    )

    PlatformDownloadView.getInput(fullFileName) { inputInfo ->
        try {
            if (inputInfo != null) {
                val (response, md5) = client.downloadFile(
                    path + fileName,
                    inputInfo.downloadFile.getLength()
                )

                Downloader.download(
                    response,
                    size,
                    inputInfo.downloadFile.openOutputStream(true),
                    inputInfo.downloadFile.getLength()
                ) { current, max, bps ->
                    model.progress.value = current to max
                    model.speed.value = bps

                    PlatformDownloadView.onProgress(strings.downloading(), current, max)
                }

                if (crc32 != null) {
                    model.speed.value = 0L
                    model.statusText.value = strings.checkingCRC()
                    val result = CryptUtils.checkCrc32(
                        inputInfo.downloadFile.openInputStream(),
                        size,
                        crc32
                    ) { current, max, bps ->
                        model.progress.value = current to max
                        model.speed.value = bps

                        PlatformDownloadView.onProgress(
                            strings.checkingCRC(),
                            current,
                            max
                        )
                    }

                    if (!result) {
                        model.endJob(strings.crcCheckFailed())
                        return@getInput
                    }
                }

                if (md5 != null) {
                    model.speed.value = 0L
                    model.statusText.value = strings.checkingMD5()

                    PlatformDownloadView.onProgress(strings.checkingMD5(), 0, 1)

                    val result = withContext(Dispatchers.Default) {
                        CryptUtils.checkMD5(
                            md5,
                            inputInfo.downloadFile.openInputStream()
                        )
                    }

                    if (!result) {
                        model.endJob(strings.md5CheckFailed())
                        return@getInput
                    }
                }

                model.speed.value = 0L
                model.statusText.value = strings.decrypting()

                val key =
                    if (fullFileName.endsWith(".enc2")) CryptUtils.getV2Key(
                        model.fw.value,
                        model.model.value,
                        model.region.value
                    ) else {
                        v4Key ?: CryptUtils.getV4Key(client, model.fw.value, model.model.value, model.region.value)
                    }

                CryptUtils.decryptProgress(
                    inputInfo.downloadFile.openInputStream(),
                    inputInfo.decryptFile.openOutputStream(),
                    key,
                    size
                ) { current, max, bps ->
                    model.progress.value = current to max
                    model.speed.value = bps

                    PlatformDownloadView.onProgress(strings.decrypting(), current, max)
                }

                model.endJob(strings.done())
            } else {
                model.endJob("")
            }
        } catch (e: Throwable) {
            model.endJob("${e.message}")
        }
    }

    PlatformDownloadView.onFinish()
}

private suspend fun onFetch(model: DownloadModel) {
    val (fw, os, error, output) = VersionFetch.getLatestVersion(model.model.value, model.region.value)

    if (error != null) {
        model.endJob(strings.firmwareCheckError(error.message.toString(), output))
        return
    }

    model.changelog.value = ChangelogHandler.getChangelog(model.model.value, model.region.value, fw.split("/")[0])

    model.fw.value = fw
    model.osCode.value = os

    model.endJob("")
}

/**
 * The Downloader View.
 * @param scrollState a shared scroll state.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
internal fun DownloadView(scrollState: ScrollState) {
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

    val canCheckVersion = !manual && modelModel.isNotBlank()
            && region.isNotBlank() && !hasRunningJobs

    val canDownload = modelModel.isNotBlank() && region.isNotBlank() && fw.isNotBlank()
            && !hasRunningJobs

    val canChangeOption = !hasRunningJobs

    var downloadErrorInfo by remember {
        mutableStateOf<DownloadErrorInfo?>(null)
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
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
                                }
                            )
                        }
                    },
                    enabled = canDownload,
                    vectorIcon = painterResource(MR.images.download),
                    text = strings.download(),
                    description = strings.downloadFirmware(),
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
                    text = strings.checkForUpdates(),
                    vectorIcon = painterResource(MR.images.refresh),
                    description = strings.checkForUpdatesDesc(),
                    parentSize = constraints.maxWidth
                )

                Spacer(Modifier.weight(1f))

                HybridButton(
                    onClick = {
                        PlatformDownloadView.onFinish()
                        model.endJob("")
                    },
                    enabled = hasRunningJobs,
                    text = strings.cancel(),
                    description = strings.cancel(),
                    vectorIcon = painterResource(MR.images.cancel),
                    parentSize = constraints.maxWidth
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val boxSource = remember { MutableInteractionSource() }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.animateContentSize()
        ) {
            Row(
                modifier = Modifier.clickable(
                    interactionSource = boxSource,
                    indication = null
                ) {
                    model.manual.value = !model.manual.value
                }
                    .padding(4.dp)
            ) {
                Checkbox(
                    checked = manual,
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
                    text = strings.manual(),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            AnimatedVisibility(
                visible = manual,
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
                        append("${strings.manualWarning()} ")
                        pushStringAnnotation(
                            "MoreInfo",
                            "MoreInfo"
                        )
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        append(strings.moreInfo())
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
                        Text(text = strings.moreInfo())
                    },
                    text = {
                        val info = buildAnnotatedString {
                            append(strings.manualWarningDetails(GradleConfig.appName, GradleConfig.appName))
                            append(" ")
                            pushStringAnnotation(
                                "IssueLink",
                                "https://github.com/zacharee/SamloaderKotlin/issues/10"
                            )
                            pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                            append(strings.manualWarningDetails2())
                            pop()
                            pop()
                            append(" ")
                            append(strings.manualWarningDetails3())
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
                            Text(strings.ok())
                        }
                    },
                    onDismissRequest = {
                        showingRequestWarningDialog = false
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        MRFLayout(model, canChangeOption, manual && canChangeOption)

        AnimatedVisibility(
            visible = !manual && osCode.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = strings.osVersion(osCode)
                )
            }
        }

        AnimatedVisibility(
            visible = hasRunningJobs || progress.first > 0 || progress.second > 0 || statusText.isNotBlank(),
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                ProgressInfo(model)
            }
        }

        val changelogCondition =
            changelog != null && !manual && !hasRunningJobs && fw.isNotBlank()

        AnimatedVisibility(
            visible = changelogCondition,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))

                ExpandButton(
                    changelogExpanded,
                    strings.changelog()
                ) { model.changelogExpanded.value = it }

                Spacer(Modifier.height(8.dp))
            }
        }

        AnimatedVisibility(
            visible = changelogExpanded && changelogCondition,
        ) {
            ChangelogDisplay(changelog!!)
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
                Text(text = strings.warning())
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
                    Text(text = strings.no())
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
                        text = strings.yes(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}
