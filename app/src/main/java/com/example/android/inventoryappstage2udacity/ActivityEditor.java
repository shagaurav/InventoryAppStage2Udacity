package com.example.android.inventoryappstage2udacity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.LoaderManager.LoaderCallbacks;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.content.CursorLoader;

import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

/**
 * Allows user to create a new game or edit an existing one.
 */
public class ActivityEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MINIMUM_QUANTITY_VALUE = 0;

    private static final int MAXIMUM_QUANTITY_VALUE = 999;
    /**
     * Identifier for the game data loader
     */
    private static final int EXISTING_GAME_LOADER = 1;
    /**
     * Boolean keeps track of whether the game has been edited (true) or not (false)
     */
    private boolean gameHasChanged = false;
    /**
     * Supplier contact number will be save in supplierContact variable
     **/
    private String supplierContact;
    /**
     * Content URI for the existing game
     */
    private Uri currentGameUri;

    // EditText field to enter the games's name
    private EditText gameNameEditText;

    // EditText field to enter the game's price
    private EditText gamePriceEditText;

    // EditText field to enter the game's quantity
    private EditText gameQuantityEditText;

    // EditText field to enter the game's supplier name
    private Spinner supplierNameSpinner;

    // EditText field to enter the game's supplier contact
    private EditText supplierContactEditText;

    // Decrement button
    private Button subtractQuantityButton;

    // Increment Button
    private Button addQuantityButton;

    // Supplier of the game
    private int supplierName = GameInventoryEntry.SUPPLIER_UNKNOWN;
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the gameHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gameHasChanged = true;
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_editor );

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new game or editing an existing one.
        Intent intent = getIntent();
        currentGameUri = intent.getData();

        // If the intent DOES NOT contain a game content URI, then we know that we are
        // creating a new game.
        if (currentGameUri == null) {
            // This is a new game, so change the app bar to say "Add a Game"
            setTitle( getString( R.string.add_a_game ) );
            // Invalidate the options menu, so the "Delete" and "Contact Supplier" menu option can be hidden.
            // (It doesn't make sense to delete a Game or contact supplier that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing Game, so change app bar to say "Edit Game"
            setTitle( getString( R.string.edit_game ) );
            getLoaderManager().initLoader( EXISTING_GAME_LOADER, null, (LoaderCallbacks<Object>) this );
        }

        // Find all relevant views that we will need to read user input from
        gameNameEditText = findViewById( R.id.product_name );
        gamePriceEditText = findViewById( R.id.poduct_price );
        gameQuantityEditText = findViewById( R.id.product_quantity );
        supplierNameSpinner = findViewById( R.id.product_supplier_name_spinner );
        supplierContactEditText = findViewById( R.id.supplier_contact );
        subtractQuantityButton = findViewById( R.id.decrease_button );
        addQuantityButton = findViewById( R.id.increase_button );

        // Add Minimum quantity value
        subtractQuantityButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentQuantityString = gameQuantityEditText.getText().toString();
                int currentQuantityInt;
                if (currentQuantityString.length() == 0) {
                    currentQuantityInt = 0;
                    gameQuantityEditText.setText( String.valueOf( currentQuantityInt ) );
                } else {
                    currentQuantityInt = Integer.parseInt( currentQuantityString ) - 1;
                    if (currentQuantityInt >= MINIMUM_QUANTITY_VALUE) {
                        gameQuantityEditText.setText( String.valueOf( currentQuantityInt ) );
                    }
                }

            }
        } );
        // Add maximum quantity value
        addQuantityButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentQuantityString = gameQuantityEditText.getText().toString();
                int currentQuantityInt;
                if (currentQuantityString.length() == 0) {
                    currentQuantityInt = 1;
                    gameQuantityEditText.setText( String.valueOf( currentQuantityInt ) );
                } else {
                    currentQuantityInt = Integer.parseInt( currentQuantityString ) + 1;
                    if (currentQuantityInt <= MAXIMUM_QUANTITY_VALUE) {
                        gameQuantityEditText.setText( String.valueOf( currentQuantityInt ) );
                    }
                }
            }
        } );

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        gameNameEditText.setOnTouchListener( mTouchListener );
        gamePriceEditText.setOnTouchListener( mTouchListener );
        gameQuantityEditText.setOnTouchListener( mTouchListener );
        supplierNameSpinner.setOnTouchListener( mTouchListener );
        subtractQuantityButton.setOnTouchListener( mTouchListener );
        addQuantityButton.setOnTouchListener( mTouchListener );
        supplierContactEditText.setOnTouchListener( mTouchListener );

        // Spinner for supplier name
        setupSpinner();
    }

    // Spinner for Supplier's name
    private void setupSpinner() {

        ArrayAdapter productSupplieNameSpinnerAdapter = ArrayAdapter.createFromResource( this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item );

        productSupplieNameSpinnerAdapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );

        supplierNameSpinner.setAdapter( productSupplieNameSpinnerAdapter );

        supplierNameSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition( position );
                if (!TextUtils.isEmpty( selection )) {
                    if (selection.equals( getString( R.string.supplier_sony ) )) {
                        supplierName = GameInventoryEntry.SUPPLIER_SONY;
                    } else if (selection.equals( getString( R.string.supplier_gamers ) )) {
                        supplierName = GameInventoryEntry.SUPPLIER_GAMERS;
                    } else if (selection.equals( getString( R.string.supplier_players ) )) {
                        supplierName = GameInventoryEntry.SUPPLIER_PLAYERS;
                    } else {
                        supplierName = GameInventoryEntry.SUPPLIER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                supplierName = GameInventoryEntry.SUPPLIER_UNKNOWN;
            }
        } );
    }

    @Override
    public void onBackPressed() {
        // If the entry hasn't changed, continue with handling back button press
        if (!gameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog( discardButtonClickListener );
    }

    //Get user input from editor and save new game info into database.
    private void insertGame() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String gameNameString = gameNameEditText.getText().toString().trim();
        String gamePriceString = gamePriceEditText.getText().toString().trim();
        String gameQuantityString = gameQuantityEditText.getText().toString().trim();
        String supplierContactString = supplierContactEditText.getText().toString().trim();

        if (TextUtils.isEmpty( gameNameString )) {
            gamePriceEditText.setError( getString( R.string.required ) );
            return;
        }

        if (TextUtils.isEmpty( gamePriceString )) {
            gamePriceEditText.setError( getString( R.string.required ) );
            return;
        }
        if (TextUtils.isEmpty( gameQuantityString )) {
            gameQuantityEditText.setError( getString( R.string.required ) );
            return;
        }

        if (TextUtils.isEmpty( supplierContactString )) {
            supplierContactEditText.setError( getString( R.string.required ) );
            return;
        }

        int gamePriceInt = Integer.parseInt( gamePriceString );
        int gameQuantityInt = Integer.parseInt( gameQuantityString );

        if (gamePriceInt < 0) {
            gamePriceEditText.setError( getString( R.string.price_cannot_be_negative ) );
            return;
        }
        if (gameQuantityInt < 0) {
            gameQuantityEditText.setError( getString( R.string.quantity_cannot_be_negative ) );
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put( GameInventoryEntry.COLUMN_GAME_NAME, gameNameString );
        values.put( GameInventoryEntry.COLUMN_GAME_PRICE, gamePriceInt );
        values.put( GameInventoryEntry.COLUMN_GAME_QUANTITY, gameQuantityInt );
        values.put( GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME, supplierName );
        values.put( GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER, supplierContact );

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

    private void deleteGame() {
        if (currentGameUri != null) {
            int rowsDeleted = 0;

            // Deletes the words that match the selection criteria
            rowsDeleted = getContentResolver().delete(
                    currentGameUri,   // the user dictionary content URI
                    null,                    // the column to select on
                    null                      // the value to compare to
            );
            if (rowsDeleted == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText( this, getString( R.string.error_deleting_game ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText( this, getString( R.string.game_deleted ),
                        Toast.LENGTH_SHORT ).show();
            }
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.delete_game ) );
        builder.setPositiveButton( getString( R.string.delete ), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the game.
                deleteGame();
            }
        } );
        builder.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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

    private void callSupplier() {
        Intent supplierNumberIntent = new Intent( Intent.ACTION_DIAL );
        supplierNumberIntent.setData( Uri.parse( "tel:" + supplierContact ) );
        startActivity( supplierNumberIntent );
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu( menu );
        // If this is a new game, hide the "Delete" menu item.
        if (currentGameUri == null) {
            MenuItem menuItem;
            menuItem = menu.findItem( R.id.action_delete );
            menuItem.setVisible( true );
            menuItem = menu.findItem( R.id.action_contact_supplier );
            menuItem.setVisible( true );
        }
        return true;
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
                    NavUtils.navigateUpFromSameTask( ActivityEditor.this );
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask( ActivityEditor.this );
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all games attributes, define a projection that contains
        // all columns from the games table
        String[] projection = {
                GameInventoryEntry._ID,
                GameInventoryEntry.COLUMN_GAME_NAME,
                GameInventoryEntry.COLUMN_GAME_PRICE,
                GameInventoryEntry.COLUMN_GAME_QUANTITY,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME,
                GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER,
        };

        return new CursorLoader( this,
                currentGameUri,
                projection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of games attributes that we're interested in
            int gameNameColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_NAME );
            int gamePriceColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_PRICE );
            int gameQuantityColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_QUANTITY );
            int supplierNameColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_SUPPLIER_NAME );
            int supplierContactColumnIndex = cursor.getColumnIndex( GameInventoryEntry.COLUMN_GAME_SUPPLIER_PHONE_NUMBER );

            // Extract out the value from the Cursor for the given column index
            String gameName = cursor.getString( gameNameColumnIndex );
            final int gamePrice = cursor.getInt( gamePriceColumnIndex );
            final int gameQuantity = cursor.getInt( gameQuantityColumnIndex );
            final int supplierName = cursor.getInt( supplierNameColumnIndex );
            final String supplierContact = cursor.getString( supplierContactColumnIndex );

            // Update the views on the screen with the values from the database
            gameNameEditText.setText( gameName );
            gamePriceEditText.setText( String.valueOf( gamePrice ) );
            gameQuantityEditText.setText( String.valueOf( gameQuantity ) );
            supplierContactEditText.setText( String.valueOf( supplierContact ) );

            switch (supplierName) {
                case GameInventoryEntry.SUPPLIER_SONY:
                    supplierNameSpinner.setSelection( 1 );
                    break;
                case GameInventoryEntry.SUPPLIER_GAMERS:
                    supplierNameSpinner.setSelection( 2 );
                    break;
                case GameInventoryEntry.SUPPLIER_PLAYERS:
                    supplierNameSpinner.setSelection( 3 );
                    break;
                default:
                    supplierNameSpinner.setSelection( 0 );
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        gameNameEditText.setText( "" );
        gamePriceEditText.setText( "" );
        gameQuantityEditText.setText( "" );
        supplierContactEditText.setText( "" );
    }
}
