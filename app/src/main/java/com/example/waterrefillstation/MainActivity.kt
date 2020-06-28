package com.example.waterrefillstation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_main.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    val database = FirebaseDatabase.getInstance()
    private lateinit var mScannerView: ZXingScannerView
    private var isPumpOpened = false
    val myRef = database.reference

    private val narasiToScan = "Scan barcode untuk refill"

    val pumpOne = myRef.child("pump_one").child("status")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initScanner()
        initDefaultView()

        pumpOne.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value
                isPumpOpened = value != "Off"
            }

        })

        btn_enough.setOnClickListener {
            if (isPumpOpened){
                pumpOne.setValue("Off")
                text_pump.text = narasiToScan
            }else{
                //Do nothing
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        mScannerView.startCamera()
        doRequestPermission()
        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun doRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                initScanner()
            }
            else -> {
                /* nothing to do in here */
            }
        }
    }

    override fun onPause() {
        mScannerView.stopCamera()
        super.onPause()
    }

    private fun initScanner(){
        mScannerView = ZXingScannerView(this)
        mScannerView.setAutoFocus(true)
        mScannerView.setResultHandler(this)
        camera_scan.addView(mScannerView)
    }

    private fun initDefaultView(){
        text_pump.text = narasiToScan
    }

    override fun handleResult(rawResult: Result?) {
        if(rawResult!!.text == "pump_one"){
            pumpOne.setValue("On")
            text_pump.text = rawResult.text!!
        }else{
            //Do Nothing
        }
    }
}
