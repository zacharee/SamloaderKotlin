/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.constraintlayout.coreimport

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
 * @DoNotShow Utility class to track metrics during the system resolution
 */
class Metrics {
    var measuresWidgetsDuration: Long = 0
    var measuresLayoutDuration: Long = 0
    var measuredWidgets: Long = 0
    var measuredMatchWidgets: Long = 0
    var measures: Long = 0
    var additionalMeasures: Long = 0
    var resolutions: Long = 0
    var tableSizeIncrease: Long = 0
    var minimize: Long = 0
    var constraints: Long = 0
    var simpleconstraints: Long = 0
    var optimize: Long = 0
    var iterations: Long = 0
    var pivots: Long = 0
    var bfs: Long = 0
    var variables: Long = 0
    var errors: Long = 0
    var slackvariables: Long = 0
    var extravariables: Long = 0
    var maxTableSize: Long = 0
    var fullySolved: Long = 0
    var graphOptimizer: Long = 0
    var graphSolved: Long = 0
    var linearSolved: Long = 0
    var resolvedWidgets: Long = 0
    var minimizeGoal: Long = 0
    var maxVariables: Long = 0
    var maxRows: Long = 0
    var centerConnectionResolved: Long = 0
    var matchConnectionResolved: Long = 0
    var chainConnectionResolved: Long = 0
    var barrierConnectionResolved: Long = 0
    var oldresolvedWidgets: Long = 0
    var nonresolvedWidgets: Long = 0
    var problematicLayouts: java.util.ArrayList<String> = java.util.ArrayList<String>()
    var lastTableSize: Long = 0
    var widgets: Long = 0
    var measuresWrap: Long = 0
    var measuresWrapInfeasible: Long = 0
    var infeasibleDetermineGroups: Long = 0
    var determineGroups: Long = 0
    var layouts: Long = 0
    var grouping: Long = 0

    /**
     * @TODO: add description
     */
    override fun toString(): String {
        return """
             
             *** Metrics ***
             measures: $measures
             measuresWrap: $measuresWrap
             measuresWrapInfeasible: $measuresWrapInfeasible
             determineGroups: $determineGroups
             infeasibleDetermineGroups: $infeasibleDetermineGroups
             graphOptimizer: $graphOptimizer
             widgets: $widgets
             graphSolved: $graphSolved
             linearSolved: $linearSolved
             
             """.trimIndent() /*
                + "measures: " + measures + "\n"
                + "additionalMeasures: " + additionalMeasures + "\n"
                + "resolutions passes: " + resolutions + "\n"
                + "table increases: " + tableSizeIncrease + "\n"
                + "maxTableSize: " + maxTableSize + "\n"
                + "maxVariables: " + maxVariables + "\n"
                + "maxRows: " + maxRows + "\n\n"
                + "minimize: " + minimize + "\n"
                + "minimizeGoal: " + minimizeGoal + "\n"
                + "constraints: " + constraints + "\n"
                + "simpleconstraints: " + simpleconstraints + "\n"
                + "optimize: " + optimize + "\n"
                + "iterations: " + iterations + "\n"
                + "pivots: " + pivots + "\n"
                + "bfs: " + bfs + "\n"
                + "variables: " + variables + "\n"
                + "errors: " + errors + "\n"
                + "slackvariables: " + slackvariables + "\n"
                + "extravariables: " + extravariables + "\n"
                + "fullySolved: " + fullySolved + "\n"
                + "graphOptimizer: " + graphOptimizer + "\n"
                + "resolvedWidgets: " + resolvedWidgets + "\n"
                + "oldresolvedWidgets: " + oldresolvedWidgets + "\n"
                + "nonresolvedWidgets: " + nonresolvedWidgets + "\n"
                + "centerConnectionResolved: " + centerConnectionResolved + "\n"
                + "matchConnectionResolved: " + matchConnectionResolved + "\n"
                + "chainConnectionResolved: " + chainConnectionResolved + "\n"
                + "barrierConnectionResolved: " + barrierConnectionResolved + "\n"
                + "problematicsLayouts: " + problematicLayouts + "\n"
                */
    }

    /**
     * @TODO: add description
     */
    fun reset() {
        measures = 0
        widgets = 0
        additionalMeasures = 0
        resolutions = 0
        tableSizeIncrease = 0
        maxTableSize = 0
        lastTableSize = 0
        maxVariables = 0
        maxRows = 0
        minimize = 0
        minimizeGoal = 0
        constraints = 0
        simpleconstraints = 0
        optimize = 0
        iterations = 0
        pivots = 0
        bfs = 0
        variables = 0
        errors = 0
        slackvariables = 0
        extravariables = 0
        fullySolved = 0
        graphOptimizer = 0
        graphSolved = 0
        resolvedWidgets = 0
        oldresolvedWidgets = 0
        nonresolvedWidgets = 0
        centerConnectionResolved = 0
        matchConnectionResolved = 0
        chainConnectionResolved = 0
        barrierConnectionResolved = 0
        problematicLayouts.clear()
    }
}
