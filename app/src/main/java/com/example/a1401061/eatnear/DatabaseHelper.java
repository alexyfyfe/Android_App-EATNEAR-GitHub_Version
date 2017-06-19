package com.example.a1401061.eatnear;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alexy on 02/04/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "EATNEARDatabase";
    private static final String TABLE_BeenTo_NAME = "BeenTo";
    private static final String TABLE_History_NAME = "History";
    private static final String TABLE_WishList_NAME = "WishList";


    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase myDB) {
        String sqlBeenTo;
        String sqlHistory;
        String sqlWishList;

        sqlBeenTo = "CREATE TABLE " + TABLE_BeenTo_NAME + " (restaurantID INTEGER PRIMARY KEY NOT NULL, restaurantName TEXT);";
        sqlHistory = "CREATE TABLE " + TABLE_History_NAME + " (restaurantID INTEGER PRIMARY KEY NOT NULL, restaurantName TEXT);";
        sqlWishList = "CREATE TABLE " + TABLE_WishList_NAME + " (restaurantID INTEGER PRIMARY KEY NOT NULL, restaurantName TEXT, restaurantRating NUMERIC);";

        myDB.execSQL (sqlBeenTo);
        myDB.execSQL(sqlHistory);
        myDB.execSQL(sqlWishList);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
