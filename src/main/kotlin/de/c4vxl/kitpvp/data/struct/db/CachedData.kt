package de.c4vxl.kitpvp.data.struct.db

/**
 * A data cache of de.c4vxl.kitpvp.data.struct.db.StoredData
 */
data class CachedData(
    var data: StoredData,
    var isDirty: Boolean = false
) {
    /**
     * Marks this cache as dirty
     */
    fun makeDirty() {
        isDirty = true
    }
}
