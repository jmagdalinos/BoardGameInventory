package com.example.android.boardgameinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.boardgameinventory.data.BoardGameContract.BoardGameEntry;

import static com.example.android.boardgameinventory.data.BoardGameDbHelper.LOG_TAG;

/**
 * Content Provider for the boardgame table.
 */

public class BoardGameProvider extends ContentProvider {

    /** Database helper object */
    private BoardGameDbHelper mDbHelper;

    /** URI matcher code for the content URI for a single board game */
    private static final int BOARDGAMES = 100;

    /** URI matcher code for the content URI for the boardgames table */
    private static final int BOARDGAME_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer which is run the first time anything is called from this class
    static {
        // All possible paths are added below along with their corresponding code
        sUriMatcher.addURI(BoardGameContract.CONTENT_AUTHORITY, BoardGameContract
                .PATH_BOARD_GAMES, BOARDGAMES);
        sUriMatcher.addURI(BoardGameContract.CONTENT_AUTHORITY, BoardGameContract
                .PATH_BOARD_GAMES +"/#", BOARDGAME_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new BoardGameDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Cursor object that will hold the results of the method
        Cursor cursor;

        // Use UriMatcher to match the given URI to a specific code
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOARDGAMES:
                // We are querying for the entire table, therefore we use the given projection,
                // selection, selectionArgs and sortOrder
                cursor = db.query(BoardGameEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOARDGAME_ID:
                // We are querying for a specific row, therefore the selection should be "_id=?"
                // and the selectionArgs should be a String array containing the ID
                selection = BoardGameEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(BoardGameEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOARDGAMES:
                return BoardGameEntry.CONTENT_LIST_TYPE;
            case BOARDGAME_ID:
                return BoardGameEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOARDGAMES:
                return insertBoardGame(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Store the number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOARDGAMES:
                // Delete all rows in the table
                rowsDeleted = db.delete(BoardGameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOARDGAME_ID:
                // Get the row id from the URI and delete that single row
                selection = BoardGameEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BoardGameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If there was at least 1 deleted row, notify all listeners that there has been a change
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Use uriMatcher to determine whether the entire table or a single board game has to be updated
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOARDGAMES:
                return updateBoardGame(uri, values, selection, selectionArgs);
            case BOARDGAME_ID:
                selection = BoardGameEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateBoardGame(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Insert a new board game into the database and return the new content URI for that specific
     * row in the table.
     */
    private Uri insertBoardGame(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(BoardGameEntry.COLUMN_BOARDGAME_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The board game requires a name");
        }

        // Check that the publication year is not null
        Integer year = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_YEAR);
        if (year == null) {
            throw new IllegalArgumentException("The board game requires a publication year");
        }

        // Check that the publication digit is in 4 digit format (e.g. 2014, not 014)
        int length = String.valueOf(year).length();
        if (length != 4) {
            throw new IllegalArgumentException("Publication year must have 4 digits");
        }

        // Check that the price is not null
        Integer price = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("The board game requires a price");
        }

        // Check that the supplier is not null and that it has an acceptable value
        Integer supplier = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER);
        if (supplier == null || !BoardGameEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("The board game requires a valid supplier");
        }

        // Check that the quantity is not null and not negative
        Integer quantity = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("The board game requires a valid quantity");
        }

        // Check that the number of players is not null
        String players = values.getAsString(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS);
        if (players == null) {
            throw new IllegalArgumentException("The board game requires a valid number of players");
        }

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new boardgame
        long id = db.insert(BoardGameEntry.TABLE_NAME, null, values);

        // Check if the insertion was successful
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that there has been a change
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update board games in the database with the given content values. Apply the changes to the
     * rows specified in the selection and selection arguments (which could be 0 or 1 or more
     * board games).
     * Return the number of rows that were successfully updated.
     */
    private int updateBoardGame(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        // If the Name is present, check that it is not null
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_NAME)) {
            String name = values.getAsString(BoardGameEntry.COLUMN_BOARDGAME_NAME);
            if (name == null) {
                throw new IllegalArgumentException("The board game requires a name");
            }
        }

        // If the publication year is present, check that it is not null
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_YEAR)) {
            Integer year = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_YEAR);
            if (year == null) {
                throw new IllegalArgumentException("The board game requires a publication year");
            }

            // Check that the publication digit is in 4 digit format (e.g. 2014, not 014)
            int length = String.valueOf(year).length();
            if (length != 4) {
                throw new IllegalArgumentException("Publication year must have 4 digits");
            }
        }

        // If the price is present, check that it is not null
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_PRICE)) {
            Integer price = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("The board game requires a price");
            }
        }

        // If the supplier is present, check that it is not null and that it has an
        // acceptable value
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER)) {
            Integer supplier = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER);
            if (supplier == null || !BoardGameEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException("The board game requires a valid supplier");
            }
        }

        // If the quantity is present, check that it is not null and not negative
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("The board game requires a valid quantity");
            }
        }

        // If the the number of players is present, check that it is not null
        if (values.containsKey(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS)) {
            String players = values.getAsString(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS);
            if (players == null) {
                throw new IllegalArgumentException("The board game requires a valid number of players");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(BoardGameEntry.TABLE_NAME, values, selection, selectionArgs);
        Log.v("BoardGameProvider", "rowsUpdated= " + rowsUpdated);

        // If there was at least 1 updated row, notify all listeners that there has been a change
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
