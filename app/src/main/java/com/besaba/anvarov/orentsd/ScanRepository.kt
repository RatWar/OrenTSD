package com.besaba.anvarov.orentsd

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.besaba.anvarov.orentsd.room.DocumentData
import com.besaba.anvarov.orentsd.room.ScanData
import com.besaba.anvarov.orentsd.room.ScanDataDao

class ScanRepository(private val scanDataDao: ScanDataDao) {

    var mNumDoc: Int = 0
    val mAllDocs: LiveData<List<DocumentData>> = scanDataDao.getAllDocs()

    fun getScans(numDoc: Int): LiveData<List<ScanData>> = scanDataDao.getAllScans(numDoc)

    @WorkerThread
    suspend fun insert(scanData: ScanData) {
        scanDataDao.insert(scanData)
    }

    @WorkerThread
    suspend fun deleteScan(scanData: ScanData) {
        scanDataDao.delScan(scanData)
    }

    @WorkerThread
    suspend fun getNumberDocument(): Int {
        return scanDataDao.getNumberDocument()
    }

    @WorkerThread
    suspend fun deleteDoc(numDoc: Int) {
        Log.d("M_ScanRepository","numDoc $numDoc")
        scanDataDao.delDoc(numDoc)
    }

    @WorkerThread
    suspend fun getAll(): List<ScanData>? {
        return scanDataDao.getAll()
    }

}