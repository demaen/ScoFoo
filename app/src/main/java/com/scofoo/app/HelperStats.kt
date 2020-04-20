package com.scofoo.app

import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.temporal.ChronoUnit




class HelperStats(private val context: Context, private val databaseHandler: DatabaseHandler, private val actualDay: String) {

    //allowed choices: 0 meat 1 veggi 2 vegan
    private var actualChoice: Int? = null
    private var today: String = ""

    private var goalDateStart = LocalDate.parse("2020-01-01")
    private var goalDateEnd = LocalDate.parse("2020-12-31")
    private var goalCountMeatMax = 52
    private var goalCountVeggiMin = ChronoUnit.DAYS.between(goalDateStart, goalDateEnd.plusDays(1)) - goalCountMeatMax
    private var goalCountVeganMin = 122

    init {


        //setup the intent
        val intent = Intent(context, ActivityStats::class.java)

        //what is today's date?
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
        if(dmcOldestEntry != null && dmcNewestEntry != null) {

            val missingDays = databaseHandler.getMissingDays(LocalDate.parse(dmcOldestEntry?.day), LocalDate.parse(dmcNewestEntry?.day))

            if (missingDays != null) {
                intent.putExtra("missingDays", missingDays.count().toString())
            } else {
                intent.putExtra("missingDays", "0")
            }

        } else {
            intent.putExtra("missingDays", "")
        }



        //count days meat
        val daysMeat = databaseHandler.getDayCountOfChoice(0)
        intent.putExtra("daysMeat", daysMeat.toString())

        //count days veggi
        val daysVeggi = databaseHandler.getDayCountOfChoice(1)

        //count days vegan
        val daysVegan = databaseHandler.getDayCountOfChoice(2)

        //of course vegan days are veggi too
        val daysVeggiInclVegan = daysVeggi + daysVegan
        intent.putExtra("daysVeggi", "$daysVeggi (+ $daysVegan = $daysVeggiInclVegan)")

        //now set the vegan days
        intent.putExtra("daysVegan", daysVegan.toString())

        //now the goals

        intent.putExtra("goalDateStart", goalDateStart.toString())
        intent.putExtra("goalDateEnd", goalDateEnd.toString())
        intent.putExtra("goalCountMeatMax", goalCountMeatMax.toString())
        intent.putExtra("goalCountVeggiMin", goalCountVeggiMin.toString())
        intent.putExtra("goalCountVeganMin", goalCountVeganMin.toString())

        //How many days are in the target zone?
        val goalDaysBetween = ChronoUnit.DAYS.between(goalDateStart, goalDateEnd.plusDays(1))
        intent.putExtra("goalDaysBetween", goalDaysBetween.toString())

        //How many days are missing in the target zone?
        val goalMissingDays = databaseHandler.getMissingDays(goalDateStart, goalDateEnd)

        if (goalMissingDays != null) {
            intent.putExtra("goalMissingDays", goalMissingDays.count().toString())
        } else {
            intent.putExtra("goalMissingDays", "0")
        }

        //count days meat in target zone
        val goalDaysMeat = databaseHandler.getDayCountOfChoice(0, goalDateStart.toString(), goalDateEnd.toString())
        intent.putExtra("goalDaysMeat", goalDaysMeat.toString())

        //count days veggi in target zone
        val goalDaysVeggi = databaseHandler.getDayCountOfChoice(1, goalDateStart.toString(), goalDateEnd.toString())

        //count days vegan in target zone
        val goalDaysVegan = databaseHandler.getDayCountOfChoice(2, goalDateStart.toString(), goalDateEnd.toString())

        //of course vegan days are veggi too
        val goalDaysVeggiInclVegan = goalDaysVeggi + goalDaysVegan
        intent.putExtra("goalDaysVeggi", "$goalDaysVeggi (+ $goalDaysVegan = $goalDaysVeggiInclVegan)")

        //now set the vegan days
        intent.putExtra("goalDaysVegan", goalDaysVegan.toString())

        //goal meat achieved?
        var goalMeat = false

        if(goalDaysMeat <= goalCountMeatMax) {
            goalMeat = true
        }

        intent.putExtra("goalMeat", goalMeat.toString())

        //goal veggi achieved?
        var goalVeggi = false

        if(goalDaysVeggiInclVegan >= goalCountVeggiMin) {
            goalVeggi = true
        }

        if(goalVeggi) {
            //if it's achieved all done

            intent.putExtra("goalVeggi", goalVeggi.toString())
        } else {
            //if not let's check if it's still achievable

            var goalVeggiAchievable = false

            if(goalMissingDays != null) {
                //There'd better be a few days left

                if(goalMissingDays.count() + goalDaysVeggiInclVegan >= goalCountVeggiMin) {
                    //and enough days left

                    goalVeggiAchievable = true
                }

            }

            intent.putExtra("goalVeggi", "$goalVeggi ($goalVeggiAchievable)")

        }


        //goal vegan achieved?
        var goalVegan = false

        if(goalDaysVegan >= goalCountVeganMin) {
            goalVegan = true
        }

        if(goalVegan) {
            //if it's achieved all done

            intent.putExtra("goalVegan", goalVegan.toString())
        } else {
            //if not let's check if it's still achievable

            var goalVeganAchievable = false

            if(goalMissingDays != null) {
                //There'd better be a few days left

                if(goalMissingDays.count() + goalDaysVegan >= goalCountVeganMin) {
                    //and enough days left

                    goalVeganAchievable = true
                }

            }

            intent.putExtra("goalVegan", "$goalVegan ($goalVeganAchievable)")

        }



        //now the new goals

        var dmcGoal1 = DMCGoal(0, 1, 52, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"))
        var dmcGoal2 = DMCGoal(1, 0, 314, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"))
        var dmcGoal3 = DMCGoal(2, 0, 122, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"))

        dmcGoal1 = goalAchieved(dmcGoal1)
        dmcGoal2 = goalAchieved(dmcGoal2)
        dmcGoal3 = goalAchieved(dmcGoal3)

        var dmcGoals: Array<DMCGoal> = arrayOf(dmcGoal1, dmcGoal2, dmcGoal3)

        intent.putExtra("dmcGoals", dmcGoals)

        //come up with the activity
        context.startActivity(intent)

    }

    private fun goalAchieved(dmcGoal: DMCGoal): DMCGoal {

        var goalDays = databaseHandler.getDayCountOfChoice(dmcGoal.choice, dmcGoal.start.toString(), dmcGoal.end.toString())
        val goalDaysMissingList = databaseHandler.getMissingDays(dmcGoal.start, dmcGoal.end)
        val goalDaysMissing = goalDaysMissingList?.count() ?: 0

        if (dmcGoal.choice == 1) {
            //vegan days count as veggi too
            goalDays += databaseHandler.getDayCountOfChoice(2, dmcGoal.start.toString(), dmcGoal.end.toString())
        }

        dmcGoal.value = goalDays;

        when (dmcGoal.type) {
            0 -> {
                //handling min relation

                dmcGoal.achieved = goalDays >= dmcGoal.target
            }
            1 -> {
                //handling max relation

                dmcGoal.achieved = goalDays <= dmcGoal.target
            }
            2 -> {
                //handling equals relation

                dmcGoal.achieved = goalDays == dmcGoal.target
            }
        }

        if(dmcGoal.achieved != null) {
            if(dmcGoal.achieved == false) {
                //let's see if it's still achievable


                val goalDaysPotential = goalDays + goalDaysMissing

                when (dmcGoal.type) {
                    0 -> {
                        //min relation


                        dmcGoal.achievable = goalDaysPotential >= dmcGoal.target

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
        }

        return dmcGoal
    }



}