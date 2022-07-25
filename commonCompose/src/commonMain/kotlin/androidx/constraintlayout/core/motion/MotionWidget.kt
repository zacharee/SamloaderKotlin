/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.constraintlayout.core.motion

import androidx.constraintlayout.core.motion.utils.TypedValues
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.motion.utils.TypedValues.MotionType
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.ConstraintWidget

class MotionWidget : TypedValues {
    var widgetFrame: WidgetFrame? = WidgetFrame()
    var mMotion = Motion()
    var mPropertySet = PropertySet()
    private var mProgress = 0f
    var mTransitionPathRotate = 0f

    /**
     * @DoNotShow
     */
    class Motion {
        var mAnimateRelativeTo: String? = null
        var mAnimateCircleAngleTo = 0
        var mTransitionEasing: String? = null
        var mPathMotionArc = UNSET
        var mDrawPath = 0
        var mMotionStagger = Float.NaN
        var mPolarRelativeTo = UNSET
        var mPathRotate = Float.NaN
        var mQuantizeMotionPhase = Float.NaN
        var mQuantizeMotionSteps = UNSET
        var mQuantizeInterpolatorString: String? = null
        var mQuantizeInterpolatorType = INTERPOLATOR_UNDEFINED // undefined
        var mQuantizeInterpolatorID = -1

        companion object {
            private const val INTERPOLATOR_REFERENCE_ID = -2
            private const val SPLINE_STRING = -1
            private const val INTERPOLATOR_UNDEFINED = -3
        }
    }

    class PropertySet {
        var visibility = VISIBLE
        var mVisibilityMode = VISIBILITY_MODE_NORMAL
        var alpha = 1f
        var mProgress = Float.NaN
    }

    constructor() {}

    val parent: MotionWidget?
        get() = null

    /**
     * @TODO: add description
     */
    fun findViewById(mTransformPivotTarget: Int): MotionWidget? {
        return null
    }

    val name: String?
        get() = widgetFrame?.id

    /**
     * @TODO: add description
     */
    fun layout(l: Int, t: Int, r: Int, b: Int) {
        setBounds(l, t, r, b)
    }

    /**
     * @TODO: add description
     */
    override fun toString(): String {
        return (widgetFrame!!.left.toString() + ", " + widgetFrame!!.top + ", "
                + widgetFrame!!.right + ", " + widgetFrame!!.bottom)
    }

    /**
     * @TODO: add description
     */
    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (widgetFrame == null) {
            widgetFrame = WidgetFrame(null as ConstraintWidget?)
        }
        widgetFrame!!.top = top
        widgetFrame!!.left = left
        widgetFrame!!.right = right
        widgetFrame!!.bottom = bottom
    }

    constructor(f: WidgetFrame?) {
        widgetFrame = f
    }

    /**
     * This populates the motion attributes from widgetFrame to the MotionWidget
     */
    fun updateMotion(toUpdate: TypedValues) {
        if (widgetFrame?.motionProperties != null) {
            widgetFrame!!.motionProperties?.applyDelta(toUpdate)
        }
    }

    override fun setValue(id: Int, value: Int): Boolean {
        val set = setValueAttributes(id, value.toFloat())
        return if (set) {
            true
        } else setValueMotion(id, value)
    }

    override fun setValue(id: Int, value: Float): Boolean {
        val set = setValueAttributes(id, value)
        return if (set) {
            true
        } else setValueMotion(id, value)
    }

    override fun setValue(id: Int, value: String): Boolean {
        if (id == MotionType.Companion.TYPE_ANIMATE_RELATIVE_TO) {
            mMotion.mAnimateRelativeTo = value
            return true
        }
        return setValueMotion(id, value)
    }

    override fun setValue(id: Int, value: Boolean): Boolean {
        return false
    }

    /**
     * @TODO: add description
     */
    fun setValueMotion(id: Int, value: Int): Boolean {
        when (id) {
            MotionType.Companion.TYPE_ANIMATE_CIRCLEANGLE_TO -> mMotion.mAnimateCircleAngleTo = value
            MotionType.Companion.TYPE_PATHMOTION_ARC -> mMotion.mPathMotionArc = value
            MotionType.Companion.TYPE_DRAW_PATH -> mMotion.mDrawPath = value
            MotionType.Companion.TYPE_POLAR_RELATIVETO -> mMotion.mPolarRelativeTo = value
            MotionType.Companion.TYPE_QUANTIZE_MOTIONSTEPS -> mMotion.mQuantizeMotionSteps = value
            MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR_TYPE -> mMotion.mQuantizeInterpolatorType = value
            MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR_ID -> mMotion.mQuantizeInterpolatorID = value
            else -> return false
        }
        return true
    }

    /**
     * @TODO: add description
     */
    fun setValueMotion(id: Int, value: String?): Boolean {
        when (id) {
            MotionType.Companion.TYPE_EASING -> mMotion.mTransitionEasing = value
            MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR -> mMotion.mQuantizeInterpolatorString = value
            else -> return false
        }
        return true
    }

    /**
     * @TODO: add description
     */
    fun setValueMotion(id: Int, value: Float): Boolean {
        when (id) {
            MotionType.Companion.TYPE_STAGGER -> mMotion.mMotionStagger = value
            MotionType.Companion.TYPE_PATH_ROTATE -> mMotion.mPathRotate = value
            MotionType.Companion.TYPE_QUANTIZE_MOTION_PHASE -> mMotion.mQuantizeMotionPhase = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun setValueAttributes(id: Int, value: Float): Boolean {
        when (id) {
            AttributesType.Companion.TYPE_ALPHA -> widgetFrame!!.alpha = value
            AttributesType.Companion.TYPE_TRANSLATION_X -> widgetFrame!!.translationX = value
            AttributesType.Companion.TYPE_TRANSLATION_Y -> widgetFrame!!.translationY = value
            AttributesType.Companion.TYPE_TRANSLATION_Z -> widgetFrame!!.translationZ = value
            AttributesType.Companion.TYPE_ROTATION_X -> widgetFrame!!.rotationX = value
            AttributesType.Companion.TYPE_ROTATION_Y -> widgetFrame!!.rotationY = value
            AttributesType.Companion.TYPE_ROTATION_Z -> widgetFrame!!.rotationZ = value
            AttributesType.Companion.TYPE_SCALE_X -> widgetFrame!!.scaleX = value
            AttributesType.Companion.TYPE_SCALE_Y -> widgetFrame!!.scaleY = value
            AttributesType.Companion.TYPE_PIVOT_X -> widgetFrame!!.pivotX = value
            AttributesType.Companion.TYPE_PIVOT_Y -> widgetFrame!!.pivotY = value
            AttributesType.Companion.TYPE_PROGRESS -> mProgress = value
            AttributesType.Companion.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun getValueAttributes(id: Int): Float {
        return when (id) {
            AttributesType.Companion.TYPE_ALPHA -> widgetFrame!!.alpha
            AttributesType.Companion.TYPE_TRANSLATION_X -> widgetFrame!!.translationX
            AttributesType.Companion.TYPE_TRANSLATION_Y -> widgetFrame!!.translationY
            AttributesType.Companion.TYPE_TRANSLATION_Z -> widgetFrame!!.translationZ
            AttributesType.Companion.TYPE_ROTATION_X -> widgetFrame!!.rotationX
            AttributesType.Companion.TYPE_ROTATION_Y -> widgetFrame!!.rotationY
            AttributesType.Companion.TYPE_ROTATION_Z -> widgetFrame!!.rotationZ
            AttributesType.Companion.TYPE_SCALE_X -> widgetFrame!!.scaleX
            AttributesType.Companion.TYPE_SCALE_Y -> widgetFrame!!.scaleY
            AttributesType.Companion.TYPE_PIVOT_X -> widgetFrame!!.pivotX
            AttributesType.Companion.TYPE_PIVOT_Y -> widgetFrame!!.pivotY
            AttributesType.Companion.TYPE_PROGRESS -> mProgress
            AttributesType.Companion.TYPE_PATH_ROTATE -> mTransitionPathRotate
            else -> Float.NaN
        }
    }

    override fun getId(name: String?): Int {
        val ret: Int = AttributesType.Companion.getId(name)
        return if (ret != -1) {
            ret
        } else MotionType.Companion.getId(name)
    }

    val top: Int
        get() = widgetFrame!!.top
    val left: Int
        get() = widgetFrame!!.left
    val bottom: Int
        get() = widgetFrame!!.bottom
    val right: Int
        get() = widgetFrame!!.right
    var rotationX: Float
        get() = widgetFrame!!.rotationX
        set(rotationX) {
            widgetFrame!!.rotationX = rotationX
        }
    var rotationY: Float
        get() = widgetFrame!!.rotationY
        set(rotationY) {
            widgetFrame!!.rotationY = rotationY
        }
    var rotationZ: Float
        get() = widgetFrame!!.rotationZ
        set(rotationZ) {
            widgetFrame!!.rotationZ = rotationZ
        }
    var translationX: Float
        get() = widgetFrame!!.translationX
        set(translationX) {
            widgetFrame!!.translationX = translationX
        }
    var translationY: Float
        get() = widgetFrame!!.translationY
        set(translationY) {
            widgetFrame!!.translationY = translationY
        }
    var translationZ: Float
        get() = widgetFrame!!.translationZ
        set(tz) {
            widgetFrame!!.translationZ = tz
        }
    var scaleX: Float
        get() = widgetFrame!!.scaleX
        set(scaleX) {
            widgetFrame!!.scaleX = scaleX
        }
    var scaleY: Float
        get() = widgetFrame!!.scaleY
        set(scaleY) {
            widgetFrame!!.scaleY = scaleY
        }
    var visibility: Int
        get() = mPropertySet.visibility
        set(visibility) {
            mPropertySet.visibility = visibility
        }
    var pivotX: Float
        get() = widgetFrame!!.pivotX
        set(px) {
            widgetFrame!!.pivotX = px
        }
    var pivotY: Float
        get() = widgetFrame!!.pivotY
        set(py) {
            widgetFrame!!.pivotY = py
        }
    val alpha: Float
        get() = mPropertySet.alpha
    val x: Int
        get() = widgetFrame!!.left
    val y: Int
        get() = widgetFrame!!.top
    val width: Int
        get() = widgetFrame!!.right - widgetFrame!!.left
    val height: Int
        get() = widgetFrame!!.bottom - widgetFrame!!.top
    val customAttributeNames: Set<String>?
        get() = widgetFrame?.customAttributeNames

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Float) {
        widgetFrame!!.setCustomAttribute(name, type, value)
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Int) {
        widgetFrame!!.setCustomAttribute(name, type, value)
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Boolean) {
        widgetFrame!!.setCustomAttribute(name, type, value)
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: String?) {
        widgetFrame!!.setCustomAttribute(name, type, value)
    }

    /**
     * @TODO: add description
     */
    fun getCustomAttribute(name: String): CustomVariable {
        return widgetFrame!!.getCustomAttribute(name)
    }

    /**
     * @TODO: add description
     */
    fun setInterpolatedValue(attribute: CustomAttribute?, mCache: FloatArray) {
        widgetFrame!!.setCustomAttribute(attribute!!.mName, TypedValues.Custom.Companion.TYPE_FLOAT, mCache[0])
    }

    companion object {
        const val VISIBILITY_MODE_NORMAL = 0
        const val VISIBILITY_MODE_IGNORE = 1
        private const val INTERNAL_MATCH_PARENT = -1
        private const val INTERNAL_WRAP_CONTENT = -2
        const val INVISIBLE = 0
        const val VISIBLE = 4
        private const val INTERNAL_MATCH_CONSTRAINT = -3
        private const val INTERNAL_WRAP_CONTENT_CONSTRAINED = -4
        const val ROTATE_NONE = 0
        const val ROTATE_PORTRATE_OF_RIGHT = 1
        const val ROTATE_PORTRATE_OF_LEFT = 2
        const val ROTATE_RIGHT_OF_PORTRATE = 3
        const val ROTATE_LEFT_OF_PORTRATE = 4
        const val UNSET = -1
        const val MATCH_CONSTRAINT = 0
        const val PARENT_ID = 0
        const val FILL_PARENT = -1
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        const val GONE_UNSET = Int.MIN_VALUE
        val MATCH_CONSTRAINT_WRAP: Int = ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
    }
}
