package dev.shulkerinsight

object ShulkerAnalyzer {
    const val SLOT_COUNT = 27
    private val MIXED = Any()

    /** Pure slot view: [type] is any identity key (Item in production), tests may use String. */
    data class Slot(val type: Any?, val count: Int, val maxStackSize: Int)

    /** uniformType is null when empty OR mixed; fillRatio in [0,1]; empty means no non-empty slot. */
    data class Result(
        @get:JvmName("uniformType") val uniformType: Any?,
        @get:JvmName("fillRatio") val fillRatio: Float,
        @get:JvmName("empty") val empty: Boolean,
    ) {
        val isUniform: Boolean
            get() = uniformType != null
    }

    /** Single source of truth for uniform detection and fill calculation. */
    @JvmStatic
    fun analyze(slots: Iterable<Slot?>, slotCount: Int): Result {
        var uniformType: Any? = null
        var typeSeen = false
        var empty = true
        var totalContribution = 0f

        for (slot in slots) {
            if (slot == null || slot.count <= 0) {
                continue
            }

            empty = false
            if (!typeSeen) {
                uniformType = slot.type
                typeSeen = true
            } else if (uniformType !== MIXED && uniformType != slot.type) {
                uniformType = MIXED
            }

            val maxStackSize = maxOf(1, slot.maxStackSize)
            totalContribution += minOf(slot.count, maxStackSize).toFloat() / maxStackSize
        }

        if (empty) {
            return Result(null, 0f, true)
        }

        return Result(
            if (uniformType === MIXED) null else uniformType,
            minOf(1f, totalContribution / maxOf(1, slotCount)),
            false,
        )
    }
}
