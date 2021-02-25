package model

import androidx.compose.runtime.*

class DownloadModel : BaseModel() {
    var manual by mutableStateOf(false)
}