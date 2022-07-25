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
package androidx.constraintlayout.core.stateimport

androidx.constraintlayout.core.dsl.OnSwipe.Drag
import androidx.constraintlayout.core.dsl.OnSwipe.TouchUp
import androidx.constraintlayout.core.dsl.OnSwipe.Boundary
import androidx.constraintlayout.core.dsl.KeyAttribute
import androidx.constraintlayout.core.dsl.KeyAttributes
import androidx.constraintlayout.core.dsl.Constraint.VSide
import androidx.constraintlayout.core.dsl.Constraint.HSide
import androidx.constraintlayout.core.dsl.Constraint.HAnchor
import androidx.constraintlayout.core.dsl.Constraint.VAnchor
import androidx.constraintlayout.core.dsl.Constraint.ChainMode
import androidx.constraintlayout.core.dsl.Constraint.Behaviour
import androidx.constraintlayout.core.dsl.KeyFrames
import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.state.HelperReference
import androidx.constraintlayout.core.state.helpers.ChainReference
import androidx.constraintlayout.core.widgets.Barrier
import androidx.constraintlayout.core.state.ConstraintReference
import androidx.constraintlayout.core.widgets.HelperWidget
import androidx.constraintlayout.core.widgets.Guideline
import androidx.constraintlayout.core.state.CorePixelDp
import androidx.constraintlayout.core.state.helpers.AlignHorizontallyReference
import androidx.constraintlayout.core.state.helpers.AlignVerticallyReference
import androidx.constraintlayout.core.state.helpers.BarrierReference
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.core.state.RegistryCallback
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.core.motion.utils.TypedValues
import androidx.constraintlayout.core.state.Transition.WidgetState
import androidx.constraintlayout.core.motion.utils.TypedBundle
import androidx.constraintlayout.core.motion.utils.StopEngine
import androidx.constraintlayout.core.motion.utils.StopLogicEngine
import androidx.constraintlayout.core.motion.utils.SpringStopEngine
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.motion.utils.TypedValues.PositionType
import androidx.constraintlayout.core.motion.MotionWidget
import androidx.constraintlayout.core.motion.key.MotionKeyPosition
import androidx.constraintlayout.core.motion.key.MotionKeyAttributes
import androidx.constraintlayout.core.motion.key.MotionKeyCycle
import androidx.constraintlayout.core.motion.CustomVariable
import androidx.constraintlayout.core.parser.CLParsingException
import androidx.constraintlayout.core.parser.CLElement
import androidx.constraintlayout.core.parser.CLObject
import androidx.constraintlayout.core.parser.CLKey
import androidx.constraintlayout.core.parser.CLNumber
import androidx.constraintlayout.core.widgets.ConstraintAnchor
import androidx.constraintlayout.core.motion.CustomAttribute
import androidx.constraintlayout.core.parser.CLContainer
import androidx.constraintlayout.core.state.TransitionParser
import androidx.constraintlayout.core.parser.CLArray
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.motion.utils.TypedValues.CycleType
import androidx.constraintlayout.core.state.ConstraintReference.IncorrectConstraintException
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLString
import androidx.constraintlayout.core.state.ConstraintSetParser
import androidx.constraintlayout.core.state.CoreMotionScene
import androidx.constraintlayout.core.state.ConstraintSetParser.LayoutVariables
import androidx.constraintlayout.core.motion.utils.TypedValues.MotionType
import androidx.constraintlayout.core.state.ConstraintSetParser.GeneratedValue
import androidx.constraintlayout.core.state.ConstraintSetParser.OverrideValue
import androidx.constraintlayout.core.state.ConstraintSetParser.FiniteGenerator
import androidx.constraintlayout.core.utils.GridEngine
import androidx.constraintlayout.core.motion.key.MotionKey
import androidx.constraintlayout.core.motion.utils.SplineSet
import androidx.constraintlayout.core.motion.utils.KeyCycleOscillator
import androidx.constraintlayout.core.motion.utils.Oscillator
import androidx.constraintlayout.core.motion.utils.FloatRect
import androidx.constraintlayout.core.motion.key.MotionKeyTrigger
import androidx.constraintlayout.core.motion.utils.TypedValues.TriggerType
import androidx.constraintlayout.core.motion.key.MotionKeyTimeCycle
import androidx.constraintlayout.core.motion.utils.TimeCycleSplineSet
import androidx.constraintlayout.core.motion.utils.TimeCycleSplineSet.CustomVarSet
import androidx.constraintlayout.core.motion.utils.SplineSet.CustomSpline
import androidx.constraintlayout.core.motion.parse.KeyParser.Ids
import androidx.constraintlayout.core.motion.utils.Utils.DebugHandle
import androidx.constraintlayout.core.motion.utils.Easing.CubicEasing
import androidx.constraintlayout.core.motion.utils.StepCurve
import androidx.constraintlayout.core.motion.utils.Schlick
import androidx.constraintlayout.core.motion.utils.CurveFit
import androidx.constraintlayout.core.motion.utils.MonotonicCurveFit
import androidx.constraintlayout.core.motion.utils.LinearCurveFit
import androidx.constraintlayout.core.motion.utils.ArcCurveFit
import androidx.constraintlayout.core.motion.utils.KeyFrameArray.CustomArray
import androidx.constraintlayout.core.motion.utils.KeyFrameArray.CustomVar
import androidx.constraintlayout.core.motion.utils.HyperSpline.Cubic
import androidx.constraintlayout.core.motion.utils.HyperSpline
import androidx.constraintlayout.core.motion.utils.KeyCycleOscillator.CycleOscillator
import androidx.constraintlayout.core.motion.utils.KeyCycleOscillator.WavePoint
import androidx.constraintlayout.core.motion.utils.KeyCycleOscillator.PathRotateSet
import androidx.constraintlayout.core.motion.MotionPaths
import androidx.constraintlayout.core.motion.MotionConstrainedPoint
import androidx.constraintlayout.core.motion.utils.DifferentialInterpolator
import androidx.constraintlayout.core.motion.utils.ViewState
import androidx.constraintlayout.core.motion.key.MotionConstraintSet
import androidx.constraintlayout.core.motion.utils.VelocityMatrix
import androidx.constraintlayout.core.parser.CLToken
import androidx.constraintlayout.core.parser.CLObject.CLObjectIterator
import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.widgets.analyzer.Direct
import androidx.constraintlayout.core.widgets.ChainHead
import androidx.constraintlayout.core.widgets.analyzer.WidgetRun
import androidx.constraintlayout.core.widgets.analyzer.WidgetGroup
import androidx.constraintlayout.core.widgets.analyzer.RunGroup
import androidx.constraintlayout.core.widgets.analyzer.HelperReferences
import androidx.constraintlayout.core.widgets.analyzer.ChainRun
import androidx.constraintlayout.core.widgets.analyzer.HorizontalWidgetRun
import androidx.constraintlayout.core.widgets.analyzer.VerticalWidgetRun
import androidx.constraintlayout.core.widgets.analyzer.DimensionDependency
import androidx.constraintlayout.core.widgets.VirtualLayout
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure
import androidx.constraintlayout.core.widgets.analyzer.BaselineDimensionDependency
import androidx.constraintlayout.core.widgets.Flow.WidgetsList
import androidx.constraintlayout.core.SolverVariable
import androidx.constraintlayout.core.ArrayRow
import androidx.constraintlayout.core.widgets.WidgetContainer
import androidx.constraintlayout.core.ArrayRow.ArrayRowVariables
import androidx.constraintlayout.core.ArrayLinkedVariables
import androidx.constraintlayout.core.SolverVariableValues
import androidx.constraintlayout.core.LinearSystem.ValuesRow
import androidx.constraintlayout.core.PriorityGoalRow
import androidx.constraintlayout.core.PriorityGoalRow.GoalVariableAccessor

/**
 * This defines the interface to motionScene functionality
 */
interface CoreMotionScene {
    /**
     * set the Transitions string onto the MotionScene
     *
     * @param elementName the name of the element
     */
    fun setTransitionContent(elementName: String?, toJSON: String?)

    /**
     * Get the ConstraintSet as a string
     */
    fun getConstraintSet(ext: String?): String?

    /**
     * set the constraintSet json string
     *
     * @param csName the name of the constraint set
     * @param toJSON the json string of the constraintset
     */
    fun setConstraintSetContent(csName: String?, toJSON: String?)

    /**
     * set the debug name for remote access
     *
     * @param name name to call this motion scene
     */
    fun setDebugName(name: String?)

    /**
     * get a transition give the name
     *
     * @param str the name of the transition
     * @return the json of the transition
     */
    fun getTransition(str: String?): String?

    /**
     * get a constraintset
     *
     * @param index of the constraintset
     */
    fun getConstraintSet(index: Int): String?
}
