package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.player.GamePlayerSelfDamageEvent
import de.c4vxl.gamemanager.gma.event.team.GamePlayerFriendlyFireEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.game
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

/**
 * This handler takes care of implementing kit rules that need custom handling
 */
class KitRulesHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    /**
     * Runs a passed function when a given game rule is enabled
     * @param game The game
     * @param gameRule The game rule to check for
     * @param block The code to run when the game rule is enabled
     */
    private fun handle(game: Game?, gameRule: (Kit) -> Boolean, block: (Game, Kit) -> Unit) {
        if (game == null) return
        val kit = game.kitData.kit ?: return

        if (gameRule(kit))
            block(game, kit)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        handle(event.player.gma.game, { !it.rules.isAllowBlockBreaking }) { _, _ ->
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        handle(event.player.gma.game, { !it.rules.isAllowBlockPlacing }) { _, _ ->
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        handle(event.player.gma.game, { !it.rules.isItemDrop }) { _, _ ->
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        handle(event.entity.world.game, { !it.rules.isExplosionDamage }) { _, _ ->
            event.blockList().clear()
        }
    }

    @EventHandler
    fun onExplosion(event: BlockExplodeEvent) {
        handle(event.block.world.game, { !it.rules.isExplosionDamage }) { _, _ ->
            event.blockList().clear()
        }
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        // If there happens to be a vanilla gamerule for this
        // consider moving this to GameHandler.onWorldLoaded
        handle((event.entity as? Player)?.gma?.game, { it.rules.isDisableHunger }) { _, _ ->
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFriendlyFire(event: GamePlayerFriendlyFireEvent) {
        val kit = event.game.kitData.kit ?: return
        event.allow = kit.rules.isFriendlyFire
    }

    @EventHandler
    fun onSelfDamage(event: GamePlayerSelfDamageEvent) {
        val kit = event.game.kitData.kit ?: return
        event.allow = kit.rules.isSelfDamage
    }

    @EventHandler
    fun onSoup(event: PlayerInteractEvent) {
        val item = event.item ?: return

        // Not mushroom stew
        if (item.type != Material.MUSHROOM_STEW)
            return

        // Only on right clicks
        if (!event.action.isRightClick)
            return

        handle(event.player.gma.game, { it.rules.isSoupPvP }, { _, _ ->
            // Heal player
            event.player.health = min(
                event.player.health + 7.0,
                event.player.maxHealth
            )

            // Add food levels
            event.player.foodLevel = min(event.player.foodLevel + 5, 20)
            event.player.saturation = 10f

            // Play sound
            event.player.playSound(event.player.location, event.player.getEatingSound(item), 1f, 1f)

            // Set to empty bowl
            event.player.inventory.setItemInMainHand(ItemStack(Material.BOWL))
        })
    }

    @EventHandler
    fun offhandSwap(event: PlayerSwapHandItemsEvent) {
        val game = event.player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        if (
            !kit.rules.isOldPvP
            && !kit.rules.isDisableOffhand
        )
            return

        event.isCancelled = true
    }

    @EventHandler
    fun offhandSwap(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val game = player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        if (
            !kit.rules.isOldPvP
            && !kit.rules.isDisableOffhand
        )
            return

        // Not offhand
        val isOffhand = event.slotType == InventoryType.SlotType.QUICKBAR && event.slot == 40 && event.inventory.type == InventoryType.CRAFTING
        if (!isOffhand)
            return

        event.isCancelled =true
    }
}