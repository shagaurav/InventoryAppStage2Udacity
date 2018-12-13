package com.example.android.inventoryappstage2udacity.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class GameProvider extends ContentProvider {



    /** URI matcher code for the content URI for the games table */
    private static final int GAME = 100;

    /** URI matcher code for the content URI for a single game in the games table */
    private static final int GAME_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(GameInventoryContract.CONTENT_AUTHORITY, GameInventoryContract.PATH_GAMES, GAME );
        sUriMatcher.addURI(GameInventoryContract.CONTENT_AUTHORITY, GameInventoryContract.PATH_GAMES + "/#", GAME_ID );
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = GameProvider.class.getSimpleName();


    //
    private GameInventoryDbHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new GameInventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     * @return Cursor object depending on provided uri.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case GAME:
                cursor = database.query(GameInventoryContract.GameInventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case GAME_ID:
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(GameInventoryContract.GameInventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw  new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Update the existing data into the provider with the given ContentValues.
     * @return rowsUpdated - number of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAME:
                return updateGame(uri, contentValues, selection, selectionArgs);
            case GAME_ID:
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateGame(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateGame(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsUpdated =  database.update(GameInventoryContract.GameInventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Perform the delete operation for the given URI. Use the given projection, selection, selection arguments.
     * @return rowsDeleted - number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int  rowsDeleted;
        switch (match) {
            case GAME:
                // Delete all rows that match the selection and selection args
                rowsDeleted =  database.delete(GameInventoryContract.GameInventoryEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case GAME_ID:
                // Delete a single row given by the ID in the URI
                selection = GameInventoryContract.GameInventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted =  database.delete(GameInventoryContract.GameInventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     * @return Uri appended with the id of row.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        int match = sUriMatcher.match(uri);
        switch(match){
            case GAME:
                return insertGame(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertGame(Uri uri, ContentValues contentValues){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(GameInventoryContract.GameInventoryEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAME:
                return GameInventoryContract.GameInventoryEntry.CONTENT_LIST_TYPE;
            case GAME_ID:
                return GameInventoryContract.GameInventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
