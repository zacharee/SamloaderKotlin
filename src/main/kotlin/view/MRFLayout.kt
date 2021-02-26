package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import model.BaseModel

@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmare: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = model.model,
            onValueChange = { model.model = it.toUpperCase() },
            label = { Text("Model") },
            modifier = Modifier.weight(1f),
            readOnly = !canChangeOption,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )

        Spacer(Modifier.width(8.dp))

        TextField(
            value = model.region,
            onValueChange = { model.region = it.toUpperCase() },
            label = { Text("Region") },
            modifier = Modifier.weight(1f),
            readOnly = !canChangeOption,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = model.fw,
            onValueChange = { model.fw = it.toUpperCase() },
            label = { Text("Firmware") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !canChangeFirmare,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
        )
    }
}