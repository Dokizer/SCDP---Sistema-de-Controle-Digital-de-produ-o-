package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DatingCheckEntity
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionDao {

    // Packaging Control
    @Query("SELECT * FROM packaging_controls ORDER BY createdAt DESC")
    fun getAllPackagingControls(): Flow<List<PackagingControlEntity>>

    @Query("SELECT * FROM packaging_controls WHERE id = :id")
    suspend fun getPackagingControlById(id: String): PackagingControlEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackagingControl(item: PackagingControlEntity)

    @Update
    suspend fun updatePackagingControl(item: PackagingControlEntity)

    @Delete
    suspend fun deletePackagingControl(item: PackagingControlEntity)

    @Query("DELETE FROM packaging_controls WHERE id = :id")
    suspend fun deletePackagingControlById(id: String)

    // Machine Downtime
    @Query("SELECT * FROM machine_downtimes ORDER BY createdAt DESC")
    fun getAllMachineDowntimes(): Flow<List<MachineDowntimeEntity>>

    @Query("SELECT * FROM machine_downtimes WHERE id = :id")
    suspend fun getMachineDowntimeById(id: String): MachineDowntimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachineDowntime(item: MachineDowntimeEntity)

    @Update
    suspend fun updateMachineDowntime(item: MachineDowntimeEntity)

    @Delete
    suspend fun deleteMachineDowntime(item: MachineDowntimeEntity)

    @Query("DELETE FROM machine_downtimes WHERE id = :id")
    suspend fun deleteMachineDowntimeById(id: String)

    // Dating Checks
    @Query("SELECT * FROM dating_checks ORDER BY createdAt DESC")
    fun getAllDatingChecks(): Flow<List<DatingCheckEntity>>

    @Query("SELECT * FROM dating_checks WHERE id = :id")
    suspend fun getDatingCheckById(id: String): DatingCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatingCheck(item: DatingCheckEntity)

    @Update
    suspend fun updateDatingCheck(item: DatingCheckEntity)

    @Delete
    suspend fun deleteDatingCheck(item: DatingCheckEntity)

    @Query("DELETE FROM dating_checks WHERE id = :id")
    suspend fun deleteDatingCheckById(id: String)
}
