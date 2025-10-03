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
import android.util.Log

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
    entryExitVerticalOffset: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var circles by remember { mutableStateOf<List<CircleParams>>(emptyList()) }

    Log.d("CirclesBG_Debug", "CirclesBackground recomposing. qOffset: $questionBasedOffset, entryExitVO: $entryExitVerticalOffset, canvasSize: $canvasSize, circles.count: ${circles.size}")

    val processedLayerConfigs = remember(layerConfigs, density) {
        Log.d("CirclesBG_Debug", "Processing LayerConfigs. Count: ${layerConfigs.size}")
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
        Log.d("CirclesBG_Debug", "LaunchedEffect triggered. canvasSize: $canvasSize, processedLayerConfigs.size: ${processedLayerConfigs.size}")
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            Log.d("CirclesBG_Debug", "Canvas size is zero, returning from LaunchedEffect.")
            circles = emptyList()
            return@LaunchedEffect
        }

        val allPlacedCircles = mutableListOf<CircleParams>()
        val yourFixedCustomSeedString = "rig"
        val configString = processedLayerConfigs.joinToString(separator = "|") { layerInfo ->
            "layer:${layerInfo.config.layerDepth};" +
                    "count:${layerInfo.config.particleCount};" +
                    "minRpx:${layerInfo.minRadiusPx};" +
                    "maxRpx:${layerInfo.maxRadiusPx};" +
                    "centerC:${layerInfo.config.centerColor.value};" +
                    "edgeA:${layerInfo.config.edgeAlpha};" +
                    "paraXMult:${layerInfo.config.parallaxXMultiplier};" +
                    "paraYMult:${layerInfo.config.parallaxYMultiplier};" +
                    "maxShiftPx:${layerInfo.maxParallaxShiftPx};" +
                    "clusters:${layerInfo.config.numberOfClusters};" +
                    "spreadF:${layerInfo.config.clusterSpreadFactor};" +
                    "minDistF:${layerInfo.config.minDistanceFactor}"
        }
        val fullSeedString = "customBaseSeed:$yourFixedCustomSeedString;canvas:${canvasSize.width}x${canvasSize.height};$configString"
        val derivedSeed = fullSeedString.hashCode().toLong()
        val random = Random(derivedSeed)
        Log.d("CirclesBG_Debug", "Seed calculated. Starting circle generation.")
        var uniqueParticleId = 0

        processedLayerConfigs.sortedBy { it.config.layerDepth }.forEach { layerInfo ->
            val config = layerInfo.config
            if (config.particleCount == 0) return@forEach

            val clusterSpreadPxWidth = canvasSize.width * config.clusterSpreadFactor
            val clusterSpreadPxHeight = canvasSize.height * config.clusterSpreadFactor
            val clusterCenters = mutableListOf<Offset>()

            if (config.numberOfClusters > 0) {
                for (c in 0 until config.numberOfClusters) {
                    clusterCenters.add(Offset(random.nextFloat() * canvasSize.width, random.nextFloat() * canvasSize.height))
                }
            } else {
                clusterCenters.add(Offset(canvasSize.width / 2f, canvasSize.height / 2f))
            }

            for (particleIndexInLayer in 0 until config.particleCount) {
                val assignedClusterCenter = if (clusterCenters.isNotEmpty()) clusterCenters[particleIndexInLayer % clusterCenters.size] else Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                var attempt = 0
                var currentCircleParams: CircleParams? = null

                while (attempt < 7) {
                    val radiusPx = random.nextFloat() * (layerInfo.maxRadiusPx - layerInfo.minRadiusPx) + layerInfo.minRadiusPx
                    val offsetXFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxWidth
                    val offsetYFromCluster = (random.nextFloat() - 0.5f) * 2f * clusterSpreadPxHeight
                    val proposedInitialCenter = Offset(assignedClusterCenter.x + offsetXFromCluster, assignedClusterCenter.y + offsetYFromCluster)
                    var isOverlappingHeavily = false

                    for (placedCircle in allPlacedCircles) {
                        if (placedCircle.layerDepth == config.layerDepth) {
                            val distance = sqrt((proposedInitialCenter.x - placedCircle.initialCenter.x).pow(2) + (proposedInitialCenter.y - placedCircle.initialCenter.y).pow(2))
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
                    currentCircleParams = CircleParams(
                        id = uniqueParticleId++,
                        initialCenter = Offset(assignedClusterCenter.x + offsetXFromCluster, assignedClusterCenter.y + offsetYFromCluster),
                        radius = radiusPx,
                        baseColorForGradient = config.centerColor,
                        edgeAlpha = config.edgeAlpha,
                        parallaxFactorX = config.parallaxXMultiplier,
                        parallaxFactorY = config.parallaxYMultiplier * (random.nextFloat() - 0.5f) * 0.5f,
                        layerDepth = config.layerDepth
                    )
                }
                allPlacedCircles.add(currentCircleParams)
                if (allPlacedCircles.size < 5 && currentCircleParams != null) {
                    Log.d("CirclesBG_Debug", "Added circle: ID=${currentCircleParams.id}, Center=${currentCircleParams.initialCenter}, Radius=${currentCircleParams.radius}, Color=${currentCircleParams.baseColorForGradient}")
                }
            }
        }
        circles = allPlacedCircles.sortedBy { it.layerDepth }
        Log.d("CirclesBG_Debug", "Finished circle generation. Total circles: ${circles.size}")
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (canvasSize != this.size.toIntSize()) {
                Log.d("CirclesBG_Debug", "Canvas size CHANGED from $canvasSize to ${this.size.toIntSize()}")
                canvasSize = this.size.toIntSize()
            }
            if (circles.isEmpty() && (canvasSize.width > 0 && canvasSize.height > 0) ) {
                Log.w("CirclesBG_Debug", "CANVAS: circles list is EMPTY but canvas has size!")
            } else if (circles.isEmpty()) {
                Log.d("CirclesBG_Debug", "CANVAS: circles list is EMPTY.")
            }

            circles.forEach { circle ->
                val circleConfig = processedLayerConfigs.find { it.config.layerDepth == circle.layerDepth }
                val maxShiftPx = circleConfig?.maxParallaxShiftPx ?: 0f

                val horizontalParallaxShiftX = circle.parallaxFactorX * questionBasedOffset * maxShiftPx
                var verticalParallaxShiftY = circle.parallaxFactorY * questionBasedOffset * maxShiftPx

                val verticalEntryExitActualShift = entryExitVerticalOffset * (maxShiftPx * 0.01f)

                verticalParallaxShiftY += verticalEntryExitActualShift

                val currentCenter = Offset(
                    circle.initialCenter.x + horizontalParallaxShiftX,
                    circle.initialCenter.y + verticalParallaxShiftY
                )

                val gradientBrush = Brush.radialGradient(
                    colors = listOf(circle.baseColorForGradient, circle.baseColorForGradient.copy(alpha = circle.edgeAlpha)),
                    center = currentCenter,
                    radius = circle.radius
                )

                if (circle.id < 5 && canvasSize.width > 0) {
                    Log.d("CirclesBG_Debug", "Drawing Circle ID ${circle.id}: Center=$currentCenter, Radius=${circle.radius}, Color=${circle.baseColorForGradient}, Alpha=${circle.edgeAlpha}, entryExitVO=$entryExitVerticalOffset, qOffset=$questionBasedOffset")
                }

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

