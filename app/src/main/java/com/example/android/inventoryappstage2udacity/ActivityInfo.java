package com.example.android.inventoryappstage2udacity;

import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.MessageFormat;
import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

public class ActivityInfo extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    // Identifier for the game data loader
    public static final int EXISTING_GAME_LOADER = 0;


    // Content URI for existing game
    private Uri currentGameUri;

    private TextView gameInfoNameTextView;
    private TextView gameInfoPriceTextView;
    private TextView gameInfoQuantityTextView;
    private TextView gameInfoSupplierNameTextView;
    private TextView gameInfoSupplierContactTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_info );
        gameInfoNameTextView = findViewById(R.id.game_details_name_text_view );
        gameInfoPriceTextView = findViewById(R.id.game_details_price_text_view );
        gameInfoQuantityTextView = findViewById(R.id.game_details_quantity_textView );
        gameInfoSupplierNameTextView = findViewById(R.id.game_details_supplier_name_textView );
        gameInfoSupplierContactTextView = findViewById(R.id.game_details_supplier_contact_text_view );

        Button deleteBookButton = findViewById(R.id.delete_game_button );

        Intent intent = getIntent();
        currentGameUri = intent.getData();

        // If the intent does not contain a game content URI, create a new game
        if (currentGameUri == null) {
            invalidateOptionsMenu();
        } else {
            // Initialize a loader to read the games data from the database and display current values in editor
            getLoaderManager().initLoader( EXISTING_GAME_LOADER, null, this);
        }

        deleteBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains all columns from games table
        String[] projection = {
                GameInventoryEntry._ID,
                GameInventoryEntry.COLUMN_GAME_NAME,
                GameInventoryEntry.COLUMN_GAME_PRICE,
                GameInventoryEntry.COLUMN_GAME_QUANTITY,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, currentGameUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of cursor and reading cursor from it
        if (cursor.moveToFirst()) {
            // figure out the index of each column
            int gameNameColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_NAME);
            int gamePriceColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_PRICE);
            int gameQuantityColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String gameName = cursor.getString( gameNameColumnIndex );
            final int gamePrice = cursor.getInt( gamePriceColumnIndex );
            final int gameQuantity = cursor.getInt( gameQuantityColumnIndex );
            final String supplierName = cursor.getString( supplierNameColumnIndex );
            final String supplierContact = cursor.getString( supplierContactColumnIndex );

            // Update the views on the screen with values from database
            gameInfoNameTextView.setText(gameName);
            gameInfoPriceTextView.setText(MessageFormat.format("${0}", gamePrice));
            gameInfoQuantityTextView.setText(MessageFormat.format("{0}", gameQuantity));
            gameInfoSupplierNameTextView.setText(supplierName);
            gameInfoSupplierContactTextView.setText(supplierContact);


            gameInfoSupplierContactTextView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.fromParts(getString(R.string.tell), supplierContact, null));
                    startActivity(intent);
                }
            });
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_game));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Delete deleteGameButton, so delete the game
                deleteBook();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Keep Editing deleteGameButton, dismiss the dialog and continue editing the game
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the game
    private void deleteBook() {
        // Only perform the delete if this is an existing game
        if (currentGameUri != null) {
            int rowsDeleted = getContentResolver().delete( currentGameUri, null, null);

            // Show a Toast depending on whether or not the delete operation was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, there was an error with delete operation
                Toast.makeText(this, getString(R.string.error_deleting_game), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.game_deleted), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from TextView
        gameInfoNameTextView.setText("");
        gameInfoPriceTextView.setText("");
        gameInfoQuantityTextView.setText("");
        gameInfoSupplierNameTextView.setText("");
        gameInfoSupplierContactTextView.setText("");
    }
}
