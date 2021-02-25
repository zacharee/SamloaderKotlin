package model

import androidx.compose.runtime.*
import java.io.File

class DecryptModel : BaseModel() {
    var fileToDecrypt by mutableStateOf<File?>(null)
}