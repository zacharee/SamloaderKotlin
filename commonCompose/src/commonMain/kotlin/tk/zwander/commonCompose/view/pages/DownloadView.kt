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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soywiz.korio.async.launch
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import tk.zwander.common.GradleConfig
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.exception.VersionMismatchException
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.common.tools.*
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.util.vectorResource
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
    model.statusText = strings.downloading()

    val (info, error, output) = Request.getBinaryFile(
        client,
        model.fw,
        model.model,
        model.region
    )

    if (error != null && error !is VersionMismatchException) {
        error.printStackTrace()
        model.endJob("${error.message ?: strings.error()}\n\n${output}")
        PlatformDownloadView.onFinish()
    } else {
        if (error is VersionMismatchException) {
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
        "_${model.fw.replace("/", "_")}_${model.region}.zip"
    )

    PlatformDownloadView.getInput(fullFileName) { inputInfo ->
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
                model.progress = current to max
                model.speed = bps

                PlatformDownloadView.onProgress(strings.downloading(), current, max)
            }

            if (crc32 != null) {
                model.speed = 0L
                model.statusText = strings.checkingCRC()
                val result = CryptUtils.checkCrc32(
                    inputInfo.downloadFile.openInputStream(),
                    size,
                    crc32
                ) { current, max, bps ->
                    model.progress = current to max
                    model.speed = bps

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
                model.speed = 0L
                model.statusText = strings.checkingMD5()

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

            model.speed = 0L
            model.statusText = strings.decrypting()

            val key =
                if (fullFileName.endsWith(".enc2")) CryptUtils.getV2Key(
                    model.fw,
                    model.model,
                    model.region
                ) else {
                    v4Key ?: CryptUtils.getV4Key(client, model.fw, model.model, model.region)
                }

            CryptUtils.decryptProgress(
                inputInfo.downloadFile.openInputStream(),
                inputInfo.decryptFile.openOutputStream(),
                key,
                size
            ) { current, max, bps ->
                model.progress = current to max
                model.speed = bps

                PlatformDownloadView.onProgress(strings.decrypting(), current, max)
            }

            model.endJob(strings.done())
        } else {
            model.endJob("")
        }
    }

    PlatformDownloadView.onFinish()
}

private suspend fun onFetch(model: DownloadModel) {
    val (fw, os, error, output) = VersionFetch.getLatestVersion(model.model, model.region)

    if (error != null) {
        model.endJob(strings.firmwareCheckError(error.message.toString(), output))
        return
    }

    model.changelog = ChangelogHandler.getChangelog(model.model, model.region, fw.split("/")[0])

    model.fw = fw
    model.osCode = os

    model.endJob("")
}

/**
 * The Downloader View.
 * @param scrollState a shared scroll state.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
fun DownloadView(scrollState: ScrollState) {
    val model = LocalDownloadModel.current

    val canCheckVersion = !model.manual && model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    val canDownload = model.model.isNotBlank() && model.region.isNotBlank() && model.fw.isNotBlank()
            && model.job == null

    val canChangeOption = model.job == null

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
                        model.job = model.scope.launch {
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
                    vectorIcon = vectorResource(MR.assets.download),
                    text = strings.download(),
                    description = strings.downloadFirmware(),
                    parentSize = constraints.maxWidth
                )

                Spacer(Modifier.width(8.dp))

                HybridButton(
                    onClick = {
                        model.job = model.scope.launch {
                            onFetch(model)
                        }
                    },
                    enabled = canCheckVersion,
                    text = strings.checkForUpdates(),
                    vectorIcon = vectorResource(MR.assets.refresh),
                    description = strings.checkForUpdatesDesc(),
                    parentSize = constraints.maxWidth
                )

                Spacer(Modifier.weight(1f))

                HybridButton(
                    onClick = {
                        PlatformDownloadView.onFinish()
                        model.endJob("")
                    },
                    enabled = model.job != null,
                    text = strings.cancel(),
                    description = strings.cancel(),
                    vectorIcon = vectorResource(MR.assets.cancel),
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
                    model.manual = !model.manual
                }
                    .padding(4.dp)
            ) {
                Checkbox(
                    checked = model.manual,
                    onCheckedChange = {
                        model.manual = it
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
                visible = model.manual,
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

        MRFLayout(model, canChangeOption, model.manual && canChangeOption)

        AnimatedVisibility(
            visible = !model.manual && model.osCode.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = strings.osVersion(model.osCode)
                )
            }
        }

        AnimatedVisibility(
            visible = model.progress.first > 0 || model.progress.second > 0 || model.statusText.isNotBlank(),
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                ProgressInfo(model)
            }
        }

        val changelogCondition =
            model.changelog != null && !model.manual && model.job == null && model.fw.isNotBlank()

        AnimatedVisibility(
            visible = changelogCondition,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))

                ExpandButton(
                    model.changelogExpanded,
                    strings.changelog()
                ) { model.changelogExpanded = it }

                Spacer(Modifier.height(8.dp))
            }
        }

        AnimatedVisibility(
            visible = model.changelogExpanded && changelogCondition,
        ) {
            ChangelogDisplay(model.changelog!!)
        }

        AlertDialogDef(
            showing = downloadErrorInfo != null,
            onDismissRequest = {
                model.scope.launch {
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
                        model.scope.launch {
                            downloadErrorInfo?.callback?.onCancel?.invoke()
                            downloadErrorInfo = null
                        }
                    }
                ) {
                    Text(text = strings.no())
                }

                TextButton(
                    onClick = {
                        model.scope.launch {
                            downloadErrorInfo?.callback?.onAccept?.invoke()
                            downloadErrorInfo = null
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
