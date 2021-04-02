package com.besaba.anvarov.orentsd.activity

import android.content.*
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.ScanListAdapter
import com.besaba.anvarov.orentsd.databinding.ActivityDocumentBinding
import com.besaba.anvarov.orentsd.room.ScanData
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider as ViewModelProvider1

class DocumentActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var mBarcode: String = ""
    private var mSGTIN: String = ""
    private var fCamera: String? = ""
    private var mDocumentNumber: Int = 0
    private lateinit var mCurrentScan: ScanData
    private var keycode: Int = 0
    private val tableScan = mutableListOf<String>()
    private lateinit var binding: ActivityDocumentBinding

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST" -> onScan(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.fabCamera.setOnClickListener { onScanner() }
        binding.fabSave.setOnClickListener { finish() }
        val scanRecyclerView = findViewById<RecyclerView>(R.id.recyclerScanList)
        val scanAdapter = ScanListAdapter(this)
        scanRecyclerView.adapter = scanAdapter
        scanRecyclerView.layoutManager = LinearLayoutManager(this)

        val onScanClickListener = object : ScanListAdapter.OnScanClickListener {
            override fun onScanClick(scan: ScanData, del: Boolean) {
                when {
                    del -> {
                        mAllViewModel.deleteScan(scan)
                        Toast.makeText(this@DocumentActivity, "delete " + scan.scanCode, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        scanAdapter.scanAdapter(onScanClickListener)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")

        val intent = intent
        mDocumentNumber = intent.getIntExtra("documentNumber", 0)
        mAllViewModel = ViewModelProvider1(this).get(AllViewModel::class.java)
        mAllViewModel.setNumDoc(mDocumentNumber)
        mAllViewModel.mAllScans.observe(this, { scans ->
            scans?.let { scanAdapter.setScans(it) }
        })
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
        setLayoutCount()
    }

    override fun onResume() {
        super.onResume()
        if (fCamera!!.toInt() == 2) {
            binding.fabCamera.hide()
            binding.fabSave.hide()
            setTriggerStates()
            openDevice(keycode)
            val filter = IntentFilter()
            filter.addAction("com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST")
            registerReceiver(broadCastReceiver, filter)
        }
    }

    override fun onPause() {
        if (fCamera!!.toInt() == 2) {
            unregisterReceiver(broadCastReceiver)
            closeDevice()
        }
        super.onPause()
    }

    private fun onScanner() {
        val intent = Intent(this, BarcodeActivity::class.java)
        intent.putExtra("fCamera", fCamera!!.toInt())
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (intentData == null) return
        val mType = intentData.getStringExtra("barcodeFormat").toString()
        mBarcode = intentData.getStringExtra("scancode").toString()
        if (mType == "DATA_MATRIX") {
            mSGTIN = mBarcode.substring(0, 31)
            mBarcode = mSGTIN.substring(2, 16)
            handlerBarcode()
        }
        if (mType == "CODE_128"){
            mSGTIN = mBarcode
            handlerTransport()
        }
    }

    private fun onScan(intent: Intent?) {
        val mType: String = intent?.getStringExtra("EXTRA_BARCODE_DECODING_SYMBOLE").toString() // "Data Matrix"
        mBarcode = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA").toString()
        if (mType == "Data Matrix") {
            mSGTIN = mBarcode.substring(0, 31)
            mBarcode = mSGTIN.substring(2, 16)
            handlerBarcode()
        }
        if (mType == "EAN-128"){
            mSGTIN = mBarcode
            handlerTransport()
        }
    }

    private fun handlerBarcode() {
        if (checkNotDoubleScan(mSGTIN)) {
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            val mNomen = mAllViewModel.getNomenByCode(mBarcode)
            if (mNomen != null) {
                mCurrentScan = ScanData(
                    df.format(Date()),
                    mDocumentNumber,
                    mBarcode,
                    mSGTIN,
                    mNomen.name,
                    mNomen.ei,
                    mNomen.mzoo
                )
            } else {
                mCurrentScan = ScanData(
                    df.format(Date()),
                    mDocumentNumber,
                    mBarcode,
                    mSGTIN,
                    "",
                    " ",
                    0
                )
            }
            mAllViewModel.insertScan(mCurrentScan)
            setLayoutCount()
        }
    }

    private fun handlerTransport() {
        if (checkNotDoubleScan(mSGTIN)) {
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            mCurrentScan = ScanData(
                df.format(Date()),
                mDocumentNumber,
                "",
                mSGTIN,
                "",
                " ",
                0
            )
            mAllViewModel.insertScan(mCurrentScan)
            setLayoutCount()
        }
    }

    private fun setLayoutCount() {
        binding.matrixLayoutCount.text = tableScan.filter { it.length > 20 }.size.toString()
        binding.transportLayoutCount.text = tableScan.filter { it.length == 20 }.size.toString()
    }

    // Установите статус Trigger buttons, Trigger buttons включены по умолчанию
    private val actionControlScankey: String = "com.xcheng.scanner.action.ACTION_CONTROL_SCANKEY"
    private val actionCloseScan = "com.xcheng.scanner.action.CLOSE_SCAN_BROADCAST"
    private val extraScankeyCode = "extra_scankey_code"
    private val extraScankeyStatus = "extra_scankey_STATUS"
    private var triggersKeys =
        intArrayOf(KeyEvent.KEYCODE_F3, KeyEvent.KEYCODE_CAMERA, KeyEvent.KEYCODE_FOCUS)

    private fun setTriggerStates() {
        triggersKeys.forEach {
            val intent = Intent()
            intent.action = actionControlScankey
            intent.putExtra(extraScankeyCode, it)
            intent.putExtra(extraScankeyStatus, true)
            sendBroadcast(intent)
        }
    }

    // Включение Scanner Atol
    private val actionOpenScan: String = "com.xcheng.scanner.action.OPEN_SCAN_BROADCAST"
    private val scankey = "scankey"
    private fun openDevice(keycode: Int) {
        val intent = Intent()
        intent.action = actionOpenScan
        intent.putExtra(scankey, keycode)
        sendBroadcast(intent)
    }

    // Отключение Scanner Атол
    private fun closeDevice() {
        val intent = Intent()
        intent.action = actionCloseScan
        sendBroadcast(intent)
    }

    // проверка, что скан не дубль
    private fun checkNotDoubleScan(scan: String): Boolean{
        return (tableScan.filter { it == scan }).isEmpty()
    }

}
