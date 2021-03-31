package com.besaba.anvarov.orentsd

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.besaba.anvarov.orentsd.room.DocumentData
import com.besaba.anvarov.orentsd.room.NomenData
import com.besaba.anvarov.orentsd.room.ScanData
import com.besaba.anvarov.orentsd.room.TSDDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AllViewModel(application: Application) : AndroidViewModel(application) {

    private val mScanRepository: ScanRepository
    private val mNomenRepository: NomenRepository
    var mAllScans: LiveData<List<ScanData>>
    val mAllDocs: LiveData<List<DocumentData>>

    init {
        val scansDao = TSDDatabase.getDatabase(application).scanDataDao()
        val nomenDao = TSDDatabase.getDatabase(application).nomenDataDao()
        mScanRepository = ScanRepository(scansDao)
        mNomenRepository = NomenRepository(nomenDao)
        mAllScans = mScanRepository.getScans(0)
        mAllDocs = mScanRepository.mAllDocs
    }

    fun insertScan(scanData: ScanData) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.insert(scanData)
    }

    fun deleteScan(scanData: ScanData) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteScan(scanData)
    }

    fun deleteDoc(numDoc: Int) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteDoc(numDoc)
    }

    fun getNumberDocument(): Int {
        var res: Int
        runBlocking { res = mScanRepository.getNumberDocument() }
        return res
    }

    fun setNumDoc(numDoc: Int){
        Log.d("M_AllViewModel","setNumDoc $numDoc")
        mScanRepository.mNumDoc = numDoc
        mAllScans = mScanRepository.getScans(numDoc)
    }

    fun insertNomen(nomenData: NomenData) = viewModelScope.launch(Dispatchers.IO) {
        mNomenRepository.insert(nomenData)
    }

    fun getNomenByCode(barcode: String): NomenData? {
        var res: NomenData?
        runBlocking {res = mNomenRepository.getNomenByCode(barcode) }
        return res
    }

    fun getAll(): List<ScanData>? {
        var res: List<ScanData>?
        runBlocking { res = mScanRepository.getAll() }
        return res
    }

    fun delNomen() {
        runBlocking { mNomenRepository.delNomen()}
    }

    fun countNomen(): Int {
        var res: Int
        runBlocking { res = mNomenRepository.countNomen() }
        return res
    }
}

