package tk.zwander.commonCompose.util

import tk.zwander.commonCompose.util.pager.PagerState
import tk.zwander.commonCompose.view.components.Page

var PagerState.currentPageEnum: Page
    get() = Page.values()[currentPage]
    set(page) {
        currentPage = page.index
    }