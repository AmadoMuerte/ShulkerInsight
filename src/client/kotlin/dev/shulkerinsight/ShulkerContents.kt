package dev.shulkerinsight

import net.minecraft.core.component.DataComponents
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.component.ItemContainerContents

object ShulkerContents {
    private val EMPTY = Analysis(ItemStack.EMPTY, 0f)

    data class Analysis(
        @get:JvmName("uniformItem") val uniformItem: ItemStack,
        @get:JvmName("fillRatio") val fillRatio: Float,
    ) {
        val isUniform: Boolean
            get() = !uniformItem.isEmpty

        val isEmpty: Boolean
            get() = fillRatio <= 0.0f
    }

    @JvmStatic
    fun of(shulkerStack: ItemStack): Analysis {
        val contents = shulkerStack.get(DataComponents.CONTAINER) ?: return EMPTY
        return of(contents)
    }

    @JvmStatic
    fun of(contents: ItemContainerContents): Analysis {
        val slots = ArrayList<ShulkerAnalyzer.Slot>()
        var first: ItemStackTemplate? = null
        for (template in contents.nonEmptyItems()) {
            if (first == null) {
                first = template
            }
            slots.add(ShulkerAnalyzer.Slot(template.item().value(), template.count(), maxStackSize(template)))
        }
        val result = ShulkerAnalyzer.analyze(slots, ShulkerAnalyzer.SLOT_COUNT)
        if (result.empty) {
            return EMPTY
        }
        val uniformItem = if (result.isUniform && first != null) first.create() else ItemStack.EMPTY
        return Analysis(uniformItem, result.fillRatio)
    }

    @JvmStatic
    fun of(container: Container): Analysis {
        val slots = ArrayList<ShulkerAnalyzer.Slot>()
        var first = ItemStack.EMPTY
        for (i in 0 until container.containerSize) {
            val stack = container.getItem(i)
            if (first.isEmpty && !stack.isEmpty) {
                first = stack
            }
            slots.add(ShulkerAnalyzer.Slot(stack.item, stack.count, stack.maxStackSize))
        }
        val result = ShulkerAnalyzer.analyze(slots, container.containerSize)
        if (result.empty) {
            return EMPTY
        }
        val uniformItem = if (result.isUniform) first.copy() else ItemStack.EMPTY
        return Analysis(uniformItem, result.fillRatio)
    }

    @JvmStatic
    fun of(stacks: Iterable<ItemStack>): Analysis {
        var first = ItemStack.EMPTY
        val slots = stacks.asSequence()
            .onEach { stack -> if (first.isEmpty && !stack.isEmpty) first = stack }
            .map { stack -> ShulkerAnalyzer.Slot(stack.item, stack.count, stack.maxStackSize) }
            .asIterable()
        val result = ShulkerAnalyzer.analyze(slots, ShulkerAnalyzer.SLOT_COUNT)
        if (result.empty) {
            return EMPTY
        }
        return Analysis(if (result.isUniform) first.copy() else ItemStack.EMPTY, result.fillRatio)
    }

    private fun maxStackSize(template: ItemStackTemplate): Int {
        return template.get(DataComponents.MAX_STACK_SIZE) ?: template.item().value().defaultMaxStackSize
    }
}
