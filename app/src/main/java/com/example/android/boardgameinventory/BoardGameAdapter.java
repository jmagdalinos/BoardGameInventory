package com.example.android.boardgameinventory;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.boardgameinventory.data.BoardGameContract.BoardGameEntry;

import java.util.concurrent.ExecutionException;

import static com.example.android.boardgameinventory.R.id.price;

/**
 * This is a custom cursor adapter for a list view or grid view.
 * It uses a cursor of board games data as its data source.
 * It creates a list item for each row of the cursor's data
 */

public class BoardGameAdapter extends CursorAdapter{

    /** Variable holding board game name */
    private String mName;

    /** Variable holding board game publication year */
    private int mYear;

    /** Variable holding board game final price */
    private String mFinalPrice;

    /**
     * Constructs a new {@link BoardGameAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BoardGameAdapter(Context context, Cursor c) {
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
     * This method binds the board game data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find the views that will be used
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView publicationTextView = (TextView) view.findViewById(R.id.publication);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(price);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.image);
        Button saleButton = (Button) view.findViewById(R.id.list_button);

        // Find the columns of board game attributes we are interested in
        int nameColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_NAME);
        int yearColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_YEAR);
        final int quantityColumnIndex = cursor.getColumnIndex(BoardGameEntry
                .COLUMN_BOARDGAME_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_PRICE);
        int pictureColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_PICTURE);
        int idColumnIndex = cursor.getColumnIndex(BoardGameEntry._ID);

        // Read the attributes from the cursor for the current board game
        mName = cursor.getString(nameColumnIndex);
        mYear = cursor.getInt(yearColumnIndex);
        final int mQuantity = cursor.getInt(quantityColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        String pictureString = cursor.getString(pictureColumnIndex);
        final int mBoardGameId = cursor.getInt(idColumnIndex);

        // Convert price to double
        double decimalPrice = (double) price/100.00;

        // Convert price to String and add Euro sign
        mFinalPrice = String.format("%.2f", decimalPrice) + " \u20ac";

        // Convert the picture to uri
        Uri imageUri = null;
        if (pictureString != null) {
            imageUri = Uri.parse(pictureString);
        } else {
            imageUri = null;
        }

        // Check if the image uri is null
        if (imageUri != null) {
            // Show the image on the imageView
            DownScaledImage downScaledImage = new DownScaledImage(context, imageUri, 120, 120);
            downScaledImage.execute();
            try {
                pictureImageView.setImageBitmap(downScaledImage.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            // Show the placeholder instead
            pictureImageView.setImageDrawable(context.getResources().getDrawable(R.drawable
                    .no_image, null));
        }

        // Update the TextViews with the attributes for the current board game
        nameTextView.setText(mName);
        publicationTextView.setText(String.valueOf(mYear));
        quantityTextView.setText(String.valueOf(mQuantity));
        priceTextView.setText(mFinalPrice);

        // Setup listener for button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity > 0 to avoid negative numbers
                if (mQuantity > 0) {
                    int quantityNew = mQuantity -1;
                    // Create Content Uri
                    Uri uri = ContentUris.withAppendedId(BoardGameEntry.CONTENT_URI, mBoardGameId);

                    // Create new content values and update the database
                    ContentValues values = new ContentValues();
                    values.put(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY, quantityNew);

                    int updatedRows = context.getContentResolver().update(uri, values, null, null);

                    // Notify all listeners that there has been a change
                    context.getContentResolver().notifyChange(BoardGameEntry.CONTENT_URI, null);
                }
            }
        });
    }
}
