package tk.zwander.commonCompose.util

import ca.weblite.objc.NSObject

class NSUserDefaults : NSObject("NSObject") {
    fun objectForKey(key: String): Any? {
        val defaults = client.sendProxy("NSUserDefaults", "standardUserDefaults")
        return defaults.send("objectForKey:", key)
    }
}
