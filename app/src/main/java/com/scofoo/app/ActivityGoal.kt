package com.scofoo.app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class ActivityGoal: AppCompatActivity() {

    private var dmcGoal: DMCGoal? = null

    private var firstDayOfTheYear: LocalDate = LocalDate.ofYearDay(LocalDate.now().year, 1)
    private var start:String = firstDayOfTheYear.toString()
    private var startYear: Int = firstDayOfTheYear.year
    private var startMonth: Int = firstDayOfTheYear.monthValue
    private var startDay: Int = firstDayOfTheYear.dayOfMonth

    private var lastDayOfTheYear: LocalDate = LocalDate.ofYearDay(LocalDate.now().year, LocalDate.parse(LocalDate.now().year.toString() + "-12-31").dayOfYear)
    private var end:String = lastDayOfTheYear.toString()
    private var endYear: Int = lastDayOfTheYear.year
    private var endMonth: Int = lastDayOfTheYear.monthValue
    private var endDay: Int = lastDayOfTheYear.dayOfMonth

    private var radioGroupChoice: RadioGroup? = null
    private var radioButtonChoice: RadioButton? = null
    private var choice: Int? = null


    private var radioGroupType: RadioGroup? = null
    private var radioButtonType: RadioButton? = null
    private var type: Int? = null

    private var target: Int? = null



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        radioGroupChoice = findViewById(R.id.radioGroupChoice)
        radioGroupType = findViewById(R.id.radioGroupType)

        var buttonStart: Button = findViewById(R.id.start)
        buttonStart.text = start

        buttonStart?.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view,
                    selectedYear,
                    selectedMonth,
                    selectedDay ->

                //we have to build the String properly, because those values are int and need leading "0" maybe
                var selectedDayString: String = "$selectedYear-"

                //month starts with 0 = jan ... thats why incrementing it
                //every month under 10 need a leading "0"
                if(selectedMonth+1 < 10) {
                    selectedDayString += "0"
                }

                //Append the month
                selectedDayString += (selectedMonth+1).toString() + "-"

                //same for the days, but days start with 1 in contrast with months
                if(selectedDay < 10) {
                    selectedDayString += "0"
                }

                //append the days
                selectedDayString += (selectedDay).toString()

                //we need to set these so that the picker highlights the right day when opened again
                startYear = selectedYear
                startMonth = selectedMonth+1
                startDay = selectedDay

                //let's set the selected day
                start = selectedDayString

                //now we need to get the choice from the database and how the buttons should be highlighted
                buttonStart.text = start

            }, startYear, startMonth-1, startDay)

            dpd.show()

        }




        var buttonEnd: Button = findViewById(R.id.end)
        buttonEnd.text = end

        buttonEnd?.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view,
                    selectedYear,
                    selectedMonth,
                    selectedDay ->

                //we have to build the String properly, because those values are int and need leading "0" maybe
                var selectedDayString: String = "$selectedYear-"

                //month starts with 0 = jan ... thats why incrementing it
                //every month under 10 need a leading "0"
                if(selectedMonth+1 < 10) {
                    selectedDayString += "0"
                }

                //Append the month
                selectedDayString += (selectedMonth+1).toString() + "-"

                //same for the days, but days start with 1 in contrast with months
                if(selectedDay < 10) {
                    selectedDayString += "0"
                }

                //append the days
                selectedDayString += (selectedDay).toString()

                //we need to set these so that the picker highlights the right day when opened again
                endYear = selectedYear
                endMonth = selectedMonth+1
                endDay = selectedDay

                //let's set the selected day
                end = selectedDayString

                //now we need to get the choice from the database and how the buttons should be highlighted
                buttonEnd.text = end

            }, endYear, endMonth-1, endDay)

            dpd.show()

        }

        var targetInput:EditText = findViewById(R.id.target)

        var buttonSave: Button = findViewById(R.id.buttonSave)
        buttonSave?.setOnClickListener {

            if(targetInput.text.toString() != "") {
                target = targetInput.text.toString().toInt()
            }

            println(choice)
            println(type)
            println(target)

            if(choice != null && type != null && target != null) {

                dmcGoal = DMCGoal(choice!!, type!!, target!!, LocalDate.parse(start), LocalDate.parse(end))

                if(dmcGoal != null) {

                    //alright
                    val intent = Intent()
                    intent.putExtra("dmcGoal", dmcGoal)
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }

            }



        }

    }

    fun checkRadioChoice(view: View) {
        var radioIdChoice = radioGroupChoice?.checkedRadioButtonId

        radioButtonChoice = radioIdChoice?.let { findViewById(it) }

        when (radioButtonChoice?.text) {
            "Meat" -> {
                choice = 0
            }
            "Veggi" -> {
                choice = 1
            }
            "Vegan" -> {
                choice = 2
            }
        }

    }

    fun checkRadioType(view: View) {
        var radioIdType = radioGroupType?.checkedRadioButtonId

        radioButtonType = radioIdType?.let { findViewById(it) }


        when (radioButtonType?.text) {
            "Min" -> {
                type = 0
            }
            "Max" -> {
                type = 1
            }
            "Equal" -> {
                type = 2
            }
        }

    }
}