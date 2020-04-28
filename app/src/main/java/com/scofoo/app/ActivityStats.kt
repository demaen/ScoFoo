package com.scofoo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stats.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ActivityStats: AppCompatActivity() {

    private val LOGTAG = "9TGbQcB5 ActivityStats"

    private val databaseHandler = DatabaseHandler(this, LOGTAG)

    private var dmcGoals: MutableList<DMCGoal>? = null

    private var today: TextView? = null
    private var selectedDateView: TextView? = null
    private var oldestEntry: TextView? = null
    private var newestEntry: TextView? = null
    private var daysBetween: TextView? = null
    private var missingDays: TextView? = null
    private var daysMeat: TextView? = null
    private var daysVeggi: TextView? = null
    private var daysVegan: TextView? = null

    private var buttonAddGoal: Button? = null

    private var viewId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        //find all the views
        today = findViewById(R.id.today)
        selectedDateView = findViewById(R.id.selectedDate)
        oldestEntry = findViewById(R.id.oldestEntry)
        newestEntry = findViewById(R.id.newestEntry)
        daysBetween = findViewById(R.id.daysBetween)
        missingDays = findViewById(R.id.missingDays)
        daysMeat = findViewById(R.id.daysMeat)
        daysVeggi = findViewById(R.id.daysVeggi)
        daysVegan = findViewById(R.id.daysVegan)
        buttonAddGoal = findViewById(R.id.buttonAddGoal)

        //what is today's date?
        today?.text = LocalDate.now().toString()

        //get the selected date
        selectedDateView?.text = intent.getStringExtra("selectedDate")

        //get the oldest entry
        val dmcOldestEntry: DMCChoice? = databaseHandler.getOldestEntry()

        if(dmcOldestEntry != null) {
            oldestEntry?.text = dmcOldestEntry.day
        }

        //get the newest entry
        val dmcNewestEntry: DMCChoice? = databaseHandler.getNewestEntry()

        if(dmcNewestEntry != null) {
            newestEntry?.text = dmcNewestEntry.day
        }

        //how many days between oldest and newest?
        if(dmcOldestEntry != null && dmcNewestEntry != null) {
            val dateBefore: LocalDate = LocalDate.parse(dmcOldestEntry.day)
            val dateAfter: LocalDate = LocalDate.parse(dmcNewestEntry.day)
            val daysBetweenCount: Long = ChronoUnit.DAYS.between(dateBefore, dateAfter.plusDays(1))

            daysBetween?.text = daysBetweenCount.toString()
        }

        //get the missing days
        if(dmcOldestEntry != null && dmcNewestEntry != null) {

            //we will get a list of dates (the missing ones)
            val missingDaysCount = databaseHandler.getMissingDaysCount(LocalDate.parse(dmcOldestEntry.day), LocalDate.parse(dmcNewestEntry.day))

            missingDays?.text = missingDaysCount.toString()

        } else {
            //if db is empty
            missingDays?.text = ""
        }

        //count days meat
        val daysMeatCount = databaseHandler.getDayCountOfChoice(0)
        daysMeat?.text = daysMeatCount.toString()

        //count days veggi
        val daysVeggiCount = databaseHandler.getDayCountOfChoice(1)

        //count days vegan
        val daysVeganCount = databaseHandler.getDayCountOfChoice(2)

        //of course vegan days are veggi too
        val daysVeggiInclVegan = daysVeggiCount + daysVeganCount
        daysVeggi?.text = "$daysVeggiCount (+ $daysVeganCount = $daysVeggiInclVegan)"

        //now set the vegan days
        daysVegan?.text = daysVeganCount.toString()


        //get all the goals
        createTable()

        //when we hit the add goal button
        buttonAddGoal?.setOnClickListener {

            //setup the intent
            val intent = Intent(this, ActivityGoalAdd::class.java)
            //come up with the activity

            startActivityForResult(intent, 1212)

        }

    }

    // This method is called when the second activity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 1212) {
            if (resultCode == Activity.RESULT_OK) {

                // Get String data from Intent
                val dmcGoal: DMCGoal = data!!.getSerializableExtra("dmcGoal") as DMCGoal

                //Insert the goal into the db
                databaseHandler.insertGoal(dmcGoal)

                //create the goal table and append it
                createTable()

            }
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

        val goalDaysMissing = databaseHandler.getMissingDaysCount(dmcGoal.start, dmcGoal.end)

        //if it's not achieved yet..
        if(!getGoalAchieved(dmcGoal)) {
            //..let's see if it's still achievable

            //all remaining days plus achieved goals until now equals our potential
            val goalDaysPotential = getGoalValue(dmcGoal) + goalDaysMissing

            when (dmcGoal.type) {
                0 -> {
                    //min relation

                    return goalDaysPotential >= dmcGoal.target

                }
                1 -> {
                    //if it's a max relation, and it's not achieved there is nothing you can do about the past

                    return false
                }
                2 -> {
                    //now it's important to know if we are under our score or above

                    //dmcGoal.achievable = goalDaysPotential >= dmcGoal.value

                }
            }
        }

        return null

    }

    private fun createTable() {

        if(viewId != 0) {
            //initially viewId is set to 0
            //the following part is important when there already is a table

            //so let's find this old table
            val oldTable = findViewById<TableLayout>(viewId)

            if (oldTable != null) {
                //we should delete it first
                (oldTable.parent as ViewGroup).removeView(oldTable)
            }

        }

        //what goals do we have?
        dmcGoals = databaseHandler.selectGoals()

        //define a new table
        val tableLayout by lazy { TableLayout(this) }

        //we should apply some simple layout rules
        val lp = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        tableLayout.apply {
            layoutParams = lp
            isShrinkAllColumns = false
        }

        //generate a new random viewId
        viewId = View.generateViewId()

        //set it
        tableLayout.id = viewId

        //we need some goals to proceed
        if(dmcGoals != null) {

            for (dmcGoal in dmcGoals!!) {
                //foreach goal we do this:


                Log.v(LOGTAG, "Starting to process a goal:")

                //generate a new row
                val row = TableRow(this)

                //add some simple layout rules
                row.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                row.gravity = Gravity.CENTER

                //first column: Start
                val tv1 = TextView(this)
                tv1.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.19f
                    )
                    text = dmcGoal.start.toString()
                    gravity = Gravity.CENTER
                }
                row.addView(tv1)

                //second column: End
                val tv2 = TextView(this)
                tv2.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.19f
                    )
                    text = dmcGoal.end.toString()
                    gravity = Gravity.CENTER
                }
                row.addView(tv2)

                //third column: Choice
                val tv3 = TextView(this)
                tv3.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.09f
                    )
                    text = when(dmcGoal.choice) { 0 -> {"Meat"} 1 -> {"Veggi"} 2 -> {"Vegan"} else -> {""} }
                    gravity = Gravity.CENTER
                }
                row.addView(tv3)

                //4th col: Value (what we have achieved so far)
                val tv4 = TextView(this)
                tv4.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.07f
                    )
                    text = getGoalValue(dmcGoal).toString()
                    gravity = Gravity.CENTER
                }
                row.addView(tv4)

                //5th col: Type
                val tv5 = TextView(this)
                tv5.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.08f
                    )
                    text = when(dmcGoal.type) { 0 -> {">="} 1 -> {"<="} 2 -> {"=="} else -> {""} }
                    gravity = Gravity.CENTER
                }
                row.addView(tv5)

                //6th col: Target
                val tv6 = TextView(this)
                tv6.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.07f
                    )
                    text = dmcGoal.target.toString()
                    gravity = Gravity.CENTER
                }
                row.addView(tv6)

                //7th col: have we already achieved our goal?
                val tv7 = TextView(this)
                val goalAchieved = getGoalAchieved(dmcGoal)
                tv7.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.1f
                    )
                    text = goalAchieved.toString()
                    gravity = Gravity.CENTER
                }
                row.addView(tv7)

                //8th col: is it still achievable?
                val getGoalAchievable = getGoalAchievable(dmcGoal)

                val tv8 = TextView(this)
                tv8.apply {
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.10f
                    )
                    text = when(getGoalAchievable) {null -> {"-"} else -> {getGoalAchievable.toString()}}
                    gravity = Gravity.CENTER
                }
                row.addView(tv8)

                tableLayout.addView(row)


                Log.v(LOGTAG, "Finished processing a goal.")

            }

            linearLayout.addView(tableLayout)

        }
    }

}