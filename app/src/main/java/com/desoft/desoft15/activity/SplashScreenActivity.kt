package com.desoft.desoft15.activity

import android.R.anim
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.desoft.desoft15.databinding.ActivitySlashScreenBinding
import com.desoft.desoft15.utils.SunmiPrintHelper
import java.io.File


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivitySlashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        cargarPermiso()
        val file = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "fotos"
        )
        if (!file.exists()) {
            file.mkdir()
        }
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this)
    }

    private fun cargarPermiso() {
        val permissions = ArrayList<String>()
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )!= PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.WRITE_EXTERNAL_STORAGE")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.READ_EXTERNAL_STORAGE")
        }
        if( ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_PHONE_STATE"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.READ_PHONE_STATE")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.CAMERA"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.CAMERA")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.ACCESS_COARSE_LOCATION")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.ACCESS_FINE_LOCATION")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_INTERNAL_STORAGE"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.READ_INTERNAL_STORAGE")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_INTERNAL_STORAGE"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.WRITE_INTERNAL_STORAGEE")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.MANAGE_EXTERNAL_STORAGE"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.MANAGE_EXTERNAL_STORAGE")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_DOWNLOAD_MANAGER"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.ACCESS_DOWNLOAD_MANAGER")
        }
        if(ContextCompat.checkSelfPermission(
                this,
                "android.permission.BLUETOOTH"
            ) != PackageManager.PERMISSION_GRANTED){
            permissions.add("android.permission.BLUETOOTH")
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            if(ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_CONNECT"
                ) != PackageManager.PERMISSION_GRANTED){
                permissions.add("android.permission.BLUETOOTH_CONNECT")
            }
            if(ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_SCAN"
                ) != PackageManager.PERMISSION_GRANTED){
                permissions.add("android.permission.BLUETOOTH_SCAN")
            }
        }else{
            if(ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_ADMIN"
                ) != PackageManager.PERMISSION_GRANTED){
                permissions.add("android.permission.BLUETOOTH_ADMIN")
            }
        }
        if(permissions.isEmpty()){
            val animation =
                AnimationUtils.loadAnimation(applicationContext,anim.fade_in)
            animation.duration = 1050
            binding.imageView.startAnimation(animation)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    val mainintent=Intent(this@SplashScreenActivity,
                        MainActivity::class.java)
                    startActivity(mainintent)
                    this@SplashScreenActivity.finish()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }else{
            val perm = arrayOfNulls<String>(permissions.size)
            permissions.toArray(perm)
            ActivityCompat.requestPermissions(this, perm, 20)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            20 -> {
                val animation =
                    AnimationUtils.loadAnimation(applicationContext,anim.fade_in)
                animation.duration = 1050
                this.binding.imageView.startAnimation(animation)
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        val mainintent=Intent(this@SplashScreenActivity,
                            MainActivity::class.java)
                        startActivity(mainintent)

                        this@SplashScreenActivity.finish()
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
