package com.bugsnag

import tk.zwander.common.util.BifrostSettings

class CustomReport(config: Configuration, throwable: Throwable?) : Report(config, throwable) {
    constructor(bugsnag: Bugsnag, throwable: Throwable?) : this(bugsnag.config, throwable)
    internal override fun setSession(session: Session?) {
        super.setSession(session)
        getSession()["user"] = hashMapOf("id" to BifrostSettings.Keys.bugsnagUuid())
    }
}
