package tk.zwander.common.util

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.Window
import org.w3c.dom.events.EventTarget
import org.w3c.files.File

inline val Window.indexedDB: IDBFactory
    get() = asDynamic().indexedDB

open external class IDBFactory {
    open fun open(name: String, version: Number = definedExternally): IDBOpenDBRequest
    open fun deleteDatabase(name: String): IDBOpenDBRequest
    open fun cmp(first: dynamic, second: dynamic): Int
    open fun databases(): List<Map<String, String>>
}

open external class IDBOpenDBRequest : IDBRequest

open external class IDBRequest : EventTarget {
    open val error: Exception
    open val result: Any?
    open val source: dynamic
    open val readyState: String
    open val transaction: IDBTransaction
}

open external class IDBTransaction {
    open val db: IDBDatabase
    open val durability: String
    open val error: Exception
    open val mode: String
    open val objectStoreNames: DOMStringList

    open fun abort()
    open fun objectStore(name: String): IDBObjectStore
    open fun commit()
}

open external class IDBDatabase : EventTarget {
    open val name: String
    open val version: Int
    open val objectStoreNames: DOMStringList

    open fun close()
    open fun createMutableFile(name: String): IDBMutableFile
    open fun createObjectStore(name: String, options: Map<String, dynamic> = definedExternally): IDBObjectStore
    open fun deleteObjectStore(name: String)
    open fun transaction(name: String, mode: String = definedExternally, options: Map<String, dynamic> = definedExternally): IDBTransaction
}

open external class IDBMutableFile {
    open val name: String
    open val type: String

    open fun open(): LockedFile
    open fun getFile(): FileRequest<File>
}

open external class LockedFile {
    open val fileHandle: IDBMutableFile
    open val mode: String
    open val active: Boolean
    open val location: Number?

    open var oncomplete: (() -> Unit)?
    open var onabort: (() -> Unit)?
    open var onerror: ((Exception) -> Unit)?

    open fun getMetadata(): FileRequest<Map<String, Any>>
    open fun readAsArrayBuffer(size: Number): FileRequest<ArrayBuffer>
    open fun readAsText(size: Number, encoding: String = definedExternally): FileRequest<String>
    //String or ArrayBuffer
    open fun write(data: dynamic): FileRequest<dynamic>
    open fun append(data: dynamic): FileRequest<dynamic>
    open fun truncate(start: Number = definedExternally): FileRequest<dynamic>
    open fun flush(): FileRequest<dynamic>
    open fun abort(): FileRequest<dynamic>
}

open external class IDBObjectStore {
    open val indexNames: DOMStringList
    open val keyPath: Any?
    open val name: String
    open val transaction: IDBTransaction
    open val autoIncrement: Boolean

    open fun add(value: Any, key: Any? = definedExternally): IDBRequest
    open fun clear(): IDBRequest
    open fun count(query: IDBKeyRange? = definedExternally): IDBRequest
    open fun createIndex(indexName: String, keyPath: Any, objectParameters: Map<String, Any>? = definedExternally): IDBIndex
    //key or IDBKeyRange
    open fun delete(key: dynamic): IDBRequest
    open fun deleteIndex(indexName: String)
    open fun get(key: dynamic): IDBRequest
    open fun getKey(key: dynamic): IDBRequest
    open fun getAll(query: Any? = definedExternally, count: Number = definedExternally): IDBRequest
    open fun getAllKeys(query: Any? = definedExternally, count: Number = definedExternally): IDBRequest
    open fun index(name: String): IDBIndex
    open fun openCursor(query: Any? = definedExternally, direction: String? = definedExternally): IDBRequest
    open fun openKeyCursor(query: Any? = definedExternally, direction: String? = definedExternally): IDBRequest
    open fun put(item: Any, key: Any = definedExternally): IDBRequest
}

open external class IDBIndex : EventTarget {
    open val isAutoLocale: Boolean
    open val locale: String
    open var name: String
    open val objectStore: String
    open val keyPath: dynamic
    open val multiEntry: Boolean
    open val unique: Boolean

    open fun count(key: dynamic = definedExternally): IDBRequest
    open fun get(key: dynamic = definedExternally): IDBRequest
    open fun getKey(key: dynamic = definedExternally): IDBRequest
    open fun getAll(query: dynamic = definedExternally, count: Number = definedExternally): IDBRequest
    open fun getAllKeys(query: dynamic = definedExternally, count: Number = definedExternally): IDBRequest
    open fun openCursor(range: dynamic = definedExternally, direction: String? = definedExternally): IDBRequest
    open fun openKeyCursor(range: dynamic = definedExternally, direction: String? = definedExternally): IDBRequest
}

external val idbKeyRange: IDBKeyRange

open external class IDBKeyRange {
    open val lower: dynamic
    open val upper: dynamic
    open val lowerOpen: Boolean
    open val upperOpen: Boolean

    fun bound(lower: dynamic, upper: dynamic, lowerOpen: dynamic = definedExternally, upperOpen: dynamic = definedExternally): IDBKeyRange
    fun only(value: dynamic): IDBKeyRange
    fun lowerBound(value: dynamic, open: Boolean = definedExternally): IDBKeyRange
    fun upperBound(value: dynamic, open: Boolean = definedExternally): IDBKeyRange

    open fun includes(key: dynamic): Boolean
}

open external class FileRequest<out T> : DOMRequest<T> {
    open val lockedFile: LockedFile
    open var onprogress: ((Map<String, Number>) -> Unit)?
}

open external class DOMRequest<out T> : EventTarget {
    open val error: Exception
    open var onerror: Function<dynamic>
    open var onsuccess: Function<dynamic>
    open val readyState: String
    open val result: dynamic

    open fun <S> then(onFulfilled: ((T) -> S)?, onRejected: ((Throwable) -> S)?): DOMRequest<S>
    open fun <S> then(onFulfilled: ((T) -> S)?): DOMRequest<S>
}

open external class DOMStringList {
    open val length: Int
    open fun item(index: Int): String

    open fun contains(item: String): Boolean
}
