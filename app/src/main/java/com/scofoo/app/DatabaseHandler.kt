package com.scofoo.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

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

        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection

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
     * returns -1 when no result
     * returns 0 1 2 when choice found
     */
    fun selectChoice(day: String):Int {
        var choice: Int = -1

        val selectQuery = "SELECT " + KEY_CHOICE + " FROM " + TABLE_CHOICES + "  WHERE " + KEY_DAY + "='" + day +"'"
        val db = this.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return choice //error
        }

        //there should be just one result!


        if (cursor.moveToFirst()) {
            choice = cursor.getInt(cursor.getColumnIndex(KEY_CHOICE))
            /*
            do {


            } while (cursor.moveToNext())

             */
        }


        return choice

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