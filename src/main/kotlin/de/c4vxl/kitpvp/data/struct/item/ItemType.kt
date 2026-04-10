package de.c4vxl.kitpvp.data.struct.item

import org.bukkit.Material

private val tierOrder = mapOf(
    "WOODEN" to 0,
    "STONE" to 1,
    "GOLDEN" to 2,
    "COPPER" to 3,
    "IRON" to 4,
    "DIAMOND" to 5,
    "NETHERITE" to 6
)

private fun findMaterials(id: String) =
    Material.entries.filter { it.name.endsWith(id) }
        .sortedBy { tierOrder.getOrDefault(it.name.substringBefore("_"), Int.MAX_VALUE) }

enum class ItemType(
    val materials: List<Material>
) {
    PICKAXE(findMaterials("_PICKAXE")),
    AXE(findMaterials("_AXE")),
    SWORD(findMaterials("_SWORD")),
    SHOVEL(findMaterials("_SHOVEL")),
    SPEAR(findMaterials("_SPEAR")),
    HELMET(findMaterials("_HELMET")),
    CHESTPLATE(findMaterials("_CHESTPLATE")),
    LEGGINGS(findMaterials("_LEGGINGS")),
    BOOTS(findMaterials("_BOOTS")),
    ARROW(findMaterials("ARROW")),
    POTION(findMaterials("POTION"))


    ;

    companion object {
        /**
         * Returns an item type from a material
         * @param material The material
         */
        fun fromMaterial(material: Material): ItemType? =
            ItemType.entries.find { it.materials.contains(material) }
    }
}