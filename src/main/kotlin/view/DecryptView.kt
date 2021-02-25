package view

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import model.DecryptModel
import tools.Crypt
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import kotlin.math.roundToInt

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
            Button(
                onClick = {
                    model.endJob("")
                },
                enabled = model.job != null
            ) {
                Text("Cancel")
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val chooser = JFileChooser()
                    val res = chooser.showOpenDialog(JFrame())

                    if (res == JFileChooser.APPROVE_OPTION) {
                        val input = chooser.selectedFile
                        if (!input.name.endsWith(".enc2") && !input.name.endsWith(".enc4")) {
                            model.endJob("Please select an encrypted firmware file ending in enc2 or enc4.")
                        } else {
                            model.fileToDecrypt = input
                            model.endJob("")
                        }
                    }
                },
                enabled = canChangeOption
            ) {
                Text("Pick File")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        val input = model.fileToDecrypt!!

                        val output = File(input.parentFile, input.nameWithoutExtension)

                        val key = if (input.name.endsWith(".enc2")) Crypt.getV2Key(
                            model.fw,
                            model.model,
                            model.region
                        ) else
                            Crypt.getV4Key(model.fw, model.model, model.region)

                        Crypt.decryptProgress(input, output.outputStream(), key, input.length()) { current, max, bps ->
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

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.model,
                onValueChange = { model.model = it },
                label = { Text("Model") },
                modifier = Modifier.weight(1f),
                readOnly = !canChangeOption
            )

            Spacer(Modifier.width(8.dp))

            TextField(
                value = model.region,
                onValueChange = { model.region = it },
                label = { Text("Region") },
                modifier = Modifier.weight(1f),
                readOnly = !canChangeOption
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.fw,
                onValueChange = { model.fw = it },
                label = { Text("Firmware") },
                modifier = Modifier.weight(1f),
                readOnly = !canChangeOption
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.fileToDecrypt?.absolutePath ?: "",
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