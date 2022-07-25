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
package androidx.constraintlayout.core.dsl

/**
 * Create automatic swipe handling object
 */
class OnSwipe {
    var dragDirection: Drag? = null
        private set
    var touchAnchorSide: Side? = null
        private set
    var touchAnchorId: String? = null
        private set
    var limitBoundsTo: String? = null
        private set
    var onTouchUp: TouchUp? = null
        private set
    var rotationCenterId: String? = null
        private set
    var maxVelocity = Float.NaN
        private set
    var maxAcceleration = Float.NaN
        private set
    var dragScale = Float.NaN
        private set
    var dragThreshold = Float.NaN
        private set
    var springDamping = Float.NaN
        private set

    /**
     * Get the mass of the spring.
     * the m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     */
    var springMass = Float.NaN
        private set

    /**
     * get the stiffness of the spring
     *
     * @return NaN if not set
     */
    var springStiffness = Float.NaN
        private set

    /**
     * The threshold for spring motion to stop.
     */
    var springStopThreshold = Float.NaN
        private set

    /**
     * The behaviour at the boundaries 0 and 1
     */
    var springBoundary: Boundary? = null
        private set

    /**
     * sets the behaviour at the boundaries 0 and 1
     * COMPLETE_MODE_CONTINUOUS_VELOCITY = 0;
     * COMPLETE_MODE_SPRING = 1;
     */
    var autoCompleteMode: Mode? = null

    constructor() {}
    constructor(anchor: String?, side: Side?, dragDirection: Drag?) {
        touchAnchorId = anchor
        touchAnchorSide = side
        this.dragDirection = dragDirection
    }

    enum class Mode {
        VELOCITY, SPRING
    }

    enum class Boundary {
        OVERSHOOT, BOUNCE_START, BOUNCE_END, BOUNCE_BOTH
    }

    enum class Drag {
        UP, DOWN, LEFT, RIGHT, START, END, CLOCKWISE, ANTICLOCKWISE
    }

    enum class Side {
        TOP, LEFT, RIGHT, BOTTOM, MIDDLE, START, END
    }

    enum class TouchUp {
        AUTOCOMPLETE, TO_START, NEVER_COMPLETE_END, TO_END, STOP, DECELERATE, DECELERATE_COMPLETE, NEVER_COMPLETE_START
    }

    /**
     * The id of the view who's movement is matched to your drag
     * If not specified it will map to a linear movement across the width of the motionLayout
     */
    fun setTouchAnchorId(id: String?): OnSwipe {
        touchAnchorId = id
        return this
    }

    /**
     * This side of the view that matches the drag movement.
     * Only meaning full if the object changes size during the movement.
     * (rotation is not considered)
     */
    fun setTouchAnchorSide(side: Side?): OnSwipe {
        touchAnchorSide = side
        return this
    }

    /**
     * The direction of the drag.
     */
    fun setDragDirection(dragDirection: Drag?): OnSwipe {
        this.dragDirection = dragDirection
        return this
    }

    /**
     * The maximum velocity (Change in progress per second) animation can achieve
     */
    fun setMaxVelocity(maxVelocity: Int): OnSwipe {
        this.maxVelocity = maxVelocity.toFloat()
        return this
    }

    /**
     * The maximum acceleration and deceleration of the animation
     * (Change in Change in progress per second)
     * Faster makes the object seem lighter and quicker
     */
    fun setMaxAcceleration(maxAcceleration: Int): OnSwipe {
        this.maxAcceleration = maxAcceleration.toFloat()
        return this
    }

    /**
     * Normally 1 this can be tweaked to make the acceleration faster
     */
    fun setDragScale(dragScale: Int): OnSwipe {
        this.dragScale = dragScale.toFloat()
        return this
    }

    /**
     * This sets the threshold before the animation is kicked off.
     * It is important when have multi state animations the have some play before the
     * System decides which animation to jump on.
     */
    fun setDragThreshold(dragThreshold: Int): OnSwipe {
        this.dragThreshold = dragThreshold.toFloat()
        return this
    }

    /**
     * Configures what happens when the user releases on mouse up.
     * One of: ON_UP_AUTOCOMPLETE, ON_UP_AUTOCOMPLETE_TO_START, ON_UP_AUTOCOMPLETE_TO_END,
     * ON_UP_STOP, ON_UP_DECELERATE, ON_UP_DECELERATE_AND_COMPLETE
     *
     * @param mode default = ON_UP_AUTOCOMPLETE
     */
    fun setOnTouchUp(mode: TouchUp?): OnSwipe {
        onTouchUp = mode
        return this
    }

    /**
     * Only allow touch actions to be initiated within this region
     */
    fun setLimitBoundsTo(id: String?): OnSwipe {
        limitBoundsTo = id
        return this
    }

    /**
     * The view to center the rotation about
     *
     * @return this
     */
    fun setRotateCenter(rotationCenterId: String?): OnSwipe {
        this.rotationCenterId = rotationCenterId
        return this
    }

    /**
     * Set the damping of the spring if using spring.
     * c in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @return this
     */
    fun setSpringDamping(springDamping: Float): OnSwipe {
        this.springDamping = springDamping
        return this
    }

    /**
     * Set the Mass of the spring if using spring.
     * m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @return this
     */
    fun setSpringMass(springMass: Float): OnSwipe {
        this.springMass = springMass
        return this
    }

    /**
     * set the stiffness of the spring if using spring.
     * If this is set the swipe will use a spring return system.
     * If set to NaN it will revert to the norm system.
     * K in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     */
    fun setSpringStiffness(springStiffness: Float): OnSwipe {
        this.springStiffness = springStiffness
        return this
    }

    /**
     * set the threshold for spring motion to stop.
     * This is in change in progress / second
     * If the spring will never go above that threshold again it will stop.
     *
     * @param springStopThreshold when to stop.
     */
    fun setSpringStopThreshold(springStopThreshold: Float): OnSwipe {
        this.springStopThreshold = springStopThreshold
        return this
    }

    /**
     * The behaviour at the boundaries 0 and 1.
     *
     * @param springBoundary behaviour at the boundaries
     */
    fun setSpringBoundary(springBoundary: Boundary?): OnSwipe {
        this.springBoundary = springBoundary
        return this
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder()
        ret.append("OnSwipe:{\n")
        if (touchAnchorId != null) {
            ret.append("anchor:'").append(touchAnchorId).append("',\n")
        }
        if (dragDirection != null) {
            ret.append("direction:'").append(dragDirection.toString().lowercase()).append(
                "',\n"
            )
        }
        if (touchAnchorSide != null) {
            ret.append("side:'").append(touchAnchorSide.toString().lowercase())
                .append("',\n")
        }
        if (!dragScale.isNaN()) {
            ret.append("scale:'").append(dragScale).append("',\n")
        }
        if (!dragThreshold.isNaN()) {
            ret.append("threshold:'").append(dragThreshold).append("',\n")
        }
        if (!maxVelocity.isNaN()) {
            ret.append("maxVelocity:'").append(maxVelocity).append("',\n")
        }
        if (!maxAcceleration.isNaN()) {
            ret.append("maxAccel:'").append(maxAcceleration).append("',\n")
        }
        if (limitBoundsTo != null) {
            ret.append("limitBounds:'").append(limitBoundsTo).append("',\n")
        }
        if (autoCompleteMode != null) {
            ret.append("mode:'").append(autoCompleteMode.toString().lowercase())
                .append("',\n")
        }
        if (onTouchUp != null) {
            ret.append("touchUp:'").append(onTouchUp.toString().lowercase()).append("',\n")
        }
        if (!springMass.isNaN()) {
            ret.append("springMass:'").append(springMass).append("',\n")
        }
        if (!springStiffness.isNaN()) {
            ret.append("springStiffness:'").append(springStiffness).append("',\n")
        }
        if (!springDamping.isNaN()) {
            ret.append("springDamping:'").append(springDamping).append("',\n")
        }
        if (!springStopThreshold.isNaN()) {
            ret.append("stopThreshold:'").append(springStopThreshold).append("',\n")
        }
        if (springBoundary != null) {
            ret.append("springBoundary:'").append(springBoundary).append("',\n")
        }
        if (rotationCenterId != null) {
            ret.append("around:'").append(rotationCenterId).append("',\n")
        }
        ret.append("}\n")
        return ret.toString()
    }

    companion object {
        const val FLAG_DISABLE_POST_SCROLL = 1
        const val FLAG_DISABLE_SCROLL = 2
    }
}
