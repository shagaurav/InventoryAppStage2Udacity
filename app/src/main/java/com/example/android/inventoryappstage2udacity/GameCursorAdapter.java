package com.example.android.inventoryappstage2udacity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.inventoryappstage2udacity.data.GameInventoryContract.GameInventoryEntry;

public class GameCursorAdapter extends CursorAdapter{

    /**
     * Constructs a new {@link GameCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView gameNameTextView = view.findViewById(R.id.display_name);
        TextView gamePriceTextView = view.findViewById(R.id.display_price);
        TextView gameQuantityTextView = view.findViewById(R.id.display_quantity);

        // Find the columns of book attributes that we're interested in
        int gameNameColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_NAME);
        int gamePriceColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_PRICE);
        int gameQuantityColumnIndex = cursor.getColumnIndex(GameInventoryEntry.COLUMN_GAME_QUANTITY);

        // Read the book attributes from the Cursor for the current book
        String gameName = cursor.getString(gameNameColumnIndex);
        int gamePrice = cursor.getInt(gamePriceColumnIndex);
        int gameQuantity = cursor.getInt(gameQuantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        gameNameTextView.setText(gameName);
        gamePriceTextView.setText(String.valueOf(gamePrice));
        gameQuantityTextView.setText(String.valueOf(gameQuantity));

        // column number of "_ID"
        int gameIdColumIndex = cursor.getColumnIndex(GameInventoryEntry._ID);

        // Read the book attributes from the Cursor for the current book for "Sale" button
        final long productIdVal = Integer.parseInt(cursor.getString(gameIdColumIndex));
        final int currentProductQuantityVal = cursor.getInt(gameQuantityColumnIndex);

        /*
         * Each list view item will have a "Sale" button
         * This "Sale" button has OnClickListener which will decrease the product quantity by one at a time.
         * Update is only carried out if quantity is greater than 0(i.e MIMINUM quantity is 0).
         */
        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentUri = ContentUris.withAppendedId(GameInventoryEntry.CONTENT_URI, productIdVal);

                String updatedQuantity = String.valueOf(currentProductQuantityVal - 1);

                if(Integer.parseInt(updatedQuantity)>=0){
                    ContentValues values = new ContentValues();
                    values.put(GameInventoryEntry.COLUMN_GAME_QUANTITY,updatedQuantity);
                    context.getContentResolver().update(currentUri,values,null,null);
                }
            }
        });
    }
}
