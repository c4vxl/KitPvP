package de.c4vxl.kitpvp.data.struct.kit.rule

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Data class holding kit game rules
 */
data class KitGameRules(
    var isAlwaysDay: Boolean = true,
    var isKeepInventory: Boolean = true,
    var isFallDamage: Boolean = true,
    var isItemDrop: Boolean = true,
    var isAllowBlockBreaking: Boolean = true,
    var isExplosionDamage: Boolean = true,
    var numRounds: Int = 3,
    var health: Double = 20.0,
    var activeEffects: MutableMap<String, Int> = mutableMapOf()
) {
    var activeEffectsMap
        get() =
            activeEffects.mapNotNull { (k, v) -> ((PotionEffectType.values().find { it.name.lowercase() == k.lowercase() } ?: return@mapNotNull null) to v) }
                .toMap().toMutableMap()
        set(value) {
            activeEffects = value.mapKeys { it.key.name }.toMutableMap()
        }

    /**
     * Adds all active effects to a player
     * @param player The player to add the effects to
     */
    fun giveEffects(player: Player) {
        activeEffectsMap.forEach { (type, amplifier) ->
            player.addPotionEffect(PotionEffect(
                type, PotionEffect.INFINITE_DURATION, amplifier, true, false, false
            ))
        }
    }
}