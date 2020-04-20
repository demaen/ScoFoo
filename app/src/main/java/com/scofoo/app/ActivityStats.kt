package com.scofoo.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityStats: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val today: TextView = findViewById(R.id.today)
        val actualDay: TextView = findViewById(R.id.actualDay)
        val oldestEntry: TextView = findViewById(R.id.oldestEntry)
        val newestEntry: TextView = findViewById(R.id.newestEntry)
        val daysBetween: TextView = findViewById(R.id.daysBetween)
        val missingDays: TextView = findViewById(R.id.missingDays)
        val daysMeat: TextView = findViewById(R.id.daysMeat)
        val daysVeggi: TextView = findViewById(R.id.daysVeggi)
        val daysVegan: TextView = findViewById(R.id.daysVegan)
        val goalDateStart: TextView = findViewById(R.id.goalDateStart)
        val goalDateEnd: TextView = findViewById(R.id.goalDateEnd)
        val goalCountMeatMax: TextView = findViewById(R.id.goalCountMeatMax)
        val goalCountVeggiMin: TextView = findViewById(R.id.goalCountVeggiMin)
        val goalCountVeganMin: TextView = findViewById(R.id.goalCountVeganMin)
        val goalDaysBetween: TextView = findViewById(R.id.goalDaysBetween)
        val goalMissingDays: TextView = findViewById(R.id.goalMissingDays)
        val goalDaysMeat: TextView = findViewById(R.id.goalDaysMeat)
        val goalDaysVeggi: TextView = findViewById(R.id.goalDaysVeggi)
        val goalDaysVegan: TextView = findViewById(R.id.goalDaysVegan)
        val goalMeat: TextView = findViewById(R.id.goalMeat)
        val goalVeggi: TextView = findViewById(R.id.goalVeggi)
        val goalVegan: TextView = findViewById(R.id.goalVegan)

        today.text = intent.getStringExtra("today")
        actualDay.text = intent.getStringExtra("actualDay")
        oldestEntry.text = intent.getStringExtra("oldestEntry")
        newestEntry.text = intent.getStringExtra("newestEntry")
        daysBetween.text = intent.getStringExtra("daysBetween")
        missingDays.text = intent.getStringExtra("missingDays")
        daysMeat.text = intent.getStringExtra("daysMeat")
        daysVeggi.text = intent.getStringExtra("daysVeggi")
        daysVegan.text = intent.getStringExtra("daysVegan")
        goalDateStart.text = intent.getStringExtra("goalDateStart")
        goalDateEnd.text = intent.getStringExtra("goalDateEnd")
        goalCountMeatMax.text = intent.getStringExtra("goalCountMeatMax")
        goalCountVeggiMin.text = intent.getStringExtra("goalCountVeggiMin")
        goalCountVeganMin.text = intent.getStringExtra("goalCountVeganMin")
        goalDaysBetween.text = intent.getStringExtra("goalDaysBetween")
        goalMissingDays.text = intent.getStringExtra("goalMissingDays")
        goalDaysMeat.text = intent.getStringExtra("goalDaysMeat")
        goalDaysVeggi.text = intent.getStringExtra("goalDaysVeggi")
        goalDaysVegan.text = intent.getStringExtra("goalDaysVegan")
        goalMeat.text = intent.getStringExtra("goalMeat")
        goalVeggi.text = intent.getStringExtra("goalVeggi")
        goalVegan.text = intent.getStringExtra("goalVegan")

    }
}