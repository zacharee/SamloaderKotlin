package tk.zwander.common.util

import android.content.Intent
import android.os.Bundle
import android.os.IBinder

fun Intent.putBinder(key: String, binder: IBinder) {
    val bundle = Bundle()
    bundle.putBinder(key, binder)
    putExtra(key, bundle)
}

fun Intent.getBinder(key: String): IBinder? {
    val bundle = getBundleExtra(key)
    return bundle?.getBinder(key)
}