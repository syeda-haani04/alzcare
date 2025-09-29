package com.risc.alzcare.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

private data class CircleParams(
    val id: Int,
    val center: Offset,
    val radius: Float,
    val baseColorForGradient: Color,
    val edgeAlpha: Float
)

@Composable
fun CirclesBackground(
    modifier: Modifier = Modifier,
    circleCount: Int = 7,
    minRadiusDp: Dp = 100.dp,
    maxRadiusDp: Dp = 200.dp,
    centerCircleColor: Color = Color.Black.copy(alpha = 0.2f),
    edgeCircleAlpha: Float = 0.0f,
    overlapReductionAttempts: Int = 5,
    minDistanceFactor: Float = 0.65f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var circles by remember { mutableStateOf<List<CircleParams>>(emptyList()) }

    val minRadiusPx = with(density) { minRadiusDp.toPx() }
    val maxRadiusPx = with(density) { maxRadiusDp.toPx() }

    LaunchedEffect(
        canvasSize,
        circleCount,
        minRadiusPx,
        maxRadiusPx,
        centerCircleColor,
        edgeCircleAlpha,
        overlapReductionAttempts,
        minDistanceFactor
    ) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            val placedCircles = mutableListOf<CircleParams>()
            for (i in 0 until circleCount) {
                var attempt = 0
                var currentCircleParams: CircleParams? = null

                while (attempt < overlapReductionAttempts) {
                    val randomRadius = Random.nextFloat() * (maxRadiusPx - minRadiusPx) + minRadiusPx

                    val randomX = if (Random.nextBoolean()) {
                        (Random.nextFloat() * randomRadius) - (randomRadius / 2f)
                    } else {
                        canvasSize.width + ((Random.nextFloat() * randomRadius) - (randomRadius / 2f))
                    }
                    val randomY = Random.nextFloat() * canvasSize.height
                    val proposedCenter = Offset(randomX, randomY)

                    var isOverlappingHeavily = false
                    if (placedCircles.isNotEmpty()) {
                        for (placedCircle in placedCircles) {
                            val distance = sqrt(
                                (proposedCenter.x - placedCircle.center.x).pow(2) +
                                        (proposedCenter.y - placedCircle.center.y).pow(2)
                            )
                            val minAllowedDistance = (randomRadius + placedCircle.radius) * minDistanceFactor
                            if (distance < minAllowedDistance) {
                                isOverlappingHeavily = true
                                break
                            }
                        }
                    }

                    if (!isOverlappingHeavily) {
                        currentCircleParams = CircleParams(
                            id = i,
                            center = proposedCenter,
                            radius = randomRadius,
                            baseColorForGradient = centerCircleColor,
                            edgeAlpha = edgeCircleAlpha
                        )
                        break
                    }
                    attempt++
                }

                if (currentCircleParams == null) {
                    val randomRadius = Random.nextFloat() * (maxRadiusPx - minRadiusPx) + minRadiusPx
                    val randomX = if (Random.nextBoolean()) {
                        (Random.nextFloat() * randomRadius) - (randomRadius / 2f)
                    } else {
                        canvasSize.width + ((Random.nextFloat() * randomRadius) - (randomRadius / 2f))
                    }
                    val randomY = Random.nextFloat() * canvasSize.height
                    currentCircleParams = CircleParams(
                        id = i,
                        center = Offset(randomX, randomY),
                        radius = randomRadius,
                        baseColorForGradient = centerCircleColor,
                        edgeAlpha = edgeCircleAlpha
                    )
                }
                placedCircles.add(currentCircleParams)
            }
            circles = placedCircles
        } else {
            circles = emptyList()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (canvasSize != this.size.toIntSize()) {
                canvasSize = this.size.toIntSize()
            }
            if (circles.isNotEmpty() && this.size.width > 0 && this.size.height > 0) {
                circles.forEach { circle ->
                    val gradientBrush = Brush.radialGradient(
                        colors = listOf(
                            circle.baseColorForGradient,
                            circle.baseColorForGradient.copy(alpha = circle.edgeAlpha)
                        ),
                        center = circle.center,
                        radius = circle.radius
                    )
                    drawCircle(
                        brush = gradientBrush,
                        radius = circle.radius,
                        center = circle.center
                    )
                }
            }
        }
        this.content()
    }
}
