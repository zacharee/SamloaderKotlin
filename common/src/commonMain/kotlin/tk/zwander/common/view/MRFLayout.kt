package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import tk.zwander.common.model.BaseModel
import tk.zwander.common.model.DownloadModel

@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmare: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = model.model,
            onValueChange = {
                model.model = it.toUpperCase().trim()
                if ((model is DownloadModel && !model.manual)) {
                    model.fw = ""
                }
            },
            label = { Text("Model (e.g. SM-N986U1)") },
            modifier = Modifier.weight(1f),
            readOnly = !canChangeOption,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )

        Spacer(Modifier.width(8.dp))

        OutlinedTextField(
            value = model.region,
            onValueChange = {
                model.region = it.toUpperCase().trim()
                if ((model is DownloadModel && !model.manual)) {
                    model.fw = ""
                }
            },
            label = { Text("Region (e.g. XAA)") },
            modifier = Modifier.weight(1f),
            readOnly = !canChangeOption,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = model.fw,
            onValueChange = { model.fw = it.toUpperCase().trim() },
            label = { Text("Firmware") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !canChangeFirmare,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )
    }
}