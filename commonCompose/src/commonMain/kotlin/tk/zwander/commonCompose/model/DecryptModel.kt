package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.common.data.DecryptFileInfo

/**
 * The model for the Decrypter view.
 */
class DecryptModel : BaseModel("decrypt_model") {
    /**
     * Contains the encrypted file and decrypted target.
     */
    val fileToDecrypt = MutableStateFlow<DecryptFileInfo?>(null)

    override fun onEnd(text: String) {
        fileToDecrypt.value = null
    }
}
