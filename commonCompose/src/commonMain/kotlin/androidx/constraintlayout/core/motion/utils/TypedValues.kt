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
package androidx.constraintlayout.core.motion.utils

/**
 * Provides an interface to values used in KeyFrames and in
 * Starting and Ending Widgets
 */
interface TypedValues {
    /**
     * Used to set integer values
     *
     * @return true if it accepted the value
     */
    fun setValue(id: Int, value: Int): Boolean

    /**
     * Used to set float values
     *
     * @return true if it accepted the value
     */
    fun setValue(id: Int, value: Float): Boolean

    /**
     * Used to set String values
     *
     * @return true if it accepted the value
     */
    fun setValue(id: Int, value: String): Boolean

    /**
     * Used to set boolean values
     *
     * @return true if it accepted the value
     */
    fun setValue(id: Int, value: Boolean): Boolean

    /**
     * @TODO: add description
     */
    fun getId(name: String?): Int
    interface AttributesType {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_CURVE_FIT -> return TYPE_CURVE_FIT
                    S_VISIBILITY -> return TYPE_VISIBILITY
                    S_ALPHA -> return TYPE_ALPHA
                    S_TRANSLATION_X -> return TYPE_TRANSLATION_X
                    S_TRANSLATION_Y -> return TYPE_TRANSLATION_Y
                    S_TRANSLATION_Z -> return TYPE_TRANSLATION_Z
                    S_ELEVATION -> return TYPE_ELEVATION
                    S_ROTATION_X -> return TYPE_ROTATION_X
                    S_ROTATION_Y -> return TYPE_ROTATION_Y
                    S_ROTATION_Z -> return TYPE_ROTATION_Z
                    S_SCALE_X -> return TYPE_SCALE_X
                    S_SCALE_Y -> return TYPE_SCALE_Y
                    S_PIVOT_X -> return TYPE_PIVOT_X
                    S_PIVOT_Y -> return TYPE_PIVOT_Y
                    S_PROGRESS -> return TYPE_PROGRESS
                    S_PATH_ROTATE -> return TYPE_PATH_ROTATE
                    S_EASING -> return TYPE_EASING
                    S_FRAME -> return TYPE_FRAME_POSITION
                    S_TARGET -> return TYPE_TARGET
                    S_PIVOT_TARGET -> return TYPE_PIVOT_TARGET
                }
                return -1
            }

            fun getType(name: Int): Int {
                when (name) {
                    TYPE_CURVE_FIT, TYPE_VISIBILITY, TYPE_FRAME_POSITION -> return INT_MASK
                    TYPE_ALPHA, TYPE_TRANSLATION_X, TYPE_TRANSLATION_Y, TYPE_TRANSLATION_Z, TYPE_ELEVATION, TYPE_ROTATION_X, TYPE_ROTATION_Y, TYPE_ROTATION_Z, TYPE_SCALE_X, TYPE_SCALE_Y, TYPE_PIVOT_X, TYPE_PIVOT_Y, TYPE_PROGRESS, TYPE_PATH_ROTATE -> return FLOAT_MASK
                    TYPE_EASING, TYPE_TARGET, TYPE_PIVOT_TARGET -> return STRING_MASK
                }
                return -1
            }

            const val NAME = "KeyAttributes"
            const val TYPE_CURVE_FIT = 301
            const val TYPE_VISIBILITY = 302
            const val TYPE_ALPHA = 303
            const val TYPE_TRANSLATION_X = 304
            const val TYPE_TRANSLATION_Y = 305
            const val TYPE_TRANSLATION_Z = 306
            const val TYPE_ELEVATION = 307
            const val TYPE_ROTATION_X = 308
            const val TYPE_ROTATION_Y = 309
            const val TYPE_ROTATION_Z = 310
            const val TYPE_SCALE_X = 311
            const val TYPE_SCALE_Y = 312
            const val TYPE_PIVOT_X = 313
            const val TYPE_PIVOT_Y = 314
            const val TYPE_PROGRESS = 315
            const val TYPE_PATH_ROTATE = 316
            const val TYPE_EASING = 317
            const val TYPE_PIVOT_TARGET = 318
            const val S_CURVE_FIT = "curveFit"
            const val S_VISIBILITY = "visibility"
            const val S_ALPHA = "alpha"
            const val S_TRANSLATION_X = "translationX"
            const val S_TRANSLATION_Y = "translationY"
            const val S_TRANSLATION_Z = "translationZ"
            const val S_ELEVATION = "elevation"
            const val S_ROTATION_X = "rotationX"
            const val S_ROTATION_Y = "rotationY"
            const val S_ROTATION_Z = "rotationZ"
            const val S_SCALE_X = "scaleX"
            const val S_SCALE_Y = "scaleY"
            const val S_PIVOT_X = "pivotX"
            const val S_PIVOT_Y = "pivotY"
            const val S_PROGRESS = "progress"
            const val S_PATH_ROTATE = "pathRotate"
            const val S_EASING = "easing"
            const val S_CUSTOM = "CUSTOM"
            const val S_FRAME = "frame"
            const val S_TARGET = "target"
            const val S_PIVOT_TARGET = "pivotTarget"
            val KEY_WORDS = arrayOf(
                S_CURVE_FIT,
                S_VISIBILITY,
                S_ALPHA,
                S_TRANSLATION_X,
                S_TRANSLATION_Y,
                S_TRANSLATION_Z,
                S_ELEVATION,
                S_ROTATION_X,
                S_ROTATION_Y,
                S_ROTATION_Z,
                S_SCALE_X,
                S_SCALE_Y,
                S_PIVOT_X,
                S_PIVOT_Y,
                S_PROGRESS,
                S_PATH_ROTATE,
                S_EASING,
                S_CUSTOM,
                S_FRAME,
                S_TARGET,
                S_PIVOT_TARGET
            )
        }
    }

    interface CycleType {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_CURVE_FIT -> return TYPE_CURVE_FIT
                    S_VISIBILITY -> return TYPE_VISIBILITY
                    S_ALPHA -> return TYPE_ALPHA
                    S_TRANSLATION_X -> return TYPE_TRANSLATION_X
                    S_TRANSLATION_Y -> return TYPE_TRANSLATION_Y
                    S_TRANSLATION_Z -> return TYPE_TRANSLATION_Z
                    S_ROTATION_X -> return TYPE_ROTATION_X
                    S_ROTATION_Y -> return TYPE_ROTATION_Y
                    S_ROTATION_Z -> return TYPE_ROTATION_Z
                    S_SCALE_X -> return TYPE_SCALE_X
                    S_SCALE_Y -> return TYPE_SCALE_Y
                    S_PIVOT_X -> return TYPE_PIVOT_X
                    S_PIVOT_Y -> return TYPE_PIVOT_Y
                    S_PROGRESS -> return TYPE_PROGRESS
                    S_PATH_ROTATE -> return TYPE_PATH_ROTATE
                    S_EASING -> return TYPE_EASING
                }
                return -1
            }

            fun getType(name: Int): Int {
                when (name) {
                    TYPE_CURVE_FIT, TYPE_VISIBILITY, TYPE_FRAME_POSITION -> return INT_MASK
                    TYPE_ALPHA, TYPE_TRANSLATION_X, TYPE_TRANSLATION_Y, TYPE_TRANSLATION_Z, TYPE_ELEVATION, TYPE_ROTATION_X, TYPE_ROTATION_Y, TYPE_ROTATION_Z, TYPE_SCALE_X, TYPE_SCALE_Y, TYPE_PIVOT_X, TYPE_PIVOT_Y, TYPE_PROGRESS, TYPE_PATH_ROTATE, TYPE_WAVE_PERIOD, TYPE_WAVE_OFFSET, TYPE_WAVE_PHASE -> return FLOAT_MASK
                    TYPE_EASING, TYPE_TARGET, TYPE_WAVE_SHAPE -> return STRING_MASK
                }
                return -1
            }

            const val NAME = "KeyCycle"
            const val TYPE_CURVE_FIT = 401
            const val TYPE_VISIBILITY = 402
            const val TYPE_ALPHA = 403
            const val TYPE_TRANSLATION_X = AttributesType.TYPE_TRANSLATION_X
            const val TYPE_TRANSLATION_Y = AttributesType.TYPE_TRANSLATION_Y
            const val TYPE_TRANSLATION_Z = AttributesType.TYPE_TRANSLATION_Z
            const val TYPE_ELEVATION = AttributesType.TYPE_ELEVATION
            const val TYPE_ROTATION_X = AttributesType.TYPE_ROTATION_X
            const val TYPE_ROTATION_Y = AttributesType.TYPE_ROTATION_Y
            const val TYPE_ROTATION_Z = AttributesType.TYPE_ROTATION_Z
            const val TYPE_SCALE_X = AttributesType.TYPE_SCALE_X
            const val TYPE_SCALE_Y = AttributesType.TYPE_SCALE_Y
            const val TYPE_PIVOT_X = AttributesType.TYPE_PIVOT_X
            const val TYPE_PIVOT_Y = AttributesType.TYPE_PIVOT_Y
            const val TYPE_PROGRESS = AttributesType.TYPE_PROGRESS
            const val TYPE_PATH_ROTATE = 416
            const val TYPE_EASING = 420
            const val TYPE_WAVE_SHAPE = 421
            const val TYPE_CUSTOM_WAVE_SHAPE = 422
            const val TYPE_WAVE_PERIOD = 423
            const val TYPE_WAVE_OFFSET = 424
            const val TYPE_WAVE_PHASE = 425
            const val S_CURVE_FIT = "curveFit"
            const val S_VISIBILITY = "visibility"
            const val S_ALPHA = AttributesType.S_ALPHA
            const val S_TRANSLATION_X = AttributesType.S_TRANSLATION_X
            const val S_TRANSLATION_Y = AttributesType.S_TRANSLATION_Y
            const val S_TRANSLATION_Z = AttributesType.S_TRANSLATION_Z
            const val S_ELEVATION = AttributesType.S_ELEVATION
            const val S_ROTATION_X = AttributesType.S_ROTATION_X
            const val S_ROTATION_Y = AttributesType.S_ROTATION_Y
            const val S_ROTATION_Z = AttributesType.S_ROTATION_Z
            const val S_SCALE_X = AttributesType.S_SCALE_X
            const val S_SCALE_Y = AttributesType.S_SCALE_Y
            const val S_PIVOT_X = AttributesType.S_PIVOT_X
            const val S_PIVOT_Y = AttributesType.S_PIVOT_Y
            const val S_PROGRESS = AttributesType.S_PROGRESS
            const val S_PATH_ROTATE = "pathRotate"
            const val S_EASING = "easing"
            const val S_WAVE_SHAPE = "waveShape"
            const val S_CUSTOM_WAVE_SHAPE = "customWave"
            const val S_WAVE_PERIOD = "period"
            const val S_WAVE_OFFSET = "offset"
            const val S_WAVE_PHASE = "phase"
            val KEY_WORDS = arrayOf(
                S_CURVE_FIT,
                S_VISIBILITY,
                S_ALPHA,
                S_TRANSLATION_X,
                S_TRANSLATION_Y,
                S_TRANSLATION_Z,
                S_ELEVATION,
                S_ROTATION_X,
                S_ROTATION_Y,
                S_ROTATION_Z,
                S_SCALE_X,
                S_SCALE_Y,
                S_PIVOT_X,
                S_PIVOT_Y,
                S_PROGRESS,
                S_PATH_ROTATE,
                S_EASING,
                S_WAVE_SHAPE,
                S_CUSTOM_WAVE_SHAPE,
                S_WAVE_PERIOD,
                S_WAVE_OFFSET,
                S_WAVE_PHASE
            )
        }
    }

    interface TriggerType {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    VIEW_TRANSITION_ON_CROSS -> return TYPE_VIEW_TRANSITION_ON_CROSS
                    VIEW_TRANSITION_ON_POSITIVE_CROSS -> return TYPE_VIEW_TRANSITION_ON_POSITIVE_CROSS
                    VIEW_TRANSITION_ON_NEGATIVE_CROSS -> return TYPE_VIEW_TRANSITION_ON_NEGATIVE_CROSS
                    POST_LAYOUT -> return TYPE_POST_LAYOUT
                    TRIGGER_SLACK -> return TYPE_TRIGGER_SLACK
                    TRIGGER_COLLISION_VIEW -> return TYPE_TRIGGER_COLLISION_VIEW
                    TRIGGER_COLLISION_ID -> return TYPE_TRIGGER_COLLISION_ID
                    TRIGGER_ID -> return TYPE_TRIGGER_ID
                    POSITIVE_CROSS -> return TYPE_POSITIVE_CROSS
                    NEGATIVE_CROSS -> return TYPE_NEGATIVE_CROSS
                    TRIGGER_RECEIVER -> return TYPE_TRIGGER_RECEIVER
                    CROSS -> return TYPE_CROSS
                }
                return -1
            }

            const val NAME = "KeyTrigger"
            const val VIEW_TRANSITION_ON_CROSS = "viewTransitionOnCross"
            const val VIEW_TRANSITION_ON_POSITIVE_CROSS = "viewTransitionOnPositiveCross"
            const val VIEW_TRANSITION_ON_NEGATIVE_CROSS = "viewTransitionOnNegativeCross"
            const val POST_LAYOUT = "postLayout"
            const val TRIGGER_SLACK = "triggerSlack"
            const val TRIGGER_COLLISION_VIEW = "triggerCollisionView"
            const val TRIGGER_COLLISION_ID = "triggerCollisionId"
            const val TRIGGER_ID = "triggerID"
            const val POSITIVE_CROSS = "positiveCross"
            const val NEGATIVE_CROSS = "negativeCross"
            const val TRIGGER_RECEIVER = "triggerReceiver"
            const val CROSS = "CROSS"
            val KEY_WORDS = arrayOf(
                VIEW_TRANSITION_ON_CROSS,
                VIEW_TRANSITION_ON_POSITIVE_CROSS,
                VIEW_TRANSITION_ON_NEGATIVE_CROSS,
                POST_LAYOUT,
                TRIGGER_SLACK,
                TRIGGER_COLLISION_VIEW,
                TRIGGER_COLLISION_ID,
                TRIGGER_ID,
                POSITIVE_CROSS,
                NEGATIVE_CROSS,
                TRIGGER_RECEIVER,
                CROSS
            )
            const val TYPE_VIEW_TRANSITION_ON_CROSS = 301
            const val TYPE_VIEW_TRANSITION_ON_POSITIVE_CROSS = 302
            const val TYPE_VIEW_TRANSITION_ON_NEGATIVE_CROSS = 303
            const val TYPE_POST_LAYOUT = 304
            const val TYPE_TRIGGER_SLACK = 305
            const val TYPE_TRIGGER_COLLISION_VIEW = 306
            const val TYPE_TRIGGER_COLLISION_ID = 307
            const val TYPE_TRIGGER_ID = 308
            const val TYPE_POSITIVE_CROSS = 309
            const val TYPE_NEGATIVE_CROSS = 310
            const val TYPE_TRIGGER_RECEIVER = 311
            const val TYPE_CROSS = 312
        }
    }

    interface PositionType {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_TRANSITION_EASING -> return TYPE_TRANSITION_EASING
                    S_DRAWPATH -> return TYPE_DRAWPATH
                    S_PERCENT_WIDTH -> return TYPE_PERCENT_WIDTH
                    S_PERCENT_HEIGHT -> return TYPE_PERCENT_HEIGHT
                    S_SIZE_PERCENT -> return TYPE_SIZE_PERCENT
                    S_PERCENT_X -> return TYPE_PERCENT_X
                    S_PERCENT_Y -> return TYPE_PERCENT_Y
                }
                return -1
            }

            fun getType(name: Int): Int {
                when (name) {
                    TYPE_CURVE_FIT, TYPE_FRAME_POSITION -> return INT_MASK
                    TYPE_PERCENT_WIDTH, TYPE_PERCENT_HEIGHT, TYPE_SIZE_PERCENT, TYPE_PERCENT_X, TYPE_PERCENT_Y -> return FLOAT_MASK
                    TYPE_TRANSITION_EASING, TYPE_TARGET, TYPE_DRAWPATH -> return STRING_MASK
                }
                return -1
            }

            const val NAME = "KeyPosition"
            const val S_TRANSITION_EASING = "transitionEasing"
            const val S_DRAWPATH = "drawPath"
            const val S_PERCENT_WIDTH = "percentWidth"
            const val S_PERCENT_HEIGHT = "percentHeight"
            const val S_SIZE_PERCENT = "sizePercent"
            const val S_PERCENT_X = "percentX"
            const val S_PERCENT_Y = "percentY"
            const val TYPE_TRANSITION_EASING = 501
            const val TYPE_DRAWPATH = 502
            const val TYPE_PERCENT_WIDTH = 503
            const val TYPE_PERCENT_HEIGHT = 504
            const val TYPE_SIZE_PERCENT = 505
            const val TYPE_PERCENT_X = 506
            const val TYPE_PERCENT_Y = 507
            const val TYPE_CURVE_FIT = 508
            const val TYPE_PATH_MOTION_ARC = 509
            const val TYPE_POSITION_TYPE = 510
            val KEY_WORDS = arrayOf(
                S_TRANSITION_EASING,
                S_DRAWPATH,
                S_PERCENT_WIDTH,
                S_PERCENT_HEIGHT,
                S_SIZE_PERCENT,
                S_PERCENT_X,
                S_PERCENT_Y
            )
        }
    }

    interface MotionType {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_STAGGER -> return TYPE_STAGGER
                    S_PATH_ROTATE -> return TYPE_PATH_ROTATE
                    S_QUANTIZE_MOTION_PHASE -> return TYPE_QUANTIZE_MOTION_PHASE
                    S_EASING -> return TYPE_EASING
                    S_QUANTIZE_INTERPOLATOR -> return TYPE_QUANTIZE_INTERPOLATOR
                    S_ANIMATE_RELATIVE_TO -> return TYPE_ANIMATE_RELATIVE_TO
                    S_ANIMATE_CIRCLEANGLE_TO -> return TYPE_ANIMATE_CIRCLEANGLE_TO
                    S_PATHMOTION_ARC -> return TYPE_PATHMOTION_ARC
                    S_DRAW_PATH -> return TYPE_DRAW_PATH
                    S_POLAR_RELATIVETO -> return TYPE_POLAR_RELATIVETO
                    S_QUANTIZE_MOTIONSTEPS -> return TYPE_QUANTIZE_MOTIONSTEPS
                    S_QUANTIZE_INTERPOLATOR_TYPE -> return TYPE_QUANTIZE_INTERPOLATOR_TYPE
                    S_QUANTIZE_INTERPOLATOR_ID -> return TYPE_QUANTIZE_INTERPOLATOR_ID
                }
                return -1
            }

            const val NAME = "Motion"
            const val S_STAGGER = "Stagger"
            const val S_PATH_ROTATE = "PathRotate"
            const val S_QUANTIZE_MOTION_PHASE = "QuantizeMotionPhase"
            const val S_EASING = "TransitionEasing"
            const val S_QUANTIZE_INTERPOLATOR = "QuantizeInterpolator"
            const val S_ANIMATE_RELATIVE_TO = "AnimateRelativeTo"
            const val S_ANIMATE_CIRCLEANGLE_TO = "AnimateCircleAngleTo"
            const val S_PATHMOTION_ARC = "PathMotionArc"
            const val S_DRAW_PATH = "DrawPath"
            const val S_POLAR_RELATIVETO = "PolarRelativeTo"
            const val S_QUANTIZE_MOTIONSTEPS = "QuantizeMotionSteps"
            const val S_QUANTIZE_INTERPOLATOR_TYPE = "QuantizeInterpolatorType"
            const val S_QUANTIZE_INTERPOLATOR_ID = "QuantizeInterpolatorID"
            val KEY_WORDS = arrayOf(
                S_STAGGER,
                S_PATH_ROTATE,
                S_QUANTIZE_MOTION_PHASE,
                S_EASING,
                S_QUANTIZE_INTERPOLATOR,
                S_ANIMATE_RELATIVE_TO,
                S_ANIMATE_CIRCLEANGLE_TO,
                S_PATHMOTION_ARC,
                S_DRAW_PATH,
                S_POLAR_RELATIVETO,
                S_QUANTIZE_MOTIONSTEPS,
                S_QUANTIZE_INTERPOLATOR_TYPE,
                S_QUANTIZE_INTERPOLATOR_ID
            )
            const val TYPE_STAGGER = 600
            const val TYPE_PATH_ROTATE = 601
            const val TYPE_QUANTIZE_MOTION_PHASE = 602
            const val TYPE_EASING = 603
            const val TYPE_QUANTIZE_INTERPOLATOR = 604
            const val TYPE_ANIMATE_RELATIVE_TO = 605
            const val TYPE_ANIMATE_CIRCLEANGLE_TO = 606
            const val TYPE_PATHMOTION_ARC = 607
            const val TYPE_DRAW_PATH = 608
            const val TYPE_POLAR_RELATIVETO = 609
            const val TYPE_QUANTIZE_MOTIONSTEPS = 610
            const val TYPE_QUANTIZE_INTERPOLATOR_TYPE = 611
            const val TYPE_QUANTIZE_INTERPOLATOR_ID = 612
        }
    }

    interface Custom {
        companion object {
            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_INT -> return TYPE_INT
                    S_FLOAT -> return TYPE_FLOAT
                    S_COLOR -> return TYPE_COLOR
                    S_STRING -> return TYPE_STRING
                    S_BOOLEAN -> return TYPE_BOOLEAN
                    S_DIMENSION -> return TYPE_DIMENSION
                    S_REFERENCE -> return TYPE_REFERENCE
                }
                return -1
            }

            const val NAME = "Custom"
            const val S_INT = "integer"
            const val S_FLOAT = "float"
            const val S_COLOR = "color"
            const val S_STRING = "string"
            const val S_BOOLEAN = "boolean"
            const val S_DIMENSION = "dimension"
            const val S_REFERENCE = "reference"
            val KEY_WORDS = arrayOf(
                S_FLOAT,
                S_COLOR,
                S_STRING,
                S_BOOLEAN,
                S_DIMENSION,
                S_REFERENCE
            )
            const val TYPE_INT = 900
            const val TYPE_FLOAT = 901
            const val TYPE_COLOR = 902
            const val TYPE_STRING = 903
            const val TYPE_BOOLEAN = 904
            const val TYPE_DIMENSION = 905
            const val TYPE_REFERENCE = 906
        }
    }

    interface MotionScene {
        companion object {
            fun getType(name: Int): Int {
                when (name) {
                    TYPE_DEFAULT_DURATION -> return INT_MASK
                    TYPE_LAYOUT_DURING_TRANSITION -> return BOOLEAN_MASK
                }
                return -1
            }

            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_DEFAULT_DURATION -> return TYPE_DEFAULT_DURATION
                    S_LAYOUT_DURING_TRANSITION -> return TYPE_LAYOUT_DURING_TRANSITION
                }
                return -1
            }

            const val NAME = "MotionScene"
            const val S_DEFAULT_DURATION = "defaultDuration"
            const val S_LAYOUT_DURING_TRANSITION = "layoutDuringTransition"
            const val TYPE_DEFAULT_DURATION = 600
            const val TYPE_LAYOUT_DURING_TRANSITION = 601
            val KEY_WORDS = arrayOf(
                S_DEFAULT_DURATION,
                S_LAYOUT_DURING_TRANSITION
            )
        }
    }

    interface TransitionType {
        companion object {
            fun getType(name: Int): Int {
                when (name) {
                    TYPE_DURATION, TYPE_PATH_MOTION_ARC -> return INT_MASK
                    TYPE_FROM, TYPE_TO, TYPE_INTERPOLATOR, TYPE_TRANSITION_FLAGS -> return STRING_MASK
                    TYPE_STAGGERED -> return FLOAT_MASK
                }
                return -1
            }

            /**
             * Method to go from String names of values to id of the values
             * IDs are use for efficiency
             *
             * @param name the name of the value
             * @return the id of the vlalue or -1 if no value exist
             */
            fun getId(name: String?): Int {
                when (name) {
                    S_DURATION -> return TYPE_DURATION
                    S_FROM -> return TYPE_FROM
                    S_TO -> return TYPE_TO
                    S_PATH_MOTION_ARC -> return TYPE_PATH_MOTION_ARC
                    S_AUTO_TRANSITION -> return TYPE_AUTO_TRANSITION
                    S_INTERPOLATOR -> return TYPE_INTERPOLATOR
                    S_STAGGERED -> return TYPE_STAGGERED
                    S_TRANSITION_FLAGS -> return TYPE_TRANSITION_FLAGS
                }
                return -1
            }

            const val NAME = "Transitions"
            const val S_DURATION = "duration"
            const val S_FROM = "from"
            const val S_TO = "to"
            const val S_PATH_MOTION_ARC = "pathMotionArc"
            const val S_AUTO_TRANSITION = "autoTransition"
            const val S_INTERPOLATOR = "motionInterpolator"
            const val S_STAGGERED = "staggered"
            const val S_TRANSITION_FLAGS = "transitionFlags"
            const val TYPE_DURATION = 700
            const val TYPE_FROM = 701
            const val TYPE_TO = 702
            const val TYPE_PATH_MOTION_ARC = PositionType.TYPE_PATH_MOTION_ARC
            const val TYPE_AUTO_TRANSITION = 704
            const val TYPE_INTERPOLATOR = 705
            const val TYPE_STAGGERED = 706
            const val TYPE_TRANSITION_FLAGS = 707
            val KEY_WORDS = arrayOf(
                S_DURATION,
                S_FROM,
                S_TO,
                S_PATH_MOTION_ARC,
                S_AUTO_TRANSITION,
                S_INTERPOLATOR,
                S_STAGGERED,
                S_FROM,
                S_TRANSITION_FLAGS
            )
        }
    }

    interface OnSwipe {
        companion object {
            const val DRAG_SCALE = "dragscale"
            const val DRAG_THRESHOLD = "dragthreshold"
            const val MAX_VELOCITY = "maxvelocity"
            const val MAX_ACCELERATION = "maxacceleration"
            const val SPRING_MASS = "springmass"
            const val SPRING_STIFFNESS = "springstiffness"
            const val SPRING_DAMPING = "springdamping"
            const val SPRINGS_TOP_THRESHOLD = "springstopthreshold"
            const val DRAG_DIRECTION = "dragdirection"
            const val TOUCH_ANCHOR_ID = "touchanchorid"
            const val TOUCH_ANCHOR_SIDE = "touchanchorside"
            const val ROTATION_CENTER_ID = "rotationcenterid"
            const val TOUCH_REGION_ID = "touchregionid"
            const val LIMIT_BOUNDS_TO = "limitboundsto"
            const val MOVE_WHEN_SCROLLAT_TOP = "movewhenscrollattop"
            const val ON_TOUCH_UP = "ontouchup"
            val ON_TOUCH_UP_ENUM = arrayOf(
                "autoComplete",
                "autoCompleteToStart",
                "autoCompleteToEnd",
                "stop",
                "decelerate",
                "decelerateAndComplete",
                "neverCompleteToStart",
                "neverCompleteToEnd"
            )
            const val SPRING_BOUNDARY = "springboundary"
            val SPRING_BOUNDARY_ENUM = arrayOf(
                "overshoot",
                "bounceStart",
                "bounceEnd",
                "bounceBoth"
            )
            const val AUTOCOMPLETE_MODE = "autocompletemode"
            val AUTOCOMPLETE_MODE_ENUM = arrayOf(
                "continuousVelocity",
                "spring"
            )
            const val NESTED_SCROLL_FLAGS = "nestedscrollflags"
            val NESTED_SCROLL_FLAGS_ENUM = arrayOf(
                "none",
                "disablePostScroll",
                "disableScroll",
                "supportScrollUp"
            )
        }
    }

    companion object {
        const val S_CUSTOM = "CUSTOM"
        const val BOOLEAN_MASK = 1
        const val INT_MASK = 2
        const val FLOAT_MASK = 4
        const val STRING_MASK = 8
        const val TYPE_FRAME_POSITION = 100
        const val TYPE_TARGET = 101
    }
}
