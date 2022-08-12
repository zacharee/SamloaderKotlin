package org.jsoup.helper

import io.ktor.utils.io.errors.*
import org.jsoup.Connection
import org.jsoup.internal.StringUtil

/**
 * Helper functions to support the Cookie Manager / Cookie Storage in HttpConnection.
 *
 * @since 1.14.1
 */
//internal object CookieUtil {
//    // cookie manager get() wants request headers but doesn't use them, so we just pass a dummy object here
//    private val EmptyRequestHeaders: Map<String, List<String>> = mapOf()
//    private val Sep: String = "; "
//    private val CookieName: String = "Cookie"
//    private val Cookie2Name: String = "Cookie2"
//
//    /**
//     * Pre-request, get any applicable headers out of the Request cookies and the Cookie Store, and add them to the request
//     * headers. If the Cookie Store duplicates any Request cookies (same name and value), they will be discarded.
//     */
//    @Throws(IOException::class)
//    fun applyCookiesToRequest(req: HttpConnection.Request, con: HttpURLConnection) {
//        // Request key/val cookies. LinkedHashSet used to preserve order, as cookie store will return most specific path first
//        val cookieSet: MutableSet<String> = requestCookieSet(req)
//        var cookies2: Set<String>? = null
//
//        // stored:
//        val storedCookies: Map<String, List<String>?> = req.cookieManager().get(asUri(req.url), EmptyRequestHeaders)
//        for (entry: Map.Entry<String, List<String>?> in storedCookies.entries) {
//            // might be Cookie: name=value; name=value\nCookie2: name=value; name=value
//            val cookies: List<String>? = entry.value // these will be name=val
//            if (cookies == null || cookies.size == 0) // the cookie store often returns just an empty "Cookie" key, no val
//                continue
//            val key: String = entry.key // Cookie or Cookie2
//            var set: MutableSet<String>
//            if ((CookieName == key)) set = cookieSet else if ((Cookie2Name == key)) {
//                set = HashSet()
//                cookies2 = set
//            } else {
//                continue  // unexpected header key
//            }
//            set.addAll(cookies)
//        }
//        if (cookieSet.size > 0) con.addRequestProperty(CookieName, StringUtil.join(cookieSet, Sep))
//        if (cookies2 != null && cookies2.size > 0) con.addRequestProperty(Cookie2Name, StringUtil.join(cookies2, Sep))
//    }
//
//    private fun requestCookieSet(req: Connection.Request): LinkedHashSet<String> {
//        val set: LinkedHashSet<String> = LinkedHashSet()
//        // req cookies are the wildcard key/val cookies (no domain, path, etc)
//        for (cookie: Map.Entry<String, String> in req.cookies().entries) {
//            set.add(cookie.key + "=" + cookie.value)
//        }
//        return set
//    }
//
//    @Throws(IOException::class)
//    fun asUri(url: URL?): URI {
//        try {
//            return url!!.toURI()
//        } catch (e: URISyntaxException) {  // this would be a WTF because we construct the URL
//            val ue: MalformedURLException = MalformedURLException(e.message)
//            ue.initCause(e)
//            throw ue
//        }
//    }
//
//    @Throws(IOException::class)
//    fun storeCookies(req: HttpConnection.Request, url: URL?, resHeaders: Map<String, MutableList<String>>?) {
//        req.cookieManager().put(asUri(url), resHeaders) // stores cookies for session
//    }
//}
