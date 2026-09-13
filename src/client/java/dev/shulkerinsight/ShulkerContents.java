package dev.shulkerinsight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ShulkerContents {
    private static final Analysis EMPTY = new Analysis(ItemStack.EMPTY, 0);

    public record Analysis(ItemStack uniformItem, float fillRatio) {
        public boolean isUniform() {
            return !uniformItem.isEmpty();
        }

        public boolean isEmpty() {
            return fillRatio <= 0.0f;
        }
    }

    private ShulkerContents() {}

    public static Analysis of(ItemStack shulkerStack) {
        ItemContainerContents contents = shulkerStack.get(DataComponents.CONTAINER);
        return contents == null ? EMPTY : of(contents);
    }

    public static Analysis of(ItemContainerContents contents) {
        List<ShulkerAnalyzer.Slot> slots = new ArrayList<>();
        ItemStackTemplate first = null;
        for (ItemStackTemplate template : contents.nonEmptyItems()) {
            if (first == null) {
                first = template;
            }
            slots.add(new ShulkerAnalyzer.Slot(
                    template.item().value(), template.count(), maxStackSize(template)));
        }
        ShulkerAnalyzer.Result result = ShulkerAnalyzer.analyze(slots, ShulkerAnalyzer.SLOT_COUNT);
        if (result.empty()) {
            return EMPTY;
        }
        ItemStack uniformItem = result.isUniform() && first != null ? first.create() : ItemStack.EMPTY;
        return new Analysis(uniformItem, result.fillRatio());
    }

    public static Analysis of(Container container) {
        List<ShulkerAnalyzer.Slot> slots = new ArrayList<>();
        ItemStack first = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (first.isEmpty() && !stack.isEmpty()) {
                first = stack;
            }
            slots.add(new ShulkerAnalyzer.Slot(stack.getItem(), stack.getCount(), stack.getMaxStackSize()));
        }
        ShulkerAnalyzer.Result result = ShulkerAnalyzer.analyze(slots, container.getContainerSize());
        if (result.empty()) {
            return EMPTY;
        }
        ItemStack uniformItem = result.isUniform() ? first.copy() : ItemStack.EMPTY;
        return new Analysis(uniformItem, result.fillRatio());
    }

    public static Analysis of(Iterable<ItemStack> stacks) {
        ItemStack[] first = {ItemStack.EMPTY};
        Iterable<ShulkerAnalyzer.Slot> slots = () -> new Iterator<>() {
            private final Iterator<ItemStack> iterator = stacks.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ShulkerAnalyzer.Slot next() {
                ItemStack stack = iterator.next();
                if (first[0].isEmpty() && !stack.isEmpty()) {
                    first[0] = stack;
                }
                return new ShulkerAnalyzer.Slot(stack.getItem(), stack.getCount(), stack.getMaxStackSize());
            }
        };
        ShulkerAnalyzer.Result result = ShulkerAnalyzer.analyze(slots, ShulkerAnalyzer.SLOT_COUNT);
        if (result.empty()) {
            return EMPTY;
        }
        return new Analysis(result.isUniform() ? first[0].copy() : ItemStack.EMPTY, result.fillRatio());
    }

    private static int maxStackSize(ItemStackTemplate template) {
        Integer override = template.get(DataComponents.MAX_STACK_SIZE);
        return override != null ? override : template.item().value().getDefaultMaxStackSize();
    }
}
