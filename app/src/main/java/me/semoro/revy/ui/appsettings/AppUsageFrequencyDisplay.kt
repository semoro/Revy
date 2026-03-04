package me.semoro.revy.ui.appsettings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Composable
fun AppUsageFrequencyDisplay(timestamps: List<Long>) {

    val countH = 13
    val countV = 7
    val now = Clock.System.now()
    val start = now.minus((countH*countV + 1).days)

    val toConsider = timestamps.map { Instant.fromEpochMilliseconds(it) }.filter { it >= start }.sortedBy { it }
    val byDay = toConsider.groupingBy { now.minus(it).inWholeDays }.eachCount()
    val maxFreq = byDay.values.maxOrNull() ?: 0

    Canvas(Modifier.fillMaxWidth().aspectRatio(countH.toFloat() / countV)) {

        var dayIndex = (countH*countV).toLong() - 1


        val pad = 3.dp.toPx()

        val hSize = (size.width - (countH-1)*pad) / countH
        val vSize = (size.height - (countV-1)*pad) / countV

        for (x in 0 until countH) {
            for (y in 0 until countV) {
                // render squares
                val freq = byDay[dayIndex] ?: 0
                val color = freq / maxFreq.toFloat()
                drawRoundRect(color = Color.Green.copy(alpha = color).compositeOver(Color.White),
                    topLeft = Offset(x.toFloat() * (hSize + pad), y.toFloat() * (vSize + pad) ),
                    size = Size(hSize, vSize),
                    cornerRadius = CornerRadius(hSize/4, vSize/4)
                )
                dayIndex--

            }
        }
    }

}

@Preview
@Composable
fun AppUsageFrequencyDisplayPreview() {
    MaterialTheme {
        val now = Clock.System.now()

        val d = generateSequence {
            val dur = (91).days
            now.minus(Random.nextInt(dur.inWholeSeconds.toInt()).seconds).toEpochMilliseconds()
        }.take(100).toList()
        AppUsageFrequencyDisplay(d)
    }
}