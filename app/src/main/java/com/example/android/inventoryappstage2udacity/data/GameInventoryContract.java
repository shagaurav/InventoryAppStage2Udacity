package com.example.android.inventoryappstage2udacity.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class GameInventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage2udacity";
    public static final Uri BASE_CONTENT_URI = Uri.parse( "content://" + CONTENT_AUTHORITY );
    public static final String PATH_GAMES = "games";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private GameInventoryContract() {
    }

    /**
     * Inner class that defines constant values for the games database table.
     */
    public static abstract class GameInventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath( BASE_CONTENT_URI, PATH_GAMES );

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /**
         * Name of database table for games
         */
        public final static String TABLE_NAME = "games";
        /**
         * Unique ID number for the game (only for use in the database table).
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * Name of the Game.
         */
        public final static String COLUMN_GAME_NAME = "product_name";
        /**
         * Price of the Game.
         */
        public final static String COLUMN_GAME_PRICE = "price";
        /**
         * Quantity of the Game.
         */
        public final static String COLUMN_GAME_QUANTITY = "quantity";
        /**
         * Game supplier name of the Game.
         */
        public final static String COLUMN_GAME_SUPPLIER_NAME = "supplier_name";
        /**
         * Game supplier phone number of the Game.
         */
        public final static String COLUMN_GAME_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";

        // Supplier name list values
        public final static int SUPPLIER_UNKNOWN = 0;
        public final static int SUPPLIER_SONY = 1;
        public final static int SUPPLIER_GAMERS = 2;
        public final static int SUPPLIER_PLAYERS = 3;

        public static boolean isValidSupplierName(int suppliername) {
            if (suppliername == SUPPLIER_UNKNOWN || suppliername == SUPPLIER_SONY || suppliername == SUPPLIER_GAMERS || suppliername == SUPPLIER_PLAYERS) {
                return true;
            }
            return false;

        }
    }
}