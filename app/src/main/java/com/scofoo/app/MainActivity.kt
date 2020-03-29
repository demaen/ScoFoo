package com.scofoo.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.*
import java.util.Calendar.*


class MainActivity: AppCompatActivity() {

    private val databaseHandler = DatabaseHandler(this)

    //allowed choices: 0 meat 1 veggi 2 vegan
    private var actualChoice: Int = -1

    private val calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)
    private var month = calendar.get(Calendar.MONTH)+1
    private var day = calendar.get(Calendar.DAY_OF_MONTH)

    private var actualDay: String = ""


    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(
            "my_channel_01",
            "ScoFoo Notifications",
            "All Notifications")

        //get the Buttons
        val buttonMeat: Button = findViewById(R.id.buttonMeat)
        val buttonVeggi: Button = findViewById(R.id.buttonVeggi)
        val buttonVegan: Button = findViewById(R.id.buttonVegan)
        val buttonNotification: Button = findViewById(R.id.buttonNotification)


        //Set today's date
        val labelDate: TextView = findViewById(R.id.date)
        actualDay = year.toString() + "-" + month.toString() + "-" + day.toString()

        updateDayChoice(labelDate, buttonMeat, buttonVeggi, buttonVegan)


        //Listeners
        buttonMeat.setOnClickListener {
            actualChoice = 0

            setButtonDesigns(buttonMeat, buttonVeggi, buttonVegan)

            saveChoice(actualDay, actualChoice)
        }

        buttonVeggi.setOnClickListener {
            actualChoice = 1

            setButtonDesigns(buttonMeat, buttonVeggi, buttonVegan)

            saveChoice(actualDay, actualChoice)
        }

        buttonVegan.setOnClickListener {
            actualChoice = 2

            setButtonDesigns(buttonMeat, buttonVeggi, buttonVegan)

            saveChoice(actualDay, actualChoice)
        }

        buttonNotification.setOnClickListener {
            sendNotification()
        }

        labelDate.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view,
                    selectedYear,
                    selectedMonth,
                    selectedDay ->

                    val selectedDayString: String = selectedYear.toString() + "-" + (selectedMonth+1).toString() + "-" + selectedDay.toString()

                    year = selectedYear
                    month = selectedMonth+1
                    day = selectedDay
                    actualDay = selectedDayString

                    updateDayChoice(labelDate, buttonMeat, buttonVeggi, buttonVegan)

            }, year, month-1, day)

            dpd.show()

        }



    }

    private fun updateDayChoice(labelDate: TextView, buttonMeat: Button, buttonVeggi: Button, buttonVegan: Button) {
        labelDate.text = actualDay;

        //what is the actual choice if any?
        actualChoice = databaseHandler.selectChoice(actualDay)

        println("ActualChoice is " + actualChoice)

        setButtonDesigns(buttonMeat, buttonVeggi, buttonVegan)

    }

    private fun setButtonDesigns(buttonMeat: Button, buttonVeggi: Button, buttonVegan: Button) {
        when(actualChoice) {
            -1 -> {
                //nothing yet
                buttonMeat.setTextColor(getColor(R.color.colorDark))
                buttonVeggi.setTextColor(getColor(R.color.colorDark))
                buttonVegan.setTextColor(getColor(R.color.colorDark))
            }
            0 -> {
                //meat
                buttonMeat.setTextColor(getColor(R.color.colorAccent))
                buttonVeggi.setTextColor(getColor(R.color.colorDark))
                buttonVegan.setTextColor(getColor(R.color.colorDark))
            }
            1 -> {
                //veggi
                buttonMeat.setTextColor(getColor(R.color.colorDark))
                buttonVeggi.setTextColor(getColor(R.color.colorAccent))
                buttonVegan.setTextColor(getColor(R.color.colorDark))
            }
            2 -> {
                //vegan
                buttonMeat.setTextColor(getColor(R.color.colorDark))
                buttonVeggi.setTextColor(getColor(R.color.colorDark))
                buttonVegan.setTextColor(getColor(R.color.colorAccent))
            }
        }
    }


    //method for saving records in database
    private fun saveChoice(day: String, choice: Int) {

        //Do we have to insert or update?


        var actualChoice = databaseHandler.selectChoice(day)

        if(actualChoice == -1) {
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
