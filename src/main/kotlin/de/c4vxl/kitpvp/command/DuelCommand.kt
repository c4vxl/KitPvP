package de.c4vxl.kitpvp.command

import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.kitpvp.utils.Dueling
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.textArgument

/**
 * Internal command used for dueling messages
 */
object DuelCommand {
    val command = commandTree("duel") {
        literalArgument("accept") {
            textArgument("id") {
                playerExecutor { player, args ->
                    val id = args.get("id").toString()
                    val language = player.language.child("kitpvp")

                    if (Dueling.joinDuel(player, id))
                        player.sendMessage(language.getCmp("msg.duel.success.accept"))
                    else
                        player.sendMessage(language.getCmp("msg.duel.error.accept"))
                }
            }
        }

        literalArgument("decline") {
            textArgument("id") {
                playerExecutor { player, args ->
                    val id = args.get("id").toString()

                    if (Dueling.declineDuel(player, id))
                        player.sendMessage(player.language.child("kitpvp").getCmp("msg.duel.success.decline"))
                }
            }
        }
    }
}