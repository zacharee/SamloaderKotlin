package tk.zwander.common

import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.Event
import tk.zwander.common.util.EventManager
import tk.zwander.common.util.eventManager
import tk.zwander.commonCompose.util.FilePicker
import java.io.File

object EventDelegate : EventManager.EventListener {
    fun create() {
        eventManager.addListener(this)
    }

    override suspend fun onEvent(event: Event) {
        coroutineScope {
            when (event) {
                is Event.Decrypt.GetInput -> {
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
                is Event.Download.GetInput -> {
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
                else -> {}
            }
        }
    }
}