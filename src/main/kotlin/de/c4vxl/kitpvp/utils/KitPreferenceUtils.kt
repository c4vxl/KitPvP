package de.c4vxl.kitpvp.utils

import de.c4vxl.kitpvp.data.struct.kit.item.KitItem

object KitPreferenceUtils {
    /**
     * Calculates the offsets between the original kit inventory and the modified one
     * @param originalInventory The original inventory
     * @param updatedInventory The updated inventory
     */
    fun calculateOffsets(originalInventory: Map<Int, KitItem>, updatedInventory: Map<Int, KitItem>): MutableMap<Int, Int> {
        if (originalInventory == updatedInventory)
            return mutableMapOf()
        
        val offsets = mutableMapOf<Int, Int>()

        // Tracks what slots in the original inventory have already been mapped
        // This has to be done to prevent confusion of two slots with equal items
        val usedOriginalSlots = mutableSetOf<Int>()

        for (slot in 0..35) {
            val newItem = updatedInventory[slot] ?: continue

            val originalSlot = originalInventory.entries
                .filter { it.value == newItem }
                .firstOrNull { it.key !in usedOriginalSlots }
                ?.key

            originalSlot?.let {
                usedOriginalSlots.add(it)
                offsets[it] = slot
            }
        }

        return offsets
    }

    /**
     * Applies an offset mapping to an inventory
     * @param inventory The inventory
     * @param offsets The offsets to apply
     */
    fun applyOffsets(inventory: Map<Int, KitItem>, offsets: Map<Int, Int>) =
        buildMap {
            inventory.forEach { (slot, item) ->
                val newSlot = offsets.getOrDefault(slot, slot)
                put(newSlot, item)
            }
        }

}