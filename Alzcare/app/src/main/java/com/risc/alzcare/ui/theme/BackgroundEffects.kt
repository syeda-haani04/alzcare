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
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

private data class CircleParams(
    val id: Int,
    val initialCenter: Offset,
    val radius: Float,
    val baseColorForGradient: Color,
    val edgeAlpha: Float,
    val parallaxFactorX: Float,
    val parallaxFactorY: Float,
    val layerDepth: Int
)

data class ParticleLayerConfig(
    val layerDepth: Int,
    val particleCount: Int,
    val minRadiusDp: Dp,
    val maxRadiusDp: Dp,
    val centerColor: Color,
    val edgeAlpha: Float,
    val parallaxXMultiplier: Float,
    val parallaxYMultiplier: Float,
    val maxParallaxShiftDp: Dp,
    val numberOfClusters: Int,
    val clusterSpreadFactor: Float,
    val minDistanceFactor: Float
)

@Composable
fun CirclesBackground(
    modifier: Modifier = Modifier,
    layerConfigs: List<ParticleLayerConfig>,
    questionBasedOffset: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var circles by remember { mutableStateOf<List<CircleParams>>(emptyList()) }

    val processedLayerConfigs = remember(layerConfigs, density) {
        layerConfigs.map { config ->
            object {
                val config = config
                val minRadiusPx = with(density) { config.minRadiusDp.toPx() }
                val maxRadiusPx = with(density) { config.maxRadiusDp.toPx() }
                val maxParallaxShiftPx = with(density) { config.maxParallaxShiftDp.toPx() }
            }
        }
    }

    LaunchedEffect(canvasSize, processedLayerConfigs) {
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            circles = emptyList()
            return@LaunchedEffect
        }

        val allPlacedCircles = mutableListOf<CircleParams>()
        val random = Random(System.currentTimeMillis())
        var uniqueParticleId = 0

        processedLayerConfigs.sortedBy { it.config.layerDepth }.forEach { layerInfo ->
            val config = layerInfo.config
            if (config.particleCount == 0) return@forEach

            val clusterSpreadPxWidth = canvasSize.width * config.clusterSpreadFactor
            val clusterSpreadPxHeight = canvasSize.height * config.clusterSpreadFactor

            val clusterCenters = mutableListOf<Offset>()
            if (config.numberOfClusters > 0) {
                for (c in 0 until config.numberOfClusters) {
                    val clusterX = random.nextFloat() * canvasSize.width
                    val clusterY = random.nextFloat() * canvasSize.height
                    clusterCenters.add(Offset(clusterX, clusterY))
                }
            } else {
                clusterCenters.add(Offset(canvasSize.width / 2f, canvasSize.height / 2f))
            }

            for (particleIndexInLayer in 0 until config.particleCount) {
                val assignedClusterCenter = if (clusterCenters.isNotEmpty()) {
                    clusterCenters[particleIndexInLayer % clusterCenters.size]
                } else {
                    Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                }

                var attempt = 0
                var currentCircleParams: CircleParams? = null

                while (attempt < 7) {
                    val radiusPx = random.nextFloat() * (layerInfo.maxRadiusPx - layerInfo.minRadiusPx) + layerInfo.minRadiusPx

                    val offsetXFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxWidth
                    val offsetYFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxHeight
                    val proposedInitialX = assignedClusterCenter.x + offsetXFromCluster
                    val proposedInitialY = assignedClusterCenter.y + offsetYFromCluster
                    val proposedInitialCenter = Offset(proposedInitialX, proposedInitialY)

                    var isOverlappingHeavily = false
                    for (placedCircle in allPlacedCircles) {
                        if (placedCircle.layerDepth == config.layerDepth) {
                            val distance = sqrt(
                                (proposedInitialCenter.x - placedCircle.initialCenter.x).pow(2) +
                                        (proposedInitialCenter.y - placedCircle.initialCenter.y).pow(2)
                            )
                            if (distance < (radiusPx + placedCircle.radius) * config.minDistanceFactor) {
                                isOverlappingHeavily = true
                                break
                            }
                        }
                    }

                    if (!isOverlappingHeavily) {
                        currentCircleParams = CircleParams(
                            id = uniqueParticleId++,
                            initialCenter = proposedInitialCenter,
                            radius = radiusPx,
                            baseColorForGradient = config.centerColor,
                            edgeAlpha = config.edgeAlpha,
                            parallaxFactorX = config.parallaxXMultiplier,
                            parallaxFactorY = config.parallaxYMultiplier * (random.nextFloat() - 0.5f) * 0.5f,
                            layerDepth = config.layerDepth
                        )
                        break
                    }
                    attempt++
                }

                if (currentCircleParams == null) {
                    val radiusPx = random.nextFloat() * (layerInfo.maxRadiusPx - layerInfo.minRadiusPx) + layerInfo.minRadiusPx
                    val offsetXFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxWidth
                    val offsetYFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxHeight
                    val fallbackInitialX = assignedClusterCenter.x + offsetXFromCluster
                    val fallbackInitialY = assignedClusterCenter.y + offsetYFromCluster

                    currentCircleParams = CircleParams(
                        id = uniqueParticleId++,
                        initialCenter = Offset(fallbackInitialX, fallbackInitialY),
                        radius = radiusPx,
                        baseColorForGradient = config.centerColor,
                        edgeAlpha = config.edgeAlpha,
                        parallaxFactorX = config.parallaxXMultiplier,
                        parallaxFactorY = config.parallaxYMultiplier * (random.nextFloat() - 0.5f) * 0.5f,
                        layerDepth = config.layerDepth
                    )
                }
                allPlacedCircles.add(currentCircleParams)
            }
        }
        circles = allPlacedCircles.sortedBy { it.layerDepth }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (canvasSize != this.size.toIntSize()) {
                canvasSize = this.size.toIntSize()
            }

            circles.forEach { circle ->
                val circleConfig = processedLayerConfigs.find { it.config.layerDepth == circle.layerDepth }
                val maxShiftPx = circleConfig?.maxParallaxShiftPx ?: 0f

                val offsetX = circle.parallaxFactorX * questionBasedOffset * maxShiftPx
                val offsetY = circle.parallaxFactorY * questionBasedOffset * maxShiftPx

                val currentCenter = Offset(
                    circle.initialCenter.x + offsetX,
                    circle.initialCenter.y + offsetY
                )

                val gradientBrush = Brush.radialGradient(
                    colors = listOf(
                        circle.baseColorForGradient,
                        circle.baseColorForGradient.copy(alpha = circle.edgeAlpha)
                    ),
                    center = currentCenter,
                    radius = circle.radius
                )
                drawCircle(
                    brush = gradientBrush,
                    radius = circle.radius,
                    center = currentCenter
                )
            }
        }
        this.content()
    }
}