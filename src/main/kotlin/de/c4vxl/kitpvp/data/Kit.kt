package de.c4vxl.kitpvp.data

import net.minecraft.world.item.equipment.ArmorType

data class Kit(
    var name: String,
    var inventory: MutableMap<Int, KitItem> = mutableMapOf(),
    var helmet: KitItem? = null,
    var chestplate: KitItem? = null,
    var leggings: KitItem? = null,
    var boots: KitItem? = null,
    var offhand: KitItem? = null
) {
    /**
     * Sets an armor piece
     * @param type The armor slot
     * @param item The armor item
     */
    fun setArmorPiece(type: ArmorType, item: KitItem?): KitItem? {
        when (type) {
            ArmorType.HELMET -> this.helmet = item
            ArmorType.CHESTPLATE -> this.chestplate = item
            ArmorType.LEGGINGS -> this.leggings = item
            ArmorType.BOOTS -> this.boots = item
            else -> {}
        }

        return item
    }

    /**
     * Returns an armor piece
     * @param type The armor slot
     */
    fun getArmorItem(type: ArmorType): KitItem? {
        return when (type) {
            ArmorType.HELMET -> this.helmet
            ArmorType.CHESTPLATE -> this.chestplate
            ArmorType.LEGGINGS -> this.leggings
            ArmorType.BOOTS -> this.boots
            else -> null
        }
    }
}