package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class, ExperimentalTime::class)
private suspend fun onDecrypt(model: DecryptModel) {
    eventManager.sendEvent(Event.Decrypt.Start)
    val info = model.fileToDecrypt.value!!
    val inputFile = info.encFile
    val outputFile = info.decFile

    val key = if (inputFile.getName().endsWith(".enc2")) CryptUtils.getV2Key(
        model.fw.value,
        model.model.value,
        model.region.value
    ) else {
        try {
            CryptUtils.getV4Key(client, model.fw.value, model.model.value, model.region.value)
        } catch (e: Throwable) {
            model.endJob(MR.strings.decryptError(e.message.toString()))
            return
        }
    }

    CryptUtils.decryptProgress(inputFile.openInputStream(), outputFile.openOutputStream(), key, inputFile.getLength()) { current, max, bps ->
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
        })
    }
}

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
            && fw.isNotBlank() && modelModel.isNotBlank() && region.isNotBlank()

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
                        text = MR.strings.decrypt(),
                        description = MR.strings.decryptFirmware(),
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
                        text = MR.strings.openFile(),
                        description = MR.strings.openFileDesc(),
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
                        text = MR.strings.cancel(),
                        description = MR.strings.cancel(),
                        vectorIcon = painterResource(MR.images.cancel),
                        parentSize = constraints.maxWidth
                    )
                }
            }
        }

        item {
            MRFLayout(model, canChangeOption, canChangeOption)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fileToDecrypt?.encFile?.getAbsolutePath() ?: "",
                    onValueChange = {},
                    label = { Text(MR.strings.file()) },
                    modifier = Modifier.weight(1f),
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
