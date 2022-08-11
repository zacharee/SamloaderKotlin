package org.jsoup.helper

/**
 * A functional interface (ala Java's [java.util.function.Consumer] interface, implemented here for cross compatibility with Android.
 * @param <T> the input type
</T> */
open fun interface Consumer<T> {
    /**
     * Execute this operation on the supplied argument. It is expected to have side effects.
     *
     * @param t the input argument
     */
    fun accept(t: T)
}
