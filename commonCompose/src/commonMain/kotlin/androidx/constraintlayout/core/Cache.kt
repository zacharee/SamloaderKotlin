/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * Cache for common objects
 */
class Cache {
    var mOptimizedArrayRowPool: Pools.Pool<ArrayRow> = Pools.SimplePool<ArrayRow>(256)
    var mArrayRowPool: Pools.Pool<ArrayRow> = Pools.SimplePool<ArrayRow>(256)
    var mSolverVariablePool: Pools.Pool<SolverVariable> = Pools.SimplePool(256)
    var mIndexedVariables = arrayOfNulls<SolverVariable>(32)
}
