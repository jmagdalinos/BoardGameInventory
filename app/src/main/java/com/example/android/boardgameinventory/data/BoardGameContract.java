package com.example.android.boardgameinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Board Game Inventory app.
 */

public class BoardGameContract {
    // To prevent someone from accidentally instantiating the contract class, we make the
    // constructor private.
    private BoardGameContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.boardgameinventory";

    /** Using CONTENT_AUTHORITY, we create the base of all URIs which other apps will use to
     * contact the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible paths (appended to base content URI for possible URIs)
     */
    public static final String PATH_BOARD_GAMES = "boardgames";


    /** Inner class that defines the table contents
     *  Each entry represents a board game title
     */
    public static class BoardGameEntry implements BaseColumns {
        /**
         * The content URI to access the board game data
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                PATH_BOARD_GAMES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of board games.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single board game.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/";

        /** Name of database table for the board games */
        public static final String TABLE_NAME = "boardgames";

        /**
         * Unique ID number for the board game (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String STRING_ID = BaseColumns._ID;

        /**
         * Name of the board game
         *
         * Type: TEXT
         */
        public static final String COLUMN_BOARDGAME_NAME = "name";

        /**
         * The original publication year for the boardgame
         *
         * Type: INTEGER
         */
        public static final String COLUMN_BOARDGAME_YEAR = "year";

        /**
         * The price of the board game
         *
         * To minimize memory usage, the price is an integer and should be divided by 100 to get
         * the actual price (e.g. an integer of 1030 represents 10,30 EUR in actuality)
         *
         * Type: INTEGER
         */
        public static final String COLUMN_BOARDGAME_PRICE = "price";

        /**
         * The supplier of the board game.
         *
         * There are 5 possible values as there are only 5 suppliers:
         * {@link #SUPPLIER_TOTAL_GAMES},
         * {@link #SUPPLIER_FANTASY_EXTREME},
         * {@link #SUPPLIER_GAMEHOLIC},
         * {@link #SUPPLIER_RARED20},
         * {@link #SUPPLIER_LEISUREGAMER};
         *
         * Type: INTEGER
         */
        public static final String COLUMN_BOARDGAME_SUPPLIER = "supplier";

        /**
         * The quantity of the board game in the inventory
         *
         * Type: INTEGER
         */
        public static final String COLUMN_BOARDGAME_QUANTITY = "quantity";

        /**
         * An image of the board game. Stores the URI of the image
         *
         * Type: TEXT
         */
        public static final String COLUMN_BOARDGAME_PICTURE = "picture";

        /**
         * Number of players the game is meant to be played with (e.g. 2, 2-5)
         *
         * Type: TEXT
         */
        public static final String COLUMN_BOARDGAME_PLAYERS = "players";

        /**
         * Possible values for the Supplier
         */
        public static final int SUPPLIER_TOTAL_GAMES = 0;
        public static final int SUPPLIER_FANTASY_EXTREME = 1;
        public static final int SUPPLIER_GAMEHOLIC = 2;
        public static final int SUPPLIER_RARED20 = 3;
        public static final int SUPPLIER_LEISUREGAMER = 4;

        /**
         * Returns whether or not the given supplier is one among the 5 listed above (for
         * validation reasons)
         */
        public static boolean isValidSupplier(int supplier) {
            if (supplier == SUPPLIER_TOTAL_GAMES || supplier == SUPPLIER_FANTASY_EXTREME ||
            supplier == SUPPLIER_GAMEHOLIC || supplier == SUPPLIER_RARED20 || supplier ==
                    SUPPLIER_LEISUREGAMER) {
                return true;
            }
            return false;
        }
    }
}
