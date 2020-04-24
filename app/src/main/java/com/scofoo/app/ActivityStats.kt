package com.scofoo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ActivityStats: AppCompatActivity() {

    private val databaseHandler = DatabaseHandler(this)
    private var dmcGoals: MutableList<DMCGoal>? = null


    private var today: TextView? = null
    private var actualDay: TextView? = null
    private var oldestEntry: TextView? = null
    private var newestEntry: TextView? = null
    private var daysBetween: TextView? = null
    private var missingDays: TextView? = null
    private var daysMeat: TextView? = null
    private var daysVeggi: TextView? = null
    private var daysVegan: TextView? = null
    private var goalTable: TableLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        today = findViewById(R.id.today)
        actualDay = findViewById(R.id.actualDay)
        oldestEntry = findViewById(R.id.oldestEntry)
        newestEntry = findViewById(R.id.newestEntry)
        daysBetween = findViewById(R.id.daysBetween)
        missingDays = findViewById(R.id.missingDays)
        daysMeat = findViewById(R.id.daysMeat)
        daysVeggi = findViewById(R.id.daysVeggi)
        daysVegan = findViewById(R.id.daysVegan)
        goalTable = findViewById(R.id.goalTable)


        today?.text = intent.getStringExtra("today")
        actualDay?.text = intent.getStringExtra("actualDay")
        oldestEntry?.text = intent.getStringExtra("oldestEntry")
        newestEntry?.text = intent.getStringExtra("newestEntry")
        daysBetween?.text = intent.getStringExtra("daysBetween")
        missingDays?.text = intent.getStringExtra("missingDays")
        daysMeat?.text = intent.getStringExtra("daysMeat")
        daysVeggi?.text = intent.getStringExtra("daysVeggi")
        daysVegan?.text = intent.getStringExtra("daysVegan")



        //get all the goals
        dmcGoals = databaseHandler.selectGoals()

        //val dmcGoals: Array<DMCGoal> = intent.getSerializableExtra("dmcGoals") as Array<DMCGoal>



        val buttonAddGoal: Button = findViewById(R.id.buttonAddGoal)

        buttonAddGoal?.setOnClickListener {

            //setup the intent
            val intent = Intent(this, ActivityGoal::class.java)
            //come up with the activity

            startActivityForResult(intent, 1212)

        }

        updateTableGoals()


    }

    // This method is called when the second activity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 1212) {
            if (resultCode == Activity.RESULT_OK) {

                // Get String data from Intent


                var dmcGoal: DMCGoal = data!!.getSerializableExtra("dmcGoal") as DMCGoal

                databaseHandler.insertGoal(dmcGoal)

                dmcGoals = databaseHandler.selectGoals()

                updateTableGoals()

            }
        }
    }

    private fun updateTableGoals() {
        var rowCount = 1

        for(dmcGoal in dmcGoals!!) {
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
            tv6.text = getGoalValue(dmcGoal).toString()
            tv7.text = getGoalAchieved(dmcGoal).toString()

            tv8.text = when(getGoalAchievable(dmcGoal)) {
                null -> {
                     "-"
                }
                else -> {
                    getGoalAchievable(dmcGoal).toString()
                }
            }


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

            goalTable?.addView(tr)

            rowCount++

        }
    }

    private fun getGoalValue(dmcGoal: DMCGoal): Int {
        var goalValue = databaseHandler.getDayCountOfChoice(dmcGoal.choice, dmcGoal.start.toString(), dmcGoal.end.toString())

        if (dmcGoal.choice == 1) {
            //vegan days count as veggi too
            goalValue += databaseHandler.getDayCountOfChoice(2, dmcGoal.start.toString(), dmcGoal.end.toString())
        }

        return goalValue
    }

    private fun getGoalAchieved(dmcGoal: DMCGoal): Boolean {
        when (dmcGoal.type) {
            0 -> {
                //handling min relation

                return getGoalValue(dmcGoal) >= dmcGoal.target
            }
            1 -> {
                //handling max relation

                return getGoalValue(dmcGoal) <= dmcGoal.target
            }
            else -> {
                //  we have faith -> it is 2!
                // handling equals relation

                return getGoalValue(dmcGoal) == dmcGoal.target
            }
        }
    }

    private fun getGoalAchievable(dmcGoal: DMCGoal): Boolean? {

        val goalDaysMissingList = databaseHandler.getMissingDays(dmcGoal.start, dmcGoal.end)
        val goalDaysMissing = goalDaysMissingList?.count() ?: 0

        if(!getGoalAchieved(dmcGoal)) {
            //let's see if it's still achievable


            val goalDaysPotential = getGoalValue(dmcGoal) + goalDaysMissing

            when (dmcGoal.type) {
                0 -> {
                    //min relation

                    return goalDaysPotential >= dmcGoal.target

                }
                1 -> {
                    //if it's a max relation, and it's not achieved there is nothing you can do about the past

                }
                2 -> {
                    //now it's important to know if we are under our score or above

                    //dmcGoal.achievable = goalDaysPotential >= dmcGoal.value

                }
            }
        }

        return null

    }

}