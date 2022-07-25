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
package androidx.constraintlayout.core.widgets

/**
 * Simple rect class
 */
class Rectangle {
    var x = 0
    var y = 0
    var width = 0
    var height = 0

    /**
     * @TODO: add description
     */
    fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun grow(w: Int, h: Int) {
        x -= w
        y -= h
        width += 2 * w
        height += 2 * h
    }

    fun intersects(bounds: Rectangle): Boolean {
        return x >= bounds.x && x < bounds.x + bounds.width && y >= bounds.y && y < bounds.y + bounds.height
    }

    /**
     * @TODO: add description
     */
    fun contains(x: Int, y: Int): Boolean {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height
    }

    val centerX: Int
        get() = (x + width) / 2
    val centerY: Int
        get() = (y + height) / 2
}
