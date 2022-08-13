package org.jsoup.helper

import com.soywiz.korio.stream.SyncInputStream
import com.soywiz.korio.stream.SyncOutputStream
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import okio.BufferedSource
import okio.buffer
import okio.Buffer
import org.jsoup.UncheckedIOException
import org.jsoup.internal.Normalizer
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.*
import org.jsoup.parser.Parser
import org.jsoup.parser.SourceMarker
import org.jsoup.parser.source
import org.jsoup.select.Elements
import tk.zwander.common.data.File
import kotlin.random.Random

/**
 * Internal static utilities for handling data.
 *
 */
object DataUtil {
    private val charsetPattern: Regex = Regex("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)")
    val UTF_8: Charset =
        Charset.forName("UTF-8") // Don't use StandardCharsets, as those only appear in Android API 19, and we target 10.
    private val defaultCharsetName: String = UTF_8.name // used if not found in header or meta charset
    private const val firstReadBufferSize: Int = 1024 * 5
    const val bufferSize: Int = 1024 * 32
    private val mimeBoundaryChars: CharArray =
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    const val boundaryLength: Int = 32

    /**
     * Loads and parses a file to a Document. Files that are compressed with gzip (and end in `.gz` or `.z`)
     * are supported in addition to uncompressed files.
     *
     * @param file file to load
     * @param charsetName (optional) character set of input; specify `null` to attempt to autodetect. A BOM in
     * the file will always override this setting.
     * @param baseUri base URI of document, to resolve relative links against
     * @param parser alternate [parser][Parser.xmlParser] to use.
     *
     * @return Document
     * @throws IOException on IO error
     * @since 1.14.2
     */
    /**
     * Loads and parses a file to a Document, with the HtmlParser. Files that are compressed with gzip (and end in `.gz` or `.z`)
     * are supported in addition to uncompressed files.
     *
     * @param file file to load
     * @param charsetName (optional) character set of input; specify `null` to attempt to autodetect. A BOM in
     * the file will always override this setting.
     * @param baseUri base URI of document, to resolve relative links against
     * @return Document
     * @throws IOException on IO error
     */
    fun load(
        file: File,
        charsetName: String?,
        baseUri: String?,
        parser: Parser? = Parser.htmlParser()
    ): Document? {
        val stream = file.openSyncInputStream()
//        val name: String = Normalizer.lowerCase(file.getName())
//        if (name!!.endsWith(".gz") || name.endsWith(".z")) {
//            // unfortunately file input streams don't support marks (why not?), so we will close and reopen after read
//            val zipped: Boolean
//            try {
//                zipped = (stream.read() == 0x1f && stream.read() == 0x8b) // gzip magic bytes
//            } finally {
//                stream.close()
//            }
//            stream = if (zipped) GZIPInputStream(FileInputStream(file)) else FileInputStream(file)
//        }
        return parseInputStream(stream, charsetName, baseUri, parser)
    }

    /**
     * Parses a Document from an input steam.
     * @param in input stream to parse. The stream will be closed after reading.
     * @param charsetName character set of input (optional)
     * @param baseUri base URI of document, to resolve relative links against
     * @return Document
     * @throws IOException on IO error
     */
    @Throws(IOException::class)
    fun load(`in`: SyncInputStream?,  charsetName: String?, baseUri: String?): Document? {
        return parseInputStream(`in`, charsetName, baseUri, Parser.htmlParser())
    }

    /**
     * Parses a Document from an input steam, using the provided Parser.
     * @param in input stream to parse. The stream will be closed after reading.
     * @param charsetName character set of input (optional)
     * @param baseUri base URI of document, to resolve relative links against
     * @param parser alternate [parser][Parser.xmlParser] to use.
     * @return Document
     * @throws IOException on IO error
     */
    @Throws(IOException::class)
    fun load(
        `in`: SyncInputStream?,
         charsetName: String?,
        baseUri: String?,
        parser: Parser?
    ): Document? {
        return parseInputStream(`in`, charsetName, baseUri, parser)
    }

    /**
     * Writes the input stream to the output stream. Doesn't close them.
     * @param in input stream to read from
     * @param out output stream to write to
     * @throws IOException on IO error
     */
    @Throws(IOException::class)
    fun crossStreams(`in`: SyncInputStream, out: SyncOutputStream) {
        val buffer = ByteArray(bufferSize)
        var len: Int
        while ((`in`.read(buffer).also { len = it }) != -1) {
            out.write(buffer, 0, len)
        }
    }

    @Throws(IOException::class)
    fun parseInputStream(
        input: SyncInputStream?,
        charsetName: String?,
        baseUri: String?,
        parser: Parser?
    ): Document? {
        var charsetName: String? = charsetName
        if (input == null) {
            return Document(baseUri)
        }
        val input = SourceMarker(input.source())
        var doc: Document? = null

        // read the start of the stream and look for a BOM or meta charset
        try {
            val reset = input.mark(bufferSize.toLong())
            val firstBytes = Buffer().apply { input.source().buffer.copyTo(this, 0, (firstReadBufferSize - 1).toLong()) } // -1 because we read one more to see if completed. First read is < buffer size, so can't be invalid.
            val fullyRead: Boolean = (input.source().exhausted())
            input.reset(reset)

            // look for BOM - overrides any other header or input
            val bomCharset: BomCharset? = detectCharsetFromBom(firstBytes)
            if (bomCharset != null) charsetName = bomCharset.charset
            if (charsetName == null) { // determine from meta. safe first parse as UTF-8
                try {
                    val defaultDecoded: Buffer = firstBytes.copy()
                    doc = parser!!.parseInput(UTF_8.newDecoder().decode(ByteReadPacket(defaultDecoded.readByteArray())), baseUri)
                } catch (e: UncheckedIOException) {
                    throw e.ioException()
                }

                // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
                val metaElements: Elements? = doc!!.select("meta[http-equiv=content-type], meta[charset]")
                var foundCharset: String? = null // if not found, will keep utf-8 as best attempt

                metaElements?.forEach { meta ->
                    if (meta.hasAttr("http-equiv")) foundCharset = getCharsetFromContentType(meta.attr("content"))
                    if (foundCharset == null && meta.hasAttr("charset")) foundCharset = meta.attr("charset")
                }

                // look for <?xml encoding='ISO-8859-1'?>
                if (foundCharset == null && doc.childNodeSize() > 0) {
                    val first: Node? = doc.childNode(0)
                    var decl: XmlDeclaration? = null
                    if (first is XmlDeclaration) decl = first else if (first is Comment) {
                        val comment: Comment = first
                        if (comment.isXmlDeclaration) decl = comment.asXmlDeclaration()
                    }
                    if (decl != null) {
                        if (decl.name().equals("xml", ignoreCase = true)) foundCharset = decl.attr("encoding")
                    }
                }
                foundCharset = validateCharset(foundCharset)
                if (foundCharset != null && !foundCharset.equals(
                        defaultCharsetName,
                        ignoreCase = true
                    )
                ) { // need to re-decode. (case insensitive check here to match how validate works)
                    foundCharset = foundCharset?.trim { it <= ' ' }?.replace("[\"']".toRegex(), "")
                    charsetName = foundCharset
                    doc = null
                } else if (!fullyRead) {
                    doc = null
                }
            } else { // specified by content type header (or by user on file load)
                Validate.notEmpty(
                    charsetName,
                    "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML"
                )
            }
            if (doc == null) {
                if (charsetName == null) charsetName = defaultCharsetName
                val reader: BufferedSource = input.source().buffer() // Android level does not allow us try-with-resources
                try {
                    if (bomCharset != null && bomCharset.offset) { // creating the buffered reader ignores the input pos, so must skip here
                        reader.skip(1)
                    }
                    try {
                        doc = parser!!.parseInput(reader, baseUri)
                    } catch (e: UncheckedIOException) {
                        // io exception when parsing (not seen before because reading the stream as we go)
                        throw e.ioException()
                    }
                    val charset: Charset =
                        if ((charsetName == defaultCharsetName)) UTF_8 else Charset.forName(charsetName)
                    doc!!.outputSettings()!!.charset(charset)
                } finally {
                    reader.close()
                }
            }
        } finally {
            input.source().close()
        }
        return doc
    }

    /**
     * Read the input stream into a byte buffer. To deal with slow input streams, you may interrupt the thread this
     * method is executing on. The data read until being interrupted will be available.
     * @param inStream the input stream to read from
     * @param maxSize the maximum size in bytes to read from the stream. Set to 0 to be unlimited.
     * @return the filled byte buffer
     * @throws IOException if an exception occurs whilst reading from the input stream.
     */
//    @Throws(IOException::class)
//    fun readToByteBuffer(inStream: SourceMarker, maxSize: Int): Buffer {
//        Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger")
//        val input: ConstrainableInputStream = ConstrainableInputStream.wrap(inStream, bufferSize, maxSize)
//        return input.readToByteBuffer(maxSize)
//    }

    fun emptyByteBuffer(): Buffer {
        return Buffer()
    }

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default
     * will kick in.)
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */

    private fun getCharsetFromContentType( contentType: String?): String? {
        if (contentType == null) return null
        val m = charsetPattern.matchEntire(contentType)

        if (m?.groups?.isNotEmpty() == true) {
            var charset: String = m.groupValues[1].trim { it <= ' ' }
            charset = charset.replace("charset=", "")
            return validateCharset(charset)
        }
        return null
    }


    private fun validateCharset(cs: String?): String? {
        var cs: String? = cs
        if (cs.isNullOrEmpty()) return null
        cs = cs.trim { it <= ' ' }.replace("[\"']".toRegex(), "")
        try {
            if (Charset.isSupported(cs)) return cs
            cs = cs.uppercase()
            if (Charset.isSupported(cs)) return cs
        } catch (e: Exception) {
            // if our this charset matching fails.... we just take the default
        }
        return null
    }

    /**
     * Creates a random string, suitable for use as a mime boundary
     */
    fun mimeBoundary(): String {
        val mime: StringBuilder = StringUtil.borrowBuilder()
        val rand: Random = Random.Default
        for (i in 0 until boundaryLength) {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.size)])
        }
        return StringUtil.releaseBuilder(mime)
    }


    private fun detectCharsetFromBom(byteData: Buffer): BomCharset? {
        val buffer = SourceMarker(byteData)
        buffer.mark(0)
        val bom = Buffer()
        if (buffer.run { limit - mark } >= bom.size) {
            byteData.copyTo(bom, 0, 4)
            buffer.rewind()
        }
        if ((bom[0].toInt() == 0x00) && (bom[1]
                .toInt() == 0x00) && (bom[2] == 0xFE.toByte()) && (bom[3] == 0xFF.toByte()) ||  // BE
            (bom[0] == 0xFF.toByte()) && (bom[1] == 0xFE.toByte()) && (bom[2]
                .toInt() == 0x00) && (bom[3].toInt() == 0x00)
        ) { // LE
            return BomCharset("UTF-32", false) // and I hope it's on your system
        } else if (bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() ||  // BE
            bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte()
        ) {
            return BomCharset("UTF-16", false) // in all Javas
        } else if ((bom[0] == 0xEF.toByte()) && (bom[1] == 0xBB.toByte()) && (bom[2] == 0xBF.toByte())) {
            return BomCharset("UTF-8", true) // in all Javas
            // 16 and 32 decoders consume the BOM to determine be/le; utf-8 should be consumed here
        }
        return null
    }

    private class BomCharset constructor(val charset: String, val offset: Boolean)
}
