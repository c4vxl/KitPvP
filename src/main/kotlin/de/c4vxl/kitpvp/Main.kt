package de.c4vxl.kitpvp

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ResourceUtils
import de.c4vxl.kitpvp.data.Database
import de.c4vxl.kitpvp.handlers.*
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
    }

    override fun onLoad() {
        instance = this
        Main.logger = this.logger

        // Load CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .silentLogs(true)
                .verboseOutput(false)
        )
    }

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Register language extensions
        ResourceUtils.readResource("langs", Main::class.java).split("\n")
            .forEach { langName ->
                Language.provideLanguageExtension(
                    "kitpvp",
                    langName,
                    ResourceUtils.readResource("lang/$langName.yml", Main::class.java)
                )
            }

        // Save configs
        saveResource("config.yml", false)
        saveResource("kiteditor.json", false)
        saveResource("enchantmentIcons.json", false)
        saveResource("potionEffects.json", false)
        saveResource("serverKits.json", false)

        // Update lobby config
        de.c4vxl.gamelobby.Main.config.set("config.queue.use-team-selection-item-solos", false)

        // Register handlers
        LobbyHandler()
        KitEditorHandler()
        UIHandler()
        TryOnHandler()
        QueueHandler()
        GameHandler()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        // Save db
        Database.saveAll()

        logger.info("[+] $name has been disabled!")
    }
}