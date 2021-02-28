package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import tk.zwander.common.model.BaseModel
import tk.zwander.common.model.DownloadModel

@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmare: Boolean) {
    OutlinedTextField(
        value = model.model,
        onValueChange = {
            model.model = it.toUpperCase().trim()
            if ((model is DownloadModel && !model.manual)) {
                model.fw = ""
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Model (e.g. SM-N986U1)") },
        readOnly = !canChangeOption,
        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = model.region,
        onValueChange = {
            model.region = it.toUpperCase().trim()
            if ((model is DownloadModel && !model.manual)) {
                model.fw = ""
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Region (e.g. XAA)") },
        readOnly = !canChangeOption,
        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = model.fw,
        onValueChange = { model.fw = it.toUpperCase().trim() },
        label = { Text("Firmware") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = !canChangeFirmare,
        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
    )
}