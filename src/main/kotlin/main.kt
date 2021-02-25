import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tools.VersionFetch

fun main() = Window {
    val model = mutableStateOf("")
    val region = mutableStateOf("")
    val fw = mutableStateOf("")
    val progress = mutableStateOf(0 to 1)
    val automatic = mutableStateOf(true)

    MaterialTheme {
        val canCheckVersion = automatic.value && model.value.isNotBlank()
                && region.value.isNotBlank()

        val canDownload = model.value.isNotBlank() && region.value.isNotBlank() && fw.value.isNotBlank()

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = automatic.value,
                    onCheckedChange = {
                        automatic.value = it
                    },
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                    },
                    enabled = canDownload
                ) {
                    Text("Download")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        fw.value = VersionFetch.getLatestVer(model.value, region.value)
                    },
                    enabled = canCheckVersion
                ) {
                    Text("Check for Updates")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = model.value,
                    onValueChange = { model.value = it },
                    label = { Text("Model") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                TextField(
                    value = region.value,
                    onValueChange = { region.value = it },
                    label = { Text("Region") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = fw.value,
                    onValueChange = { fw.value = it },
                    label = { Text("Firmware") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = automatic.value
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

            }
        }
    }
}