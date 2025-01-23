package my.nanihadesuka.compose.controller

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Stable
interface StateController<IndicatorValue> {
    val thumbSizeNormalized: State<Float>
    val thumbOffsetNormalized: State<Float>
    val thumbIsInAction: State<Boolean>
    val isSelected: State<Boolean>

    fun indicatorValue(): IndicatorValue
    fun onDraggableState(deltaPixels: Float, maxLengthPixels: Float)
    fun onDragStarted(offsetPixels: Float, maxLengthPixels: Float)
    fun onDragStopped()
}
