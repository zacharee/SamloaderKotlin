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
package androidx.constraintlayout.core.widgets

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.SolverVariable

/**
 * Guideline
 */
class Guideline : ConstraintWidget() {
    var relativePercent = -1f
        protected set
    var relativeBegin = -1
        protected set
    var relativeEnd = -1
        protected set
    protected var mGuidelineUseRtl = true
    var anchor = mTop
        private set
    private var mOrientation = HORIZONTAL
    /**
     * Get the Minimum Position
     * @return the Minimum Position
     */
    /**
     * set the minimum position
     * @param minimum
     */
    var minimumPosition = 0
    override var isResolvedVertically = false
        private set
        get() = field
    init {
        mAnchors.clear()
        mAnchors.add(anchor)
        val count = mListAnchors.size
        for (i in 0 until count) {
            mListAnchors[i] = anchor
        }
    }

    override fun copy(src: ConstraintWidget, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        super.copy(src, map)
        val srcGuideline = src as Guideline
        relativePercent = srcGuideline.relativePercent
        relativeBegin = srcGuideline.relativeBegin
        relativeEnd = srcGuideline.relativeEnd
        mGuidelineUseRtl = srcGuideline.mGuidelineUseRtl
        orientation = srcGuideline.mOrientation
    }

    override fun allowedInBarrier(): Boolean {
        return true
    }

    /**
     * @TODO: add description
     */
    val relativeBehaviour: Int
        get() {
            if (relativePercent != -1f) {
                return RELATIVE_PERCENT
            }
            if (relativeBegin != -1) {
                return RELATIVE_BEGIN
            }
            return if (relativeEnd != -1) {
                RELATIVE_END
            } else RELATIVE_UNKNOWN
        }

    /**
     * Specify the xml type for the container
     */
    override var type: String?
        get() = "Guideline"
        set(type) {
            super.type = type
        }
    /**
     * get the orientation VERTICAL or HORIZONTAL
     * @return orientation
     */
    /**
     * @TODO: add description
     */
    var orientation: Int
        get() = mOrientation
        set(orientation) {
            if (mOrientation == orientation) {
                return
            }
            mOrientation = orientation
            mAnchors.clear()
            if (mOrientation == VERTICAL) {
                anchor = mLeft!!
            } else {
                anchor = mTop
            }
            mAnchors.add(anchor)
            val count = mListAnchors.size
            for (i in 0 until count) {
                mListAnchors[i] = anchor
            }
        }

    override fun getAnchor(anchorType: ConstraintAnchor.Type?): ConstraintAnchor? {
        when (anchorType) {
            ConstraintAnchor.Type.LEFT, ConstraintAnchor.Type.RIGHT -> {
                if (mOrientation == VERTICAL) {
                    return anchor
                }
            }

            ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.BOTTOM -> {
                if (mOrientation == HORIZONTAL) {
                    return anchor
                }
            }

            ConstraintAnchor.Type.BASELINE, ConstraintAnchor.Type.CENTER, ConstraintAnchor.Type.CENTER_X, ConstraintAnchor.Type.CENTER_Y, ConstraintAnchor.Type.NONE -> return null

            else -> return null
        }

        return null
    }

    /**
     * @TODO: add description
     */
    fun setGuidePercent(value: Int) {
        setGuidePercent(value / 100f)
    }

    /**
     * @TODO: add description
     */
    fun setGuidePercent(value: Float) {
        if (value > -1) {
            relativePercent = value
            relativeBegin = -1
            relativeEnd = -1
        }
    }

    /**
     * @TODO: add description
     */
    fun setGuideBegin(value: Int) {
        if (value > -1) {
            relativePercent = -1f
            relativeBegin = value
            relativeEnd = -1
        }
    }

    /**
     * @TODO: add description
     */
    fun setGuideEnd(value: Int) {
        if (value > -1) {
            relativePercent = -1f
            relativeBegin = -1
            relativeEnd = value
        }
    }

    /**
     * @TODO: add description
     */
    fun setFinalValue(position: Int) {
        if (LinearSystem.Companion.FULL_DEBUG) {
            println(
                "*** SET FINAL GUIDELINE VALUE "
                        + position + " FOR " + debugName
            )
        }
        anchor.finalValue = position
        isResolvedVertically = true
    }

    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        if (LinearSystem.Companion.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding $debugName to the solver")
            println("----------------------------------------------\n")
        }
        val parent = parent as? ConstraintWidgetContainer ?: return
        var begin: ConstraintAnchor? = parent.getAnchor(ConstraintAnchor.Type.LEFT)
        var end: ConstraintAnchor? = parent.getAnchor(ConstraintAnchor.Type.RIGHT)
        var parentWrapContent =
            parent.mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_HORIZONTAL) == DimensionBehaviour.WRAP_CONTENT
        if (mOrientation == HORIZONTAL) {
            begin = parent.getAnchor(ConstraintAnchor.Type.TOP)
            end = parent.getAnchor(ConstraintAnchor.Type.BOTTOM)
            parentWrapContent =
                parent.mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_VERTICAL) == DimensionBehaviour.WRAP_CONTENT
        }
        if (isResolvedVertically && anchor.hasFinalValue()) {
            val guide = system.createObjectVariable(anchor)
            if (LinearSystem.Companion.FULL_DEBUG) {
                println(
                    "*** SET FINAL POSITION FOR GUIDELINE "
                            + debugName + " TO " + anchor.finalValue
                )
            }
            system.addEquality(guide!!, anchor.finalValue)
            if (relativeBegin != -1) {
                if (parentWrapContent) {
                    system.addGreaterThan(
                        system.createObjectVariable(end)!!, guide!!,
                        0, SolverVariable.Companion.STRENGTH_EQUALITY
                    )
                }
            } else if (relativeEnd != -1) {
                if (parentWrapContent) {
                    val parentRight = system.createObjectVariable(end)
                    system.addGreaterThan(
                        guide!!, system.createObjectVariable(begin)!!,
                        0, SolverVariable.Companion.STRENGTH_EQUALITY
                    )
                    system.addGreaterThan(parentRight!!, guide, 0, SolverVariable.Companion.STRENGTH_EQUALITY)
                }
            }
            isResolvedVertically = false
            return
        }
        if (relativeBegin != -1) {
            val guide = system.createObjectVariable(anchor)
            val parentLeft = system.createObjectVariable(begin)
            system.addEquality(guide!!, parentLeft!!, relativeBegin, SolverVariable.Companion.STRENGTH_FIXED)
            if (parentWrapContent) {
                system.addGreaterThan(
                    system.createObjectVariable(end)!!,
                    guide!!, 0, SolverVariable.Companion.STRENGTH_EQUALITY
                )
            }
        } else if (relativeEnd != -1) {
            val guide = system.createObjectVariable(anchor)
            val parentRight = system.createObjectVariable(end)
            system.addEquality(guide!!, parentRight!!, -relativeEnd, SolverVariable.Companion.STRENGTH_FIXED)
            if (parentWrapContent) {
                system.addGreaterThan(
                    guide, system.createObjectVariable(begin)!!,
                    0, SolverVariable.Companion.STRENGTH_EQUALITY
                )
                system.addGreaterThan(parentRight, guide, 0, SolverVariable.Companion.STRENGTH_EQUALITY)
            }
        } else if (relativePercent != -1f) {
            val guide = system.createObjectVariable(anchor)
            val parentRight = system.createObjectVariable(end)
            system.addConstraint(
                LinearSystem.Companion.createRowDimensionPercent(
                    system, guide!!, parentRight!!,
                    relativePercent
                )
            )
        }
    }

    override fun updateFromSolver(system: LinearSystem, optimize: Boolean) {
        if (parent == null) {
            return
        }
        val value = system.getObjectVariableValue(anchor)
        if (mOrientation == VERTICAL) {
            x = value
            y = 0
            height = parent!!.height
            width = 0
        } else {
            x = 0
            y = value
            width = parent!!.width
            height = 0
        }
    }

    fun inferRelativePercentPosition() {
        var percent: Float = x / parent!!.width.toFloat()
        if (mOrientation == HORIZONTAL) {
            percent = y / parent!!.height.toFloat()
        }
        setGuidePercent(percent)
    }

    fun inferRelativeBeginPosition() {
        var position = x
        if (mOrientation == HORIZONTAL) {
            position = y
        }
        setGuideBegin(position)
    }

    fun inferRelativeEndPosition() {
        var position: Int = parent!!.width - x
        if (mOrientation == HORIZONTAL) {
            position = parent!!.height - y
        }
        setGuideEnd(position)
    }

    /**
     * @TODO: add description
     */
    fun cyclePosition() {
        if (relativeBegin != -1) {
            // cycle to percent-based position
            inferRelativePercentPosition()
        } else if (relativePercent != -1f) {
            // cycle to end-based position
            inferRelativeEndPosition()
        } else if (relativeEnd != -1) {
            // cycle to begin-based position
            inferRelativeBeginPosition()
        }
    }

    val isPercent: Boolean
        get() = relativePercent != -1f && relativeBegin == -1 && relativeEnd == -1

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val RELATIVE_PERCENT = 0
        const val RELATIVE_BEGIN = 1
        const val RELATIVE_END = 2
        const val RELATIVE_UNKNOWN = -1
    }
}
