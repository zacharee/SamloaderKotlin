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

package androidx.constraintlayout.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.constraintlayout.core.state.Dimension
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.Optimizer
import com.soywiz.kds.iterators.fastForEach
import org.jetbrains.skia.Matrix33
import tk.zwander.commonCompose.util.Matrix
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return true }
    return false
}

@PublishedApi
internal class MotionMeasurer : Measurer() {
    private val DEBUG = false
    private var lastProgressInInterpolation = 0f
    val transition = Transition()

    // TODO: Explicitly declare `getDesignInfo` so that studio tooling can identify the method, also
    //  make sure that the constraints/dimensions returned are for the start/current ConstraintSet

    private fun measureConstraintSet(
        optimizationLevel: Int,
        constraintSet: ConstraintSet,
        measurables: List<Measurable>,
        constraints: Constraints
    ) {
        state.reset()
        constraintSet.applyTo(state, measurables)
        state.apply(root)
        root.children.fastForEach { it.isAnimated = true }
        applyRootSize(constraints)
        root.updateHierarchy()

        if (DEBUG) {
            root.debugName = "ConstraintLayout"
            root.children.forEach { child ->
                child.debugName =
                    (childWidget as? Measurable)?.layoutId?.toString() ?: "NOTAG"
            }
        }

        root.children.forEach { child ->
            val measurable = (childWidget as? Measurable)
            val id = measurable?.layoutId ?: measurable?.constraintLayoutId
            child.stringId = id?.toString()
        }

        root.optimizationLevel = optimizationLevel
        // No need to set sizes and size modes as we passed them to the state above.
        root.measure(Optimizer.OPTIMIZATION_NONE, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    fun performInterpolationMeasure(
        constraints: Constraints,
        layoutDirection: LayoutDirection,
        constraintSetStart: ConstraintSet,
        constraintSetEnd: ConstraintSet,
        transition: TransitionImpl?,
        measurables: List<Measurable>,
        optimizationLevel: Int,
        progress: Float,
        measureScope: MeasureScope
    ): IntSize {
        this.density = measureScope
        this.measureScope = measureScope

        val needsRemeasure = needsRemeasure(constraints)

        if (lastProgressInInterpolation != progress ||
            (layoutInformationReceiver?.getForcedWidth() != Int.MIN_VALUE &&
                layoutInformationReceiver?.getForcedHeight() != Int.MIN_VALUE) ||
            needsRemeasure
        ) {
            recalculateInterpolation(
                constraints = constraints,
                layoutDirection = layoutDirection,
                constraintSetStart = constraintSetStart,
                constraintSetEnd = constraintSetEnd,
                transition = transition,
                measurables = measurables,
                optimizationLevel = optimizationLevel,
                progress = progress,
                remeasure = needsRemeasure
            )
        }
        return IntSize(root.width, root.height)
    }

    /**
     * Indicates if the layout requires measuring before computing the interpolation.
     *
     * This might happen if the size of MotionLayout or any of its children changed.
     *
     * MotionLayout size might change from its parent Layout, and in some cases the children size
     * might change (eg: A Text layout has a longer string appended).
     */
    private fun needsRemeasure(constraints: Constraints): Boolean {
        if (this.transition.isEmpty || frameCache.isEmpty()) {
            // Nothing measured (by MotionMeasurer)
            return true
        }

        if ((constraints.hasFixedHeight && !state.sameFixedHeight(constraints.maxHeight)) ||
            (constraints.hasFixedWidth && !state.sameFixedWidth(constraints.maxWidth))
        ) {
            // Layout size changed
            return true
        }

        return root.children.fastAny { child ->
            // Check if measurables have changed their size
            val measurable = (childWidget as? Measurable) ?: return@fastAny false
            val interpolatedFrame = this.transition.getInterpolated(child) ?: return@fastAny false
            val placeable = placeables[measurable] ?: return@fastAny false
            val currentWidth = placeable.width
            val currentHeight = placeable.height

            // Need to recalculate interpolation if the size of any element changed
            return@fastAny currentWidth != interpolatedFrame.width() ||
                currentHeight != interpolatedFrame.height()
        }
    }

    /**
     * Remeasures based on [constraintSetStart] and [constraintSetEnd] if needed.
     *
     * Runs the interpolation for the given [progress].
     *
     * Finally, updates the [Measurable]s dimension if they changed during interpolation.
     */
    private fun recalculateInterpolation(
        constraints: Constraints,
        layoutDirection: LayoutDirection,
        constraintSetStart: ConstraintSet,
        constraintSetEnd: ConstraintSet,
        transition: TransitionImpl?,
        measurables: List<Measurable>,
        optimizationLevel: Int,
        progress: Float,
        remeasure: Boolean
    ) {
        lastProgressInInterpolation = progress
        if (remeasure) {
            this.transition.clear()
            resetMeasureState()
            state.reset()
            // Define the size of the ConstraintLayout.
            state.width(
                if (constraints.hasFixedWidth) {
                    Dimension.createFixed(constraints.maxWidth)
                } else {
                    Dimension.createWrap().min(constraints.minWidth)
                }
            )
            state.height(
                if (constraints.hasFixedHeight) {
                    Dimension.createFixed(constraints.maxHeight)
                } else {
                    Dimension.createWrap().min(constraints.minHeight)
                }
            )
            // Build constraint set and apply it to the state.
            state.rootIncomingConstraints = constraints
            state.layoutDirection = layoutDirection

            measureConstraintSet(
                optimizationLevel, constraintSetStart, measurables, constraints
            )
            this.transition.updateFrom(root, Transition.START)
            measureConstraintSet(
                optimizationLevel, constraintSetEnd, measurables, constraints
            )
            this.transition.updateFrom(root, Transition.END)
            transition?.applyKeyFramesTo(this.transition)
        }

        this.transition.interpolate(root.width, root.height, progress)

        root.children.fastForEach { child ->
            // Update measurables to the interpolated dimensions
            val measurable = (childWidget as? Measurable) ?: return@fastForEach
            val interpolatedFrame = this.transition.getInterpolated(child) ?: return@fastForEach
            val placeable = placeables[measurable]
            val currentWidth = placeable?.width
            val currentHeight = placeable?.height
            if (placeable == null ||
                currentWidth != interpolatedFrame.width() ||
                currentHeight != interpolatedFrame.height()
            ) {
                measurable.measure(
                    Constraints.fixed(interpolatedFrame.width(), interpolatedFrame.height())
                ).also { newPlaceable ->
                    placeables[measurable] = newPlaceable
                }
            }
            frameCache[measurable] = interpolatedFrame
        }

        if (layoutInformationReceiver?.getLayoutInformationMode() == LayoutInfoFlags.BOUNDS) {
            computeLayoutResult()
        }
    }

    private fun encodeKeyFrames(
        json: StringBuilder,
        location: FloatArray,
        types: IntArray,
        progress: IntArray,
        count: Int
    ) {
        if (count == 0) {
            return
        }
        json.append("keyTypes : [")
        for (i in 0 until count) {
            val m = types[i]
            json.append(" $m,")
        }
        json.append("],\n")

        json.append("keyPos : [")
        for (i in 0 until count * 2) {
            val f = location[i]
            json.append(" $f,")
        }
        json.append("],\n ")

        json.append("keyFrames : [")
        for (i in 0 until count) {
            val f = progress[i]
            json.append(" $f,")
        }
        json.append("],\n ")
    }

    fun encodeRoot(json: StringBuilder) {
        json.append("  root: {")
        json.append("interpolated: { left:  0,")
        json.append("  top:  0,")
        json.append("  right:   ${root.width} ,")
        json.append("  bottom:  ${root.height} ,")
        json.append(" } }")
    }

    override fun computeLayoutResult() {
        val json = StringBuilder()
        json.append("{ ")
        encodeRoot(json)
        val mode = IntArray(50)
        val pos = IntArray(50)
        val key = FloatArray(100)

        for (child in root.children) {
            val start = transition.getStart(child.stringId)
            val end = transition.getEnd(child.stringId)
            val interpolated = transition.getInterpolated(child.stringId)
            val path = transition.getPath(child.stringId)
            val count = transition.getKeyFrames(child.stringId, key, mode, pos)

            json.append(" ${child.stringId}: {")
            json.append(" interpolated : ")
            interpolated!!.serialize(json, true)

            json.append(", start : ")
            start!!.serialize(json)

            json.append(", end : ")
            end!!.serialize(json)
            encodeKeyFrames(json, key, mode, pos, count)
            json.append(" path : [")
            for (point in path) {
                json.append(" $point ,")
            }
            json.append(" ] ")
            json.append("}, ")
        }
        json.append(" }")
        layoutInformationReceiver?.setLayoutInformation(json.toString())
    }

    fun DrawScope.drawDebug() {
        var index = 0
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (child in root.children) {
            val startFrame = transition.getStart(child)
            val endFrame = transition.getEnd(child)
            translate(2f, 2f) {
                drawFrameDebug(
                    size.width,
                    size.height,
                    startFrame,
                    endFrame,
                    pathEffect,
                    Color.White
                )
            }
            drawFrameDebug(
                size.width,
                size.height,
                startFrame,
                endFrame,
                pathEffect,
                Color.Blue
            )
            index++
        }
    }

    private fun DrawScope.drawFrameDebug(
        parentWidth: Float,
        parentHeight: Float,
        startFrame: WidgetFrame,
        endFrame: WidgetFrame,
        pathEffect: PathEffect,
        color: Color
    ) {
//        drawFrame(startFrame, pathEffect, color)
//        drawFrame(endFrame, pathEffect, color)
//        val numKeyPositions = transition.getNumberKeyPositions(startFrame)
//        val debugRender = MotionRenderDebug(23f)
//
//        debugRender.draw(
//            drawContext.canvas.nativeCanvas, transition.getMotion(startFrame.widget.stringId),
//            1000, Motion.DRAW_PATH_BASIC,
//            parentWidth.toInt(), parentHeight.toInt()
//        )
//        if (numKeyPositions == 0) {
////            drawLine(
////                start = Offset(startFrame.centerX(), startFrame.centerY()),
////                end = Offset(endFrame.centerX(), endFrame.centerY()),
////                color = color,
////                strokeWidth = 3f,
////                pathEffect = pathEffect
////            )
//        } else {
//            val x = FloatArray(numKeyPositions)
//            val y = FloatArray(numKeyPositions)
//            val pos = FloatArray(numKeyPositions)
//            transition.fillKeyPositions(startFrame, x, y, pos)
//
//            for (i in 0..numKeyPositions - 1) {
//                val keyFrameProgress = pos[i] / 100f
//                val frameWidth =
//                    ((1 - keyFrameProgress) * startFrame.width()) +
//                        (keyFrameProgress * endFrame.width())
//                val frameHeight =
//                    ((1 - keyFrameProgress) * startFrame.height()) +
//                        (keyFrameProgress * endFrame.height())
//                val curX = x[i] * parentWidth + frameWidth / 2f
//                val curY = y[i] * parentHeight + frameHeight / 2f
////                drawLine(
////                    start = Offset(prex, prey),
////                    end = Offset(curX, curY),
////                    color = color,
////                    strokeWidth = 3f,
////                    pathEffect = pathEffect
////                )
//                val path = Path()
//                val pathSize = 20f
//                path.moveTo(curX - pathSize, curY)
//                path.lineTo(curX, curY + pathSize)
//                path.lineTo(curX + pathSize, curY)
//                path.lineTo(curX, curY - pathSize)
//                path.close()
//
//                val stroke = Stroke(width = 3f)
//                drawPath(path, color, 1f, stroke)
//            }
////            drawLine(
////                start = Offset(prex, prey),
////                end = Offset(endFrame.centerX(), endFrame.centerY()),
////                color = color,
////                strokeWidth = 3f,
////                pathEffect = pathEffect
////            )
//        }
    }

    private fun DrawScope.drawFrame(
        frame: WidgetFrame,
        pathEffect: PathEffect,
        color: Color
    ) {
        if (frame.isDefaultTransform) {
            val drawStyle = Stroke(width = 3f, pathEffect = pathEffect)
            drawRect(
                color, Offset(frame.left.toFloat(), frame.top.toFloat()),
                Size(frame.width().toFloat(), frame.height().toFloat()), style = drawStyle
            )
        } else {
            val matrix = Matrix()
            if (!frame.rotationZ.isNaN()) {
                matrix.preRotate(frame.rotationZ, frame.centerX(), frame.centerY())
            }
            val scaleX = if (frame.scaleX.isNaN()) 1f else frame.scaleX
            val scaleY = if (frame.scaleY.isNaN()) 1f else frame.scaleY
            matrix.preScale(
                scaleX,
                scaleY,
                frame.centerX(),
                frame.centerY()
            )
            val points = floatArrayOf(
                frame.left.toFloat(), frame.top.toFloat(),
                frame.right.toFloat(), frame.top.toFloat(),
                frame.right.toFloat(), frame.bottom.toFloat(),
                frame.left.toFloat(), frame.bottom.toFloat()
            )
            matrix.mapPoints(points)
            drawLine(
                start = Offset(points[0], points[1]),
                end = Offset(points[2], points[3]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[2], points[3]),
                end = Offset(points[4], points[5]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[4], points[5]),
                end = Offset(points[6], points[7]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[6], points[7]),
                end = Offset(points[0], points[1]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
        }
    }

    /**
     * Calculates and returns a [Color] value of the custom property given by [name] on the
     * ConstraintWidget corresponding to [id], the value is calculated at the given [progress] value
     * on the current Transition.
     *
     * Returns [Color.Black] if the custom property doesn't exist.
     */
    fun getCustomColor(id: String, name: String, progress: Float): Color {
        if (!transition.contains(id)) {
            return Color.Black
        }
        transition.interpolate(root.width, root.height, progress)

        val interpolatedFrame = transition.getInterpolated(id)
        val color = interpolatedFrame!!.getCustomColor(name)
        return Color(color)
    }

    /**
     * Calculates and returns a [Float] value of the custom property given by [name] on the
     * ConstraintWidget corresponding to [id], the value is calculated at the given [progress] value
     * on the current Transition.
     *
     * Returns `0f` if the custom property doesn't exist.
     */
    fun getCustomFloat(id: String, name: String, progress: Float): Float {
        if (!transition.contains(id)) {
            return 0f
        }
        val startFrame = transition.getStart(id)
        val endFrame = transition.getEnd(id)
        val startFloat = startFrame!!.getCustomFloat(name)
        val endFloat = endFrame!!.getCustomFloat(name)
        return (1f - progress) * startFloat + progress * endFloat
    }

    fun clearConstraintSets() {
        transition.clear()
        frameCache.clear()
    }

    fun initWith(
        start: ConstraintSet,
        end: ConstraintSet,
        transition: TransitionImpl?,
        progress: Float
    ) {
        clearConstraintSets()
        start.applyTo(this.transition, Transition.START)
        end.applyTo(this.transition, Transition.END)
        this.transition.interpolate(0, 0, progress)
        transition?.applyAllTo(this.transition, 0)
    }
}
