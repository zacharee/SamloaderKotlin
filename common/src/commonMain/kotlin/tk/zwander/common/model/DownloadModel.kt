package tk.zwander.common.model

import androidx.compose.runtime.*

class DownloadModel : BaseModel() {
    var manual by mutableStateOf(false)
    var osCode by mutableStateOf("")
}