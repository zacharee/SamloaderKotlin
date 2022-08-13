package org.jsoup.helper

/**
 * Implementation of ArrayList that watches out for changes to the contents.
 */
abstract class ChangeNotifyingArrayList<E> constructor(initialCapacity: Int) : MutableList<E> {
    private val wrapped = ArrayList<E>(initialCapacity)

    abstract fun onContentsChanged()

    override val size: Int
        get() = wrapped.size

    override fun contains(element: E): Boolean {
        return wrapped.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return wrapped.containsAll(elements)
    }

    override fun get(index: Int): E {
        return wrapped[index]
    }

    override fun indexOf(element: E): Int {
        return wrapped.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return wrapped.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return wrapped.iterator()
    }

    override fun lastIndexOf(element: E): Int {
        return wrapped.lastIndexOf(element)
    }

    override fun listIterator(): MutableListIterator<E> {
        return wrapped.listIterator()
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        return wrapped.subList(fromIndex, toIndex)
    }

    override fun hashCode(): Int {
        return wrapped.hashCode()
    }

    override fun toString(): String {
        return wrapped.toString()
    }

    override fun equals(other: Any?): Boolean {
        return wrapped == other
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        return wrapped.listIterator(index)
    }

    override fun set(index: Int, element: E): E {
        onContentsChanged()
        return wrapped.set(index, element)
    }

    override fun add(element: E): Boolean {
        onContentsChanged()
        return wrapped.add(element)
    }

    override fun add(index: Int, element: E) {
        onContentsChanged()
        wrapped.add(index, element)
    }

    override fun removeAt(index: Int): E {
        onContentsChanged()
        return wrapped.removeAt(index)
    }

    override fun remove(element: E): Boolean {
        onContentsChanged()
        return wrapped.remove(element)
    }

    override fun clear() {
        onContentsChanged()
        wrapped.clear()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        onContentsChanged()
        return wrapped.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        onContentsChanged()
        return wrapped.addAll(index, elements)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        onContentsChanged()
        return wrapped.removeAll(elements.toSet())
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        onContentsChanged()
        return wrapped.retainAll(elements.toSet())
    }
}
