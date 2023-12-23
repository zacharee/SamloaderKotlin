package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.commonCompose.view.components.Page

class MainModel {
    val currentPage = MutableStateFlow<Page>(Page.Downloader)
}
