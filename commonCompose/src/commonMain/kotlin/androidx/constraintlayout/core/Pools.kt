/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.core

/**
 * Helper class for crating pools of objects. An example use looks like this:
 * <pre>
 * public class MyPooledClass {
 *
 * private static final SimplePool<MyPooledClass> sPool =
 * new SimplePool<MyPooledClass>(10);
 *
 * public static MyPooledClass obtain() {
 * MyPooledClass instance = sPool.acquire();
 * return (instance != null) ? instance : new MyPooledClass();
 * }
 *
 * public void recycle() {
 * // Clear state if needed.
 * sPool.release(this);
 * }
 *
 * . . .
 * }
</MyPooledClass></MyPooledClass></pre> *
 */
object Pools {
    private const val DEBUG = false

    /**
     * Interface for managing a pool of objects.
     *
     * @param <T> The pooled type.
    </T> */
    interface Pool<T> {
        /**
         * @return An instance from the pool if such, null otherwise.
         */
        fun acquire(): T?

        /**
         * Release an instance to the pool.
         *
         * @param instance The instance to release.
         * @return Whether the instance was put in the pool.
         * @throws IllegalStateException If the instance is already in the pool.
         */
        fun release(instance: T): Boolean

        /**
         * Try releasing all instances at the same time
         *
         * @param variables the variables to release
         * @param count     the number of variables to release
         */
        fun releaseAll(variables: Array<T?>?, count: Int)
    }

    /**
     * Simple (non-synchronized) pool of objects.
     *
     * @param <T> The pooled type.
    </T> */
    internal class SimplePool<T>(maxPoolSize: Int) : Pool<T> {
        private val mPool: Array<Any?>
        private var mPoolSize = 0

        /**
         * Creates a new instance.
         *
         * @param maxPoolSize The max pool size.
         * @throws IllegalArgumentException If the max pool size is less than zero.
         */
        init {
            if (maxPoolSize <= 0) {
                throw IllegalArgumentException("The max pool size must be > 0")
            }
            mPool = arrayOfNulls(maxPoolSize)
        }

        override fun acquire(): T? {
            if (mPoolSize > 0) {
                val lastPooledIndex = mPoolSize - 1
                val instance = mPool[lastPooledIndex] as T?
                mPool[lastPooledIndex] = null
                mPoolSize--
                return instance
            }
            return null
        }

        override fun release(instance: T): Boolean {
            if (DEBUG) {
                if (isInPool(instance)) {
                    throw IllegalStateException("Already in the pool!")
                }
            }
            if (mPoolSize < mPool.size) {
                mPool[mPoolSize] = instance
                mPoolSize++
                return true
            }
            return false
        }

        override fun releaseAll(variables: Array<T?>?, count: Int) {
            if (variables == null) return

            var count = count
            if (count > variables.size) {
                count = variables.size
            }
            for (i in 0 until count) {
                val instance = variables[i]
                if (DEBUG) {
                    if (isInPool(instance)) {
                        throw IllegalStateException("Already in the pool!")
                    }
                }
                if (mPoolSize < mPool.size) {
                    mPool[mPoolSize] = instance
                    mPoolSize++
                }
            }
        }

        private fun isInPool(instance: T?): Boolean {
            for (i in 0 until mPoolSize) {
                if (mPool[i] === instance) {
                    return true
                }
            }
            return false
        }
    }
}
