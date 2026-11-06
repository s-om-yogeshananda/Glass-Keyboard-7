package com.glasskb.keyboard

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle

/** কীবোর্ড নিজে অনুমতি চাইতে পারে না, তাই এই স্বচ্ছ অ্যাক্টিভিটি মাইক্রোফোনের অনুমতি চায়। */
class VoicePermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            finish()
            return
        }
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 7)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }
}
