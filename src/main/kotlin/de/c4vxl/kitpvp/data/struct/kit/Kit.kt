package de.c4vxl.kitpvp.data.struct.kit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.c4vxl.kitpvp.data.struct.kit.item.KitItem
import de.c4vxl.kitpvp.data.struct.kit.item.KitMetadata
import de.c4vxl.kitpvp.data.struct.kit.rule.KitGameRules
import net.minecraft.world.item.equipment.ArmorType
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

data class Kit(
    var metadata: KitMetadata,
    var inventory: MutableMap<Int, KitItem> = mutableMapOf(),
    var helmet: KitItem? = null,
    var chestplate: KitItem? = null,
    var leggings: KitItem? = null,
    var boots: KitItem? = null,
    var offhand: KitItem? = null,
    var rules: KitGameRules = KitGameRules()
) {
    /**
     * Returns {@code true} if the kit doesn't contain items
     */
    val isEmpty get() =
        inventory.isEmpty() && helmet == null && chestplate == null && leggings == null && boots == null && offhand == null

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

    /**
     * Converts this kit to json format
     * @param pretty Pretty printing
     */
    fun toJson(pretty: Boolean = false): String =
        GsonBuilder()
            .apply { if (pretty) setPrettyPrinting() }
            .create()
            .toJson(this)

    /**
     * Equips a player with the kit
     * @param player The player to equip
     */
    fun equip(player: Player) {
        fun item(item: KitItem?) = item?.builder?.build()

        player.inventory.clear()

        player.inventory.helmet = item(helmet)
        player.inventory.chestplate = item(chestplate)
        player.inventory.leggings = item(leggings)
        player.inventory.boots = item(boots)
        player.inventory.setItemInOffHand(item(offhand))
        inventory.forEach { (slot, item) -> player.inventory.setItem(slot, item(item)) }
    }

    companion object {
        /**
         * Restores a kit from json
         * @param json The json string
         */
        fun fromJson(json: String?): Kit? {
            if (json.isNullOrEmpty() || json.isBlank())
                return null

            return Gson().fromJson(json, Kit::class.java)
        }

        /**
         * Creates a new kit
         * @param name The name of the kit
         * @param creator The creator of the kit
         */
        fun new(name: String, creator: OfflinePlayer) =
            Kit(KitMetadata(
                name,
                creator.uniqueId.toString()
            ))
    }
    
    override fun toString(): String { return toJson(true) }
}