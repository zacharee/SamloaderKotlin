package tk.zwander.common.model

import androidx.compose.runtime.*
import tk.zwander.common.data.DecryptFileInfo

class DecryptModel : BaseModel() {
    var fileToDecrypt by mutableStateOf<DecryptFileInfo?>(null)

    override fun onEnd(text: String) {
        fileToDecrypt = null
    }
}