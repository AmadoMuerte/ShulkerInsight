package dev.shulkerinsight

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import java.lang.ThreadLocal
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.core.Direction
import org.joml.Matrix4f

// Shared render helpers must NOT live as file-level statics under dev.shulkerinsight.mixin:
// Kotlin compiles those into *Kt facade classes inside the mixin package, and Mixin refuses
// to load classes from a package owned by a mixin config ("cannot be referenced directly").
internal object ShulkerRenderHelpers {
    val RENDERING_BADGE: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    /** Ratio at or above which a shulker counts as exactly full (single source of truth). */
    const val FULL_RATIO = 0.9999f

    fun fillColor(ratio: Float): Int {
        if (ratio >= FULL_RATIO) return FULL_COLOR
        if (ratio >= 0.75f) return 0xFF63C64A.toInt() // зелёный
        if (ratio >= 0.50f) return 0xFFE0C93A.toInt() // жёлтый
        if (ratio >= 0.25f) return 0xFFE0862E.toInt() // оранжевый
        return 0xFFE05050.toInt() // красный
    }

    /** Same cyan as the full-shulker fill indicator in the player's inventory. */
    const val FULL_COLOR: Int = 0xFF22D3C5.toInt() // тускло-бирюзовый: ровно полный

    /**
     * Highlight drawn on the icon plane of a shulker box: a translucent pie-slice
     * disc whose sweep equals the fill ratio and whose color comes from
     * [fillColor] — the same stage colors as the inventory indicator. Must be
     * called with the same transform that the icon was submitted with (the icon
     * plane is local z = 0, the camera sits on the local -z side), so the disc
     * lands just in front of the box face.
     */
    fun submitFullHighlight(submitNodeCollector: SubmitNodeCollector, poseStack: PoseStack, fillRatio: Float) {
        val radius = 0.55f
        poseStack.pushPose()
        poseStack.translate(0f, 0f, -0.005f)
        val color = (fillColor(fillRatio) and 0x00FFFFFF) or 0x38000000 // ~22% alpha
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugTriangleFan()) { pose, vertices ->
            // TRIANGLE_FAN topology: the GPU connects every rim vertex to the center,
            // so there is no per-segment seam to leave gaps. The sweep starts at the
            // top (12 o'clock) and goes clockwise, like a filling gauge. The final
            // rim vertex uses the exact sweep angle so any ratio (including 1.0)
            // closes precisely.
            val segments = 32
            val sweepAngle = fillRatio.coerceIn(0f, 1f) * 2f * Math.PI.toFloat()
            fun vertex(x: Float, y: Float) = vertices.addVertex(pose, x, y, 0f).setColor(color)
            vertex(0f, 0f) // fan center
            val steps = 32
            for (i in 0..steps) {
                val t = sweepAngle * i / steps
                val angle = -Math.PI.toFloat() / 2f + t
                vertex(radius * kotlin.math.cos(angle), radius * kotlin.math.sin(angle))
            }
        }
        poseStack.popPose()
    }

    fun applyFaceTransform(poseStack: PoseStack, dir: Direction, progress: Float, scale: Float) {
        val o = 0.01
        val lid = progress * 0.5
        when (dir) {
            Direction.UP -> {
                poseStack.translate(0.5, 1.0 + lid + o, 0.5)
                rotate(poseStack, Axis.XP, -90f)
            }
            Direction.DOWN -> {
                poseStack.translate(0.5, -lid - o, 0.5)
                rotate(poseStack, Axis.XP, 90f)
            }
            Direction.NORTH -> {
                poseStack.translate(0.5, 0.5, -lid - o)
                rotate(poseStack, Axis.YP, 180f)
            }
            Direction.SOUTH -> poseStack.translate(0.5, 0.5, 1.0 + lid + o)
            Direction.EAST -> {
                poseStack.translate(1.0 + lid + o, 0.5, 0.5)
                rotate(poseStack, Axis.YP, 90f)
            }
            Direction.WEST -> {
                poseStack.translate(-lid - o, 0.5, 0.5)
                rotate(poseStack, Axis.YP, -90f)
            }
        }
        rotate(poseStack, Axis.YP, 180f)
        poseStack.scale(scale, scale, scale)
    }

    // 26.3 replaced mulPose(Quaternionf) with rotate(Quaternionf); mulPose(Matrix4f) exists in both.
    fun rotate(poseStack: PoseStack, axis: Axis, degrees: Float) {
        poseStack.mulPose(Matrix4f().rotation(axis.rotationDegrees(degrees)))
    }
}
