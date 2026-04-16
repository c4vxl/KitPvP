package de.c4vxl.kitpvp.data.struct.kit.rule

import de.c4vxl.gamemanager.language.Language
import net.kyori.adventure.text.TextComponent
import net.minecraft.world.item.alchemy.Potion
import org.bukkit.Material

enum class KitGameRule(
    val icon: Material,
    val type: Class<*>
) {
    ALWAYS_DAY(Material.CLOCK, Boolean::class.java),
    KEEP_INVENTORY(Material.CHEST, Boolean::class.java),
    FALL_DAMAGE(Material.DIAMOND_BOOTS, Boolean::class.java),
    ITEM_DROP(Material.STRING, Boolean::class.java),
    BLOCK_DROPS(Material.GRASS_BLOCK, Boolean::class.java),
    ENTITY_DROPS(Material.SLIME_BLOCK, Boolean::class.java),
    ALLOW_BLOCK_BREAKING(Material.IRON_PICKAXE, Boolean::class.java),
    ALLOW_BLOCK_PLACING(Material.SCAFFOLDING, Boolean::class.java),
    EXPLOSION_DAMAGE(Material.TNT, Boolean::class.java),
    FRIENDLY_FIRE(Material.IRON_SWORD, Boolean::class.java),
    SELF_DAMAGE(Material.ARROW, Boolean::class.java),
    RESET_MAP(Material.TNT_MINECART, Boolean::class.java),
    OLD_PVP(Material.IRON_SWORD, Boolean::class.java),
    SOUP_PVP(Material.MUSHROOM_STEW, Boolean::class.java),
    ALLOW_MAP_BREAKING(Material.DIAMOND_PICKAXE, Boolean::class.java),
    DISABLE_OFFHAND(Material.ITEM_FRAME, Boolean::class.java),
    DISABLE_HUNGER(Material.COOKED_BEEF, Boolean::class.java),
    DISABLE_CRAFTING(Material.CRAFTING_TABLE, Boolean::class.java),
    NUM_ROUNDS(Material.COMPASS, Int::class.java),
    HEALTH(Material.GOLDEN_APPLE, Double::class.java),
    ACTIVE_EFFECTS(Material.BREWING_STAND, Potion::class.java)

    ;

    fun getNameKey(value: Any) =
        when (type) {
            Boolean::class.java -> "rule.${this.name.lowercase()}.name.$value"
            else -> "rule.${this.name.lowercase()}.name"
        }

    fun getLore(language: Language, value: Any) =
        buildList {
            val typeName = when (this@KitGameRule.type) {
                Boolean::class.java -> "boolean"
                Int::class.java -> "int"
                Double::class.java -> "double"
                else -> "num"
            }

            for (i in 1..10) {
                val key = "rule.type.$typeName.lore.$i"
                if (language.get(key) == key)
                    break

                add(language.getCmp(key, value.toString()) as TextComponent)
            }
        }
}