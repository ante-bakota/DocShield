package com.digitaldude.docshield.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digitaldude.docshield.ui.theme.DocAmber
import com.digitaldude.docshield.ui.theme.DocAmberDark
import com.digitaldude.docshield.ui.theme.DocLavender
import com.digitaldude.docshield.ui.theme.DocLavenderDark
import com.digitaldude.docshield.ui.theme.DocTeal
import com.digitaldude.docshield.ui.theme.DocTealDark

@Composable
fun VaultBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val bgColors = if (dark) {
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
            Color(0xFF100E19)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFFFFFBFF),
            Color(0xFFF1ECFA)
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgColors))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(DocTeal.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(widthPx, heightPx * 0.08f),
                            radius = widthPx * 0.85f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(DocLavender.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(0f, heightPx * 0.75f),
                            radius = widthPx * 0.95f
                        )
                    )
            )
            content()
        }
    }
}

@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

@Composable
fun CategoryChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val resolvedAccent = accent ?: when (text.trim().lowercase()) {
        "racun" -> DocAmber
        "zdravlje" -> DocTeal
        "osobne isprave" -> DocLavender
        else -> DocAmber
    }
    val textColor = if (isSystemInDarkTheme()) {
        resolvedAccent
    } else {
        when (resolvedAccent) {
            DocTeal -> DocTealDark
            DocLavender -> DocLavenderDark
            else -> DocAmberDark
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(resolvedAccent, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun SecureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = DocLavender,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
fun FullScreenLoader(
    message: String,
    modifier: Modifier = Modifier
) {
    VaultBackground(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            VaultCard(modifier = Modifier.padding(32.dp)) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    MorphingLoader(size = 72.dp)
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Keeping your document local and protected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InlineLoader(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = message },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MorphingLoader(size = 30.dp)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DocumentSkeletonCards(
    modifier: Modifier = Modifier,
    count: Int = 3
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            ShimmerCard()
        }
    }
}

@Composable
private fun ShimmerCard() {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    VaultCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 70.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            )
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(14.dp)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(11.dp)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun MorphingLoader(size: Dp) {
    val transition = rememberInfiniteTransition(label = "vault_loader")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing)),
        label = "loader_rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loader_pulse"
    )

    Box(
        modifier = Modifier
            .size(size)
            .semantics { contentDescription = "Loading" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val stroke = Stroke(width = this.size.minDimension * 0.10f, cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(this.size.width - stroke.width, this.size.height - stroke.width)
            drawArc(
                color = DocLavender,
                startAngle = -40f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = DocTeal,
                startAngle = 145f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = DocAmber,
                startAngle = 285f,
                sweepAngle = 52f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
        }
        Box(
            modifier = Modifier
                .size(size * 0.36f * pulse)
                .background(DocTeal.copy(alpha = 0.24f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size * 0.18f)
                .background(DocTeal, CircleShape)
        )
    }
}
