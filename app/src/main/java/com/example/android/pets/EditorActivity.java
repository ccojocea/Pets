/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = EditorActivity.class.getName();

    private static final int PET_LOADER = 0;
    private Uri mPetUri;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.
    private boolean mPetHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;

            // If you return true you tell android that the press is taken care of. Forget it.
            //
            // If you return false you basically say "Not my problem, Someone else will have to take care of this click".
            // Then android will pass the event down to other views, which could be under your view.
            return false;
        }
    };

    private boolean inEditMode = false;

    // These are the Pets rows that we will retrieve
    static final String[] PROJECTION = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
    };

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        // Get intent and uri from data attached to the intent
        mPetUri = getIntent().getData();
        if (mPetUri != null) {
            inEditMode = true;
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            // Prepare the loader. Either re-connect with an existing one, or start a new one.
            getLoaderManager().initLoader(PET_LOADER, null, this);
        } else {
            inEditMode = false;

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();

            setTitle(getString(R.string.editor_activity_title_new_pet));
        }

        //mDbHelper = new PetDbHelper(this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     * Get user input from the editor and save new pet into database
     */
    private void savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        values.put(PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        String weight = mWeightEditText.getText().toString().trim();
        if (!weight.equals("")) {
            values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
        }
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);

        //SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //long result = db.insert(PetEntry.TABLE_NAME, null, values);

        // Insert a new pet into the provider, returning the content URI for the new pet.
        // Or Update the current pet!
        // should check if (mPetUri == null) instead of boolean
        if (inEditMode) {
            String mSelectionClause = PetEntry._ID + " =?";
            String[] mSelectionArgs = { String.valueOf(ContentUris.parseId(mPetUri)) };

            //int mRowsUpdated = getContentResolver().update(PetEntry.CONTENT_URI, values, mSelectionClause, mSelectionArgs);
            int mRowsUpdated = getContentResolver().update(mPetUri, values, null, null);
            if (mRowsUpdated != 0) {
                // The update was successful and we can display a toast.
                Toast.makeText(this, "Updated pet with ID: " + ContentUris.parseId(mPetUri), Toast.LENGTH_LONG).show();
            } else {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Error updating pet!", Toast.LENGTH_SHORT).show();
            }
            //TODO: show toast message if the update was successful, also add a value to where and selectionArgs
        } else {
            Uri resultUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (resultUri != null) {
                // The insertion was successful and we can display a toast.
                Toast.makeText(this, "You have inserted pet with ID: " + resultUri.getLastPathSegment() + "!", Toast.LENGTH_SHORT).show();
            } else {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error saving pet!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Perform the deletion of the pet in the database
    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int mRowsDeleted = getContentResolver().delete(mPetUri, null, null);
            if (mRowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_delete_pet_successful + " ID: " + String.valueOf(ContentUris.parseId(mPetUri)), Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to the database
                try {
                    savePet();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return false;
                }

                // Close the activity
                finish();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
//            case android.R.id.home:
//                // Navigate back to parent activity (CatalogActivity)
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // Hide or show the delete option in editor activity as needed (called after invalidateOptionsMenu())
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "Delete" menu item.
        if (mPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        return new CursorLoader(this, mPetUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME));
            mNameEditText.setText(name);

            String breed = data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
            mBreedEditText.setText(breed);

            int gender = data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
            mGenderSpinner.setSelection(gender);

            int weight = data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT));
            mWeightEditText.setText(String.valueOf(weight));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
        mWeightEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            // TODO: This was added outside of course material - to test with and without
            NavUtils.navigateUpFromSameTask(this);
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard" button, close the current activity
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Delete" button, so delete the pet
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "cancel" button, so dismiss the dialog and continue editing the pet
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}