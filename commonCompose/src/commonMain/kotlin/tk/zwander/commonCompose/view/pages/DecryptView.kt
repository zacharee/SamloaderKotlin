package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.tools.CryptUtils
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
import kotlin.time.ExperimentalTime

/**
 * Delegate getting the decryption input and output to the platform.
 */
expect object PlatformDecryptView {
    suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit)
    fun onStart()
    fun onFinish()
    fun onProgress(status: String, current: Long, max: Long)
}

/**
 * The Decrypter View.
 * @param model the model for this View.
 * @param scrollState a shared scroll state among all pages.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
fun DecryptView(model: DecryptModel, scrollState: ScrollState) {
    val canDecrypt = model.fileToDecrypt != null && model.job == null
            && model.fw.isNotBlank() && model.model.isNotBlank() && model.region.isNotBlank()

    val canChangeOption = model.job == null

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        val rowSize = remember { mutableStateOf(0.dp) }
        Row(
            modifier = Modifier.fillMaxWidth()
                .onSizeChanged { rowSize.value = it.width.dp }
        ) {
            HybridButton(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        PlatformDecryptView.onStart()
                        val info = model.fileToDecrypt!!
                        val inputFile = info.encFile
                        val outputFile = info.decFile

                        val key = if (inputFile.getName().endsWith(".enc2")) CryptUtils.getV2Key(
                            model.fw,
                            model.model,
                            model.region
                        ) else {
                            CryptUtils.getV4Key(client, model.fw, model.model, model.region)
                        }

                        CryptUtils.decryptProgress(inputFile.openInputStream(), outputFile.openOutputStream(), key, inputFile.getLength()) { current, max, bps ->
                            model.progress = current to max
                            model.speed = bps
                            PlatformDecryptView.onProgress("Decrypting", current, max)
                        }

                        PlatformDecryptView.onFinish()
                        model.endJob("Done")
                    }
                },
                enabled = canDecrypt,
                text = "Decrypt",
                description = "Decrypt Firmware",
                vectorIcon = vectorResource("decrypt.xml"),
                parentSize = rowSize.value
            )
            Spacer(Modifier.width(8.dp))
            HybridButton(
                onClick = {
                    model.scope.launch {
                        PlatformDecryptView.getInput { info ->
                            if (info != null) {
                                if (!info.encFile.getName().endsWith(".enc2") && !info.encFile.getName().endsWith(
                                        ".enc4"
                                    )
                                ) {
                                    model.endJob("Please select an encrypted firmware file ending in enc2 or enc4.")
                                } else {
                                    model.endJob("")
                                    model.fileToDecrypt = info
                                }
                            } else {
                                model.endJob("")
                            }
                        }
                    }
                },
                enabled = canChangeOption,
                text = "Open File",
                description = "Open File to Decrypt",
                vectorIcon = vectorResource("open.xml"),
                parentSize = rowSize.value
            )
            Spacer(Modifier.weight(1f))
            HybridButton(
                onClick = {
                    PlatformDecryptView.onFinish()
                    model.endJob("")
                },
                enabled = model.job != null,
                text = "Cancel",
                description = "Cancel",
                vectorIcon = vectorResource("cancel.xml"),
                parentSize = rowSize.value
            )
        }

        Spacer(Modifier.height(8.dp))

        MRFLayout(model, canChangeOption, canChangeOption)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = model.fileToDecrypt?.encFile?.getAbsolutePath() ?: "",
                onValueChange = {},
                label = { Text("File") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                singleLine = true,
            )
        }

        Spacer(Modifier.height(16.dp))

        ProgressInfo(model)
    }
}
