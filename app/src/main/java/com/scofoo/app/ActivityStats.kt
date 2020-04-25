package com.scofoo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stats.*

class ActivityStats: AppCompatActivity() {

    private val databaseHandler = DatabaseHandler(this)

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

        today = findViewById(R.id.today)
        selectedDateView = findViewById(R.id.selectedDate)
        oldestEntry = findViewById(R.id.oldestEntry)
        newestEntry = findViewById(R.id.newestEntry)
        daysBetween = findViewById(R.id.daysBetween)
        missingDays = findViewById(R.id.missingDays)
        daysMeat = findViewById(R.id.daysMeat)
        daysVeggi = findViewById(R.id.daysVeggi)
        daysVegan = findViewById(R.id.daysVegan)

        today?.text = intent.getStringExtra("today")
        selectedDateView?.text = intent.getStringExtra("selectedDate")
        oldestEntry?.text = intent.getStringExtra("oldestEntry")
        newestEntry?.text = intent.getStringExtra("newestEntry")
        daysBetween?.text = intent.getStringExtra("daysBetween")
        missingDays?.text = intent.getStringExtra("missingDays")
        daysMeat?.text = intent.getStringExtra("daysMeat")
        daysVeggi?.text = intent.getStringExtra("daysVeggi")
        daysVegan?.text = intent.getStringExtra("daysVegan")

        buttonAddGoal = findViewById(R.id.buttonAddGoal)

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

            val oldTable = findViewById<TableLayout>(viewId)

            if (oldTable != null) {
                //we should delete it first
                (oldTable.parent as ViewGroup).removeView(oldTable)

            }

        }

        dmcGoals = databaseHandler.selectGoals()

        val tableLayout by lazy { TableLayout(this) }

        val lp = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        tableLayout.apply {
            layoutParams = lp
            isShrinkAllColumns = false
        }

        //generate new viewId
        viewId = View.generateViewId()

        //set it
        tableLayout.id = viewId

        if(dmcGoals != null) {

            for (dmcGoal in dmcGoals!!) {

                val row = TableRow(this)

                row.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val tv1 = TextView(this)
                tv1.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = dmcGoal.start.toString()
                }

                val tv2 = TextView(this)
                tv2.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = dmcGoal.end.toString()
                }

                val tv3 = TextView(this)
                tv3.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = when(dmcGoal.choice) { 0 -> {"Meat"} 1 -> {"Veggi"} 2 -> {"Vegan"} else -> {""} }
                }

                val tv4 = TextView(this)
                tv4.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = when(dmcGoal.type) { 0 -> {">="} 1 -> {"<="} 2 -> {"=="} else -> {""} }
                }

                val tv5 = TextView(this)
                tv5.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = dmcGoal.target.toString()
                }

                val tv6 = TextView(this)
                tv6.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = getGoalValue(dmcGoal).toString()
                }

                val tv7 = TextView(this)
                tv7.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = getGoalAchieved(dmcGoal).toString()
                }

                val tv8 = TextView(this)
                tv8.apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    text = when(getGoalAchievable(dmcGoal)) {
                        null -> {
                            "-"
                        }
                        else -> {
                            getGoalAchievable(dmcGoal).toString()
                        }
                    }
                }

                row.addView(tv1)
                row.addView(tv2)
                row.addView(tv3)
                row.addView(tv4)
                row.addView(tv5)
                row.addView(tv6)
                row.addView(tv7)
                row.addView(tv8)

                tableLayout.addView(row)
            }

            linearLayout.addView(tableLayout)

        }
    }

}