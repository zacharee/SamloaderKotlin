package org.jsoup

import com.soywiz.korio.stream.SyncInputStream
import io.ktor.utils.io.errors.*
import org.jsoup.helper.DataUtil.load
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import tk.zwander.common.data.File

/**
 * The core public access point to the jsoup functionality.
 *
 * @author Jonathan Hedley
 */
object Jsoup {
    /**
     * Parse HTML into a Document. The parser will make a sensible, balanced document tree out of any HTML.
     *
     * @param html    HTML to parse
     * @param baseUri The URL where the HTML was retrieved from. Used to resolve relative URLs to absolute URLs, that occur
     * before the HTML declares a `<base href>` tag.
     * @return sane HTML
     */
    fun parse(html: String, baseUri: String?): Document? {
        return Parser.parse(html, baseUri)
    }

    /**
     * Parse HTML into a Document, using the provided Parser. You can provide an alternate parser, such as a simple XML
     * (non-HTML) parser.
     *
     * @param html    HTML to parse
     * @param baseUri The URL where the HTML was retrieved from. Used to resolve relative URLs to absolute URLs, that occur
     * before the HTML declares a `<base href>` tag.
     * @param parser alternate [parser][Parser.xmlParser] to use.
     * @return sane HTML
     */
    fun parse(html: String, baseUri: String?, parser: Parser): Document? {
        return parser.parseInput(html, baseUri)
    }

    /**
     * Parse HTML into a Document, using the provided Parser. You can provide an alternate parser, such as a simple XML
     * (non-HTML) parser.  As no base URI is specified, absolute URL resolution, if required, relies on the HTML including
     * a `<base href>` tag.
     *
     * @param html    HTML to parse
     * before the HTML declares a `<base href>` tag.
     * @param parser alternate [parser][Parser.xmlParser] to use.
     * @return sane HTML
     */
    fun parse(html: String, parser: Parser): Document? {
        return parser.parseInput(html, "")
    }

    /**
     * Parse HTML into a Document. As no base URI is specified, absolute URL resolution, if required, relies on the HTML
     * including a `<base href>` tag.
     *
     * @param html HTML to parse
     * @return sane HTML
     * @see .parse
     */
    fun parse(html: String): Document? {
        return Parser.parse(html, "")
    }

//    /**
//     * Creates a new [Connection] (session), with the defined request URL. Use to fetch and parse a HTML page.
//     *
//     *
//     * Use examples:
//     *
//     *  * `Document doc = Jsoup.connect("http://example.com").userAgent("Mozilla").data("name", "jsoup").get();`
//     *  * `Document doc = Jsoup.connect("http://example.com").cookie("auth", "token").post();`
//     *
//     * @param url URL to connect to. The protocol must be `http` or `https`.
//     * @return the connection. You can add data, cookies, and headers; set the user-agent, referrer, method; and then execute.
//     * @see .newSession
//     * @see Connection.newRequest
//     */
//    fun connect(url: String?): Connection {
//        return HttpConnection.Companion.connect(url)
//    }

//    /**
//     * Creates a new [Connection] to use as a session. Connection settings (user-agent, timeouts, URL, etc), and
//     * cookies will be maintained for the session. Use examples:
//     * <pre>`
//     * Connection session = Jsoup.newSession()
//     * .timeout(20 * 1000)
//     * .userAgent("FooBar 2000");
//     *
//     * Document doc1 = session.newRequest()
//     * .url("https://jsoup.org/").data("ref", "example")
//     * .get();
//     * Document doc2 = session.newRequest()
//     * .url("https://en.wikipedia.org/wiki/Main_Page")
//     * .get();
//     * Connection con3 = session.newRequest();
//    `</pre> *
//     *
//     *
//     * For multi-threaded requests, it is safe to use this session between threads, but take care to call [Connection.newRequest] per request and not share that instance between threads when executing or parsing.
//     *
//     * @return a connection
//     * @since 1.14.1
//     */
//    fun newSession(): Connection {
//        return HttpConnection()
//    }

    /**
     * Parse the contents of a file as HTML.
     *
     * @param file          file to load HTML from. Supports gzipped files (ending in .z or .gz).
     * @param charsetName (optional) character set of file contents. Set to `null` to determine from `http-equiv` meta tag, if
     * present, or fall back to `UTF-8` (which is often safe to do).
     * @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     * @return sane HTML
     * @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    suspend fun parse(file: File, charsetName: String, baseUri: String?): Document? {
        return load(file, charsetName, baseUri)
    }

    /**
     * Parse the contents of a file as HTML. The location of the file is used as the base URI to qualify relative URLs.
     *
     * @param file        file to load HTML from. Supports gzipped files (ending in .z or .gz).
     * @param charsetName (optional) character set of file contents. Set to `null` to determine from `http-equiv` meta tag, if
     * present, or fall back to `UTF-8` (which is often safe to do).
     * @return sane HTML
     * @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     * @see .parse
     */
    suspend fun parse(file: File,  charsetName: String?): Document? {
        return load(file, charsetName, file.getAbsolutePath())
    }

    /**
     * Parse the contents of a file as HTML. The location of the file is used as the base URI to qualify relative URLs.
     * The charset used to read the file will be determined by the byte-order-mark (BOM), or a `<meta charset>` tag,
     * or if neither is present, will be `UTF-8`.
     *
     *
     * This is the equivalent of calling [parse(file, null)][.parse]
     *
     * @param file the file to load HTML from. Supports gzipped files (ending in .z or .gz).
     * @return sane HTML
     * @throws IOException if the file could not be found or read.
     * @see .parse
     * @since 1.15.1
     */
    suspend fun parse(file: File): Document? {
        return load(file, null, file.getAbsolutePath())
    }

    /**
     * Parse the contents of a file as HTML.
     *
     * @param file          file to load HTML from. Supports gzipped files (ending in .z or .gz).
     * @param charsetName (optional) character set of file contents. Set to `null` to determine from `http-equiv` meta tag, if
     * present, or fall back to `UTF-8` (which is often safe to do).
     * @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     * @param parser alternate [parser][Parser.xmlParser] to use.
     * @return sane HTML
     * @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     * @since 1.14.2
     */
    suspend fun parse(file: File,  charsetName: String?, baseUri: String?, parser: Parser?): Document? {
        return load(file, charsetName, baseUri, parser)
    }

    /**
     * Read an input stream, and parse it to a Document.
     *
     * @param in          input stream to read. The stream will be closed after reading.
     * @param charsetName (optional) character set of file contents. Set to `null` to determine from `http-equiv` meta tag, if
     * present, or fall back to `UTF-8` (which is often safe to do).
     * @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     * @return sane HTML
     * @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    @Throws(IOException::class)
    fun parse(`in`: SyncInputStream?,  charsetName: String?, baseUri: String?): Document? {
        return load(`in`, charsetName, baseUri)
    }

    /**
     * Read an input stream, and parse it to a Document. You can provide an alternate parser, such as a simple XML
     * (non-HTML) parser.
     *
     * @param in          input stream to read. Make sure to close it after parsing.
     * @param charsetName (optional) character set of file contents. Set to `null` to determine from `http-equiv` meta tag, if
     * present, or fall back to `UTF-8` (which is often safe to do).
     * @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     * @param parser alternate [parser][Parser.xmlParser] to use.
     * @return sane HTML
     * @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    @Throws(IOException::class)
    fun parse(`in`: SyncInputStream?,  charsetName: String?, baseUri: String?, parser: Parser?): Document? {
        return load(`in`, charsetName, baseUri, parser)
    }

    /**
     * Parse a fragment of HTML, with the assumption that it forms the `body` of the HTML.
     *
     * @param bodyHtml body HTML fragment
     * @param baseUri  URL to resolve relative URLs against.
     * @return sane HTML document
     * @see Document.body
     */
    fun parseBodyFragment(bodyHtml: String?, baseUri: String?): Document {
        return Parser.Companion.parseBodyFragment(bodyHtml, baseUri)
    }

    /**
     * Parse a fragment of HTML, with the assumption that it forms the `body` of the HTML.
     *
     * @param bodyHtml body HTML fragment
     * @return sane HTML document
     * @see Document.body
     */
    fun parseBodyFragment(bodyHtml: String?): Document {
        return Parser.Companion.parseBodyFragment(bodyHtml, "")
    }

//    /**
//     * Fetch a URL, and parse it as HTML. Provided for compatibility; in most cases use [.connect] instead.
//     *
//     *
//     * The encoding character set is determined by the content-type header or http-equiv meta tag, or falls back to `UTF-8`.
//     *
//     * @param url           URL to fetch (with a GET). The protocol must be `http` or `https`.
//     * @param timeoutMillis Connection and read timeout, in milliseconds. If exceeded, IOException is thrown.
//     * @return The parsed HTML.
//     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
//     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
//     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
//     * @throws java.net.SocketTimeoutException if the connection times out
//     * @throws IOException if a connection or read error occurs
//     * @see .connect
//     */
//    @Throws(IOException::class)
//    fun parse(url: URL?, timeoutMillis: Int): Document? {
//        val con: Connection = HttpConnection.Companion.connect(url)
//        con.timeout(timeoutMillis)
//        return con.get()
//    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through an allow-list of safe
     * tags and attributes.
     *
     * @param bodyHtml  input untrusted HTML (body fragment)
     * @param baseUri   URL to resolve relative URLs against
     * @param safelist  list of permitted HTML elements
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(bodyHtml: String?, baseUri: String?, safelist: Safelist): String? {
        val dirty: Document = Jsoup.parseBodyFragment(bodyHtml, baseUri)
        val cleaner = Cleaner(safelist)
        val clean: Document = cleaner.clean(dirty)
        return clean.body()?.html()
    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through a safe-list of permitted
     * tags and attributes.
     *
     *
     * Note that as this method does not take a base href URL to resolve attributes with relative URLs against, those
     * URLs will be removed, unless the input HTML contains a `<base href> tag`. If you wish to preserve those, use
     * the [Jsoup.clean] method instead, and enable
     * [Safelist.preserveRelativeLinks].
     *
     * @param bodyHtml input untrusted HTML (body fragment)
     * @param safelist list of permitted HTML elements
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(bodyHtml: String?, safelist: Safelist): String? {
        return Jsoup.clean(bodyHtml, "", safelist)
    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through a safe-list of
     * permitted tags and attributes.
     *
     * The HTML is treated as a body fragment; it's expected the cleaned HTML will be used within the body of an
     * existing document. If you want to clean full documents, use [Cleaner.clean] instead, and add
     * structural tags (`html, head, body` etc) to the safelist.
     *
     * @param bodyHtml input untrusted HTML (body fragment)
     * @param baseUri URL to resolve relative URLs against
     * @param safelist list of permitted HTML elements
     * @param outputSettings document output settings; use to control pretty-printing and entity escape modes
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(
        bodyHtml: String?,
        baseUri: String?,
        safelist: Safelist,
        outputSettings: Document.OutputSettings?
    ): String? {
        val dirty: Document = Jsoup.parseBodyFragment(bodyHtml, baseUri)
        val cleaner = Cleaner(safelist)
        val clean: Document = cleaner.clean(dirty)
        clean.outputSettings(outputSettings)
        return clean.body()?.html()
    }

    /**
     * Test if the input body HTML has only tags and attributes allowed by the Safelist. Useful for form validation.
     *
     * The input HTML should still be run through the cleaner to set up enforced attributes, and to tidy the output.
     *
     * Assumes the HTML is a body fragment (i.e. will be used in an existing HTML document body.)
     * @param bodyHtml HTML to test
     * @param safelist safelist to test against
     * @return true if no tags or attributes were removed; false otherwise
     * @see .clean
     */
    fun isValid(bodyHtml: String?, safelist: Safelist): Boolean {
        return Cleaner(safelist).isValidBodyHtml(bodyHtml)
    }
}
