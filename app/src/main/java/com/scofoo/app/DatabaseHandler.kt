package com.scofoo.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DatabaseHandler(context: Context, parentLogtag: String): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    private var LOGTAG = "DatabaseHandler"

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "EmployeeDatabase"
        private val TABLE_CHOICES = "Choices"
        private val TABLE_GOALS = "Goals"
        private val KEY_DAY = "day"
        private val KEY_CHOICE = "choice"
        private val KEY_GOAL_ID = "rowid"
        private val KEY_GOAL_CHOICE = "goalChoice"
        private val KEY_GOAL_TYPE = "goalType"
        private val KEY_GOAL_TARGET = "goalTarget"
        private val KEY_GOAL_Start = "goalStart"
        private val KEY_GOAL_END = "goalEnd"
    }

    init {

        LOGTAG = "$parentLogtag - ${this.LOGTAG}"

        Log.v(LOGTAG, "init()")


    }

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields

        Log.v(LOGTAG, "onCreate()")

        db?.execSQL("CREATE TABLE " + TABLE_CHOICES + "("
                + KEY_DAY + " TEXT PRIMARY KEY,"
                + KEY_CHOICE + " INTEGER" + ")")

        db?.execSQL("CREATE TABLE " + TABLE_GOALS + "("
                + KEY_GOAL_CHOICE + " INTEGER,"
                + KEY_GOAL_TYPE + " INTEGER,"
                + KEY_GOAL_TARGET + " INTEGER,"
                + KEY_GOAL_Start + " TEXT,"
                + KEY_GOAL_END + " TEXT" + ")")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        //@todo we definitely should start to handle this!

        Log.v(LOGTAG, "onUpgrade()")

        //db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CHOICES)
        //onCreate(db)
    }

    /** Method to insert data
     *
     */
    fun insertChoice(dmc: DMCChoice):Long {

        Log.v(LOGTAG, "insertChoice()")

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_DAY, dmc.day)
        contentValues.put(KEY_CHOICE, dmc.choice)

        // Inserting Row
        val success = db.insert(TABLE_CHOICES, null, contentValues)

        // Closing database connection
        db.close()

        return success
    }

    fun insertGoal(dmcGoal: DMCGoal): Long {

        Log.v(LOGTAG, "insertGoal()")

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_GOAL_CHOICE, dmcGoal.choice)
        contentValues.put(KEY_GOAL_TYPE, dmcGoal.type)
        contentValues.put(KEY_GOAL_TARGET, dmcGoal.target)
        contentValues.put(KEY_GOAL_Start, dmcGoal.start.toString())
        contentValues.put(KEY_GOAL_END, dmcGoal.end.toString())

        // Inserting Row
        // returns the row ID of the newly inserted row, or -1 if an error occurred
        val rowId = db.insert(TABLE_GOALS, null, contentValues)

        // Closing database connection
        db.close()

        return rowId

    }

    fun selectGoals(): MutableList<DMCGoal> {

        Log.v(LOGTAG, "selectGoals()")

        val dmcGoals = mutableListOf<DMCGoal>()

        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_GOAL_CHOICE, $KEY_GOAL_TYPE, $KEY_GOAL_TARGET, $KEY_GOAL_Start, $KEY_GOAL_END FROM $TABLE_GOALS"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                do {

                    val dmcGoal = DMCGoal(
                        cursor.getInt(cursor.getColumnIndex(KEY_GOAL_CHOICE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_GOAL_TYPE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_GOAL_TARGET)),
                        LocalDate.parse(cursor.getString(cursor.getColumnIndex(KEY_GOAL_Start))),
                        LocalDate.parse(cursor.getString(cursor.getColumnIndex(KEY_GOAL_END)))
                    )

                    dmcGoals.add(dmcGoal)

                } while (cursor.moveToNext())


            }

            cursor.close()

        }

        db.close()

        return dmcGoals

    }

    fun selectGoalsCount(): Int {

        Log.v(LOGTAG, "selectGoalsCount()")

        var result = 0

        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_GOAL_ID FROM $TABLE_GOALS"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        if (cursor != null) {

            result = cursor.count

            cursor.close()

        }

        return result
    }

    /**
     * returns null when no result
     * returns 0 1 2 when choice found
     */
    fun selectChoice(day: String):Int? {

        Log.v(LOGTAG, "selectChoice($day)")

        val selectQuery = "SELECT $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_DAY='$day'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        var result:Int? = null

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(KEY_CHOICE))
            }

            cursor.close()
        }

        db.close()

        return result

    }

    fun getOldestEntry(): DMCChoice? {

        Log.v(LOGTAG, "getOldestEntry()")

        var dmcChoice: DMCChoice? = null

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES  ORDER BY $KEY_DAY ASC"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                dmcChoice = DMCChoice(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))
            }

            cursor.close()
        }

        db.close()

        return dmcChoice

    }

    fun getNewestEntry(): DMCChoice? {

        Log.v(LOGTAG, "getNewestEntry()")

        var dmcChoice: DMCChoice? = null

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES ORDER BY $KEY_DAY DESC"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                dmcChoice = DMCChoice(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))
            }

            cursor.close()
        }

        db.close()

        return dmcChoice
    }

    /**
     * You will get a List of all Dates, that haven't been set by any choice
     * This is very expensive in time consumption!
     */
    fun getMissingDays(start:LocalDate, end:LocalDate): MutableList<LocalDate>? {

        Log.v(LOGTAG, "getMissingDays($start, $end) STARTING --->")

        val resultList = mutableListOf<LocalDate>()

        if(start <= end) {
            //now we want to find all dates since our oldest entry, which have not been set by any choice


            //we start at the oldest entry
            var pointer = start


            while (pointer <= end) {
                //unless we have reached the end

                //@todo requesting each day on it's own is slow
                if(selectChoice(pointer.toString()) == null) {
                    //this day has no db entry
                    resultList.add(pointer)
                }

                //set the pointer to the next day
                pointer = pointer.plusDays(1)
            }

        } else {

            Log.e(LOGTAG, "getMissingDays($start, $end) Start must be <= End")

            return null
        }

        return if(resultList.count() == 0) {
            Log.v(LOGTAG, "getMissingDays($start, $end) = 0 <---- RESULT")

            null
        } else {
            Log.v(LOGTAG, "getMissingDays($start, $end) = ${resultList.count()} <---- RESULT")

            resultList
        }


    }

    /**
     * Here you get a count of all unset Dates
     * That is much faster than getMissingDays()!
     */
    fun getMissingDaysCount(date1:LocalDate, date2:LocalDate): Int {

        var missingDaysCount = 0

        //we assume that date1 <= date2
        var start: LocalDate = date1
        var end: LocalDate = date2

        //let's get this in order
        if(date1 > date2) {
            //guessed wrong -> swap
            start = date2
            end = date1
        }

        val selectQuery = "SELECT COUNT($KEY_DAY) FROM $TABLE_CHOICES WHERE $KEY_DAY >= '$start' AND $KEY_DAY <= '$end'"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                val daysInRangeWithChoices = cursor.getInt(0)

                val daysBetweenCount: Long = ChronoUnit.DAYS.between(start, end.plusDays(1))

                missingDaysCount = daysBetweenCount.toInt() - daysInRangeWithChoices

            }

            cursor.close()
        }

        db.close()

        Log.v(LOGTAG, "getMissingDaysNew($start, $end) = $missingDaysCount")

        return missingDaysCount

    }

    fun getDayCountOfChoice(choice: Int): Int {

        Log.v(LOGTAG, "getDaysCountOfChoice($choice)")

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_CHOICE='$choice'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, e.toString())
        }


        var counter = 0

        if(cursor != null) {

            counter = cursor.count

            cursor.close()

        }

        db.close()

        return counter

    }

    fun getDayCountOfChoice(choice: Int, start: String, end: String): Int {

        var counter = 0

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_CHOICE='$choice' AND $KEY_DAY >= '$start' AND $KEY_DAY <= '$end'"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            Log.e(LOGTAG, "Could not exec getMeatDays() query!")
        }

        if(cursor != null) {

            counter = cursor.count

            cursor.close()

        }

        db.close()

        Log.v(LOGTAG, "getDayCountOfChoice($choice, $start, $end) = $counter")

        return counter

    }

    /**
     * method to update data
     */
    fun updateChoice(dmc: DMCChoice):Int{

        Log.v(LOGTAG, "updateChoice()")

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_DAY, dmc.day)
        contentValues.put(KEY_CHOICE, dmc.choice)

        // Updating Row
        val success = db.update(TABLE_CHOICES, contentValues,KEY_DAY + "='" + dmc.day+"'",null)

        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection

        //number of rows effected
        return success
    }

    /*
    //method to delete data
    fun deleteChoice(dmc: DataModelClass):Int{

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_DAY, dmc.day)

        // Deleting Row
        val success = db.delete(TABLE_CHOICES,KEY_DAY + "=" + dmc.day,null)

        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection

        return success
    }
    */

}