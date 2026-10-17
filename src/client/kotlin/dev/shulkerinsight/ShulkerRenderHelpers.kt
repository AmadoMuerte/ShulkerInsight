package dev.shulkerinsight

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import java.lang.ThreadLocal
import net.minecraft.core.Direction
import org.joml.Matrix4f

// Shared render helpers must NOT live as file-level statics under dev.shulkerinsight.mixin:
// Kotlin compiles those into *Kt facade classes inside the mixin package, and Mixin refuses
// to load classes from a package owned by a mixin config ("cannot be referenced directly").
internal object ShulkerRenderHelpers {
    val RENDERING_BADGE: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    fun fillColor(ratio: Float): Int {
        if (ratio >= 0.9999f) return 0xFF22D3C5.toInt() // тускло-бирюзовый: ровно полный
        if (ratio >= 0.75f) return 0xFF63C64A.toInt() // зелёный
        if (ratio >= 0.50f) return 0xFFE0C93A.toInt() // жёлтый
        if (ratio >= 0.25f) return 0xFFE0862E.toInt() // оранжевый
        return 0xFFE05050.toInt() // красный
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
