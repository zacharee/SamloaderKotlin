package org.jsoup.helper

import com.soywiz.korio.stream.SyncInputStream
import com.soywiz.korio.stream.SyncOutputStream
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.errors.*
import org.jsoup.internal.ConstrainableInputStream
import org.jsoup.internal.Normalizer
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.*
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import tk.zwander.common.data.File

/**
 * Internal static utilities for handling data.
 *
 */
object DataUtil {
    private val charsetPattern: Regex = Regex("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)")
    val UTF_8: Charset =
        Charset.forName("UTF-8") // Don't use StandardCharsets, as those only appear in Android API 19, and we target 10.
    val defaultCharsetName: String = UTF_8.name // used if not found in header or meta charset
    private val firstReadBufferSize: Int = 1024 * 5
    val bufferSize: Int = 1024 * 32
    private val mimeBoundaryChars: CharArray =
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    val boundaryLength: Int = 32
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
    suspend fun load(
        file: File,
        charsetName: String?,
        baseUri: String?,
        parser: Parser? = Parser.htmlParser()
    ): Document? {
        var stream = file.openInputStream()
        val name: String? = Normalizer.lowerCase(file.getName())
        if (name!!.endsWith(".gz") || name.endsWith(".z")) {
            // unfortunately file input streams don't support marks (why not?), so we will close and reopen after read
            val zipped: Boolean
            try {
                zipped = (stream.read() == 0x1f && stream.read() == 0x8b) // gzip magic bytes
            } finally {
                stream.close()
            }
            stream = if (zipped) GZIPInputStream(FileInputStream(file)) else FileInputStream(file)
        }
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
        return parseInputStream(`in`, charsetName, baseUri, Parser.Companion.htmlParser())
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
        var input: SyncInputStream? = input
        var charsetName: String? = charsetName
        if (input == null) // empty body
            return Document(baseUri)
        input = ConstrainableInputStream.Companion.wrap(input, bufferSize, 0)
         var doc: Document? = null

        // read the start of the stream and look for a BOM or meta charset
        try {
            input.mark(bufferSize)
            val firstBytes: ByteBuffer? = readToByteBuffer(
                input,
                firstReadBufferSize - 1
            ) // -1 because we read one more to see if completed. First read is < buffer size, so can't be invalid.
            val fullyRead: Boolean = (input.read() == -1)
            input.reset()

            // look for BOM - overrides any other header or input
            val bomCharset: BomCharset? = detectCharsetFromBom(firstBytes)
            if (bomCharset != null) charsetName = bomCharset.charset
            if (charsetName == null) { // determine from meta. safe first parse as UTF-8
                try {
                    val defaultDecoded: CharBuffer = UTF_8.decode(firstBytes)
                    if (defaultDecoded.hasArray()) doc = parser!!.parseInput(
                        CharArrayReader(
                            defaultDecoded.array(),
                            defaultDecoded.arrayOffset(),
                            defaultDecoded.limit()
                        ), baseUri
                    ) else doc = parser!!.parseInput(defaultDecoded.toString(), baseUri)
                } catch (e: jsoup.UncheckedIOException) {
                    throw e.ioException()
                }

                // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
                val metaElements: Elements? = doc!!.select("meta[http-equiv=content-type], meta[charset]")
                var foundCharset: String? = null // if not found, will keep utf-8 as best attempt

                metaElements?.forEach { meta ->
                    if (meta?.hasAttr("http-equiv") == true) foundCharset = getCharsetFromContentType(meta.attr("content"))
                    if (foundCharset == null && meta?.hasAttr("charset") == true) foundCharset = meta.attr("charset")
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
                val reader: BufferedReader = BufferedReader(
                    InputStreamReader(input, Charset.forName(charsetName)),
                    bufferSize
                ) // Android level does not allow us try-with-resources
                try {
                    if (bomCharset != null && bomCharset.offset) { // creating the buffered reader ignores the input pos, so must skip here
                        val skipped: Long = reader.skip(1)
                        Validate.isTrue(skipped == 1L) // WTF if this fails.
                    }
                    try {
                        doc = parser!!.parseInput(reader, baseUri)
                    } catch (e: jsoup.UncheckedIOException) {
                        // io exception when parsing (not seen before because reading the stream as we go)
                        throw e.ioException()
                    }
                    val charset: Charset =
                        if ((charsetName == defaultCharsetName)) UTF_8 else Charset.forName(charsetName)
                    doc!!.outputSettings()!!.charset(charset)
                    if (!charset.canEncode()) {
                        // some charsets can read but not encode; switch to an encodable charset and update the meta el
                        doc.charset(UTF_8)
                    }
                } finally {
                    reader.close()
                }
            }
        } finally {
            input.close()
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
    @Throws(IOException::class)
    fun readToByteBuffer(inStream: InputStream?, maxSize: Int): ByteBuffer? {
        Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger")
        val input: ConstrainableInputStream = ConstrainableInputStream.Companion.wrap(inStream, bufferSize, maxSize)
        return input.readToByteBuffer(maxSize)
    }

    fun emptyByteBuffer(): ByteBuffer {
        return ByteBuffer.allocate(0)
    }

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default
     * will kick in.)
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */

    fun getCharsetFromContentType( contentType: String?): String? {
        if (contentType == null) return null
        val m: Matcher = charsetPattern.matcher(contentType)
        if (m.find()) {
            var charset: String = m.group(1).trim({ it <= ' ' })
            charset = charset.replace("charset=", "")
            return validateCharset(charset)
        }
        return null
    }


    private fun validateCharset( cs: String?): String? {
        var cs: String? = cs
        if (cs == null || cs.length == 0) return null
        cs = cs.trim({ it <= ' ' }).replace("[\"']".toRegex(), "")
        try {
            if (Charset.isSupported(cs)) return cs
            cs = cs.uppercase()
            if (Charset.isSupported(cs)) return cs
        } catch (e: IllegalCharsetNameException) {
            // if our this charset matching fails.... we just take the default
        }
        return null
    }

    /**
     * Creates a random string, suitable for use as a mime boundary
     */
    fun mimeBoundary(): String? {
        val mime: StringBuilder? = StringUtil.borrowBuilder()
        val rand: Random = Random()
        for (i in 0 until boundaryLength) {
            mime!!.append(mimeBoundaryChars.get(rand.nextInt(mimeBoundaryChars.size)))
        }
        return StringUtil.releaseBuilder(mime)
    }


    private fun detectCharsetFromBom(byteData: ByteBuffer?): BomCharset? {
        val buffer: Buffer? =
            byteData // .mark and rewind used to return Buffer, now ByteBuffer, so cast for backward compat
        buffer!!.mark()
        val bom: ByteArray = ByteArray(4)
        if (byteData!!.remaining() >= bom.size) {
            byteData.get(bom)
            buffer.rewind()
        }
        if ((bom.get(0).toInt() == 0x00) && (bom.get(1)
                .toInt() == 0x00) && (bom.get(2) == 0xFE.toByte()) && (bom.get(3) == 0xFF.toByte()) ||  // BE
            (bom.get(0) == 0xFF.toByte()) && (bom.get(1) == 0xFE.toByte()) && (bom.get(2)
                .toInt() == 0x00) && (bom.get(3).toInt() == 0x00)
        ) { // LE
            return BomCharset("UTF-32", false) // and I hope it's on your system
        } else if (bom.get(0) == 0xFE.toByte() && bom.get(1) == 0xFF.toByte() ||  // BE
            bom.get(0) == 0xFF.toByte() && bom.get(1) == 0xFE.toByte()
        ) {
            return BomCharset("UTF-16", false) // in all Javas
        } else if ((bom.get(0) == 0xEF.toByte()) && (bom.get(1) == 0xBB.toByte()) && (bom.get(2) == 0xBF.toByte())) {
            return BomCharset("UTF-8", true) // in all Javas
            // 16 and 32 decoders consume the BOM to determine be/le; utf-8 should be consumed here
        }
        return null
    }

    private class BomCharset constructor(val charset: String, val offset: Boolean)
}
