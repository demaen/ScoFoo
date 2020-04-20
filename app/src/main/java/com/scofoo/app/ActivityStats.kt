package com.scofoo.app

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt


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
        val goalTable: TableLayout = findViewById(R.id.goalTable)

        today.text = intent.getStringExtra("today")
        actualDay.text = intent.getStringExtra("actualDay")
        oldestEntry.text = intent.getStringExtra("oldestEntry")
        newestEntry.text = intent.getStringExtra("newestEntry")
        daysBetween.text = intent.getStringExtra("daysBetween")
        missingDays.text = intent.getStringExtra("missingDays")
        daysMeat.text = intent.getStringExtra("daysMeat")
        daysVeggi.text = intent.getStringExtra("daysVeggi")
        daysVegan.text = intent.getStringExtra("daysVegan")




        val dmcGoals: Array<DMCGoal> = intent.getSerializableExtra("dmcGoals") as Array<DMCGoal>

        var rowCount = 1

        for(dmcGoal in dmcGoals) {
            val tr = TableRow(this)

            val tv1 = TextView(this)
            val tv2 = TextView(this)
            val tv3 = TextView(this)
            val tv4 = TextView(this)
            val tv5 = TextView(this)
            val tv6 = TextView(this)
            val tv7 = TextView(this)
            val tv8 = TextView(this)

            tv1.text = dmcGoal.start.toString()
            tv2.text = dmcGoal.end.toString()
            tv3.text = when(dmcGoal.choice) { 0 -> {"Meat"} 1 -> {"Veggi"} 2 -> {"Vegan"} else -> {""} }
            tv4.text = when(dmcGoal.type) { 0 -> {">="} 1 -> {"<="} 2 -> {"=="} else -> {""} }
            tv5.text = dmcGoal.target.toString()
            tv6.text = dmcGoal.value.toString()
            tv7.text = dmcGoal.achieved.toString()
            tv8.text = when(dmcGoal.achievable) { null -> {"-"} else -> {dmcGoal.achievable.toString()} }

            tv1.textSize = 10.0F
            tv2.textSize = 10.0F
            tv3.textSize = 10.0F
            tv4.textSize = 10.0F
            tv5.textSize = 10.0F
            tv6.textSize = 10.0F
            tv7.textSize = 10.0F
            tv8.textSize = 10.0F

            tr.addView(tv1)
            tr.addView(tv2)
            tr.addView(tv3)
            tr.addView(tv4)
            tr.addView(tv5)
            tr.addView(tv6)
            tr.addView(tv7)
            tr.addView(tv8)

            if(rowCount % 2 == 0) {
                //even
            } else {
                //odd
                tr.background = getDrawable(R.color.colorMildGray)
            }

            tr.gravity = Gravity.CENTER
            tr.orientation = LinearLayout.HORIZONTAL

            goalTable.addView(tr)

            rowCount++

        }




    }
}