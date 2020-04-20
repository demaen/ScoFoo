package com.scofoo.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Calendar.*


class MainActivity: AppCompatActivity() {

    private val databaseHandler = DatabaseHandler(this)

    //allowed choices: 0 meat 1 veggi 2 vegan
    private var actualChoice: Int? = null

    private val calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)
    private var month = calendar.get(Calendar.MONTH)+1
    private var day = calendar.get(Calendar.DAY_OF_MONTH)

    private var actualDay: String = ""
    private var today: String = ""

    var x1: Float? = 0.toFloat()
    var x2: Float? = 0.toFloat()

    private var zeile0: TextView? = null
    private var zeile1: TextView? = null
    private var zeile2: TextView? = null
    private var zeile3: TextView? = null
    private var zeile4: TextView? = null


    private var buttonMeat: Button? = null
    private var buttonVeggi: Button? = null
    private var buttonVegan: Button? = null
    private var buttonNotification: Button? = null
    private var buttonGoToStats: Button? = null

    private var labelDate: TextView? = null

    private var displaySizeX = 0;

    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get the Buttons
        buttonMeat = findViewById(R.id.buttonMeat)
        buttonVeggi = findViewById(R.id.buttonVeggi)
        buttonVegan = findViewById(R.id.buttonVegan)
        buttonNotification = findViewById(R.id.buttonNotification)
        buttonGoToStats = findViewById(R.id.buttonGoToStats)

        //get the table elements
        zeile0 = findViewById(R.id.zeile0)
        zeile1 = findViewById(R.id.zeile1)
        zeile2 = findViewById(R.id.zeile2)
        zeile3 = findViewById(R.id.zeile3)
        zeile4 = findViewById(R.id.zeile4)

        //get the datepickerlabelbutton
        labelDate = findViewById(R.id.date)

        //let's find out how big our screen is
        val display: Display = windowManager.defaultDisplay
        val size: Point = Point()
        display.getSize(size)
        displaySizeX = size.x

        //let's take care of the notifications
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(
            "my_channel_01",
            "ScoFoo Notifications",
            "All Notifications")


        //Set today's date
        today = LocalDate.now().toString()
        actualDay = today

        //!!! Be aware of that next function @todo deprecated -> delete it
        //databaseHandler.updateDateFormat()

        //now we need to get the choice from the database and how the buttons should be highlighted
        updateDayChoice()

        ///////////////////////////
        //Big section with Listeners

        buttonMeat?.setOnClickListener {

            if(actualChoice != 0) {

                actualChoice = 0

                setButtonDesigns()

                saveChoice(actualDay, 0)

            }

        }

        buttonVeggi?.setOnClickListener {

            if(actualChoice != 1) {

                actualChoice = 1

                setButtonDesigns()

                saveChoice(actualDay, 1)
            }
        }

        buttonVegan?.setOnClickListener {

            if(actualChoice != 2) {

                actualChoice = 2

                setButtonDesigns()

                saveChoice(actualDay, 2)
            }
        }

        buttonNotification?.setOnClickListener {
            sendNotification()
        }

        labelDate?.setOnClickListener {

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
                    year = selectedYear
                    month = selectedMonth+1
                    day = selectedDay

                    //let's set the selected day
                    actualDay = selectedDayString

                    //now we need to get the choice from the database and how the buttons should be highlighted
                    updateDayChoice()

            }, year, month-1, day)

            dpd.show()

        }

        buttonGoToStats?.setOnClickListener {

            HelperStats(this, databaseHandler, actualDay)

        }



    }

    override fun onTouchEvent(event: MotionEvent): Boolean {



        var action = event.action

        var actionMode = ""
        var direction = ""
        var difference = 0.toFloat()

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //this is when the screen is tapped

                actionMode = "DOWN"
                x1 = event.rawX     //we started here
                x2 = 0.toFloat()    //just safety reasons



            }
            MotionEvent.ACTION_MOVE -> {
                //this is when the finger swipes - in any direction

                actionMode = "MOVE"
                x2 = event.rawX     //we moved over here

            }
            MotionEvent.ACTION_UP -> {
                //this is when the finger goes up - I guess

                actionMode = "UP"
                x2 = event.rawX     //and we finally ended up over here


                if(x1!! > x2!!) {
                    //swipe from right to left

                    direction = "left"
                    difference = x1!! - x2!!

                } else {
                    //swipe from left to right

                    direction = "right"
                    difference = x2!! - x1!!

                }

                //let's reset everything
                x1 = 0.toFloat()
                x2 = 0.toFloat()

            }
            else -> {

                //we ignore any other cases

            }
        }

        //what should we do about the results?

        //maybe we should beat some threshold
        //difference > 1/2 display width or sth like that
        if(difference > displaySizeX/10) {

            var newDate = LocalDate.parse(actualDay)

            if(direction == "right") {
                //go back in time
                newDate = newDate.minusDays(1)

            } else if (direction == "left") {
                //go next day
                newDate = newDate.plusDays(1)

            }


            println("$actualDay ($year-$month-$day)")

            year = newDate.year
            month = newDate.month.value
            day = newDate.dayOfMonth
            actualDay = newDate.toString()


            println("$actualDay ($year-$month-$day)")

            updateDayChoice()

        }


        zeile0?.text = actionMode
        zeile1?.text = x1.toString()
        zeile2?.text = x2.toString()
        zeile3?.text = direction
        zeile4?.text = difference.toString()


        // tell the View that we handled the event
        return true


    }

    private fun updateDayChoice() {
        labelDate?.text = actualDay;

        //what is the actual choice if any?
        actualChoice = databaseHandler.selectChoice(actualDay)

        println("ActualChoice is $actualChoice")

        setButtonDesigns()

    }

    private fun setButtonDesigns() {
        when(actualChoice) {
            null -> {
                //nothing yet
                buttonMeat?.setTextColor(getColor(R.color.colorDark))
                buttonVeggi?.setTextColor(getColor(R.color.colorDark))
                buttonVegan?.setTextColor(getColor(R.color.colorDark))
            }
            0 -> {
                //meat
                buttonMeat?.setTextColor(getColor(R.color.colorAccent))
                buttonVeggi?.setTextColor(getColor(R.color.colorDark))
                buttonVegan?.setTextColor(getColor(R.color.colorDark))
            }
            1 -> {
                //veggi
                buttonMeat?.setTextColor(getColor(R.color.colorDark))
                buttonVeggi?.setTextColor(getColor(R.color.colorAccent))
                buttonVegan?.setTextColor(getColor(R.color.colorDark))
            }
            2 -> {
                //vegan
                buttonMeat?.setTextColor(getColor(R.color.colorDark))
                buttonVeggi?.setTextColor(getColor(R.color.colorDark))
                buttonVegan?.setTextColor(getColor(R.color.colorAccent))
            }
        }
    }


    /**
     * method for saving records in database
     */
    private fun saveChoice(day: String, choice: Int) {

        //Do we have to insert or update?


        var actualChoice = databaseHandler.selectChoice(day)

        if(actualChoice == null) {
            //nothing set -> insert

            if(day.trim() != "") {

                var dmc = DataModelClass(day, choice)

                val status = databaseHandler.insertChoice(dmc)

                if(status > -1){
                    Toast.makeText(applicationContext,"choice saved", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(applicationContext,"day cannot be blank",Toast.LENGTH_LONG).show()
            }

        } else {
            //we do have something

            //different?
            if(actualChoice != choice) {
                //update!

                var dmc = DataModelClass(day, choice)

                var resultUpdate = databaseHandler.updateChoice(dmc)

                if(resultUpdate == 1) {
                    Toast.makeText(applicationContext,"choice updated", Toast.LENGTH_LONG).show()
                }


            } else {
                //still the same..relax


            }
        }


    }

    private fun createNotificationChannel(id: String, name: String, description: String) {

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun sendNotification() {

        val notificationID = 101

        val resultIntent = Intent(this, MainActivity::class.java)

        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = System.currentTimeMillis()
        datetimeToAlarm.set(HOUR_OF_DAY, 22)
        datetimeToAlarm.set(MINUTE, 0)
        datetimeToAlarm.set(SECOND, 0)
        datetimeToAlarm.set(MILLISECOND, 0)
        datetimeToAlarm.set(DAY_OF_WEEK, 0)

        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelID = "my_channel_01"

        val notification = Notification.Builder(this@MainActivity, channelID)
            .setContentTitle("Example Notification")
            .setContentText("This is an example notification.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelID)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(notificationID, notification)


    }


}
