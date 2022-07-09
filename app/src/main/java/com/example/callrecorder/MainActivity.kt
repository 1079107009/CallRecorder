package com.example.callrecorder

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsProvider
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import java.io.File

class MainActivity : AppCompatActivity() {

    private val REQUEST_MEDIA_OPEN = 1
    private val liveData = MutableLiveData<Int>()
    private var service: TelephonyManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_call).setOnClickListener {
            dialPhoneNumber("10086")
        }
        service = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        service?.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                Log.d("MainActivity", "onCallStateChanged: $state")
                if (phoneNumber != null) {
                    liveData.value = state
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
        liveData.observe(this) { state ->
            if (state == 0) {
                showRecorderFile()
            }
        }
    }

    private fun showRecorderFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivityForResult(intent, REQUEST_MEDIA_OPEN)
        } catch (ex: Exception) {
            Toast.makeText(this, "应用未找到", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_OPEN && resultCode == Activity.RESULT_OK) {
            showUploadDialog(data?.data)
        }
    }

    private fun showUploadDialog(uri: Uri?) {
        if (uri == null) {
            return
        }
        AlertDialog.Builder(this).apply {
            setTitle("我要开始上传啦")
            setMessage("路径：${uri.path}")
            setPositiveButton(
                "确定"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            setNegativeButton(
                "取消"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(this, "应用未找到", Toast.LENGTH_SHORT).show()
        }
    }

}