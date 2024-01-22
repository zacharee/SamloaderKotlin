package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.utils.io.core.internal.DangerousInternalIoApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.tools.CryptUtils
import tk.zwander.common.util.Event
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.model.DecryptModel
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
import tk.zwander.samloaderkotlin.resources.MR
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class, ExperimentalTime::class)
private suspend fun onDecrypt(model: DecryptModel) {
    eventManager.sendEvent(Event.Decrypt.Start)
    val info = model.fileToDecrypt.value!!
    val inputFile = info.encFile
    val outputFile = info.decFile

    val key = if (inputFile.getName().endsWith(".enc2")) CryptUtils.getV2Key(
        model.fw.value ?: "",
        model.model.value ?: "",
        model.region.value ?: ""
    ) else {
        try {
            CryptUtils.getV4Key(
                client,
                model.fw.value ?: "",
                model.model.value ?: "",
                model.region.value ?: "",
                model.imeiSerial.value ?: "",
            )
        } catch (e: Throwable) {
            model.endJob(MR.strings.decryptError(e.message.toString()))
            return
        }
    }

    val inputStream = try {
        inputFile.openInputStream()
    } catch (e: Throwable) {
        model.endJob(MR.strings.decryptError(e.message.toString()))
        return
    }

    val outputStream = try {
        outputFile.openOutputStream()
    } catch (e: Throwable) {
        model.endJob(MR.strings.decryptError(e.message.toString()))
        return
    }

    CryptUtils.decryptProgress(inputStream, outputStream, key, inputFile.getLength()) { current, max, bps ->
        model.progress.value = current to max
        model.speed.value = bps
        eventManager.sendEvent(Event.Decrypt.Progress(MR.strings.decrypting(), current, max))
    }

    eventManager.sendEvent(Event.Decrypt.Finish)
    model.endJob(MR.strings.done())
}

private suspend fun onOpenFile(model: DecryptModel) {
    coroutineScope {
        eventManager.sendEvent(Event.Decrypt.GetInput { info ->
            handleFileInput(model, info)
        })
    }
}

private fun handleFileInput(model: DecryptModel, info: DecryptFileInfo?) {
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

@Composable
expect fun Modifier.handleFileDrag(
    enabled: Boolean = true,
    onDragStart: (PlatformFile?) -> Unit = {},
    onDrag: (PlatformFile?) -> Unit = {},
    onDragExit: () -> Unit = {},
    onDrop: (PlatformFile?) -> Unit = {},
): Modifier

/**
 * The Decrypter View.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
internal fun DecryptView() {
    val model = LocalDecryptModel.current

    val fw by model.fw.collectAsState()
    val modelModel by model.model.collectAsState()
    val region by model.region.collectAsState()
    val fileToDecrypt by model.fileToDecrypt.collectAsState()

    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
    val canDecrypt = fileToDecrypt != null && !hasRunningJobs
            && !fw.isNullOrBlank() && !modelModel.isNullOrBlank() && !region.isNullOrBlank()

    val canChangeOption = !hasRunningJobs

    val scope = rememberCoroutineScope()

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
                                onDecrypt(model)
                            }
                        },
                        enabled = canDecrypt,
                        text = stringResource(MR.strings.decrypt),
                        description = stringResource(MR.strings.decryptFirmware),
                        vectorIcon = painterResource(MR.images.lock_open_outline),
                        parentSize = constraints.maxWidth
                    )
                    Spacer(Modifier.width(8.dp))
                    HybridButton(
                        onClick = {
                            model.launchJob {
                                onOpenFile(model)
                            }
                        },
                        enabled = canChangeOption,
                        text = stringResource(MR.strings.openFile),
                        description = stringResource(MR.strings.openFileDesc),
                        vectorIcon = painterResource(MR.images.open_in_new),
                        parentSize = constraints.maxWidth
                    )
                    Spacer(Modifier.weight(1f))
                    HybridButton(
                        onClick = {
                            scope.launch {
                                eventManager.sendEvent(Event.Decrypt.Finish)
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
        }

        item {
            MRFLayout(model, canChangeOption, canChangeOption, showImeiSerial = true)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fileToDecrypt?.encFile?.getAbsolutePath() ?: "",
                    onValueChange = {},
                    label = { Text(text = stringResource(MR.strings.file)) },
                    modifier = Modifier.weight(1f)
                        .handleFileDrag {
                            if (it != null) {
                                scope.launch {
                                    val decInfo = DecryptFileInfo(
                                        encFile = it,
                                        decFile = PlatformFile(it.getParent()!!, File(it.getAbsolutePath()).nameWithoutExtension),
                                    )

                                    handleFileInput(model, decInfo)
                                }
                            }
                        },
                    readOnly = true,
                    singleLine = true,
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.size(8.dp))

                ProgressInfo(model)
            }
        }
    }
}
