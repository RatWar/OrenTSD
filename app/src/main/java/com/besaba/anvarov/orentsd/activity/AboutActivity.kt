package com.besaba.anvarov.orentsd.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.besaba.anvarov.orentsd.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        button.setOnClickListener { finish() }
    }
}
