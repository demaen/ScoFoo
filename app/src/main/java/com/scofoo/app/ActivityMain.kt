package com.scofoo.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.*
import java.util.Calendar.*


class ActivityMain: AppCompatActivity() {

    private val LOGTAG = "9TGbQcB5 ActivityMain"

    //by activating the debug mode some additional information will be displayed (like touch coordinates)
    private val debugMode = false

    //we need to connect to the database
    private val databaseHandler = DatabaseHandler(this, LOGTAG)

    private var today: LocalDate = LocalDate.now()

    private var selectedDate: LocalDate = today

    private var displaySizeX = 0
    private var x1: Float? = 0.toFloat()
    private var x2: Float? = 0.toFloat()

    private var debugTable: TableLayout? = null
    private var debugTableRow0: TextView? = null
    private var debugTableRow1: TextView? = null
    private var debugTableRow2: TextView? = null
    private var debugTableRow3: TextView? = null
    private var debugTableRow4: TextView? = null

    private var buttonMeat: Button? = null
    private var buttonVeggi: Button? = null
    private var buttonVegan: Button? = null
    private var buttonNotification: Button? = null
    private var buttonGoToStats: Button? = null

    private var selectedDateLabel: TextView? = null

    private var notificationManager: NotificationManager? = null

    init {

        Log.v(LOGTAG, "Starting the Application")

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get the buttons
        buttonMeat = findViewById(R.id.buttonMeat)
        buttonVeggi = findViewById(R.id.buttonVeggi)
        buttonVegan = findViewById(R.id.buttonVegan)
        buttonNotification = findViewById(R.id.buttonNotification)
        buttonGoToStats = findViewById(R.id.buttonGoToStats)

        //get the debug table elements
        debugTable = findViewById(R.id.debugTable)
        debugTableRow0 = findViewById(R.id.zeile0)
        debugTableRow1 = findViewById(R.id.zeile1)
        debugTableRow2 = findViewById(R.id.zeile2)
        debugTableRow3 = findViewById(R.id.zeile3)
        debugTableRow4 = findViewById(R.id.zeile4)

        //if wanted display the debug table
        if(debugMode) {
            debugTable?.visibility = View.VISIBLE
            buttonNotification?.visibility = View.VISIBLE
        }

        Log.v(LOGTAG, "Debugmode: $debugMode")

        //get the Label for the selectedDate
        selectedDateLabel = findViewById(R.id.selectedDate)

        //let's find out how big our screen is
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        displaySizeX = size.x

        Log.v(LOGTAG, "Handling a display width of $displaySizeX px")

        //let's take care of the notifications
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //open a Notification Channel
        createNotificationChannel("my_channel_01","ScoFoo Notifications","All Notifications")

        //now we need to get the choice from the database and how the buttons should be highlighted
        updateDayChoice()

        //Big section with Listeners

        //when we hit the selectedDateLabel
        selectedDateLabel?.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view,
                    selectedYear,
                    selectedMonth,
                    selectedDay ->

                //we have to build the String properly, because those values are int and need leading "0" maybe
                var selectedDateString = "$selectedYear-"

                //month starts with 0 = jan ... thats why incrementing it
                //every month under 10 need a leading "0"
                if(selectedMonth+1 < 10) {
                    selectedDateString += "0"
                }

                //Append the month
                selectedDateString += (selectedMonth+1).toString() + "-"

                //same for the days, but days start with 1 in contrast with months
                if(selectedDay < 10) {
                    selectedDateString += "0"
                }

                //append the days
                selectedDateString += (selectedDay).toString()

                //we need to set these so that the picker highlights the right day when opened again
                selectedDate = LocalDate.parse(selectedDateString)

                //now we need to get the choice from the database and how the buttons should be highlighted
                updateDayChoice()

            }, selectedDate.year, selectedDate.monthValue-1, selectedDate.dayOfMonth)

            dpd.show()

        }

        //when we hit the meat-button
        buttonMeat?.setOnClickListener {

            if(databaseHandler.selectChoice(selectedDate.toString()) != 0) {
                //only if it's not meat again

                if(saveChoice(DMCChoice(selectedDate.toString(), 0))) {
                    setButtonDesigns()
                }


            }

        }

        //when we hit the veggi-button
        buttonVeggi?.setOnClickListener {

            if(databaseHandler.selectChoice(selectedDate.toString()) != 1) {
                //only if it's not veggi actually

                if(saveChoice(DMCChoice(selectedDate.toString(), 1))) {
                    setButtonDesigns()
                }
            }
        }

        //when we hit the vegan-button
        buttonVegan?.setOnClickListener {

            if(databaseHandler.selectChoice(selectedDate.toString()) != 2) {
                //only if it's not vegan actually

                if(saveChoice(DMCChoice(selectedDate.toString(), 2))) {
                    setButtonDesigns()
                }
            }
        }

        //when we hit the notification button
        buttonNotification?.setOnClickListener {
            sendNotification()
        }

        //when we hit the stats button
        buttonGoToStats?.setOnClickListener {

            //setup the intent
            val intent = Intent(this, ActivityStats::class.java)

            //what days is actually selected?
            intent.putExtra("selectedDate", selectedDate.toString())

            //come up with the activity
            this.startActivity(intent)

        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action = event.action

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
        }

        //what should we do with these results?

        //maybe we should beat some threshold
        //difference > 1/10 display width or sth like that
        if(difference > displaySizeX/10) {

            var newDate = selectedDate

            if(direction == "right") {
                //go back in time
                newDate = newDate.minusDays(1)

            } else if (direction == "left") {
                //go next day
                newDate = newDate.plusDays(1)

            }

            selectedDate = newDate

            updateDayChoice()

        }


        debugTableRow0?.text = actionMode
        debugTableRow1?.text = x1.toString()
        debugTableRow2?.text = x2.toString()
        debugTableRow3?.text = direction
        debugTableRow4?.text = difference.toString()


        // tell the view that we handled the event
        return true

    }

    private fun updateDayChoice() {

        //set the selectedDateLabel's Text to what date is actual selected
        selectedDateLabel?.text = selectedDate.toString()

        //the actual choice may have changed -> set the button design
        setButtonDesigns()

    }

    private fun setButtonDesigns() {
        when(databaseHandler.selectChoice(selectedDate.toString())) {
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
     * @return true when choice is inserted or updated; false when sth else happened
     */
    private fun saveChoice(dmcChoice: DMCChoice): Boolean {

        //Do we have to insert or update?


        val actualChoice = databaseHandler.selectChoice(dmcChoice.day)

        if(actualChoice == null) {
            //nothing set -> insert


            val status = databaseHandler.insertChoice(dmcChoice)

            if(status > -1){
                Toast.makeText(applicationContext,"choice saved", Toast.LENGTH_LONG).show()

                return true
            }

        } else {
            //we already have something for the selected date

            //different?
            if(actualChoice != dmcChoice.choice) {
                //update!

                val resultUpdate = databaseHandler.updateChoice(dmcChoice)

                if(resultUpdate == 1) {
                    Toast.makeText(applicationContext,"choice updated", Toast.LENGTH_LONG).show()
                    return true
                }


            }

        }

        return false


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

        val resultIntent = Intent(this, ActivityMain::class.java)

        val datetimeToAlarm = getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = System.currentTimeMillis()
        datetimeToAlarm.set(HOUR_OF_DAY, 22)
        datetimeToAlarm.set(MINUTE, 0)
        datetimeToAlarm.set(SECOND, 0)
        datetimeToAlarm.set(MILLISECOND, 0)
        datetimeToAlarm.set(DAY_OF_WEEK, 0)

        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelID = "my_channel_01"

        val notification = Notification.Builder(this@ActivityMain, channelID)
            .setContentTitle("Example Notification")
            .setContentText("This is an example notification.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelID)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(notificationID, notification)


    }


}
