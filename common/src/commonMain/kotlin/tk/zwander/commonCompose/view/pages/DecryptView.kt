package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.compose.alertdialog.InWindowAlertDialog
import dev.zwander.kotlin.file.PlatformFile
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.ColumnScrollbar
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.tools.delegates.Decrypter
import tk.zwander.common.util.Event
import tk.zwander.common.util.eventManager
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.util.OffsetCorrectedIdentityTransformation
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.util.collectAsImmediateMutableState
import tk.zwander.commonCompose.util.handleFileDrag
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
import tk.zwander.commonCompose.view.components.SplitComponent
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.time.ExperimentalTime

/**
 * The Decrypter View.
 */
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
    val statusText by model.statusText.collectAsState()

    val canChangeOption = !hasRunningJobs

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var decryptKey by model.decryptionKey.collectAsImmediateMutableState()
    var showingDecryptHelpDialog by remember { mutableStateOf(false) }

    ColumnScrollbar(
        state = scrollState,
        settings = ThemeConstants.ScrollBarSettings.Default,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
                .verticalScroll(scrollState),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp, top = LocalMenuBarHeight.current),
            ) {
                val constraints = constraints

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HybridButton(
                        onClick = {
                            model.launchJob {
                                Decrypter.onDecrypt(model)
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
                                Decrypter.onOpenFile(model)
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

            MRFLayout(model, canChangeOption, canChangeOption, showImeiSerial = true)

            SplitComponent(
                startComponent = {
                    val value = fileToDecrypt?.encFile?.getAbsolutePath() ?: ""
                    OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        label = { Text(text = stringResource(MR.strings.file)) },
                        modifier = Modifier
                            .handleFileDrag {
                                if (it != null) {
                                    scope.launch {
                                        val decInfo = DecryptFileInfo(
                                            encFile = it,
                                            decFile = PlatformFile(it.getParent()!!, PlatformFile(it.getAbsolutePath()).nameWithoutExtension),
                                        )

                                        Decrypter.handleFileInput(model, decInfo)
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
                        readOnly = true,
                        singleLine = true,
                        visualTransformation = OffsetCorrectedIdentityTransformation(value),
                    )
                },
                endComponent = {
                    OutlinedTextField(
                        value = decryptKey,
                        onValueChange = { decryptKey = it },
                        label = { Text(text = stringResource(MR.strings.decryption_key)) },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showingDecryptHelpDialog = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(MR.strings.help),
                                )
                            }
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                startRatio = 0.6,
                endRatio = 0.4,
            )

            AnimatedVisibility(
                visible = hasRunningJobs || statusText.isNotBlank(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(Modifier.size(8.dp))

                    ProgressInfo(model)
                }
            }

            InWindowAlertDialog(
                showing = showingDecryptHelpDialog,
                title = { Text(text = stringResource(MR.strings.decryption_key)) },
                text = { Text(text = stringResource(MR.strings.decryption_key_help)) },
                buttons = {
                    TextButton(
                        onClick = { showingDecryptHelpDialog = false },
                    ) {
                        Text(text = stringResource(MR.strings.ok))
                    }
                },
                onDismissRequest = { showingDecryptHelpDialog = false },
            )
        }
    }
}
