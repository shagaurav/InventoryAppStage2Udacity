package com.example.android.inventoryappstage2udacity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;

import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

public class ActivityInfo extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the game data loader
    public static final int EXISTING_GAME_LOADER = 0;

    private boolean gameHasChanged = false;

    /**
     * Supplier contact number will be save in supplierContact variable
     **/
    private String supplierContact;

    // Content URI for existing game
    private Uri currentGameUri;

    private EditText gameInfoNameTextView;
    private TextView gameInfoPriceTextView;
    private TextView gameInfoQuantityTextView;
    private TextView gameInfoSupplierNameTextView;
    private TextView gameInfoSupplierContactTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_info );
        gameInfoNameTextView = findViewById( R.id.game_details_name_text_view );
        gameInfoPriceTextView = findViewById( R.id.game_details_price_text_view );
        gameInfoQuantityTextView = findViewById( R.id.game_details_quantity_textView );
        gameInfoSupplierNameTextView = findViewById( R.id.game_details_supplier_name_textView );
        gameInfoSupplierContactTextView = findViewById( R.id.game_details_supplier_contact_text_view );

        Button deleteGameButton = findViewById( R.id.delete_game_button );

        Intent intent = getIntent();
        currentGameUri = intent.getData();

        // If the intent does not contain a game content URI, create a new game
        if (currentGameUri == null) {
            invalidateOptionsMenu();
        } else {
            // Initialize a loader to read the games data from the database and display current values in editor
            getLoaderManager().initLoader( EXISTING_GAME_LOADER, null, this );
        }

        deleteGameButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        } );
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
        return new CursorLoader( this, currentGameUri, projection, null, null, null );
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
            int gameNameColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_NAME );
            int gamePriceColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_PRICE );
            int gameQuantityColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_QUANTITY );
            int supplierNameColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME );
            int supplierContactColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER );

            // Extract out the value from the Cursor for the given column index
            String gameName = cursor.getString( gameNameColumnIndex );
            final int gamePrice = cursor.getInt( gamePriceColumnIndex );
            final int gameQuantity = cursor.getInt( gameQuantityColumnIndex );
            final String supplierName = cursor.getString( supplierNameColumnIndex );
            final Integer supplierContact = cursor.getInt( supplierContactColumnIndex );

            // Update the views on the screen with values from database
            gameInfoNameTextView.setText( gameName );
            gameInfoPriceTextView.setText( MessageFormat.format( "${0}", gamePrice ) );
            gameInfoQuantityTextView.setText( MessageFormat.format( "{0}", gameQuantity ) );
            gameInfoSupplierNameTextView.setText( supplierName );
            gameInfoSupplierContactTextView.setText( Integer.toString( supplierContact ) );

            Button gameDetailsIncrementQuantityButtonImageView = findViewById( R.id.gameDetailsIncrementQuantityButtonImageView );
            gameDetailsIncrementQuantityButtonImageView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incrementGameQuantity( gameQuantity );
                }
            } );

            Button gameDetailsDecrementQuantityButtonImageView = findViewById( R.id.gameDetailsDecrementQuantityButtonImageView );
            gameDetailsDecrementQuantityButtonImageView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decrementGameQuantity( gameQuantity );
                }
            } );

        }
    }

    private void incrementGameQuantity(int gameCalculateQuantity) {
        gameCalculateQuantity = gameCalculateQuantity + 1;
        if (gameCalculateQuantity >= 0) {
            updateBook( gameCalculateQuantity );
            Toast.makeText( this, getString( R.string.game_quantity_increment_successful ), Toast.LENGTH_SHORT ).show();
        }
    }

    private void decrementGameQuantity(int gameCalculateQuantity) {
        gameCalculateQuantity = gameCalculateQuantity - 1;
        if (gameCalculateQuantity >= 0) {
            updateBook( gameCalculateQuantity );
            Toast.makeText( this, getString( R.string.game_quantity_decrement_successful ), Toast.LENGTH_SHORT ).show();
        } else {
            Toast.makeText( this, getString( R.string.out_of_stock ), Toast.LENGTH_SHORT ).show();
        }
    }

    private void updateBook(int gameCalculateQuantity) {
        if (currentGameUri == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put( GameInventoryEntry.COLUMN_GAME_QUANTITY, gameCalculateQuantity );

        if (currentGameUri == null) {
            // This is a new book
            Uri newUri = getContentResolver().insert( GameInventoryEntry.CONTENT_URI, values );

            // Show a Toast depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, there was an error with insertion
                Toast.makeText( this, getString( R.string.error_saving_games ), Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise insertion was successful
                Toast.makeText( this, getString( R.string.game_saved ), Toast.LENGTH_SHORT ).show();
            }
        } else {
            // Otherwise this is an existing book, so update the book with content URI
            int rowsAffected = getContentResolver().update( currentGameUri, values, null, null );

            // Show a Toast depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, there was an error with update
                Toast.makeText( this, getString( R.string.error_saving_games ), Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise update was successful
                Toast.makeText( this, getString( R.string.game_saved ), Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.delete_game ) );
        builder.setPositiveButton( getString( R.string.delete ), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Delete deleteGameButton, so delete the game
                deleteBook();
            }
        } );
        builder.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Keep Editing deleteGameButton, dismiss the dialog and continue editing the game
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the game
    private void deleteBook() {
        // Only perform the delete if this is an existing game
        if (currentGameUri != null) {
            int rowsDeleted = getContentResolver().delete( currentGameUri, null, null );

            // Show a Toast depending on whether or not the delete operation was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, there was an error with delete operation
                Toast.makeText( this, getString( R.string.error_deleting_game ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.game_deleted ), Toast.LENGTH_SHORT ).show();
            }
        }
        // Close the activity
        finish();
    }

    //Get user input from editor and save new game info into database.
    private void insertGame() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String gameNameString = gameInfoNameTextView.getText().toString().trim();
        String gamePriceString = gameInfoPriceTextView.getText().toString().trim();
        String gameQuantityString = gameInfoQuantityTextView.getText().toString().trim();
        String supplierNameString = gameInfoSupplierNameTextView.getText().toString().trim();
        String supplierContactString = gameInfoSupplierContactTextView.getText().toString().trim();

        if (TextUtils.isEmpty( gameNameString )) {
            gameInfoNameTextView.setError( getString( R.string.required ) );
            return;
        }

        if (TextUtils.isEmpty( gamePriceString )) {
            gameInfoPriceTextView.setError( getString( R.string.required ) );
            return;
        }
        if (TextUtils.isEmpty( gameQuantityString )) {
            gameInfoQuantityTextView.setError( getString( R.string.required ) );
            return;
        }

        if (TextUtils.isEmpty( supplierNameString )) {
            gameInfoSupplierNameTextView.setError( getString( R.string.required ) );
            return;
        }

        if (TextUtils.isEmpty( supplierContactString )) {
            gameInfoSupplierContactTextView.setError( getString( R.string.required ) );
            return;
        }

        int gamePriceInt = Integer.parseInt( gamePriceString );
        int gameQuantityInt = Integer.parseInt( gameQuantityString );

        if (gamePriceInt < 0) {
            gameInfoPriceTextView.setError( getString( R.string.price_cannot_be_negative ) );
            return;
        }
        if (gameQuantityInt < 0) {
            gameInfoQuantityTextView.setError( getString( R.string.quantity_cannot_be_negative ) );
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put( GameInventoryEntry.COLUMN_GAME_NAME, gameNameString );
        values.put( GameInventoryEntry.COLUMN_GAME_PRICE, gamePriceInt );
        values.put( GameInventoryEntry.COLUMN_GAME_QUANTITY, gameQuantityInt );
        values.put( GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME, supplierNameString );
        values.put( GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER, supplierContactString );

        // Determine if this is a new or existing game by checking if currentGameUri is null or not
        if (currentGameUri == null) {
            // This is a NEW GAME, so insert a new game into the provider,
            // returning the content URI for the new game.
            Uri newUri = getContentResolver().insert( GameInventoryEntry.CONTENT_URI, values );

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText( this, getString( R.string.insert_game_error ), Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText( this, getString( R.string.insert_game_successful ), Toast.LENGTH_SHORT ).show();
            }
        } else {
            // Otherwise this is an EXISTING game, so update the game with content URI: currentGameUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because currentGameUri will already identify the correct row in the database that
            // we want to modify.
            int rowAffected = getContentResolver().update( currentGameUri, values, null, null );

            // Show a toast message depending on whether or not the update was successful.
            if (rowAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText( this, getString( R.string.editor_update_game_error ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText( this, getString( R.string.editor_update_game_successful ),
                        Toast.LENGTH_SHORT ).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.discard_changes_and_quit_editing ) );
        builder.setPositiveButton( getString( R.string.discard ), discardButtonClickListener );
        builder.setNegativeButton( getString( R.string.keep_editing ), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate( R.menu.menu_editor, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                insertGame();
                // Exit activity
                return true;

            // Respond to a click on the "Contact Supplier" menu option
            case R.id.action_contact_supplier:
                // Contact the supplier via intent
                callSupplier();
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Allow user to confirm for deleting the entry
                showDeleteConfirmationDialog();
                break;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                if (!gameHasChanged) {
                    NavUtils.navigateUpFromSameTask( ActivityInfo.this );
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask( ActivityInfo.this );
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    private void callSupplier() {
        Intent supplierNumberIntent = new Intent( Intent.ACTION_DIAL );
        supplierNumberIntent.setData( Uri.parse( "tel:" + supplierContact ) );
        startActivity( supplierNumberIntent );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from TextView
        gameInfoNameTextView.setText( "" );
        gameInfoPriceTextView.setText( "" );
        gameInfoQuantityTextView.setText( "" );
        gameInfoSupplierNameTextView.setText( "" );
        gameInfoSupplierContactTextView.setText( "" );
    }
}
