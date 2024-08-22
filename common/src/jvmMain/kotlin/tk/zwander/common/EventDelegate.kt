package tk.zwander.common

import dev.zwander.kotlin.file.PlatformFile
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
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
        when (event) {
            is Event.Decrypt.GetInput -> {
                val file = FilePicker.pickFile()

                if (file != null) {
                    event.callback(
                        DecryptFileInfo(
                            file,
                            PlatformFile(
                                file.getParent()!!,
                                File(file.getAbsolutePath()).nameWithoutExtension
                            ),
                        ),
                    )
                } else {
                    event.callback(null)
                }
            }

            is Event.Download.GetInput -> {
                val file = FilePicker.createFile(name = event.fileName)

                if (file == null) {
                    event.callback(null)
                } else {
                    val decFile = PlatformFile(
                        file.getParent()!!,
                        event.fileName.replace(".enc2", "")
                            .replace(".enc4", ""),
                    )

                    val decKeyFile = event.decryptKeyFileName?.let {
                        PlatformFile(
                            file.getParent()!!,
                            event.decryptKeyFileName,
                        )
                    }

                    event.callback(
                        DownloadFileInfo(
                            file,
                            decFile,
                            decKeyFile,
                        ),
                    )
                }
            }

            else -> {}
        }
    }
}