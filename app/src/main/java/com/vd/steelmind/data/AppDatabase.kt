package com.vd.steelmind.data

import androidx.room.*

@Entity
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val materialType: String? = null,
    val useInventory: Boolean = false,
    val kerfMmDefault: Int = 3,
    val minOffcutMm: Int = 200
)

@Entity(indices = [Index("projectId")])
data class StockOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val lengthMm: Int,
    val availableCount: Int? = null
)

@Entity
data class ItemTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val materialType: String? = null,
    val priceFtPerM: Int = 0
)

@Entity(indices = [Index("projectId"), Index("itemTypeId")])
data class CutDemandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val itemTypeId: Long,
    val lengthMm: Int,
    val quantity: Int,
    val kerfOverrideMm: Int? = null
)

@Entity
data class InventoryBarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lengthMm: Int,
    val count: Int
)

@Entity
data class OffcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lengthMm: Int,
    val count: Int
)

@androidx.room.Dao
interface AppDao {
    @Insert suspend fun insertProject(p: ProjectEntity): Long
    @Update suspend fun updateProject(p: ProjectEntity)
    @Query("SELECT * FROM ProjectEntity ORDER BY createdAt DESC")
    suspend fun listProjects(): List<ProjectEntity>

    @Insert suspend fun insertStockOption(s: StockOptionEntity)
    @Query("SELECT * FROM StockOptionEntity WHERE projectId=:projectId")
    suspend fun stockOptions(projectId: Long): List<StockOptionEntity>
    @Query("DELETE FROM StockOptionEntity WHERE projectId=:projectId")
    suspend fun clearStockOptions(projectId: Long)

    @Insert suspend fun insertItemType(i: ItemTypeEntity): Long
    @Update suspend fun updateItemType(i: ItemTypeEntity)
    @Query("SELECT * FROM ItemTypeEntity ORDER BY name")
    suspend fun listItemTypes(): List<ItemTypeEntity>

    @Insert suspend fun insertCutDemand(d: CutDemandEntity): Long
    @Query("SELECT * FROM CutDemandEntity WHERE projectId=:projectId")
    suspend fun listDemands(projectId: Long): List<CutDemandEntity>
    @Query("DELETE FROM CutDemandEntity WHERE projectId=:projectId")
    suspend fun clearDemands(projectId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertInventoryBar(b: InventoryBarEntity): Long
    @Query("SELECT * FROM InventoryBarEntity ORDER BY lengthMm DESC")
    suspend fun listInventory(): List<InventoryBarEntity>
    @Query("UPDATE InventoryBarEntity SET count=:count WHERE id=:id")
    suspend fun setInventoryCount(id: Long, count: Int)
    @Query("DELETE FROM InventoryBarEntity WHERE id=:id")
    suspend fun deleteInventory(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertOffcut(o: OffcutEntity): Long
    @Query("SELECT * FROM OffcutEntity ORDER BY lengthMm DESC")
    suspend fun listOffcuts(): List<OffcutEntity>
    @Query("UPDATE OffcutEntity SET count=:count WHERE id=:id")
    suspend fun setOffcutCount(id: Long, count: Int)
    @Query("DELETE FROM OffcutEntity WHERE id=:id")
    suspend fun deleteOffcut(id: Long)
}

@Database(
    entities = [
        ProjectEntity::class, StockOptionEntity::class, ItemTypeEntity::class,
        CutDemandEntity::class, InventoryBarEntity::class, OffcutEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}