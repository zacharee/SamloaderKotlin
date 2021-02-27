package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.tools.Crypt
import kotlin.time.ExperimentalTime

expect object PlatformDecryptView {
    fun getInput(callback: (DecryptFileInfo?) -> Unit)
}

@DangerousInternalIoApi
@ExperimentalTime
@Composable
fun DecryptView(model: DecryptModel) {
    val canDecrypt = model.fileToDecrypt != null && model.job == null
            && model.fw.isNotBlank() && model.model.isNotBlank() && model.region.isNotBlank()

    val canChangeOption = model.job == null

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    model.endJob("")
                },
                enabled = model.job != null
            ) {
                Text("Cancel")
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = {
                      PlatformDecryptView.getInput { info ->
                          if (info != null) {
                                if (!info.fileName.endsWith(".enc2") && !info.fileName.endsWith(".enc4")) {
                                    model.endJob("Please select an encrypted firmware file ending in enc2 or enc4.")
                                } else {
                                    model.fileToDecrypt = info
                                    model.endJob("")
                                }
                          }
                      }
                },
                enabled = canChangeOption
            ) {
                Text("Pick File")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        val info = model.fileToDecrypt!!

                        val key = if (info.fileName.endsWith(".enc2")) Crypt.getV2Key(
                            model.fw,
                            model.model,
                            model.region
                        ) else
                            Crypt.getV4Key(model.fw, model.model, model.region)

                        Crypt.decryptProgress(info.input, info.output, key, info.inputSize) { current, max, bps ->
                            model.progress = current to max
                            model.speed = bps
                        }

                        model.endJob("Done")
                    }
                },
                enabled = canDecrypt
            ) {
                Text("Decrypt")
            }
        }

        Spacer(Modifier.height(16.dp))

        MRFLayout(model, canChangeOption, canChangeOption)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.fileToDecrypt?.inputPath ?: "",
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