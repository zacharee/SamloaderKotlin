package tk.zwander.commonCompose.locals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DecryptModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.model.HistoryModel

val LocalDownloadModel = compositionLocalOfModel<DownloadModel>()
val LocalDecryptModel = compositionLocalOfModel<DecryptModel>()
val LocalHistoryModel = compositionLocalOfModel<HistoryModel>()

@Composable
internal fun ProvideModels(content: @Composable () -> Unit) {
    val downloadModel = DownloadModel()
    val decryptModel = DecryptModel()
    val historyModel = HistoryModel()

    CompositionLocalProvider(
        LocalDownloadModel provides downloadModel,
        LocalDecryptModel provides decryptModel,
        LocalHistoryModel provides historyModel,
    ) {
        content()
    }
}

private inline fun <reified T : BaseModel> compositionLocalOfModel(): ProvidableCompositionLocal<T> {
    return compositionLocalOf { error("No ${T::class.simpleName} provided") }
}
