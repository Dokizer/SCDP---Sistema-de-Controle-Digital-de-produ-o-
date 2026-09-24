package com.example.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.AppScreen

/**
 * Material You (Material 3) Motion tokens, easings, and animation specs.
 * Adheres strictly to the M3 Expressive Motion guidelines.
 */
object MaterialYouMotion {
    // Material 3 Standard Easings
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val Standard = FastOutSlowInEasing

    // Durations
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationMediumExtended = 380
    const val DurationLong = 480

    // Spring physics for micro-interactions
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val StiffSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * Material You tactile spring press modifier.
 * Scales the component down on press and bounces back on release.
 */
fun Modifier.materialYouPress(
    scaleFactor: Float = 0.96f,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleFactor else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "M3PressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null, // Custom spring animation acts as primary tactile feedback
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

/**
 * Screen navigation transition spec with Material 3 Shared Axis physics.
 */
fun materialYouScreenTransitionSpec(
    targetState: AppScreen,
    initialState: AppScreen
): ContentTransform {
    val duration = MaterialYouMotion.DurationMediumExtended
    val easing = MaterialYouMotion.Emphasized

    return if (targetState == AppScreen.Dashboard) {
        // Navigating back to Dashboard (Pop / Shared Axis X reverse + scale)
        (slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(animationSpec = tween(duration / 2)) + scaleIn(
            initialScale = 0.93f,
            animationSpec = tween(duration, easing = easing)
        )).togetherWith(
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(duration, easing = easing)
            ) + fadeOut(animationSpec = tween(duration / 2))
        )
    } else if (initialState == AppScreen.Dashboard) {
        // Navigating from Dashboard into a feature (Forward / Shared Axis X forward)
        (slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(animationSpec = tween(duration / 2))).togetherWith(
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(duration, easing = easing)
            ) + fadeOut(animationSpec = tween(duration / 2)) + scaleOut(
                targetScale = 0.94f,
                animationSpec = tween(duration, easing = easing)
            )
        )
    } else {
        // Sub-screen transitions (e.g., History -> RecordDashboard or between sibling screens)
        (fadeIn(animationSpec = tween(duration / 2)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(duration, easing = easing)
        )).togetherWith(
            fadeOut(animationSpec = tween(duration / 3)) + scaleOut(
                targetScale = 1.05f,
                animationSpec = tween(duration, easing = easing)
            )
        )
    }
}

/**
 * Tab and step transition spec with directional sliding and smooth fade.
 */
fun materialYouTabTransitionSpec(isForward: Boolean): ContentTransform {
    val duration = MaterialYouMotion.DurationMedium
    val easing = MaterialYouMotion.Standard

    val initialOffset = if (isForward) { width: Int -> (width * 0.4f).toInt() } else { width: Int -> (-width * 0.4f).toInt() }
    val exitOffset = if (isForward) { width: Int -> (-width * 0.4f).toInt() } else { width: Int -> (width * 0.4f).toInt() }

    return (slideInHorizontally(
        initialOffsetX = initialOffset,
        animationSpec = tween(duration, easing = easing)
    ) + fadeIn(animationSpec = tween(duration - 50))).togetherWith(
        slideOutHorizontally(
            targetOffsetX = exitOffset,
            animationSpec = tween(duration - 50, easing = easing)
        ) + fadeOut(animationSpec = tween(duration / 2))
    )
}

/**
 * Animated step counter transition (odometer style).
 */
fun materialYouOdometerTransitionSpec(isIncrement: Boolean): ContentTransform {
    val duration = 220
    val offset = if (isIncrement) 20 else -20

    return (slideInVertically(
        initialOffsetY = { offset },
        animationSpec = tween(duration, easing = MaterialYouMotion.Standard)
    ) + fadeIn(animationSpec = tween(duration))).togetherWith(
        slideOutVertically(
            targetOffsetY = { -offset },
            animationSpec = tween(duration, easing = MaterialYouMotion.Standard)
        ) + fadeOut(animationSpec = tween(duration))
    )
}
