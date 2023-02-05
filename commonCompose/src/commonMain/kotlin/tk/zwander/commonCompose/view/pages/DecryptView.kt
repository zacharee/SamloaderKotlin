package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.commonCompose.model.DecryptModel
import tk.zwander.common.tools.CryptUtils
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings
import kotlin.time.ExperimentalTime

/**
 * Delegate getting the decryption input and output to the platform.
 */
expect object PlatformDecryptView {
    suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit)
    fun onStart()
    fun onFinish()
    fun onProgress(status: String, current: Long, max: Long)
}

@OptIn(DangerousInternalIoApi::class, ExperimentalTime::class, ExperimentalMaterial3Api::class)
private suspend fun onDecrypt(model: DecryptModel) {
    PlatformDecryptView.onStart()
    val info = model.fileToDecrypt!!
    val inputFile = info.encFile
    val outputFile = info.decFile

    val key = if (inputFile.getName().endsWith(".enc2")) CryptUtils.getV2Key(
        model.fw,
        model.model,
        model.region
    ) else {
        try {
            CryptUtils.getV4Key(client, model.fw, model.model, model.region)
        } catch (e: Throwable) {
            model.endJob(strings.decryptError(e.message.toString()))
            return
        }
    }

    CryptUtils.decryptProgress(inputFile.openInputStream(), outputFile.openOutputStream(), key, inputFile.getLength()) { current, max, bps ->
        model.progress = current to max
        model.speed = bps
        PlatformDecryptView.onProgress(strings.decrypting(), current, max)
    }

    PlatformDecryptView.onFinish()
    model.endJob(strings.done())
}

private suspend fun onOpenFile(model: DecryptModel) {
    PlatformDecryptView.getInput { info ->
        if (info != null) {
            if (!info.encFile.getName().endsWith(".enc2") && !info.encFile.getName().endsWith(
                    ".enc4"
                )
            ) {
                model.endJob(strings.selectEncrypted())
            } else {
                model.endJob("")
                model.fileToDecrypt = info
            }
        } else {
            model.endJob("")
        }
    }
}

/**
 * The Decrypter View.
 * @param scrollState a shared scroll state among all pages.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DecryptView(scrollState: ScrollState) {
    val model = LocalDecryptModel.current
    val canDecrypt = model.fileToDecrypt != null && model.job == null
            && model.fw.isNotBlank() && model.model.isNotBlank() && model.region.isNotBlank()

    val canChangeOption = model.job == null

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val constraints = constraints

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                HybridButton(
                    onClick = {
                        model.job = model.scope.launch {
                            onDecrypt(model)
                        }
                    },
                    enabled = canDecrypt,
                    text = strings.decrypt(),
                    description = strings.decryptFirmware(),
                    vectorIcon = vectorResource(MR.assets.lock_open_outline),
                    parentSize = constraints.maxWidth
                )
                Spacer(Modifier.width(8.dp))
                HybridButton(
                    onClick = {
                        model.scope.launch {
                            onOpenFile(model)
                        }
                    },
                    enabled = canChangeOption,
                    text = strings.openFile(),
                    description = strings.openFileDesc(),
                    vectorIcon = vectorResource(MR.assets.open_in_new),
                    parentSize = constraints.maxWidth
                )
                Spacer(Modifier.weight(1f))
                HybridButton(
                    onClick = {
                        PlatformDecryptView.onFinish()
                        model.endJob("")
                    },
                    enabled = model.job != null,
                    text = strings.cancel(),
                    description = strings.cancel(),
                    vectorIcon = vectorResource(MR.assets.cancel),
                    parentSize = constraints.maxWidth
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        MRFLayout(model, canChangeOption, canChangeOption)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = model.fileToDecrypt?.encFile?.getAbsolutePath() ?: "",
                onValueChange = {},
                label = { Text(strings.file()) },
                modifier = Modifier.weight(1f),
                readOnly = true,
                singleLine = true,
            )
        }

        Spacer(Modifier.height(16.dp))

        ProgressInfo(model)
    }
}
