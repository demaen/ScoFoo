package com.scofoo.app

import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.temporal.ChronoUnit




class HelperStats(private val context: Context, private val databaseHandler: DatabaseHandler, private val actualDay: String) {

    //allowed choices: 0 meat 1 veggi 2 vegan
    private var actualChoice: Int? = null
    private var today: String = ""

    init {


        //setup the intent
        val intent = Intent(context, ActivityStats::class.java)

        //what is todays date?
        today = LocalDate.now().toString()
        intent.putExtra("today", today)
        
        //what days is actually selected?
        intent.putExtra("actualDay", actualDay)


        //what is the actual choice if any?
        actualChoice = databaseHandler.selectChoice(actualDay)
        

        //get the oldest entry
        val dmcOldestEntry: DataModelClass? = databaseHandler.getOldestEntry()

        if(dmcOldestEntry != null) {
            println("Oldest Entry: " + dmcOldestEntry.day + " " + dmcOldestEntry.choice)

            intent.putExtra("oldestEntry", dmcOldestEntry.day)
        } else {
            println("Oldest Entry: No choices found.")
        }

        //get the newest entry
        val dmcNewestEntry: DataModelClass? = databaseHandler.getNewestEntry()

        if(dmcNewestEntry != null) {
            println("Newest Entry: " + dmcNewestEntry.day + " " + dmcNewestEntry.choice)

            intent.putExtra("newestEntry", dmcNewestEntry.day)
        } else {
            println("Newest Entry: No choices found.")
        }


        //how many days between oldest and newest?
        if(dmcOldestEntry != null && dmcNewestEntry != null) {
            var dateBefore: LocalDate = LocalDate.parse(dmcOldestEntry?.day)
            var dateAfter: LocalDate = LocalDate.parse(dmcNewestEntry?.day)
            val daysBetween: Long = ChronoUnit.DAYS.between(dateBefore, dateAfter.plusDays(1))

            intent.putExtra("daysBetween", daysBetween.toString())
        }


        //get the missing days
        val missingDays = databaseHandler.getMissingDays()

        if (missingDays != null) {
            intent.putExtra("missingDays", missingDays.count().toString())
        } else {
            intent.putExtra("missingDays", "0")
        }

        /*
        if (missingDays != null) {
            if(missingDays.count() > 0) {
                for(day in missingDays) {
                    println("This day has got no choice: $day")
                }


            } else {
                println("Missing days could not be evaluated! #1")
            }
        } else {
            println("Missing days could not be evaluated! #2")
        }*/



        //count days meat
        val daysMeat = databaseHandler.getDayCountOfChoice(0)
        intent.putExtra("daysMeat", daysMeat.toString())


        //count days veggi
        val daysVeggi = databaseHandler.getDayCountOfChoice(1)
        intent.putExtra("daysVeggi", daysVeggi.toString())

        //count days vegan
        val daysVegan = databaseHandler.getDayCountOfChoice(2)
        intent.putExtra("daysVegan", daysVegan.toString())


        //come up with the activity
        context.startActivity(intent)

    }



}