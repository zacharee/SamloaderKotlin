package tk.zwander.commonCompose.locals

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DecryptModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.model.HistoryModel

val LocalDownloadModel = compositionLocalOfModel<DownloadModel>(DownloadModel())
val LocalDecryptModel = compositionLocalOfModel<DecryptModel>(DecryptModel())
val LocalHistoryModel = compositionLocalOfModel<HistoryModel>(HistoryModel())

private inline fun <reified T : BaseModel> compositionLocalOfModel(model: T): ProvidableCompositionLocal<T> {
    return compositionLocalOf { model }
}
