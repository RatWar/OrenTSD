package com.besaba.anvarov.orentsd.room

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ScanDataDao {

    @Query("SELECT * from ScanData where NumDoc = :numDoc")
    fun getAllScans(numDoc: Int): LiveData<List<ScanData>>

    @Query("SELECT DateTime, NumDoc from ScanData group by NumDoc")
    fun getAllDocs(): LiveData<List<DocumentData>>

    @Query("SELECT * from ScanData order by NumDoc")
    suspend fun getAll(): List<ScanData>?

    @Query("SELECT IFNULL(max(NumDoc), '0') + 1 FROM scanData")
    suspend fun getNumberDocument(): Int

    @Query("SELECT SGTIN FROM scanData where NumDoc = :numDoc")
    suspend fun getSGTINfromDocument(numDoc: Int): List<String>

    @Query("DELETE from ScanData where NumDoc = :numDoc")
    suspend fun delDoc(numDoc: Int)

    @Insert(onConflict = REPLACE)
    suspend fun insert(scanData: ScanData)

    @Delete
    suspend fun delScan(scanData: ScanData)

}