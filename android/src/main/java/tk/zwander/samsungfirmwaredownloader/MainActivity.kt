package tk.zwander.samsungfirmwaredownloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val page = remember { mutableStateOf(Page.DOWNLOADER) }

            val downloadModel = remember { DownloadModel() }
            val decryptModel = remember { DecryptModel() }

            CustomMaterialTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                                .fillMaxWidth()
                        ) {
                            TabView(page)

                            Divider(
                                thickness = 1.dp,
                                color = MaterialTheme.colors.onSurface
                            )

                            Spacer(Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                when (page.value) {
                                    Page.DOWNLOADER -> DownloadView(downloadModel)
                                    Page.DECRYPTER -> DecryptView(decryptModel)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        FooterView()
                    }
                }
            }
        }
    }
}