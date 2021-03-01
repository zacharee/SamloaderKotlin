package tk.zwander.samsungfirmwaredownloader;

import tk.zwander.samsungfirmwaredownloader.IOpenCallback;

interface IMainActivity {
    void openDownloadTree(IOpenCallback callback);
    void openDecryptInput(IOpenCallback callback);
    void openDecryptOutput(String fileName, IOpenCallback callback);
}