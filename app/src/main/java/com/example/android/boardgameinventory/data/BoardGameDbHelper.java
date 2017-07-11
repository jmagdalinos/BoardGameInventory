package com.example.android.boardgameinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.boardgameinventory.data.BoardGameContract.BoardGameEntry;

/**
 * Created by Gianni on 02/07/2017.
 */

public class BoardGameDbHelper extends SQLiteOpenHelper {

    /**
     * Log tag for errors
     */
    public static final String LOG_TAG = BoardGameDbHelper.class.getSimpleName();

    /**
     * Name of the database
     */
    public static final String DATABASE_NAME = "boardgames.db";

    /**
     * Database version. If the database schema is changed, the version has to be changed as
     * well.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Constructor for the class
     *
     * @param context of the app
     */
    public BoardGameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called the first time the database is created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a string that contains the SQL statement that creates the table
        String SQL_CREATE_BOARD_GAMES_TABLE = "CREATE TABLE " + BoardGameEntry.TABLE_NAME + " (" +
                            BoardGameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            BoardGameEntry.COLUMN_BOARDGAME_NAME + " TEXT NOT NULL, " +
                            BoardGameEntry.COLUMN_BOARDGAME_YEAR + " INTEGER NOT NULL, " +
                            BoardGameEntry.COLUMN_BOARDGAME_PRICE + " INT NOT NULL DEFAULT 0, " +
                            BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER + " INTEGER NOT NULL, " +
                            BoardGameEntry.COLUMN_BOARDGAME_QUANTITY + " INTEGER NOT NULL DEFAULT" +
                            " 0, " +
                            BoardGameEntry.COLUMN_BOARDGAME_PICTURE + " TEXT, " +
                            BoardGameEntry.COLUMN_BOARDGAME_PLAYERS + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOARD_GAMES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded
     * @param db
     * @param oldVersion the previous version of the database
     * @param newVersion the new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since the database is in version 1, there is nothing here
    }
}
