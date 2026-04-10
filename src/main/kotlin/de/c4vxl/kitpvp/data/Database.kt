package de.c4vxl.kitpvp.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.c4vxl.gamelobby.Main
import de.c4vxl.kitpvp.data.struct.db.CachedData
import de.c4vxl.kitpvp.data.struct.db.StoredData
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Central data accessing point
 */
object Database {
    init {
        val period = Main.instance.config.getInt("config.db.save-cache", 7200)
        Main.instance.logger.info("Scheduling db cache saving. Interval: $period")

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, Runnable {
            saveAll()
        }, 0, period * 20L)
    }

    /**
     * Returns the directory where the db should store data
     */
    private val dir get() =
        File(Main.instance.config.getString("config.db.path") ?: "./kits/")
            .also { it.mkdirs() }

    /**
     * Returns a configured version of GSON
     */
    private val gson: Gson get() =
        GsonBuilder()
            .apply {
                if (Main.instance.config.getBoolean("config.db.pretty-print", false))
                    setPrettyPrinting()
            }
            .create()

    private val cache = ConcurrentHashMap<UUID, CachedData>()

    /**
     * Returns the entry file of a specific player
     * @param uuid The uuid of the player
     */
    private fun getPlayerEntry(uuid: String) =
        dir.resolve("player/$uuid.json")
            .also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }

    /**
     * Returns the stored data of a player
     * @param player The player to retrieve the data of
     */
    fun get(player: OfflinePlayer): StoredData =
        cache.computeIfAbsent(player.uniqueId) {
            CachedData(loadFromDisk(player))
        }.data

    /**
     * Update a db entry
     * @param player The player entry to update
     * @param block The update
     */
    inline fun update(player: OfflinePlayer, block: StoredData.() -> Unit) {
        block(get(player))
        makeDirty(player.uniqueId)
    }

    /**
     * Loads data from disk
     * @param player The player to load the data of
     */
    private fun loadFromDisk(player: OfflinePlayer): StoredData {
        val empty = StoredData(player.uniqueId.toString())

        // Read db entry
        val content = getPlayerEntry(player.uniqueId.toString()).readText()
            .takeUnless { it.isEmpty() || it.isBlank() }
            ?: return empty

        // Parse content
        return gson.fromJson(content, StoredData::class.java) ?: return empty
    }

    /**
     * Saves a storage entry
     * @param data The data to store
     */
    fun save(data: StoredData) {
        // Serialize data
        val serialized = gson.toJson(data)

        // Save
        getPlayerEntry(data.uuid).writeText(serialized)
    }

    /**
     * Marks a cache entry as dirty
     * @param uuid The uuid of the cache (player id)
     */
    fun makeDirty(uuid: UUID) { cache[uuid]?.makeDirty() }

    /**
     * Saves a data cache entry to disk
     * @param uuid The uuid of the cache entry
     */
    fun save(uuid: UUID) {
        val entry = cache[uuid] ?: return

        // Cache is clean
        if (!entry.isDirty) return

        save(entry.data)
    }

    /**
     * Saves the entire cache to disk
     */
    fun saveAll() {
        Main.instance.logger.info("Saving cache to disk.")
        cache.keys.forEach { save(it) }
    }
}