package org.jsoup.helper

/**
 * Simple validation methods. Designed for jsoup internal use.
 */
object Validate {
    /**
     * Validates that the object is not null
     * @param obj object to test
     * @throws IllegalArgumentException if the object is null
     */
    fun notNull( obj: Any?) {
        if (obj == null) throw IllegalArgumentException("Object must not be null")
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     * @param msg message to include in the Exception if validation fails
     * @throws IllegalArgumentException if the object is null
     */
    fun notNull( obj: Any?, msg: String?) {
        if (obj == null) throw IllegalArgumentException(msg)
    }

    /**
     * Verifies the input object is not null, and returns that object. Effectively this casts a nullable object to a non-
     * null object. (Works around lack of Objects.requestNonNull in Android version.)
     * @param obj nullable object to case to not-null
     * @return the object, or throws an exception if it is null
     * @throws IllegalArgumentException if the object is null
     */
    fun ensureNotNull( obj: Any?): Any {
        if (obj == null) throw IllegalArgumentException("Object must not be null") else return obj
    }

    /**
     * Validates that the value is true
     * @param val object to test
     * @throws IllegalArgumentException if the object is not true
     */
    fun isTrue(`val`: Boolean) {
        if (!`val`) throw IllegalArgumentException("Must be true")
    }

    /**
     * Validates that the value is true
     * @param val object to test
     * @param msg message to include in the Exception if validation fails
     * @throws IllegalArgumentException if the object is not true
     */
    fun isTrue(`val`: Boolean, msg: String?) {
        if (!`val`) throw IllegalArgumentException(msg)
    }

    /**
     * Validates that the value is false
     * @param val object to test
     * @throws IllegalArgumentException if the object is not false
     */
    fun isFalse(`val`: Boolean) {
        if (`val`) throw IllegalArgumentException("Must be false")
    }

    /**
     * Validates that the value is false
     * @param val object to test
     * @param msg message to include in the Exception if validation fails
     * @throws IllegalArgumentException if the object is not false
     */
    fun isFalse(`val`: Boolean, msg: String?) {
        if (`val`) throw IllegalArgumentException(msg)
    }
    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @param msg message to include in the Exception if validation fails
     * @throws IllegalArgumentException if the array contains a null element
     */
    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @throws IllegalArgumentException if the array contains a null element
     */
    fun noNullElements(objects: Array<out Any?>, msg: String? = "Array must not contain any null objects") {
        for (obj: Any? in objects) if (obj == null) throw IllegalArgumentException(msg)
    }

    /**
     * Validates that the string is not null and is not empty
     * @param string the string to test
     * @throws IllegalArgumentException if the string is null or empty
     */
    fun notEmpty( string: String?) {
        if (string == null || string.length == 0) throw IllegalArgumentException("String must not be empty")
    }

    /**
     * Validates that the string is not null and is not empty
     * @param string the string to test
     * @param msg message to include in the Exception if validation fails
     * @throws IllegalArgumentException if the string is null or empty
     */
    fun notEmpty( string: String?, msg: String?) {
        if (string == null || string.length == 0) throw IllegalArgumentException(msg)
    }

    /**
     * Blow up if we reach an unexpected state.
     * @param msg message to think about
     * @throws IllegalStateException if we reach this state
     */
    fun wtf(msg: String?) {
        throw IllegalStateException(msg)
    }

    /**
     * Cause a failure.
     * @param msg message to output.
     * @throws IllegalStateException if we reach this state
     */
    fun fail(msg: String?) {
        throw IllegalArgumentException(msg)
    }
}
