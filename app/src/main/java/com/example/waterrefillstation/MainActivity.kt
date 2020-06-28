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
    private var isPumpOneOpened = false
    private var isPumpTwoOpened = false
    private var isPumpThreeOpened = false
    val myRef = database.reference

    private val narasiToScan = "Scan barcode untuk refill"
    private val titleAirPutih = "Air Putih"
    private val titleAirTeh = "Air Teh"
    private val titleAirJeruk = "Air Jeruk"

    val pumpOne = myRef.child("pump_one").child("status")
    val pumpTwo = myRef.child("pump_two").child("status")
    val pumpThree = myRef.child("pump_three").child("status")

    @RequiresApi(Build.VERSION_CODES.M)
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
                when(value){
                    "Off" -> isPumpOneOpened = false
                    "On" -> isPumpOneOpened = true
                }
                initDefaultView()
            }

        })

        pumpTwo.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value
                when(value){
                    "Off" -> isPumpTwoOpened = false
                    "On" -> isPumpTwoOpened = true
                }
                initDefaultView()
            }

        })

        pumpThree.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value
                when(value){
                    "Off" -> isPumpThreeOpened = false
                    "On" -> isPumpThreeOpened = true
                }
                initDefaultView()
            }

        })

        btn_enough.setOnClickListener {
            if(isPumpOneOpened || isPumpTwoOpened || isPumpThreeOpened){
                pumpOne.setValue("Off")
                pumpTwo.setValue("Off")
                pumpThree.setValue("Off")
                rescan()
            }
        }

    }

    private fun rescan(){
        if (mScannerView!=null){
            mScannerView.resumeCameraPreview(this)
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                onStart()
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
        if(!isPumpOneOpened&& !isPumpTwoOpened && !isPumpThreeOpened){
            text_pump.text = narasiToScan
        }
    }

    override fun handleResult(rawResult: Result?) {
        when(rawResult!!.text){
            "pump_one" -> {
                pumpOne.setValue("On")
                text_pump.text = "Anda sedang merefill dengan $titleAirPutih"
            }
            "pump_two" -> {
                pumpTwo.setValue("On")
                text_pump.text = "Anda sedang merefill dengan $titleAirJeruk"
            }
            "pump_three" -> {
                pumpThree.setValue("On")
                text_pump.text = "Anda sedang merefill dengan $titleAirTeh"
            }
        }
    }
}
