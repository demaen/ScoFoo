package com.scofoo.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityStats: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val zeile0: TextView = findViewById(R.id.zeile0)
        val zeile1: TextView = findViewById(R.id.zeile1)
        val zeile2: TextView = findViewById(R.id.zeile2)
        val zeile3: TextView = findViewById(R.id.zeile3)
        val zeile4: TextView = findViewById(R.id.zeile4)
        val zeile5: TextView = findViewById(R.id.zeile5)
        val zeile6: TextView = findViewById(R.id.zeile6)
        val zeile7: TextView = findViewById(R.id.zeile7)
        val zeile8: TextView = findViewById(R.id.zeile8)

        zeile0.text = intent.getStringExtra("today")
        zeile1.text = intent.getStringExtra("actualDay")
        zeile2.text = intent.getStringExtra("oldestEntry")
        zeile3.text = intent.getStringExtra("newestEntry")
        zeile4.text = intent.getStringExtra("daysBetween")
        zeile5.text = intent.getStringExtra("missingDays")
        zeile6.text = intent.getStringExtra("daysMeat")
        zeile7.text = intent.getStringExtra("daysVeggi")
        zeile8.text = intent.getStringExtra("daysVegan")

    }
}