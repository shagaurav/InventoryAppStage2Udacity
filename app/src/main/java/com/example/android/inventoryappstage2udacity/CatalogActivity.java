package com.example.android.inventoryappstage2udacity;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the book data loader */
    private static final int GAME_LOADER = 0;

    RelativeLayout emptyView;

    /** Adapter for the ListView */
    GameCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, ActivityEditor.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        ListView gamesListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);
        gamesListView.setEmptyView(emptyView);

        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new GameCursorAdapter(this, null);
        gamesListView.setAdapter( mCursorAdapter );

        // Setup the item click listener
        gamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link ActivityEditor}
                Intent i = new Intent(CatalogActivity.this, ActivityEditor.class);

                Uri currentBookUri = ContentUris.withAppendedId(GameInventoryEntry.CONTENT_URI, id);
                i.setData(currentBookUri);
                startActivity(i);
            }
        });


        getLoaderManager().initLoader( GAME_LOADER, null, this);
    }

    private void deleteAllGames() {
        // Defines a variable to contain the number of rows deleted
        int rowsDeleted = 0;

        // Deletes the rows that match the selection criteria
        rowsDeleted = getContentResolver().delete(
                GameInventoryEntry.CONTENT_URI,   // the user dictionary content URI
                null,                    // the column to select on
                null                      // the value to compare to
        );
        if (rowsDeleted == 0) {
            // If the value of rowsDeleted is 0, then there was problem with deleting rows
            // or no rows match the selection criteria.
            Toast.makeText(this, R.string.error_while_deleting_games,
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the deletion was successful and we can display a toast.
            Toast.makeText(this, R.string.all_games_deleted,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {

        /*
         * If emptyView is already visible, then it means there are no entries in the table.
         * Thus we don't need to show dialog box to the user for deleting all the entries in the table as table is already empty.
         */
        if(!(emptyView.getVisibility() == View.VISIBLE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_all_games);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button to delete the game.
                    deleteAllGames();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button,
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()){
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                GameInventoryEntry._ID,
                GameInventoryEntry.COLUMN_GAME_NAME,
                GameInventoryEntry.COLUMN_GAME_PRICE,
                GameInventoryEntry.COLUMN_GAME_QUANTITY,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER
        };
        // Perform a query on the games table
        return new CursorLoader( this,
                GameInventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link BooksCursorAdapter} with this new cursor containing updated game data
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
