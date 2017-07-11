package com.example.android.boardgameinventory;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.boardgameinventory.data.BoardGameContract.BoardGameEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>{

    /** ID for the loader. Can be any number */
    private static final int BOARD_GAME_LOADER_ID = 0;

    /**
     * Request code to identify the intent that requests permissions for reading/writing on
     * external storage
     * */
    private static final int MY_PERMISSIONS_REQUEST = 1;

    /** String with sort by preference */
    private static String sortOrder = BoardGameEntry.COLUMN_BOARDGAME_NAME + " DESC";

    /** String with search "where" preference */
    private static String selection = null;

    /** String with search "like" preference */
    private static String[] selectionArgs = null;

    /** Adapter that will show list of items on the list view */
    BoardGameAdapter mBoardGameAdapter;

    /** Name of shared preferences */
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the board game data
        ListView listView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of board game data in the Cursor.
        // There is no board game data yet (until the Loader finishes) so pass in null for the
        // Cursor.
        mBoardGameAdapter = new BoardGameAdapter(this, null);
        listView.setAdapter(mBoardGameAdapter);

        // Setup item Click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new Intent to go to {@link DetailActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI which represents the specific board game that was clicked
                // on, by appending the "id" (passed as input to this method)
                Uri currentBoardGameUri = ContentUris.withAppendedId(BoardGameEntry.CONTENT_URI,
                        id);

                // Set the URI on the data field of the intent
                intent.setData(currentBoardGameUri);

                // Launch the activity
                startActivity(intent);
            }
        });

        requestPermissions();

        // Start the loader
        getSupportLoaderManager().initLoader(BOARD_GAME_LOADER_ID, null, this);
    }

    /**
     * Requests permissions to read/write on external storage
     */
    private void requestPermissions() {
        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission
                            .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    /**
     * Helper method that inserts 10 dummy board games to test the app
     */
    private void insertDummyBoardGames() {
        // Initialize arrays which will get their values from arrays.xml
        String[] names = getResources().getStringArray(R.array.dummy_names);
        int[] years = getResources().getIntArray(R.array.dummy_years);
        int[] prices = getResources().getIntArray(R.array.dummy_prices);
        int[] suppliers = getResources().getIntArray(R.array.dummy_suppliers);
        int[] quantities = getResources().getIntArray(R.array.dummy_quantities);
        String[] players = getResources().getStringArray(R.array.dummy_players);

        // Use for loop to enter values from array
        for (int i = 0; i < 10; i ++) {
            // Create ContentValues object where the column names are the keys
            ContentValues values = new ContentValues();
            values.put(BoardGameEntry.COLUMN_BOARDGAME_NAME, names[i]);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_YEAR, years[i]);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_PRICE, prices[i]);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER, suppliers[i]);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY, quantities[i]);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS, players[i]);

            Uri newUri = getContentResolver().insert(BoardGameEntry.CONTENT_URI, values);
            // Insert a new row for the habit in the database, returning the ID of that new row.
        }
    }

    /**
     * Helper method to delete all board games in the database
     */
    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(BoardGameEntry.CONTENT_URI, null, null);
    }

    /**
     * Creates an alert dialog so the user can sort the data
     */
    private void showSortByDialog() {
        // Set sortOrder to default to avoid double values
        sortOrder = BoardGameEntry.COLUMN_BOARDGAME_NAME;
        // Create an AlertDialog.Builder and set the title
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_by_list_label);
        // Set the list of sort-by options
        builder.setSingleChoiceItems(R.array.settings_sort_by_labels, 0, new DialogInterface
                .OnClickListener
                () {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                    case 0:
                        sortOrder = BoardGameEntry.COLUMN_BOARDGAME_NAME;
                        break;
                    case 1:
                        sortOrder = BoardGameEntry.COLUMN_BOARDGAME_PRICE;
                        break;
                    case 2:
                        sortOrder = BoardGameEntry.COLUMN_BOARDGAME_YEAR;
                        break;
                }

            }
        });

        // Set the positive button (Ascending order)
        builder.setPositiveButton(R.string.sort_by_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set "ASC" in the preference
                sortOrder = sortOrder + " ASC";
                // Restart the loader
                getSupportLoaderManager().restartLoader(BOARD_GAME_LOADER_ID, null,
                        CatalogActivity.this);
            }
        });
        // Set the negative button (Descending order)
        builder.setNegativeButton(R.string.sort_by_negative_button, new DialogInterface.OnClickListener
                () {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set "DESC" in the preference
                sortOrder = sortOrder + " DESC";
                // Restart the loader
                getSupportLoaderManager().restartLoader(BOARD_GAME_LOADER_ID, null,
                        CatalogActivity.this);
            }
        });
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Creates a search dialog so the user can find a substring. It also has the option to clear
     * the search and return to the full list.
     */
    private void showSearchDialog() {
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(this);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogLayout = inflater.inflate(R.layout.search_dialog, null);

        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Assign the custom view to the dialog
        builder.setView(dialogLayout);
        // Set the dialog title
        builder.setTitle(R.string.search_label);
        // Setup the edit text
        final EditText searchEditText = (EditText) dialogLayout.findViewById(R.id.dialog_search);
        // Set the positive and negative buttons
        builder.setPositiveButton(R.string.search_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Search" button, so get the value from the edit text and
                // assign it to selectionArgs
                selectionArgs = new String[1];
                selection = BoardGameEntry.COLUMN_BOARDGAME_NAME + " LIKE ?";
                selectionArgs[0] = "%" + searchEditText.getText().toString().trim() + "%";

                // Restart the loader
                getSupportLoaderManager().restartLoader(BOARD_GAME_LOADER_ID, null,
                        CatalogActivity.this);
            }
        });
        builder.setNegativeButton(R.string.search_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Cancel" button, so close the dialog
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.search_neutral_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Clear Search" button, so set selection & selectionArgs to null
                selection = null;
                selectionArgs = null;

                // Restart the loader
                getSupportLoaderManager().restartLoader(BOARD_GAME_LOADER_ID, null,
                        CatalogActivity.this);
            }
        });

        // Start and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /**
     * Results of permissions request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME,
                MODE_PRIVATE).edit();
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions were granted, save value to shared preferences
                    editor.putBoolean("permissionsGranted", true);
                    editor.apply();

                } else {
                    // Permissions were denied, save value to shared preferences
                    editor.putBoolean("permissionsGranted", false);
                    editor.apply();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Create loader with a projection of the columns we need for the list view.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BoardGameEntry._ID,
                BoardGameEntry.COLUMN_BOARDGAME_NAME,
                BoardGameEntry.COLUMN_BOARDGAME_YEAR,
                BoardGameEntry.COLUMN_BOARDGAME_QUANTITY,
                BoardGameEntry.COLUMN_BOARDGAME_PRICE,
                BoardGameEntry.COLUMN_BOARDGAME_PICTURE,};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                BoardGameEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    /**
     * When the loader finishes, return cursor object with the data requested
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBoardGameAdapter.swapCursor(data);
    }

    /**
     * When the loader resets, swap the cursor for a null one
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBoardGameAdapter.swapCursor(null);
    }

    /**
     * Creates the overflow menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * Sets the options in the overflow menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Search" option
            case R.id.action_find_data:
                showSearchDialog();
                return true;
            // Respond to a click on the "Sort data" option
            case R.id.action_sort_data:
                showSortByDialog();
                return true;
            // Respond to a click on the "Insert dummy data" option
            case R.id.action_insert_dummy_data:
                insertDummyBoardGames();
                return true;
            // Respond to a click on the "Delete all data" option
            case R.id.action_delete_all_data:
                deleteAllData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
