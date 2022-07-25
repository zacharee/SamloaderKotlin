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
package androidx.constraintlayout.core

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
    var problematicLayouts: ArrayList<String> = ArrayList<String>()
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
