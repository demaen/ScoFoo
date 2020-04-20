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
        private val KEY_DAY = "day"
        private val KEY_CHOICE = "choice"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_CHOICES + "("
                + KEY_DAY + " TEXT PRIMARY KEY,"
                + KEY_CHOICE + " INTEGER" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CHOICES)
        onCreate(db)
    }


    //method to insert data
    fun insertChoice(dmc: DataModelClass):Long {

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


    /*
    //method to read data
    fun viewAllDayChoice():List<DataModelClass>  {

        val choiceList:ArrayList<DataModelClass> = ArrayList<DataModelClass>()

        val selectQuery = "SELECT  * FROM $TABLE_CHOICES"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var day: String
        var choice: Int

        if (cursor.moveToFirst()) {
            do {

                day = cursor.getString(cursor.getColumnIndex(KEY_DAY))
                choice = cursor.getInt(cursor.getColumnIndex(KEY_CHOICE))

                val dmc = DataModelClass(day, choice)

                choiceList.add(dmc)

            } while (cursor.moveToNext())
        }

        return choiceList
    }
    */

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

    fun getOldestEntry(): DataModelClass? {

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
                return DataModelClass(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))
            }
        }

        return null

    }

    fun getNewestEntry(): DataModelClass? {

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

                return DataModelClass(cursor.getString(cursor.getColumnIndex(KEY_DAY)), cursor.getInt(cursor.getColumnIndex(KEY_CHOICE)))

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
    fun updateChoice(dmc: DataModelClass):Int{

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

    fun updateDateFormat() {
        val selectQuery = "SELECT $KEY_DAY FROM $TABLE_CHOICES"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            println(e)
        }




        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val date = cursor.getString(cursor.getColumnIndex(KEY_DAY))
                    val parts = date.split("-")

                    val oldMonth = parts[1]
                    val oldDay = parts[2]

                    var newMonth = ""
                    var newDay = ""

                    var newDate = ""

                    newMonth = if(oldMonth.toInt() < 10) {
                        "0$oldMonth"
                    } else {
                        oldMonth
                    }

                    newDay = if(oldDay.toInt() < 10) {
                        "0$oldDay"
                    } else {
                        oldDay
                    }

                    newDate = parts[0] + "-" + newMonth + "-" + newDay

                    //println("Year: " + parts[0])
                    //println("Month: $oldMonth -> $newMonth")
                    //println("Day: $oldDay -> $newDay")
                    println("$date -> $newDate")



                    val db = this.writableDatabase

                    val contentValues = ContentValues()
                    contentValues.put(KEY_DAY, newDate)

                    // Updating Row
                    val success = db.update(TABLE_CHOICES, contentValues, "$KEY_DAY='$date'",null)

                    //2nd argument is String containing nullColumnHack
                    db.close() // Closing database connection

                    println(success)

                } while (cursor.moveToNext())


            }
        }


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