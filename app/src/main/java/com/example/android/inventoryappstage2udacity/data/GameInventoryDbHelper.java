package com.example.android.inventoryappstage2udacity.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

/**
 * Database helper for app.
 */
public class GameInventoryDbHelper extends SQLiteOpenHelper {


    /** Name of the database file */
    private static final String DATABASE_NAME = "games_inventory.db";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 1;

    // Constructs a new instance
    public GameInventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a String that contains the SQL statement to create the games table
        String SQL_CREATE_GAMES_TABLE = "CREATE TABLE " + GameInventoryEntry.TABLE_NAME + " ("
                + GameInventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GameInventoryEntry.COLUMN_GAME_NAME + " TEXT NOT NULL, "
                + GameInventoryEntry.COLUMN_GAME_PRICE + " INTEGER NOT NULL, "
                + GameInventoryEntry.COLUMN_GAME_QUANTITY + " INTEGER NOT NULL, "
                + GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME + " INTEGER NOT NULL DEFAULT 0, "
                + GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER + " INTEGER );";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_GAMES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}

