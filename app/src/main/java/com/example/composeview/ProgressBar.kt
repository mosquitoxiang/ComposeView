package com.example.composeview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlin.random.nextInt

@Composable
fun Progress() {
    val sweepState = remember {
        mutableStateOf(0f)
    }
    val max = 100f
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        ProgressBarView(sweepState)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            sweepState.value = Random.nextInt(1 until 99) / max
        }) {
            Text(text = "按钮")
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun ProgressBarView(sweepState: MutableState<Float>) {
    val animAngle = animateFloatAsState(
        targetValue = sweepState.value * 360,
        animationSpec = tween(1000)
    )
    val animPercent = animateIntAsState(
        targetValue = (sweepState.value * 100).toInt(),
        animationSpec = tween(1000)
    )
    val textPercent = "${animPercent.value}%"
    val textPercentLayResult = rememberTextMeasurer().measure(
        text = AnnotatedString(textPercent),
        style = TextStyle(
            color = Color(96, 98, 172),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    )
    val textDesc = "出勤率"
    val textDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString(textDesc),
        TextStyle(color = Color(178, 193, 209))
    )
    Canvas(modifier = Modifier.size(300.dp), onDraw = {
        val innerStrokeWidth = 10.dp.toPx()
        val radius = 120.dp.toPx()
        val outStrokeWidth = 17.dp.toPx()
        val canvasWidth = size.width
        val canvasHeight = size.height
        //内部圆
        drawCircle(
            Color(222, 228, 246),
            radius = radius,
            center = Offset(canvasWidth / 2, canvasHeight / 2),
            style = Stroke(innerStrokeWidth)
        )
        //圆弧进度
        drawArc(
            Color(46, 120, 249),
            startAngle = -90f,
            sweepAngle = animAngle.value,
            useCenter = false,
            size = Size(radius * 2, radius * 2),
            style = Stroke(outStrokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius)
        )
        val textPercentWidth = textPercentLayResult.size.width
        val textPercentHeight = textPercentLayResult.size.height
        //百分比文字
        drawText(
            textLayoutResult = textPercentLayResult,
            topLeft = Offset(
                canvasWidth / 2 - textPercentWidth / 2,
                canvasHeight / 2 - textPercentHeight
            ),
        )

        val textDescWidth = textDescLayoutResult.size.width
        val textDescHeight = textDescLayoutResult.size.height //用不着
        //出勤率
        drawText(
            textLayoutResult = textDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2
            ),
        )
    })

}