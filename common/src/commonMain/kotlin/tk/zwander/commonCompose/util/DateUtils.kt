package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.format.MonthNames
import tk.zwander.samloaderkotlin.resources.MR

@Composable
fun monthNames(): MonthNames {
    val resources = listOf(
        MR.strings.january_short,
        MR.strings.february_short,
        MR.strings.march_short,
        MR.strings.april_short,
        MR.strings.may_short,
        MR.strings.june_short,
        MR.strings.july_short,
        MR.strings.august_short,
        MR.strings.september_short,
        MR.strings.october_short,
        MR.strings.november_short,
        MR.strings.december_short,
    )

    return MonthNames(resources.map { stringResource(it) })
}
