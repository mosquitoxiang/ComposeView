package com.example.composeview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

data class DragStatus(
    //是否正在拖动
    val onDrag: Boolean = false,
    //是否超出最小的距离
    val onDragExceedDistance: Boolean = false,
    //超出距离，气泡消失
    val onDragComplete: Boolean = false
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun MessageBubble() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var controlX by remember { mutableStateOf(0f) }
    var controlY by remember { mutableStateOf(0f) }
    var sin by remember { mutableStateOf(0f) }
    var cos by remember { mutableStateOf(0f) }
    var circleStartX by remember { mutableStateOf(0f) }
    var circleStartY by remember { mutableStateOf(0f) }
    var bubbleEndX by remember { mutableStateOf(0f) }
    var bubbleEndY by remember { mutableStateOf(0f) }
    var bubbleStartX by remember { mutableStateOf(0f) }
    var bubbleStartY by remember { mutableStateOf(0f) }
    var circleEndX by remember { mutableStateOf(0f) }
    var circleEndY by remember { mutableStateOf(0f) }
    var dragStatus by remember {
        mutableStateOf(DragStatus())
    }
    var circleRadius by remember { mutableStateOf(MainActivity.RADIUS) }
    val bubbleRadius by remember { mutableStateOf(MainActivity.RADIUS) }
    var minDragDistance by remember { mutableStateOf(0f) }
    var previousDistance by remember { mutableStateOf(0f) }
    var textWidth by remember { mutableStateOf(0f) }
    var textHeight by remember { mutableStateOf(0f) }
    val textLayoutResult = rememberTextMeasurer().measure(AnnotatedString("66"))
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(modifier = Modifier.padding(top = 100.dp), onClick = {
            circleRadius = MainActivity.RADIUS
            previousDistance = 0f
            minDragDistance = 0f
            offsetX = 0f
            offsetY = 0f
            dragStatus = dragStatus.copy(
                onDrag = false,
                onDragExceedDistance = false,
                onDragComplete = false
            )
        }) {
            Text(text = "reset")
        }
        Canvas(modifier = Modifier, onDraw = {
            if (dragStatus.onDrag && !dragStatus.onDragExceedDistance) {
                //原点的圆
                drawCircle(Color.Red, circleRadius)
            }
        })
        val path = Path()
        Canvas(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            minDragDistance = sqrt(
                                (offsetX.toDouble().pow(2.0) + offsetY.toDouble()
                                    .pow(2.0)).toFloat()
                            )
                            //这里为什么除以8？除以几都可以看你心情，主要是不让原点的圆因为短距离的拖动而缩小的太快
                            if (minDragDistance > previousDistance) {
                                circleRadius -= ((minDragDistance - previousDistance) / 8f)
                            } else if (minDragDistance < previousDistance) {
                                circleRadius += ((previousDistance - minDragDistance) / 8f)
                            }
                            previousDistance = minDragDistance
                            dragStatus = dragStatus.copy(onDrag = true)
                            if (minDragDistance > MainActivity.MIN_DRAG_DISTANCE) {
                                dragStatus = dragStatus.copy(onDragExceedDistance = true)
                            }
                        },
                        onDragEnd = {
                            dragStatus = dragStatus.copy(onDrag = false)
                            if (minDragDistance < MainActivity.MIN_DRAG_DISTANCE) {
                                //拖动距离小于设置的距离，让可拖动的小圆回到原点
                                offsetX = 0f
                                offsetY = 0f
                                minDragDistance = 0f
                                circleRadius = MainActivity.RADIUS
                                dragStatus = dragStatus.copy(
                                    onDrag = false,
                                    onDragExceedDistance = false
                                )
                            } else {
                                dragStatus = dragStatus.copy(
                                    onDragExceedDistance = true,
                                    onDragComplete = true
                                )
                            }
                        }
                    )
                }, onDraw = {
                //如果正在拖动小圆，并且没有超过一定距离，才画连接的路径
                if (dragStatus.onDrag && !dragStatus.onDragExceedDistance) {
                    controlX = (offsetX + 0) / 2
                    controlY = (offsetY + 0) / 2
                    sin = offsetY / minDragDistance
                    cos = offsetX / minDragDistance
                    circleStartX = 0f - circleRadius * sin
                    circleStartY = 0f + circleRadius * cos
                    bubbleEndX = offsetX - bubbleRadius * sin
                    bubbleEndY = offsetY + bubbleRadius * cos
                    bubbleStartX = offsetX + bubbleRadius * sin
                    bubbleStartY = offsetY - bubbleRadius * cos
                    circleEndX = 0f + circleRadius * sin
                    circleEndY = 0f - circleRadius * cos
                    println(
                        "---------------------------\n" +
                                "minDragDistance: $minDragDistance\n" +
                                "offsetX: $offsetX, offsetY: $offsetY\n" +
                                "($circleStartX, $circleStartY)\n" +
                                "($controlX, $controlY, $bubbleEndX, $bubbleEndY)\n" +
                                "($bubbleStartX, $bubbleStartY)\n" +
                                "($controlX, $controlX, $circleEndX, $circleEndY)\n" +
                                "---------------------------\n"
                    )
                    path.reset()
                    path.moveTo(circleStartX, circleStartY)
                    path.quadraticBezierTo(controlX, controlY, bubbleEndX, bubbleEndY)
                    path.lineTo(bubbleStartX, bubbleStartY)
                    path.quadraticBezierTo(controlX, controlY, circleEndX, circleEndY)
                    drawPath(path, Color.Red)
                }
                //画可以拖动的圆
                if (!dragStatus.onDragComplete) {
                    drawCircle(Color.Red, bubbleRadius, Offset(offsetX, offsetY))
                    textWidth = textLayoutResult.size.width.toFloat()
                    textHeight = textLayoutResult.size.width.toFloat()
                    drawText(
                        textLayoutResult, Color.White, topLeft = Offset(
                            offsetX - textWidth / 2,
                            offsetY - textHeight / 2
                        )
                    )
                }
            })
    }
}