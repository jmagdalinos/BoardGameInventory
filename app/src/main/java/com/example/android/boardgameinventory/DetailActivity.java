package com.example.android.boardgameinventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.boardgameinventory.data.BoardGameContract.BoardGameEntry;

/**
 * Allows user to add a new board game or edit an existing one
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>{

    /** ID for the loader. Can be any number */
    private static final int BOARD_GAME_LOADER_ID = 0;

    /**
     * Content Uri for the board game which was clicked in {@link CatalogActivity}
     * Black if this is a new board game (clicked from FAB)
     */
    Uri mCurrentBoardGameUri;

    /** ImageView field for the board game's image */
    private ImageView mImageView;

    /** EditText field for the board game's name */
    private EditText mNameEditText;

    /** EditText field for the board game's year */
    private EditText mYearEditText;

    /** EditText field for the board game's no. of players */
    private EditText mPlayersEditText;

    /** EditText field for the board game's price */
    private EditText mPriceEditText;

    /** EditText field for the board game's quantity */
    private EditText mQuantityEditText;

    /** EditText field for the board game's quantity change */
    private EditText mQuantityChangeEditText;

    /** Spinner for the board game's supplier */
    private Spinner mSupplierSpinner;

    /** Buttons for increasing and decreasing the quantity respectively */
    private Button mIncreaseQuantityButton;
    private Button mDecreaseQuantityButton;

    /** Button for placing an order */
    Button mPlaceOrderButton;

    /** Request code to identify the intent that returns the image in the onActivityResult method */
    private static final int REQUEST_IMAGE_GET = 1;

    /** Uri of the board game image on the device's storage*/
    private static Uri mImageUri;

    /** Boolean flag stating if it is ok to save (true), that is if all edit texts are not empty */
    private Boolean mReadyToSave = true;

    /** Boolean flag stating if the board game has been edited (true) or not (false) */
    private Boolean mBoardGameHasChanged = false;

    /** Boolean flag stating if the permissions have been granted */
    private static boolean permissionsGranted = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are
     * modifying the view, and we change the mBoardGameHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBoardGameHasChanged = true;
            return false;
        }
    };

    /**
     * Supplier for the board game
     *
     * Possible values:
     * #SUPPLIER_TOTAL_GAMES
     * #SUPPLIER_FANTASY_EXTREME
     * #SUPPLIER_GAMEHOLIC
     * #SUPPLIER_RARED20
     * #SUPPLIER_LEISUREGAMER
     *
     * Default: #SUPPLIER_TOTAL_GAMES
     *
     */
    private int mSupplier = BoardGameEntry.SUPPLIER_TOTAL_GAMES;

    /** Variable holding quantity value */
    private int quantity = 0;

    /** Variable holding the value by which the user wants to change the quantity */
    private int mQuantityChange = 1;

    /** Quantity for placing an order (only used in the custom dialog) */
    private int dialogQuantity = 1;

    /** String used to store the quantity when saving state */
    private final String STATE_QUANTITY = "mChangedQuantity";

    /** String used to store the image Uri when saving state */
    private final String STATE_M_IMAGE_URI = "mImageUri";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the Intent that was used to launch the activity, in order to figure out if
        // we're creating a new board game or editing an existing one.
        Intent intent = getIntent();
        mCurrentBoardGameUri = intent.getData();

        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(CatalogActivity
                .MY_PREFS_NAME, MODE_PRIVATE);
        // Get permission value from preferences
        permissionsGranted = sharedPreferences.getBoolean("permissionsGranted",false);

        // If the intent does not contain a URI, we are creating a new board game
        if (mCurrentBoardGameUri == null) {
            // Change title to "Add a board game"
            setTitle(R.string.title_add);

            // Hide "Delete" from the options menu
            invalidateOptionsMenu();
        } else {
            // Change title to "Edit board game"
            setTitle(R.string.title_edit);

            // Start the loader
            getSupportLoaderManager().initLoader(BOARD_GAME_LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mImageView = (ImageView) findViewById(R.id.detail_image);
        mNameEditText = (EditText) findViewById(R.id.detail_name);
        mYearEditText = (EditText) findViewById(R.id.detail_year);
        mPlayersEditText = (EditText) findViewById(R.id.detail_players);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mQuantityChangeEditText = (EditText) findViewById(R.id.detail_quantity_change);
        mSupplierSpinner = (Spinner) findViewById(R.id.detail_spinner);
        mIncreaseQuantityButton = (Button) findViewById(R.id.detail_button_increase);
        mDecreaseQuantityButton = (Button) findViewById(R.id.detail_button_decrease);
        mPlaceOrderButton = (Button) findViewById(R.id.detail_button_place_order);

        // Setup onTouchListeners
        mImageView.setOnClickListener(addImage());
        mNameEditText.setOnTouchListener(mTouchListener);
        mYearEditText.setOnTouchListener(mTouchListener);
        mPlayersEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mQuantityChangeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Read the user's input
                // Trim is used to eliminate all leading and trailing blank spaces
                String mQuantityChangeString = mQuantityChangeEditText.getText().toString().trim();

                try {
                    // Check if input is integer
                    mQuantityChange = Integer.parseInt(mQuantityChangeString);

                    if (TextUtils.isEmpty(mQuantityChangeString) || mQuantityChange < 1) {
                        Toast.makeText(getApplicationContext(), R.string.detail_invalid_quantity_change, Toast
                                .LENGTH_SHORT).show();
                        mQuantityChange = 0;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), R.string.detail_invalid_quantity_change, Toast
                            .LENGTH_SHORT).show();
                    mQuantityChange = 0;
                }
            }
        });
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        mIncreaseQuantityButton.setOnClickListener(changeQuantityOnClickListener());
        mDecreaseQuantityButton.setOnClickListener(changeQuantityOnClickListener());
        mPlaceOrderButton.setOnClickListener(placeOrderOnClickListener());

        // Setup the spinner
        setupSpinner();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Get current quantity from EditText
        String tempQuantity = mQuantityEditText.getText().toString().trim();
        // Store the variables
        savedInstanceState.putString(STATE_QUANTITY, tempQuantity);
        if (mImageUri != null) {
            savedInstanceState.putString(STATE_M_IMAGE_URI, mImageUri.toString());
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve variable values
        if (savedInstanceState.getString(STATE_QUANTITY) != null && !TextUtils.isEmpty
                (savedInstanceState
                .getString(STATE_QUANTITY))) {
            quantity = Integer.parseInt(savedInstanceState.getString(STATE_QUANTITY));
        }
        if (savedInstanceState.getString(STATE_M_IMAGE_URI) != null) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_M_IMAGE_URI));

            // Check if the image uri is null
            if (mImageUri != null) {
                // Show the image on the imageView using the dimensions in the dimen.xml)
                Bitmap selectedImage = DownScaledImage.prepareBitmap(this, mImageUri, getResources().getDimensionPixelSize(R.dimen.image_size),
                        getResources().getDimensionPixelSize(R.dimen.image_size));
                mImageView.setImageBitmap(selectedImage);
            } else {
                // Show the placeholder instead
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image, null));
            }
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.supplier_names,
                R.layout.custom_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(adapter);

        // Set the integer mSupplier to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_total_games))) {
                        mSupplier = BoardGameEntry.SUPPLIER_TOTAL_GAMES;
                    } else if (selection.equals(getString(R.string.supplier_fantasy_extreme))) {
                        mSupplier = BoardGameEntry.SUPPLIER_FANTASY_EXTREME;
                    } else if (selection.equals(getString(R.string.supplier_gameholic))) {
                        mSupplier = BoardGameEntry.SUPPLIER_GAMEHOLIC;
                    } else if (selection.equals(getString(R.string.supplier_leisure_gamer))) {
                        mSupplier = BoardGameEntry.SUPPLIER_LEISUREGAMER;
                    } else if (selection.equals(getString(R.string.supplier_rared20))) {
                        mSupplier = BoardGameEntry.SUPPLIER_RARED20;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = BoardGameEntry.SUPPLIER_TOTAL_GAMES;
            }
        });
    }

    /**
     * Deletes current board game
     */
    private void deleteBoardGame() {
        // Only delete the board game if this is an existing board game
        if (mCurrentBoardGameUri != null) {
            // Call the ContentResolver to delete the board game at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBoardGameUri
            // content URI already identifies the board gamethat we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBoardGameUri, null, null);

            // Show a toast message advising the user if the board game was deleted successfully
            if (rowsDeleted == 0) {
                // No rows were deleted, therefore there has been an error
                Toast.makeText(this, R.string.detail_delete_error, Toast.LENGTH_SHORT).show();
            } else {
                // The board game was deleted successfully
                Toast.makeText(this, R.string.detail_delete_successful, Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     *  Saves the changes (in edit mode) or saves the the board game (in create mode)
     */
    private void saveBoardGame() {
        // Read the user's input
        // Trim is used to eliminate all leading and trailing blank spaces
        String nameString = mNameEditText.getText().toString().trim();
        String yearString = mYearEditText.getText().toString().trim();
        String playersString = mPlayersEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        // If this is a new board game and if all the fields in the editor are blank, return early
        if (mCurrentBoardGameUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(yearString)
                && TextUtils.isEmpty(playersString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty
                (quantityString)) {
            // No fields have been modified, so there is no need to save the new board game
            return;
        }

        if (!mBoardGameHasChanged && mReadyToSave) {
            // This is an existing board game but there has been no change, so there is no need
            // to save anything
            Toast.makeText(this, R.string.detail_update_not_changed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create a ContentValues object to save the input
        ContentValues values = new ContentValues();

        // Save the supplier in the database
        values.put(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER, mSupplier);

        // Validate user's input.
        // If there is nothing in one or more of the fields, show toast message
        // Image validation
        if (mImageUri != null) {
            values.put(BoardGameEntry.COLUMN_BOARDGAME_PICTURE, String.valueOf(mImageUri));
        } else {
                Toast.makeText(this, R.string.detail_null_image, Toast.LENGTH_SHORT).show();
                mReadyToSave = false;
        }

        if (mCurrentBoardGameUri != null) {
            Toast.makeText(this, R.string.detail_null_image, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        }

        // Name validation
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.detail_null_name, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else {
            values.put(BoardGameEntry.COLUMN_BOARDGAME_NAME, nameString);
        }

        // Year validation
        if (TextUtils.isEmpty(yearString)) {
            Toast.makeText(this, R.string.detail_null_year, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else if (yearString.length() != 4) {
            Toast.makeText(this, R.string.detail_invalid_year, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else {
            int year = Integer.parseInt(yearString);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_YEAR, year);
        }


        // No. of players validation
        if (TextUtils.isEmpty(playersString)) {
            Toast.makeText(this, R.string.detail_null_players, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else {
            values.put(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS, playersString);
        }

        // Price validation
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.detail_null_price, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else {
            priceString = priceString.replace(",", ".");
            double priceDouble = Double.parseDouble(priceString);

            // Convert double to integer by multiplying by 100
            int priceInt = (int) (priceDouble * 100);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_PRICE, priceInt);
        }

        // Quantity validation
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.detail_null_quantity, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else if (quantity < 0) {
            Toast.makeText(this, R.string.detail_invalid_quantity, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        } else {
            int quantity = Integer.parseInt(quantityString);
            values.put(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY, quantity);
        }

        // Determine if this is a new or existing pet by checking if mCurrentBoardGameUri is null or not
        if (mCurrentBoardGameUri == null && mReadyToSave == true) {
            // This is a new board game, so insert the new board game and return the URI
            Uri newUri = getContentResolver().insert(BoardGameEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.detail_insert_error, Toast.LENGTH_SHORT).show();
            } else {
                // The content URI is not null, therefore the insertion was successful.
                Toast.makeText(this, R.string.detail_insert_successful, Toast.LENGTH_SHORT).show();
            }
        } else if (mCurrentBoardGameUri != null && mReadyToSave == true){
            // This is an EXISTING board game, so update it with content URI:mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBoardGame will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentBoardGameUri, values, null,
                    null);

            // Show a toast message advising the user if the board game was deleted successfully
            if (rowsAffected == 0) {
                // No rows were updated, therefore there has been an error
                Toast.makeText(this, R.string.detail_update_error, Toast.LENGTH_SHORT).show();
            } else {
                // The board game was updated successfully
                Toast.makeText(this, R.string.detail_update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Listener placed on the image view
     * Prompts user to select an image
     */
    private View.OnClickListener addImage() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardGameHasChanged = true;
                // Create intent to prompt user for an image

                // Check if the permissions have been granted
                if (!permissionsGranted) {
                    // No permissions, so return early
                    return;
                }
                Intent imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images
                        .Media.EXTERNAL_CONTENT_URI);
                // Make sure that the user selects only images
                imageIntent.setType("image/*");
                if (imageIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(imageIntent, REQUEST_IMAGE_GET);
                }
            }
        };
    }

    /**
     * Listener placed on the increase and decrease quantity buttons
     */
    private  View.OnClickListener changeQuantityOnClickListener() {
         return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBoardGameHasChanged = true;
                    // New content values in order to update the quantity
                    ContentValues values = new ContentValues();

                    // Temporary variable to hold quantity
                    int tempQuantity = Integer.parseInt(mQuantityEditText.getText().toString());

                    // Check which button was pressed
                    if (v.getId() == R.id.detail_button_increase) {
                        // Increase quantity and add it to the content values
                        tempQuantity = tempQuantity + mQuantityChange;
                    } else if (v.getId() == R.id.detail_button_decrease) {
                        // Check if quantity = 0
                        if (tempQuantity == 0) {
                            // Don't decrease quantity, just show 0
                            tempQuantity = 0;
                        } else {
                            // Decrease quantity and add it to the content values
                            tempQuantity = tempQuantity - mQuantityChange;
                        }
                    }
                        mQuantityEditText.setText(String.valueOf(tempQuantity));
                        quantity = tempQuantity;
                }
            };
        }

    /**
     * Listener placed on the "Place Order" button.
     * Checks if the board game is new or not and if the data has been changed and acts
     * appropriately
     */
    private  View.OnClickListener placeOrderOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentBoardGameUri == null && !mBoardGameHasChanged) {
                    // This is a new board game, therefore inform the user that all input fields
                    // should be completed.
                    Toast.makeText(DetailActivity.this, R.string.detail_incomplete_form, Toast
                            .LENGTH_SHORT)
                            .show();
                }

                if ((mCurrentBoardGameUri == null && mBoardGameHasChanged) || (mCurrentBoardGameUri
                        != null && mBoardGameHasChanged)) {
                    // This is a CHANGED board game, therefore ask the user to either save
                    // before placing the order or continue editing
                   showPlaceOrderSaveDialog();
                }

                if (mCurrentBoardGameUri != null && !mBoardGameHasChanged) {
                    // Place the order
                    showSelectOrderQuantityDialog();
                }
            }
        };
    }

    /**
     * Creates an intent to send an email and place an order.
     * The quantity is requested in a custom alert dialog.
     * The email address is found in arrays.xml
     */
    private void showSelectOrderQuantityDialog() {
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(this);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogLayout = inflater.inflate(R.layout.change_quantity_dialog, null);

        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Assign the custom view to the dialog
        builder.setView(dialogLayout);
        // Set the dialog title
        builder.setTitle(R.string.dialog_select_quantity_title);
        // Setup the edit text and buttons
        final EditText dialogEditText = (EditText) dialogLayout.findViewById(R.id
                .dialog_select_quantity);
        final Button dialogIncreaseQuantityButton = (Button) dialogLayout.findViewById(R.id
                .dialog_button_increase);
        final Button dialogDecreaseQuantityButton = (Button) dialogLayout.findViewById(R.id
                .dialog_button_decrease);

        // Setup listeners for the buttons
        dialogIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogQuantity ++;
                dialogEditText.setText(String.valueOf(dialogQuantity));
            }
        });

        dialogDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogQuantity > 1) {
                    dialogQuantity --;
                    dialogEditText.setText(String.valueOf(dialogQuantity));
                }
            }
        });

        // Assign value to edit text
        dialogEditText.setText(String.valueOf(dialogQuantity));

        // Add action buttons
        builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Read user's input
                String dialogQuantityString = dialogEditText.getText().toString().trim();

                // Get addresses array
                String[] address = getResources().getStringArray(R.array.supplier_email);
                // Get current supplier from array
                String supplierAddress = address[mSupplier];
                // Get board game name
                String boardGameName = mNameEditText.getText().toString().trim();
                // Get publication date
                String boardGameDate = mYearEditText.getText().toString().trim();
                // Create the email body
                String emailBody =getString(R.string.email_body_start) +
                        dialogQuantityString + " x " +
                        boardGameName + " (" +
                        boardGameDate + ")" + getString(R.string.email_body_end);

                // Create intent
                Intent email = new Intent(Intent.ACTION_SENDTO);
                // Make sure only email apps can use it
                email.setData(Uri.parse("mailto:"));
                // Add subject
                email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                // Add the email address
                email.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {supplierAddress});
                // Add email body
                email.putExtra(Intent.EXTRA_TEXT, emailBody);

                // Start the activity
                if (email.resolveActivity(getPackageManager()) != null) {
                    startActivity(email);
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_negative, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked ton "Cancel" button, so close the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Set up and show a dialog to ask from the user to save the board game before placing an order
     */
    private void showPlaceOrderSaveDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.place_order_dialog_not_saved));
        builder.setPositiveButton(getString(R.string.place_order_dialog_continue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The user clicked on "Save" so we call the saveBoardGame() method
                        // and afterwards, we place the order.

                        saveBoardGame();
                        showSelectOrderQuantityDialog();
                    }
                });

        builder.setNegativeButton(getString(R.string.place_order_dialog_keep_editing)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The user clicked on "Keep editing" so close the dialog and
                        // continue editing
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Set up and show a dialog to ask from the user for confirmation before permanently
     * deleting the board game
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete_dialog_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Delete" button, so delete the board game
                deleteBoardGame();
            }
        });
        builder.setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Cancel" button, so close the dialog and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Set up and show a dialog to ask from the user for confirmation before leaving the detail
     * activity without saving any changes.
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonOnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_dialog_message);
        builder.setPositiveButton(R.string.unsaved_dialog_discard, discardButtonOnClickListener);
        builder.setNegativeButton(R.string.unsaved_dialog_keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Keep editing" button, so dismiss the dialog and return to the
                // activity
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                BoardGameEntry.COLUMN_BOARDGAME_PRICE,
                BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER,
                BoardGameEntry.COLUMN_BOARDGAME_QUANTITY,
                BoardGameEntry.COLUMN_BOARDGAME_PICTURE,
                BoardGameEntry.COLUMN_BOARDGAME_PLAYERS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentBoardGameUri,
                projection,
                null,
                null,
                null);
    }

    /**
     * When the loader finishes, return cursor object with the data requested and fill the
     * appropriate views with the data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If the cursor is null or there are less than 1 rows, return early
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        cursor.moveToFirst();

        // Find the columns of board game attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_NAME);
        int yearColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_YEAR);
        int priceColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_PRICE);
        int supplierColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_SUPPLIER);
        int quantityColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_QUANTITY);
        int pictureColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_PICTURE);
        int playersColumnIndex = cursor.getColumnIndex(BoardGameEntry.COLUMN_BOARDGAME_PLAYERS);

        // Read the attributes from the cursor for the current board game
        String name = cursor.getString(nameColumnIndex);
        int year = cursor.getInt(yearColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        int supplier = cursor.getInt(supplierColumnIndex);
        quantity = cursor.getInt(quantityColumnIndex);
        String pictureString = cursor.getString(pictureColumnIndex);
        String players = cursor.getString(playersColumnIndex);

        Log.v("DetailActivity", "Picture: " + pictureString);

        // Convert price to double
        double decimalPrice = (double) price/100.00;

        // Convert price to String
        String finalPrice = String.format("%.2f", decimalPrice);

        // Convert the picture to uri
        if (pictureString != null) {
            mImageUri = Uri.parse(pictureString);
        } else {
            mImageUri = null;

        }

        // Check if the image uri is null
        if (mImageUri != null) {
            // Show the image on the imageView using the dimensions in the dimen.xml)
            Bitmap selectedImage = DownScaledImage.prepareBitmap(this, mImageUri, getResources().getDimensionPixelSize(R.dimen.image_size),
                    getResources().getDimensionPixelSize(R.dimen.image_size));
            mImageView.setImageBitmap(selectedImage);
        } else {
            // Use the placeholder image instead
            mImageView.setImageDrawable(getDrawable(R.drawable.no_image));
            Toast.makeText(this, R.string.detail_null_image, Toast.LENGTH_SHORT).show();
            mReadyToSave = false;
        }

        // Update the views on the screen with the values from the database
        mNameEditText.setText(name);
        mYearEditText.setText(String.valueOf(year));
        mPriceEditText.setText(String.valueOf(finalPrice));
        mQuantityEditText.setText(String.valueOf(quantity));
        mPlayersEditText.setText(players);

        switch (supplier) {
            case BoardGameEntry.SUPPLIER_TOTAL_GAMES:
                mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_TOTAL_GAMES);
                break;
            case BoardGameEntry.SUPPLIER_FANTASY_EXTREME:
                mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_FANTASY_EXTREME);
                break;
            case BoardGameEntry.SUPPLIER_GAMEHOLIC:
                mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_GAMEHOLIC);
                break;
            case BoardGameEntry.SUPPLIER_RARED20:
                mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_RARED20);
                break;
            case BoardGameEntry.SUPPLIER_LEISUREGAMER:
                mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_LEISUREGAMER);
                break;
        }
    }

    /**
     * When the loader resets, clear the input fields
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mYearEditText.setText(String.valueOf(""));
        mPriceEditText.setText(String.valueOf(""));
        mQuantityEditText.setText(String.valueOf(""));
        mPlayersEditText.setText("");

        // Set spinner to default (0)
        mSupplierSpinner.setSelection(BoardGameEntry.SUPPLIER_TOTAL_GAMES);
    }

    /**
     * Gets called after the user has selected an image by clicking on the imageView.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the image has been selected, get the image uri
        if (resultCode == RESULT_OK) {
            mImageUri = data.getData();

            // Check if the image uri is null
            if (mImageUri != null) {
                // Show the image on the imageView using the dimensions in the dimen.xml)
                Bitmap selectedImage = DownScaledImage.prepareBitmap(this, mImageUri, getResources().getDimensionPixelSize(R.dimen.image_size),
                        getResources().getDimensionPixelSize(R.dimen.image_size));
                mImageView.setImageBitmap(selectedImage);
            } else {
                // Show the placeholder instead
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image, null));
            }
        }
    }

    /**
     * Creates the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * Sets the options in the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save_board_game:
                // Save the board game in the database
                saveBoardGame();
                if (mReadyToSave == true) {
                    // Exit the activity
                    finish();
                } else {
                    mReadyToSave = true;
                }
                break;
            case R.id.action_delete_board_game:
                // Show the delete confirmation dialog and delete if necessary
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the board game hasn't changed, continue navigating up to parent activity
                if (!mBoardGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // If there are unsaved changes, show dialog box informing the user
                // Create a click listener to handle the user confirming that changes should be
                // discarded.
                DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked on "Discard" button, so navigate to parent activity
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonOnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This is called if the invalidateOptionsMenu() method was called (if this is a new board
     * game)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If this is a new board game, hide the "Delete" option.
        if (mCurrentBoardGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_board_game);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Handle what happens when the user clicks on the "Back" button
     */
    @Override
    public void onBackPressed() {
        // If the board game hasn't changed, continue navigating up to
        // parent activity
        if (!mBoardGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // If there are unsaved changes, show dialog box informing the user
        // Create a click listener to handle the user confirming that changes should
        // be discarded.
        DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on "Discard" button, so close the activity
                finish();
            }
        };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonOnClickListener);
    }
}
