package com.example.a1401061.eatnear;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class storing the methods to interact with the tables in the Database.
 */

public class DatabaseUtility {
    SQLiteDatabase myDB;
    SharedPreferences prefs;

    public DatabaseUtility(Context con) {
        DatabaseHelper db = new DatabaseHelper(con);
        myDB = db.getWritableDatabase();
        prefs = PreferenceManager.getDefaultSharedPreferences(con);

    }

    public ArrayList<Integer> getRestaurantsIDBeenTo() {
        ArrayList<Integer> restaurantID = new ArrayList<Integer>();
        Cursor cur = null;
        String[] rows = new String[]{"restaurantID"};

        try {
            cur = myDB.query("BeenTo", rows, null, null, null, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < cur.getColumnCount(); i++) {
            restaurantID.add(cur.getInt(i));
        }
        cur.close();
        return restaurantID;
    }

    //DATABASE BeenTo methods
    public boolean checkRestaurantsIDBeenTo(Integer aRestaurantID) {
        boolean result = false;
        String[] rows = new String[]{"restaurantID"};
        Cursor cur = null;
        try {
            cur = myDB.query("BeenTo", rows,"restaurantID="+aRestaurantID.toString(),null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (cur.getCount() > 0){
            result = true;
        }
        cur.close();
        Log.d("BeenTo?",result+"");
        return result;
    }

    public boolean setRestaurantsBeenTo(Integer aRestaurantID, String aRestaurantName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("restaurantID", aRestaurantID);
        contentValues.put("restaurantName", aRestaurantName);
        long result = myDB.insert("BeenTo", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            Log.d("BEENTO Restaurant Added",aRestaurantName + " " +aRestaurantID);
            return true;
        }
    }

    public HashMap getAllRestaurantsBeenTo() {
        HashMap<Integer,String> hashMap = new HashMap<>();
        Cursor cur = null;
        try {
            cur = myDB.query("BeenTo",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            hashMap.put(cur.getInt(0),cur.getString(1));
        }
        cur.close();
        return hashMap;
    }

    public int getNumRestaurantsBeenTo() {
        int result = 0;
        Cursor cur = null;
        try {
            cur = myDB.query("BeenTo",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            result++;
        }
        cur.close();
        return result;
    }

    public boolean deleteRestaurantsIDBeenTo(Integer aRestaurantID){
        long result = myDB.delete("BeenTo","restaurantID = "+ aRestaurantID, null);
        if (result == -1) {
            return false;
        } else {
            Log.d("BEENTO Restaurant Del","ID: "+aRestaurantID);
            return true;
        }
    }

    //DATABASE History methods
    public boolean setRestaurantsHistory(Integer aRestaurantID, String aRestaurantName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("restaurantID", aRestaurantID);
        contentValues.put("restaurantName", aRestaurantName);
        long result = myDB.insert("History", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            Log.d("HISTORY Restaurant Add",aRestaurantName + " " +aRestaurantID);
            return true;
        }
    }

    public HashMap getAllRestaurantsHistory() {
        HashMap<Integer,String> hashMap = new HashMap<>();
        Cursor cur = null;
        try {
            cur = myDB.query("History",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            hashMap.put(cur.getInt(0),cur.getString(1));
        }
        cur.close();
        return hashMap;
    }

    public int getNumRestaurantsHistory() {
        int result = 0;
        Cursor cur = null;
        try {
            cur = myDB.query("History",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            result++;
        }
        cur.close();
        return result;
    }

    //DATABASE WishList methods
    public boolean checkRestaurantsIDWishList(Integer aRestaurantID) {
        boolean result = false;
        String[] rows = new String[]{"restaurantID"};
        Cursor cur = null;
        try {
            cur = myDB.query("WishList", rows,"restaurantID="+aRestaurantID.toString(),null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (cur.getCount() > 0){
            result = true;
        }
        cur.close();
        Log.d("WishList?",result+"");
        return result;
    }

    public boolean setRestaurantsWishList(Integer aRestaurantID, String aRestaurantName, Float aRestaurantRating) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("restaurantID", aRestaurantID);
        contentValues.put("restaurantName", aRestaurantName);
        contentValues.put("restaurantRating", aRestaurantRating);
        long result = myDB.insert("WishList", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            Log.d("WishList Restaurant Add",aRestaurantName + " " +aRestaurantID+ " " + aRestaurantRating);
            return true;
        }
    }

    public HashMap getAllRestaurantsWishList() {
        HashMap<Integer,RestaurantObj> hashMap = new HashMap<>();
        Cursor cur = null;
        try {
            cur = myDB.query("WishList",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            RestaurantObj restaurantObj = new RestaurantObj(cur.getString(1),cur.getFloat(2));
            hashMap.put(cur.getInt(0),restaurantObj);
        }
        cur.close();
        return hashMap;
    }

    public int getNumRestaurantsWishList() {
        int result = 0;
        Cursor cur = null;
        try {
            cur = myDB.query("WishList",null,null,null,null,null,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (cur.moveToNext()){
            result++;
        }
        cur.close();
        return result;
    }

    public boolean deleteRestaurantsIDWishList(Integer aRestaurantID){
        long result = myDB.delete("WishList","restaurantID = "+ aRestaurantID, null);
        if (result == -1) {
            return false;
        } else {
            Log.d("WishList Restaurant Del","ID: "+aRestaurantID);
            return true;
        }
    }


    //DELETE ALL DATA FROM DATABASE
    public void deleteAllData(){
        myDB.delete("WishList",null,null);
        myDB.delete("History",null,null);
        myDB.delete("BeenTo",null,null);
    }

}


