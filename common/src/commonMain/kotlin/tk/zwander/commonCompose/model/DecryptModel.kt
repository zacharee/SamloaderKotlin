package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.SettingsKey

/**
 * The model for the Decrypter view.
 */
class DecryptModel : BaseModel("decrypt_model") {
    companion object {
        const val DECRYPT_KEY = "field_decrypt_key"
    }

    /**
     * Contains the encrypted file and decrypted target.
     */
    val fileToDecrypt = MutableStateFlow<DecryptFileInfo?>(null)

    val decryptionKey = SettingsKey.String(DECRYPT_KEY.fullKey, "", BifrostSettings.settings).asMutableStateFlow()

    override fun onEnd(text: String) {
        fileToDecrypt.value = null
    }
}
