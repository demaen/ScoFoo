package com.scofoo.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

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

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields

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
        //db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CHOICES)
        //onCreate(db)
    }

    /** Method to insert data
     *
     */
    fun insertChoice(dmc: DMCChoice):Long {

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

        var dmcGoals = mutableListOf<DMCGoal>()

        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_GOAL_CHOICE, $KEY_GOAL_TYPE, $KEY_GOAL_TARGET, $KEY_GOAL_Start, $KEY_GOAL_END FROM $TABLE_GOALS"

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println(e)
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
        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_GOAL_ID FROM $TABLE_GOALS"

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println(e)
        }

        if (cursor != null) {

            return cursor.count

        }

        return 0
    }

    /**
     * returns null when no result
     * returns 0 1 2 when choice found
     */
    fun selectChoice(day: String):Int? {

        val selectQuery = "SELECT $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_DAY='$day'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println(e)
        }

        var result:Int? = null

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(KEY_CHOICE))
            }

            cursor.close()
        }


        return result


    }

    fun getOldestEntry(): DMCChoice? {

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES  ORDER BY $KEY_DAY ASC"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println("error!")
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return DMCChoice(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))
            }
        }

        return null

    }

    fun getNewestEntry(): DMCChoice? {

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES  ORDER BY $KEY_DAY DESC"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println("error!")
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                return DMCChoice(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))

            }
        }

        return null
    }

    fun getMissingDays(start:LocalDate, end:LocalDate): MutableList<LocalDate>? {

        var resultList = mutableListOf<LocalDate>()

        if(start <= end) {
            //now we want to find all dates since our oldest entry, which have not been set by any choice


            //we start at the oldest entry
            var pointer = start


            while (pointer <= end) {

                if(selectChoice(pointer.toString()) == null) {
                    //this day has no db entry
                    resultList.add(pointer)
                }

                //set the pointer to the next day
                pointer = pointer.plusDays(1)
            }

        } else {
            return null
        }

        return if(resultList.count() == 0) {
            null
        } else {
            resultList
        }


    }

    fun getDayCountOfChoice(choice: Int): Int {

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_CHOICE='$choice'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println("Could not exec getMeatDays() query!")
        }

        return cursor?.count ?: 0

    }

    fun getDayCountOfChoice(choice: Int, start: String, end: String): Int {

        val selectQuery = "SELECT $KEY_DAY, $KEY_CHOICE FROM $TABLE_CHOICES WHERE $KEY_CHOICE='$choice' AND $KEY_DAY >= '$start' AND $KEY_DAY <= '$end'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println("Could not exec getMeatDays() query!")
        }

        return cursor?.count ?: 0

    }



    //method to update data
    fun updateChoice(dmc: DMCChoice):Int{

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