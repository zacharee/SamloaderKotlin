package org.jsoup.helper

/**
 * Implementation of ArrayList that watches out for changes to the contents.
 */
abstract class ChangeNotifyingArrayList<E> constructor(initialCapacity: Int) : ArrayList<E>(initialCapacity) {
    abstract fun onContentsChanged()
    public override fun set(index: Int, element: E): E {
        onContentsChanged()
        return super.set(index, element)
    }

    public override fun add(e: E): Boolean {
        onContentsChanged()
        return super.add(e)
    }

    public override fun add(index: Int, element: E) {
        onContentsChanged()
        super.add(index, element)
    }

    public override fun removeAt(index: Int): E {
        onContentsChanged()
        return super.removeAt(index)
    }

    public override fun remove(element: E): Boolean {
        onContentsChanged()
        return super.remove(element)
    }

    public override fun clear() {
        onContentsChanged()
        super.clear()
    }

    public override fun addAll(elements: Collection<E>): Boolean {
        onContentsChanged()
        return super.addAll(elements)
    }

    public override fun addAll(index: Int, elements: Collection<E>): Boolean {
        onContentsChanged()
        return super.addAll(index, elements)
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        onContentsChanged()
        super.removeRange(fromIndex, toIndex)
    }

    public override fun removeAll(elements: Collection<E>): Boolean {
        onContentsChanged()
        return super.removeAll(elements.toSet())
    }

    public override fun retainAll(c: Collection<E>): Boolean {
        onContentsChanged()
        return super.retainAll(c.toSet())
    }
}
