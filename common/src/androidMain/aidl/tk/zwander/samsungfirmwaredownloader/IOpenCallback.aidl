package tk.zwander.samsungfirmwaredownloader;

import android.net.Uri;

interface IOpenCallback {
    void onOpen(in Uri uri);
}