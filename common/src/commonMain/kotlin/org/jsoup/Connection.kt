package org.jsoup

import com.soywiz.korio.net.URL
import com.soywiz.korio.stream.BufferedStreamBase
import com.soywiz.korio.stream.SyncInputStream
import io.ktor.utils.io.errors.*
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

/**
 * The Connection interface is a convenient HTTP client and session object to fetch content from the web, and parse them
 * into Documents.
 *
 * To start a new session, use either [Jsoup.newSession] or [Jsoup.connect].
 * Connections contain [Request] and [Response] objects (once executed). Configuration
 * settings (URL, timeout, useragent, etc) set on a session will be applied by default to each subsequent request.
 *
 * To start a new request from the session, use [.newRequest].
 *
 * Cookies are stored in memory for the duration of the session. For that reason, do not use one single session for all
 * requests in a long-lived application, or you are likely to run out of memory, unless care is taken to clean up the
 * cookie store. The cookie store for the session is available via [.cookieStore]. You may provide your own
 * implementation via [.cookieStore] before making requests.
 *
 * Request configuration can be made using either the shortcut methods in Connection (e.g. [.userAgent]),
 * or by methods in the Connection.Request object directly. All request configuration must be made before the request is
 * executed. When used as an ongoing session, initialize all defaults prior to making multi-threaded [.newRequest]s.
 *
 * Note that the term "Connection" used here does not mean that a long-lived connection is held against a server for
 * the lifetime of the Connection object. A socket connection is only made at the point of request execution ([.execute], [.get], or [.post]), and the server's response consumed.
 *
 * For multi-threaded implementations, it is important to use a [.newRequest] for each request. The session may
 * be shared across threads but a given request, not.
 */
open interface Connection {
    /**
     * GET and POST http methods.
     */
    enum class Method(private val hasBody: Boolean) {
        GET(false), POST(true), PUT(true), DELETE(false), PATCH(true), HEAD(false), OPTIONS(false), TRACE(false);

        /**
         * Check if this HTTP method has/needs a request body
         * @return if body needed
         */
        fun hasBody(): Boolean {
            return hasBody
        }
    }

    /**
     * Creates a new request, using this Connection as the session-state and to initialize the connection settings (which may then be independently on the returned Connection.Request object).
     * @return a new Connection object, with a shared Cookie Store and initialized settings from this Connection and Request
     * @since 1.14.1
     */
    open fun newRequest(): Connection

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    open fun url(url: URL?): Connection

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    open fun url(url: String?): Connection

    /**
     * Set the HTTP proxy to use for this request.
     * @param host the proxy hostname
     * @param port the proxy port
     * @return this Connection, for chaining
     */
    open fun proxy(host: String?, port: Int): Connection

    /**
     * Set the request user-agent header.
     * @param userAgent user-agent to use
     * @return this Connection, for chaining
     * @see org.jsoup.helper.HttpConnection.DEFAULT_UA
     */
    open fun userAgent(userAgent: String?): Connection

    /**
     * Set the total request timeout duration. If a timeout occurs, an [java.net.SocketTimeoutException] will be thrown.
     *
     * The default timeout is **30 seconds** (30,000 millis). A timeout of zero is treated as an infinite timeout.
     *
     * Note that this timeout specifies the combined maximum duration of the connection time and the time to read
     * the full response.
     * @param millis number of milliseconds (thousandths of a second) before timing out connects or reads.
     * @return this Connection, for chaining
     * @see .maxBodySize
     */
    open fun timeout(millis: Int): Connection

    /**
     * Set the maximum bytes to read from the (uncompressed) connection into the body, before the connection is closed,
     * and the input truncated (i.e. the body content will be trimmed). **The default maximum is 2MB**. A max size of
     * `0` is treated as an infinite amount (bounded only by your patience and the memory available on your
     * machine).
     *
     * @param bytes number of bytes to read from the input before truncating
     * @return this Connection, for chaining
     */
    open fun maxBodySize(bytes: Int): Connection

    /**
     * Set the request referrer (aka "referer") header.
     * @param referrer referrer to use
     * @return this Connection, for chaining
     */
    open fun referrer(referrer: String?): Connection

    /**
     * Configures the connection to (not) follow server redirects. By default this is **true**.
     * @param followRedirects true if server redirects should be followed.
     * @return this Connection, for chaining
     */
    open fun followRedirects(followRedirects: Boolean): Connection

    /**
     * Set the request method to use, GET or POST. Default is GET.
     * @param method HTTP request method
     * @return this Connection, for chaining
     */
    open fun method(method: Connection.Method?): Connection

    /**
     * Configures the connection to not throw exceptions when a HTTP error occurs. (4xx - 5xx, e.g. 404 or 500). By
     * default this is **false**; an IOException is thrown if an error is encountered. If set to **true**, the
     * response is populated with the error body, and the status message will reflect the error.
     * @param ignoreHttpErrors - false (default) if HTTP errors should be ignored.
     * @return this Connection, for chaining
     */
    open fun ignoreHttpErrors(ignoreHttpErrors: Boolean): Connection

    /**
     * Ignore the document's Content-Type when parsing the response. By default this is **false**, an unrecognised
     * content-type will cause an IOException to be thrown. (This is to prevent producing garbage by attempting to parse
     * a JPEG binary image, for example.) Set to true to force a parse attempt regardless of content type.
     * @param ignoreContentType set to true if you would like the content type ignored on parsing the response into a
     * Document.
     * @return this Connection, for chaining
     */
    open fun ignoreContentType(ignoreContentType: Boolean): Connection

    /**
     * Add a request data parameter. Request parameters are sent in the request query string for GETs, and in the
     * request body for POSTs. A request may have multiple values of the same name.
     * @param key data key
     * @param value data value
     * @return this Connection, for chaining
     */
    open fun data(key: String?, value: String?): Connection

    /**
     * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
     * input stream.
     * @param key data key (form item name)
     * @param filename the name of the file to present to the remove server. Typically just the name, not path,
     * component.
     * @param inputStream the input stream to upload, that you probably obtained from a [java.io.FileInputStream].
     * You must close the InputStream in a `finally` block.
     * @return this Connections, for chaining
     * @see .data
     */
    open fun data(key: String?, filename: String?, inputStream: SyncInputStream?): Connection

    /**
     * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
     * input stream.
     * @param key data key (form item name)
     * @param filename the name of the file to present to the remove server. Typically just the name, not path,
     * component.
     * @param inputStream the input stream to upload, that you probably obtained from a [java.io.FileInputStream].
     * @param contentType the Content Type (aka mimetype) to specify for this file.
     * You must close the InputStream in a `finally` block.
     * @return this Connections, for chaining
     */
    open fun data(key: String?, filename: String?, inputStream: SyncInputStream?, contentType: String?): Connection

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data collection of data parameters
     * @return this Connection, for chaining
     */
    open fun data(data: Collection<Connection.KeyVal?>): Connection

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data map of data parameters
     * @return this Connection, for chaining
     */
    open fun data(data: Map<String?, String?>): Connection

    /**
     * Add one or more request `key, val` data parameter pairs.
     *
     *Multiple parameters may be set at once, e.g.:
     * `.data("name", "jsoup", "language", "Java", "language", "English");` creates a query string like:
     * `?name=jsoup&language=Java&language=English`
     *
     * For GET requests, data parameters will be sent on the request query string. For POST (and other methods that
     * contain a body), they will be sent as body form parameters, unless the body is explicitly set by [.requestBody], in which case they will be query string parameters.
     *
     * @param keyvals a set of key value pairs.
     * @return this Connection, for chaining
     */
    open fun data(vararg keyvals: String?): Connection

    /**
     * Get the data KeyVal for this key, if any
     * @param key the data key
     * @return null if not set
     */

    open fun data(key: String): Connection.KeyVal?

    /**
     * Set a POST (or PUT) request body. Useful when a server expects a plain request body, not a set for URL
     * encoded form key/value pairs. E.g.:
     * `<pre>Jsoup.connect(url)
     * .requestBody(json)
     * .header("Content-Type", "application/json")
     * .post();</pre>`
     * If any data key/vals are supplied, they will be sent as URL query params.
     * @return this Request, for chaining
     */
    open fun requestBody(body: String?): Connection

    /**
     * Set a request header.
     * @param name header name
     * @param value header value
     * @return this Connection, for chaining
     * @see Request.headers
     */
    open fun header(name: String?, value: String?): Connection

    /**
     * Adds each of the supplied headers to the request.
     * @param headers map of headers name -&gt; value pairs
     * @return this Connection, for chaining
     * @see Request.headers
     */
    open fun headers(headers: Map<String?, String?>): Connection

    /**
     * Set a cookie to be sent in the request.
     * @param name name of cookie
     * @param value value of cookie
     * @return this Connection, for chaining
     */
    open fun cookie(name: String?, value: String?): Connection

    /**
     * Adds each of the supplied cookies to the request.
     * @param cookies map of cookie name -&gt; value pairs
     * @return this Connection, for chaining
     */
    open fun cookies(cookies: Map<String?, String?>): Connection

    /**
     * Provide an alternate parser to use when parsing the response to a Document. If not set, defaults to the HTML
     * parser, unless the response content-type is XML, in which case the XML parser is used.
     * @param parser alternate parser
     * @return this Connection, for chaining
     */
    open fun parser(parser: Parser?): Connection

    /**
     * Sets the default post data character set for x-www-form-urlencoded post data
     * @param charset character set to encode post data
     * @return this Connection, for chaining
     */
    open fun postDataCharset(charset: String?): Connection

    /**
     * Execute the request as a GET, and parse the result.
     * @return parsed Document
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    @Throws(IOException::class)
    open fun get(): Document?

    /**
     * Execute the request as a POST, and parse the result.
     * @return parsed Document
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    @Throws(IOException::class)
    open fun post(): Document?

    /**
     * Execute the request.
     * @return a response object
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    @Throws(IOException::class)
    open fun execute(): Response?

    /**
     * Get the request object associated with this connection
     * @return request
     */
    open fun request(): Request

    /**
     * Set the connection's request
     * @param request new request object
     * @return this Connection, for chaining
     */
    open fun request(request: Request): Connection

    /**
     * Get the response, once the request has been executed.
     * @return response
     * @throws IllegalArgumentException if called before the response has been executed.
     */
    open fun response(): Response

    /**
     * Set the connection's response
     * @param response new response
     * @return this Connection, for chaining
     */
    open fun response(response: Response?): Connection

    /**
     * Common methods for Requests and Responses
     * @param <T> Type of Base, either Request or Response
    </T> */
    open interface Base<T : Connection.Base<T>?> {
        /**
         * Get the URL of this Request or Response. For redirected responses, this will be the final destination URL.
         * @return URL
         * @throws IllegalArgumentException if called on a Request that was created without a URL.
         */
        open fun url(): URL?

        /**
         * Set the URL
         * @param url new URL
         * @return this, for chaining
         */
        open fun url(url: URL?): T

        /**
         * Get the request method, which defaults to `GET`
         * @return method
         */
        open fun method(): Connection.Method

        /**
         * Set the request method
         * @param method new method
         * @return this, for chaining
         */
        open fun method(method: Connection.Method): T

        /**
         * Get the value of a header. If there is more than one header value with the same name, the headers are returned
         * comma seperated, per [rfc2616-sec4](https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2).
         *
         *
         * Header names are case insensitive.
         *
         * @param name name of header (case insensitive)
         * @return value of header, or null if not set.
         * @see .hasHeader
         * @see .cookie
         */

        open fun header(name: String): String?

        /**
         * Get the values of a header.
         * @param name header name, case insensitive.
         * @return a list of values for this header, or an empty list if not set.
         */
        open fun headers(name: String): List<String>

        /**
         * Set a header. This method will overwrite any existing header with the same case insensitive name. (If there
         * is more than one value for this header, this method will update the first matching header.
         * @param name Name of header
         * @param value Value of header
         * @return this, for chaining
         * @see .addHeader
         */
        open fun header(name: String, value: String?): T

        /**
         * Add a header. The header will be added regardless of whether a header with the same name already exists.
         * @param name Name of new header
         * @param value Value of new header
         * @return this, for chaining
         */
        open fun addHeader(name: String, value: String?): T

        /**
         * Check if a header is present
         * @param name name of header (case insensitive)
         * @return if the header is present in this request/response
         */
        open fun hasHeader(name: String): Boolean

        /**
         * Check if a header is present, with the given value
         * @param name header name (case insensitive)
         * @param value value (case insensitive)
         * @return if the header and value pair are set in this req/res
         */
        open fun hasHeaderWithValue(name: String, value: String): Boolean

        /**
         * Remove headers by name. If there is more than one header with this name, they will all be removed.
         * @param name name of header to remove (case insensitive)
         * @return this, for chaining
         */
        open fun removeHeader(name: String?): T

        /**
         * Retrieve all of the request/response header names and corresponding values as a map. For headers with multiple
         * values, only the first header is returned.
         *
         * Note that this is a view of the headers only, and changes made to this map will not be reflected in the
         * request/response object.
         * @return headers
         * @see .multiHeaders
         */
        open fun headers(): Map<String, String>

        /**
         * Retreive all of the headers, keyed by the header name, and with a list of values per header.
         * @return a list of multiple values per header.
         */
        open fun multiHeaders(): Map<String, List<String>>

        /**
         * Get a cookie value by name from this request/response.
         *
         *
         * Response objects have a simplified cookie model. Each cookie set in the response is added to the response
         * object's cookie key=value map. The cookie's path, domain, and expiry date are ignored.
         *
         * @param name name of cookie to retrieve.
         * @return value of cookie, or null if not set
         */

        open fun cookie(name: String): String?

        /**
         * Set a cookie in this request/response.
         * @param name name of cookie
         * @param value value of cookie
         * @return this, for chaining
         */
        open fun cookie(name: String, value: String): T

        /**
         * Check if a cookie is present
         * @param name name of cookie
         * @return if the cookie is present in this request/response
         */
        open fun hasCookie(name: String): Boolean

        /**
         * Remove a cookie by name
         * @param name name of cookie to remove
         * @return this, for chaining
         */
        open fun removeCookie(name: String): T

        /**
         * Retrieve all of the request/response cookies as a map
         * @return cookies
         */
        open fun cookies(): Map<String, String>
    }

    /**
     * Represents a HTTP request.
     */
    open interface Request : Connection.Base<Request?> {
        /**
         * Set the HTTP proxy to use for this request.
         * @param host the proxy hostname
         * @param port the proxy port
         * @return this Connection, for chaining
         */
        open fun proxy(host: String?, port: Int): Request

        /**
         * Get the request timeout, in milliseconds.
         * @return the timeout in milliseconds.
         */
        open fun timeout(): Int

        /**
         * Update the request timeout.
         * @param millis timeout, in milliseconds
         * @return this Request, for chaining
         */
        open fun timeout(millis: Int): Request

        /**
         * Get the maximum body size, in bytes.
         * @return the maximum body size, in bytes.
         */
        open fun maxBodySize(): Int

        /**
         * Update the maximum body size, in bytes.
         * @param bytes maximum body size, in bytes.
         * @return this Request, for chaining
         */
        open fun maxBodySize(bytes: Int): Request

        /**
         * Get the current followRedirects configuration.
         * @return true if followRedirects is enabled.
         */
        open fun followRedirects(): Boolean

        /**
         * Configures the request to (not) follow server redirects. By default this is **true**.
         * @param followRedirects true if server redirects should be followed.
         * @return this Request, for chaining
         */
        open fun followRedirects(followRedirects: Boolean): Request

        /**
         * Get the current ignoreHttpErrors configuration.
         * @return true if errors will be ignored; false (default) if HTTP errors will cause an IOException to be
         * thrown.
         */
        open fun ignoreHttpErrors(): Boolean

        /**
         * Configures the request to ignore HTTP errors in the response.
         * @param ignoreHttpErrors set to true to ignore HTTP errors.
         * @return this Request, for chaining
         */
        open fun ignoreHttpErrors(ignoreHttpErrors: Boolean): Request

        /**
         * Get the current ignoreContentType configuration.
         * @return true if invalid content-types will be ignored; false (default) if they will cause an IOException to
         * be thrown.
         */
        open fun ignoreContentType(): Boolean

        /**
         * Configures the request to ignore the Content-Type of the response.
         * @param ignoreContentType set to true to ignore the content type.
         * @return this Request, for chaining
         */
        open fun ignoreContentType(ignoreContentType: Boolean): Request

        /**
         * Add a data parameter to the request
         * @param keyval data to add.
         * @return this Request, for chaining
         */
        open fun data(keyval: Connection.KeyVal?): Request

        /**
         * Get all of the request's data parameters
         * @return collection of keyvals
         */
        open fun data(): MutableCollection<Connection.KeyVal?>

        /**
         * Set a POST (or PUT) request body. Useful when a server expects a plain request body, not a set for URL
         * encoded form key/value pairs. E.g.:
         * `<pre>Jsoup.connect(url)
         * .requestBody(json)
         * .header("Content-Type", "application/json")
         * .post();</pre>`
         * If any data key/vals are supplied, they will be sent as URL query params.
         * @param body to use as the request body. Set to null to clear a previously set body.
         * @return this Request, for chaining
         */
        open fun requestBody( body: String?): Request

        /**
         * Get the current request body.
         * @return null if not set.
         */

        open fun requestBody(): String?

        /**
         * Specify the parser to use when parsing the document.
         * @param parser parser to use.
         * @return this Request, for chaining
         */
        open fun parser(parser: Parser?): Request

        /**
         * Get the current parser to use when parsing the document.
         * @return current Parser
         */
        open fun parser(): Parser?

        /**
         * Sets the post data character set for x-www-form-urlencoded post data
         * @param charset character set to encode post data
         * @return this Request, for chaining
         */
        open fun postDataCharset(charset: String?): Request

        /**
         * Gets the post data character set for x-www-form-urlencoded post data
         * @return character set to encode post data
         */
        open fun postDataCharset(): String?
    }

    /**
     * Represents a HTTP response.
     */
    open interface Response : Connection.Base<Response?> {
        /**
         * Get the status code of the response.
         * @return status code
         */
        open fun statusCode(): Int

        /**
         * Get the status message of the response.
         * @return status message
         */
        open fun statusMessage(): String

        /**
         * Get the character set name of the response, derived from the content-type header.
         * @return character set name if set, **null** if not
         */

        open fun charset(): String?

        /**
         * Set / override the response character set. When the document body is parsed it will be with this charset.
         * @param charset to decode body as
         * @return this Response, for chaining
         */
        open fun charset(charset: String?): Response

        /**
         * Get the response content type (e.g. "text/html");
         * @return the response content type, or **null** if one was not set
         */

        open fun contentType(): String?

        /**
         * Read and parse the body of the response as a Document. If you intend to parse the same response multiple
         * times, you should [.bufferUp] first.
         * @return a parsed Document
         * @throws IOException on error
         */
        @Throws(IOException::class)
        open fun parse(): Document?

        /**
         * Get the body of the response as a plain string.
         * @return body
         */
        open fun body(): String

        /**
         * Get the body of the response as an array of bytes.
         * @return body bytes
         */
        open fun bodyAsBytes(): ByteArray

        /**
         * Read the body of the response into a local buffer, so that [.parse] may be called repeatedly on the
         * same connection response (otherwise, once the response is read, its InputStream will have been drained and
         * may not be re-read). Calling [.body] or [.bodyAsBytes] has the same effect.
         * @return this response, for chaining
         * @throws UncheckedIOException if an IO exception occurs during buffering.
         */
        open fun bufferUp(): Response

        /**
         * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
         * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
         *
         * This method is useful for writing large responses to disk, without buffering them completely into memory first.
         * @return the response body input stream
         */
        open fun bodyStream(): SyncInputStream
    }

    /**
     * A Key:Value tuple(+), used for form data.
     */
    open interface KeyVal {
        /**
         * Update the key of a keyval
         * @param key new key
         * @return this KeyVal, for chaining
         */
        open fun key(key: String?): Connection.KeyVal

        /**
         * Get the key of a keyval
         * @return the key
         */
        open fun key(): String?

        /**
         * Update the value of a keyval
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        open fun value(value: String?): Connection.KeyVal

        /**
         * Get the value of a keyval
         * @return the value
         */
        open fun value(): String?

        /**
         * Add or update an input stream to this keyVal
         * @param inputStream new input stream
         * @return this KeyVal, for chaining
         */
        open fun inputStream(inputStream: SyncInputStream?): Connection.KeyVal

        /**
         * Get the input stream associated with this keyval, if any
         * @return input stream if set, or null
         */

        open fun inputStream(): SyncInputStream?

        /**
         * Does this keyval have an input stream?
         * @return true if this keyval does indeed have an input stream
         */
        open fun hasInputStream(): Boolean

        /**
         * Set the Content Type header used in the MIME body (aka mimetype) when uploading files.
         * Only useful if [.inputStream] is set.
         *
         * Will default to `application/octet-stream`.
         * @param contentType the new content type
         * @return this KeyVal
         */
        open fun contentType(contentType: String?): Connection.KeyVal

        /**
         * Get the current Content Type, or `null` if not set.
         * @return the current Content Type.
         */

        open fun contentType(): String?
    }
}
