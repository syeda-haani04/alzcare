package com.risc.alzcare.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val myLayerConfigs = listOf(
    ParticleLayerConfig(
        layerDepth = 0,
        particleCount = 30,
        minRadiusDp = 150.dp,
        maxRadiusDp = 300.dp,
        centerColor = Color.White.copy(alpha = 0.03f),
        edgeAlpha = 0.0f,
        parallaxXMultiplier = -0f,
        parallaxYMultiplier = 0.1f,
        maxParallaxShiftDp = 50.dp,
        numberOfClusters = 5,
        clusterSpreadFactor = 0.5f,
        minDistanceFactor = 0.6f
    ),
    ParticleLayerConfig(
        layerDepth = 1,
        particleCount = 100,
        minRadiusDp = 30.dp,
        maxRadiusDp = 50.dp,
        centerColor = Color.White.copy(alpha = 0.07f),
        edgeAlpha = 0.02f,
        parallaxXMultiplier = -0.1f,
        parallaxYMultiplier = 0.15f,
        maxParallaxShiftDp = 150.dp,
        numberOfClusters = 10,
        clusterSpreadFactor = 0.2f,
        minDistanceFactor = 0.4f
    ),
    ParticleLayerConfig(
        layerDepth = 2,
        particleCount = 100,
        minRadiusDp = 5.dp,
        maxRadiusDp = 30.dp,
        centerColor = Color.White.copy(alpha = 0.1f),
        edgeAlpha = 0.07f,
        parallaxXMultiplier = -0.5f,
        parallaxYMultiplier = 0.2f,
        maxParallaxShiftDp = 200.dp,
        numberOfClusters = 10,
        clusterSpreadFactor = 0.1f,
        minDistanceFactor = 0.25f
    )
)