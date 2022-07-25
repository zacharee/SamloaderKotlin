/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.constraintlayout.core.state

import androidx.constraintlayout.core.motion.utils.TypedBundle
import androidx.constraintlayout.core.motion.utils.TypedValues
import androidx.constraintlayout.core.motion.utils.TypedValues.*
import androidx.constraintlayout.core.parser.*

/**
 * Contains code for Parsing Transitions
 */
object TransitionParser {
    /**
     * Parse a JSON string of a Transition and insert it into the Transition object
     *
     * @param json       Transition Object to parse.
     * @param transition Transition Object to write transition to
     */
    @Throws(CLParsingException::class)
    fun parse(json: CLObject, transition: Transition, dpToPixel: CorePixelDp?) {
        val pathMotionArc = json.getStringOrNull("pathMotionArc")
        val bundle = TypedBundle()
        transition.mToPixel = dpToPixel
        var setBundle = false
        if (pathMotionArc != null) {
            setBundle = true
            when (pathMotionArc) {
                "none" -> bundle.add(PositionType.Companion.TYPE_PATH_MOTION_ARC, 0)
                "startVertical" -> bundle.add(PositionType.Companion.TYPE_PATH_MOTION_ARC, 1)
                "startHorizontal" -> bundle.add(PositionType.Companion.TYPE_PATH_MOTION_ARC, 2)
                "flip" -> bundle.add(PositionType.Companion.TYPE_PATH_MOTION_ARC, 3)
            }
        }
        val interpolator = json.getStringOrNull("interpolator")
        if (interpolator != null) {
            setBundle = true
            bundle.add(TypedValues.TransitionType.Companion.TYPE_INTERPOLATOR, interpolator)
        }
        val staggered: Float = json.getFloatOrNaN("staggered")
        if (!staggered.isNaN()) {
            setBundle = true
            bundle.add(TypedValues.TransitionType.Companion.TYPE_STAGGERED, staggered)
        }
        if (setBundle) {
            transition.setTransitionProperties(bundle)
        }
        val onSwipe = json.getObjectOrNull("onSwipe")
        if (onSwipe != null) {
            parseOnSwipe(onSwipe, transition)
        }
        parseKeyFrames(json, transition)
    }

    private fun parseOnSwipe(onSwipe: CLContainer, transition: Transition) {
        val anchor = onSwipe.getStringOrNull("anchor")
        val side = map(onSwipe.getStringOrNull("side"), *Transition.OnSwipe.Companion.SIDES)
        val direction = map(
            onSwipe.getStringOrNull("direction"),
            *Transition.OnSwipe.Companion.DIRECTIONS
        )
        val scale = onSwipe.getFloatOrNaN("scale")
        val threshold = onSwipe.getFloatOrNaN("threshold")
        val maxVelocity = onSwipe.getFloatOrNaN("maxVelocity")
        val maxAccel = onSwipe.getFloatOrNaN("maxAccel")
        val limitBounds = onSwipe.getStringOrNull("limitBounds")
        val autoCompleteMode = map(onSwipe.getStringOrNull("mode"), *Transition.OnSwipe.Companion.MODE)
        val touchUp = map(onSwipe.getStringOrNull("touchUp"), *Transition.OnSwipe.Companion.TOUCH_UP)
        val springMass = onSwipe.getFloatOrNaN("springMass")
        val springStiffness = onSwipe.getFloatOrNaN("springStiffness")
        val springDamping = onSwipe.getFloatOrNaN("springDamping")
        val stopThreshold = onSwipe.getFloatOrNaN("stopThreshold")
        val springBoundary = map(
            onSwipe.getStringOrNull("springBoundary"),
            *Transition.OnSwipe.Companion.BOUNDARY
        )
        val around = onSwipe.getStringOrNull("around")
        val swipe: Transition.OnSwipe? = transition.createOnSwipe()
        swipe!!.setAnchorId(anchor)
        swipe.setAnchorSide(side)
        swipe.setDragDirection(direction)
        swipe.setDragScale(scale)
        swipe.setDragThreshold(threshold)
        swipe.setMaxVelocity(maxVelocity)
        swipe.setMaxAcceleration(maxAccel)
        swipe.setLimitBoundsTo(limitBounds)
        swipe.setAutoCompleteMode(autoCompleteMode)
        swipe.setOnTouchUp(touchUp)
        swipe.setSpringMass(springMass)
        swipe.setSpringStiffness(springStiffness)
        swipe.setSpringDamping(springDamping)
        swipe.setSpringStopThreshold(stopThreshold)
        swipe.setSpringBoundary(springBoundary)
        swipe.setRotationCenterId(around)
    }

    private fun map(`val`: String?, vararg types: String): Int {
        for (i in types.indices) {
            if (types[i] == `val`) {
                return i
            }
        }
        return 0
    }

    private fun map(bundle: TypedBundle, type: Int, `val`: String, vararg types: String) {
        for (i in types.indices) {
            if (types[i] == `val`) {
                bundle.add(type, i)
            }
        }
    }

    /**
     * Parses `KeyFrames` attributes from the [CLObject] into [Transition].
     *
     * @param transitionCLObject the CLObject for the root transition json
     * @param transition         core object that holds the state of the Transition
     */
    @Throws(CLParsingException::class)
    fun parseKeyFrames(transitionCLObject: CLObject, transition: Transition) {
        val keyframes: CLContainer = transitionCLObject.getObjectOrNull("KeyFrames") ?: return
        val keyPositions = keyframes.getArrayOrNull("KeyPositions")
        if (keyPositions != null) {
            for (i in 0 until keyPositions.size()) {
                val keyPosition: CLElement = keyPositions.get(i)
                if (keyPosition is CLObject) {
                    parseKeyPosition(keyPosition, transition)
                }
            }
        }
        val keyAttributes = keyframes.getArrayOrNull("KeyAttributes")
        if (keyAttributes != null) {
            for (i in 0 until keyAttributes.size()) {
                val keyAttribute: CLElement = keyAttributes.get(i)
                if (keyAttribute is CLObject) {
                    parseKeyAttribute(keyAttribute, transition)
                }
            }
        }
        val keyCycles = keyframes.getArrayOrNull("KeyCycles")
        if (keyCycles != null) {
            for (i in 0 until keyCycles.size()) {
                val keyCycle: CLElement = keyCycles.get(i)
                if (keyCycle is CLObject) {
                    parseKeyCycle(keyCycle, transition)
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyPosition(
        keyPosition: CLObject,
        transition: Transition
    ) {
        val bundle = TypedBundle()
        val targets: CLArray = keyPosition.getArray("target")
        val frames: CLArray = keyPosition.getArray("frames")
        val percentX = keyPosition.getArrayOrNull("percentX")
        val percentY = keyPosition.getArrayOrNull("percentY")
        val percentWidth = keyPosition.getArrayOrNull("percentWidth")!!
        val percentHeight = keyPosition.getArrayOrNull("percentHeight")!!
        val pathMotionArc = keyPosition.getStringOrNull("pathMotionArc")
        val transitionEasing = keyPosition.getStringOrNull("transitionEasing")!!
        val curveFit = keyPosition.getStringOrNull("curveFit")
        var type = keyPosition.getStringOrNull("type")
        if (type == null) {
            type = "parentRelative"
        }
        if (percentX != null && frames.size() != percentX.size()) {
            return
        }
        if (percentY != null && frames.size() != percentY.size()) {
            return
        }
        for (i in 0 until targets.size()) {
            val target: String = targets.getString(i)!!
            val pos_type = map(type, "deltaRelative", "pathRelative", "parentRelative")
            bundle.clear()
            bundle.add(PositionType.Companion.TYPE_POSITION_TYPE, pos_type)
            if (curveFit != null) {
                map(
                    bundle, PositionType.Companion.TYPE_CURVE_FIT, curveFit,
                    "spline", "linear"
                )
            }
            bundle.addIfNotNull(PositionType.Companion.TYPE_TRANSITION_EASING, transitionEasing)
            if (pathMotionArc != null) {
                map(
                    bundle, PositionType.Companion.TYPE_PATH_MOTION_ARC, pathMotionArc,
                    "none", "startVertical", "startHorizontal", "flip"
                )
            }
            for (j in 0 until frames.size()) {
                val frame: Int = frames.getInt(j)
                bundle.add(TypedValues.Companion.TYPE_FRAME_POSITION, frame)
                TransitionParser[bundle, PositionType.Companion.TYPE_PERCENT_X, percentX] = j
                TransitionParser[bundle, PositionType.Companion.TYPE_PERCENT_Y, percentY] = j
                TransitionParser[bundle, PositionType.Companion.TYPE_PERCENT_WIDTH, percentWidth] = j
                TransitionParser[bundle, PositionType.Companion.TYPE_PERCENT_HEIGHT, percentHeight] = j
                transition.addKeyPosition(target, bundle)
            }
        }
    }

    @Throws(CLParsingException::class)
    private operator fun set(
        bundle: TypedBundle, type: Int,
        array: CLArray?, index: Int
    ) {
        if (array != null) {
            bundle.add(type, array.getFloat(index))
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyAttribute(
        keyAttribute: CLObject,
        transition: Transition
    ) {
        val targets: CLArray = keyAttribute.getArrayOrNull("target") ?: return
        val frames: CLArray = keyAttribute.getArrayOrNull("frames") ?: return
        val transitionEasing: String = keyAttribute.getStringOrNull("transitionEasing")!!
        // These present an ordered list of attributes that might be used in a keyCycle
        val attrNames = arrayOf<String>(
            AttributesType.Companion.S_SCALE_X,
            AttributesType.Companion.S_SCALE_Y,
            AttributesType.Companion.S_TRANSLATION_X,
            AttributesType.Companion.S_TRANSLATION_Y,
            AttributesType.Companion.S_TRANSLATION_Z,
            AttributesType.Companion.S_ROTATION_X,
            AttributesType.Companion.S_ROTATION_Y,
            AttributesType.Companion.S_ROTATION_Z,
            AttributesType.Companion.S_ALPHA
        )
        val attrIds = intArrayOf(
            AttributesType.Companion.TYPE_SCALE_X,
            AttributesType.Companion.TYPE_SCALE_Y,
            AttributesType.Companion.TYPE_TRANSLATION_X,
            AttributesType.Companion.TYPE_TRANSLATION_Y,
            AttributesType.Companion.TYPE_TRANSLATION_Z,
            AttributesType.Companion.TYPE_ROTATION_X,
            AttributesType.Companion.TYPE_ROTATION_Y,
            AttributesType.Companion.TYPE_ROTATION_Z,
            AttributesType.Companion.TYPE_ALPHA
        )
        // if true scale the values from pixels to dp
        val scaleTypes = booleanArrayOf(
            false,
            false,
            true,
            true,
            true,
            false,
            false,
            false,
            false
        )
        val bundles = arrayOfNulls<TypedBundle>(frames.size())
        for (i in 0 until frames.size()) {
            bundles[i] = TypedBundle()
        }
        for (k in attrNames.indices) {
            val attrName = attrNames[k]
            val attrId = attrIds[k]
            val scale = scaleTypes[k]
            val arrayValues = keyAttribute.getArrayOrNull(attrName)
            // array must contain one per frame
            if (arrayValues != null && arrayValues.size() != bundles.size) {
                throw CLParsingException(
                    "incorrect size for " + attrName + " array, "
                            + "not matching targets array!", keyAttribute
                )
            }
            if (arrayValues != null) {
                for (i in bundles.indices) {
                    var value: Float = arrayValues.getFloat(i)
                    if (scale) {
                        value = transition.mToPixel!!.toPixels(value)
                    }
                    bundles[i]!!.add(attrId, value)
                }
            } else {
                var value: Float = keyAttribute.getFloatOrNaN(attrName)
                if (!value.isNaN()) {
                    if (scale) {
                        value = transition.mToPixel!!.toPixels(value)
                    }
                    for (i in bundles.indices) {
                        bundles[i]!!.add(attrId, value)
                    }
                }
            }
        }
        val curveFit = keyAttribute.getStringOrNull("curveFit")
        for (i in 0 until targets.size()) {
            for (j in bundles.indices) {
                val target: String = targets.getString(i)!!
                val bundle = bundles[j]
                if (curveFit != null) {
                    bundle!!.add(
                        PositionType.Companion.TYPE_CURVE_FIT,
                        map(curveFit, "spline", "linear")
                    )
                }
                bundle!!.addIfNotNull(
                    PositionType.Companion.TYPE_TRANSITION_EASING,
                    transitionEasing
                )
                val frame: Int = frames.getInt(j)
                bundle.add(TypedValues.Companion.TYPE_FRAME_POSITION, frame)
                transition.addKeyAttribute(target, bundle)
            }
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyCycle(
        keyCycleData: CLObject,
        transition: Transition
    ) {
        val targets: CLArray = keyCycleData.getArray("target")
        val frames: CLArray = keyCycleData.getArray("frames")
        val transitionEasing: String = keyCycleData.getStringOrNull("transitionEasing")!!
        // These present an ordered list of attributes that might be used in a keyCycle
        val attrNames = arrayOf<String>(
            CycleType.Companion.S_SCALE_X,
            CycleType.Companion.S_SCALE_Y,
            CycleType.Companion.S_TRANSLATION_X,
            CycleType.Companion.S_TRANSLATION_Y,
            CycleType.Companion.S_TRANSLATION_Z,
            CycleType.Companion.S_ROTATION_X,
            CycleType.Companion.S_ROTATION_Y,
            CycleType.Companion.S_ROTATION_Z,
            CycleType.Companion.S_ALPHA,
            CycleType.Companion.S_WAVE_PERIOD,
            CycleType.Companion.S_WAVE_OFFSET,
            CycleType.Companion.S_WAVE_PHASE
        )
        val attrIds = intArrayOf(
            CycleType.Companion.TYPE_SCALE_X,
            CycleType.Companion.TYPE_SCALE_Y,
            CycleType.Companion.TYPE_TRANSLATION_X,
            CycleType.Companion.TYPE_TRANSLATION_Y,
            CycleType.Companion.TYPE_TRANSLATION_Z,
            CycleType.Companion.TYPE_ROTATION_X,
            CycleType.Companion.TYPE_ROTATION_Y,
            CycleType.Companion.TYPE_ROTATION_Z,
            CycleType.Companion.TYPE_ALPHA,
            CycleType.Companion.TYPE_WAVE_PERIOD,
            CycleType.Companion.TYPE_WAVE_OFFSET,
            CycleType.Companion.TYPE_WAVE_PHASE
        )
        // type 0 the values are used as.
        // type 1 the value is scaled from dp to pixels.
        // type 2 are scaled if the system has another type 1.
        val scaleTypes = intArrayOf(
            0,
            0,
            1,
            1,
            1,
            0,
            0,
            0,
            0,
            0,
            2,
            1
        )

//  TODO S_WAVE_SHAPE S_CUSTOM_WAVE_SHAPE
        val bundles = arrayOfNulls<TypedBundle>(frames.size())
        for (i in bundles.indices) {
            bundles[i] = TypedBundle()
        }
        var scaleOffset = false
        for (k in attrNames.indices) {
            if (keyCycleData.has(attrNames[k]) && scaleTypes[k] == 1) {
                scaleOffset = true
            }
        }
        for (k in attrNames.indices) {
            val attrName = attrNames[k]
            val attrId = attrIds[k]
            val scale = scaleTypes[k]
            val arrayValues = keyCycleData.getArrayOrNull(attrName)
            // array must contain one per frame
            if (arrayValues != null && arrayValues.size() != bundles.size) {
                throw CLParsingException(
                    "incorrect size for \$attrName array, "
                            + "not matching targets array!", keyCycleData
                )
            }
            if (arrayValues != null) {
                for (i in bundles.indices) {
                    var value: Float = arrayValues.getFloat(i)
                    if (scale == 1) {
                        value = transition.mToPixel!!.toPixels(value)
                    } else if (scale == 2 && scaleOffset) {
                        value = transition.mToPixel!!.toPixels(value)
                    }
                    bundles[i]!!.add(attrId, value)
                }
            } else {
                var value: Float = keyCycleData.getFloatOrNaN(attrName)
                if (!value.isNaN()) {
                    if (scale == 1) {
                        value = transition.mToPixel!!.toPixels(value)
                    } else if (scale == 2 && scaleOffset) {
                        value = transition.mToPixel!!.toPixels(value)
                    }
                    for (i in bundles.indices) {
                        bundles[i]!!.add(attrId, value)
                    }
                }
            }
        }
        val curveFit = keyCycleData.getStringOrNull(CycleType.Companion.S_CURVE_FIT)
        val easing = keyCycleData.getStringOrNull(CycleType.Companion.S_EASING)
        val waveShape = keyCycleData.getStringOrNull(CycleType.Companion.S_WAVE_SHAPE)
        val customWave = keyCycleData.getStringOrNull(CycleType.Companion.S_CUSTOM_WAVE_SHAPE)
        for (i in 0 until targets.size()) {
            for (j in bundles.indices) {
                val target: String = targets.getString(i)!!
                val bundle = bundles[j]
                if (curveFit != null) {
                    when (curveFit) {
                        "spline" -> bundle!!.add(CycleType.Companion.TYPE_CURVE_FIT, 0)
                        "linear" -> bundle!!.add(CycleType.Companion.TYPE_CURVE_FIT, 1)
                    }
                }
                bundle!!.addIfNotNull(
                    PositionType.Companion.TYPE_TRANSITION_EASING,
                    transitionEasing
                )
                if (easing != null) {
                    bundle.add(CycleType.Companion.TYPE_EASING, easing)
                }
                if (waveShape != null) {
                    bundle.add(CycleType.Companion.TYPE_WAVE_SHAPE, waveShape)
                }
                if (customWave != null) {
                    bundle.add(CycleType.Companion.TYPE_CUSTOM_WAVE_SHAPE, customWave)
                }
                val frame: Int = frames.getInt(j)
                bundle.add(TypedValues.Companion.TYPE_FRAME_POSITION, frame)
                transition.addKeyCycle(target, bundle)
            }
        }
    }
}
