import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.DecryptView
import tk.zwander.common.view.DownloadView
import java.awt.Desktop
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

enum class Page {
    DOWNLOADER,
    DECRYPTER
}

@ExperimentalTime
fun main() = Window(
    title = "Samsung Firmware Downloader",
    icon = Thread.currentThread().contextClassLoader.getResource("icon.png")
        ?.openStream().use(ImageIO::read)
) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

//    Test.main(arrayOf())

    var page by remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0x4C, 0xAF, 0x50),
            primaryVariant = Color(0x52, 0xC7, 0x56),
            secondary = Color(0xFFBB86FC),
            secondaryVariant = Color(0xFF3700B3)
        )
    ) {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                ) {
                    TabRow(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp),
                        selectedTabIndex = page.ordinal
                    ) {
                        Tab(
                            selected = page == Page.DOWNLOADER,
                            text = { Text("Downloader") },
                            onClick = {
                                page = Page.DOWNLOADER
                            }
                        )
                        Tab(
                            selected = page == Page.DECRYPTER,
                            text = { Text("Decrypter") },
                            onClick = {
                                page = Page.DECRYPTER
                            }
                        )
                    }

                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(8.dp)
                    ) {
                        when (page) {
                            Page.DOWNLOADER -> DownloadView(downloadModel)
                            Page.DECRYPTER -> DecryptView(decryptModel)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                ) {
                    val handler = { url: String ->
                        val uri = URI(url)
                        val desktop = Desktop.getDesktop()

                        desktop.browse(uri)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val copyrightAnnotated = buildAnnotatedString {
                            pushStyle(
                                SpanStyle(
                                    color = LocalContentColor.current,
                                    fontSize = 16.sp
                                )
                            )
                            append("\u00A9 ")
                            pushStyle(
                                SpanStyle(
                                    color = MaterialTheme.colors.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            pushStringAnnotation("WebsiteLink", "https://zwander.dev")
                            append("Zachary Wander")
                            pop()
                        }

                        val samloaderAnnotated = buildAnnotatedString {
                            pushStyle(
                                SpanStyle(
                                    color = LocalContentColor.current,
                                    fontSize = 16.sp
                                )
                            )
                            append("Code based on ")
                            pushStyle(
                                SpanStyle(
                                    color = MaterialTheme.colors.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            pushStringAnnotation("SamloaderLink", "https://github.com/nlscc/samloader")
                            append("Samloader")
                            pop()
                        }

                        val githubAnnotated = buildAnnotatedString {
                            pushStyle(
                                SpanStyle(
                                    color = LocalContentColor.current,
                                    fontSize = 16.sp
                                )
                            )
                            append("Check out the source on ")
                            pushStyle(
                                SpanStyle(
                                    color = MaterialTheme.colors.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            pushStringAnnotation("GitHubLink", "https://github.com/zacharee/SamloaderKotlin")
                            append("GitHub")
                            pop()
                        }

                        ClickableText(
                            text = copyrightAnnotated,
                            onClick = {
                                copyrightAnnotated.getStringAnnotations("WebsiteLink", it, it)
                                    .firstOrNull()?.let { item ->
                                        handler(item.item)
                                    }
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        ClickableText(
                            text = samloaderAnnotated,
                            onClick = {
                                samloaderAnnotated.getStringAnnotations("SamloaderLink", it, it)
                                    .firstOrNull()?.let { item ->
                                        handler(item.item)
                                    }
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        ClickableText(
                            text = githubAnnotated,
                            onClick = {
                                githubAnnotated.getStringAnnotations("GitHubLink", it, it)
                                    .firstOrNull()?.let { item ->
                                        handler(item.item)
                                    }
                            }
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.weight(1f)
                            .align(Alignment.Bottom)
                    ) {
                        Spacer(Modifier.weight(1f))

                        TextButton(
                            onClick = {
                                handler("https://twitter.com/wander1236")
                            }
                        ) {
                            Text("Follow me on Twitter")
                        }

                        Spacer(Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                handler("https://patreon.com/zacharywander")
                            }
                        ) {
                            Text("Check out my Patreon")
                        }
                    }
                }
            }
        }
    }
}