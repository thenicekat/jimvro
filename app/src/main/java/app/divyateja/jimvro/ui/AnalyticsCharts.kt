package app.divyateja.jimvro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divyateja.jimvro.ui.theme.Clay

data class ChartPoint(val label: String, val value: Double)

@Composable
fun LineTrendChart(points: List<ChartPoint>, modifier: Modifier = Modifier, color: Color = Clay) {
    if (points.size < 2) return
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val text = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(118.dp)) {
            val min = points.minOf { it.value }
            val max = points.maxOf { it.value }
            val range = (max - min).takeIf { it > 0.0 } ?: 1.0
            val top = 10.dp.toPx()
            val bottom = size.height - 10.dp.toPx()
            drawLine(grid, Offset(0f, bottom), Offset(size.width, bottom), 1.dp.toPx())
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = index * size.width / (points.size - 1)
                val y = bottom - ((point.value - min) / range).toFloat() * (bottom - top)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            points.forEachIndexed { index, point ->
                val x = index * size.width / (points.size - 1)
                val y = bottom - ((point.value - min) / range).toFloat() * (bottom - top)
                drawCircle(color, 3.dp.toPx(), Offset(x, y))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(points.first().label, fontSize = 10.sp, color = text)
            Text(points.last().label, fontSize = 10.sp, color = text)
        }
    }
}

@Composable
fun VolumeBarChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val max = points.maxOf { it.value }.coerceAtLeast(1.0)
    val empty = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            val gap = 8.dp.toPx()
            val width = (size.width - gap * (points.size - 1)) / points.size
            points.forEachIndexed { index, point ->
                val rawHeight = (point.value / max).toFloat() * size.height
                val height = if (point.value > 0) rawHeight.coerceAtLeast(6.dp.toPx()) else 2.dp.toPx()
                drawRoundRect(
                    color = if (point.value > 0) Clay else empty,
                    topLeft = Offset(index * (width + gap), size.height - height),
                    size = Size(width, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { Text(it.label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
