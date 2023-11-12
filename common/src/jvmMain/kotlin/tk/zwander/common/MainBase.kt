package tk.zwander.common

import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.Event
import tk.zwander.common.util.EventManager
import tk.zwander.common.util.eventManager
import tk.zwander.commonCompose.util.FilePicker
import java.io.File

class MainBase : EventManager.EventListener {
    init {
        eventManager.addListener(this)
    }

    override suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Decrypt.GetInput -> {
                event.callbackScope.launch {
                    val file = FilePicker.pickFile()

                    if (file != null) {
                        event.callback(
                            this,
                            DecryptFileInfo(
                                file,
                                PlatformFile(file.getParent()!!, File(file.getAbsolutePath()).nameWithoutExtension),
                            ),
                        )
                    } else {
                        event.callback(this, null)
                    }
                }
            }
            is Event.Download.GetInput -> {
                event.callbackScope.launch {
                    val file = FilePicker.createFile(name = event.fileName)

                    if (file == null) {
                        event.callback(this, null)
                    } else {
                        val decFile = PlatformFile(
                            file.getParent()!!,
                            event.fileName.replace(".enc2", "")
                                .replace(".enc4", ""),
                        )

                        event.callback(
                            this,
                            DownloadFileInfo(
                                file,
                                decFile
                            ),
                        )
                    }
                }
            }
            else -> {}
        }
    }
}