package tk.zwander.common.model

import androidx.compose.runtime.*
import tk.zwander.common.data.DecryptFileInfo

/**
 * The model for the Decrypter view.
 */
class DecryptModel : BaseModel() {
    /**
     * Contains the encrypted file and decrypted target.
     */
    var fileToDecrypt by mutableStateOf<DecryptFileInfo?>(null)

    override fun onEnd(text: String) {
        fileToDecrypt = null
    }
}