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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = CatalogActivity.class.getName();

    private ListView petListView;
    private View emptyView;

    //private PetDbHelper mDbHelper;

    // This is the Adapter being used to display the list's data
    PetCursorAdapter mCursorAdapter;

    private static final int PET_LOADER = 0;

    // These are the Pets rows that we will retrieve
    static final String[] PROJECTION = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the ListView which will be populated with the pet data
        petListView = findViewById(R.id.list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);
        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // For the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                Uri uri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                // Create the new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //intent.putExtra("uri", uri.toString());
                // Set the URI on the data field of the intent
                intent.setData(uri);

                // Launch the {@link EditorActivity}
                startActivity(intent);
            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // For the cursor adapter, specify which columns go into which views
        //String[] fromColumns = {PetEntry.COLUMN_PET_NAME, PetEntry.COLUMN_PET_BREED};
        //int[] toViews = {R.id.name, R.id.summary}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        //mCursorAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null, fromColumns, toViews, 0);
        mCursorAdapter = new PetCursorAdapter(this, null);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(PET_LOADER, null, this);

        // Attach the cursor adapter to the listview
        petListView.setAdapter(mCursorAdapter);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        // mDbHelper = new PetDbHelper(this);

        //displayDatabaseInfo();

        //PetDbHelper petDbHelper = new PetDbHelper(this);
        //SQLiteDatabase db = petDbHelper.getReadableDatabase();
        // to create, update, delete:
        //db = petDbHelper.getWritableDatabase();
    }

    // When the activity starts again (comes back from the EditorActivity)
    @Override
    protected void onStart() {
        super.onStart();
        //displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    public void insertData(){
        // Gets the data repository in write mode
        // SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        //long result = db.insert(PetEntry.TABLE_NAME, null, values);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri resultUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        Log.d(TAG, "insertData: RESULT: " + resultUri);
    }

    public void deleteData() {
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");

        //long result = db.delete(PetEntry.TABLE_NAME, null, null);
        //Log.d(TAG, "deleteData: RESULT: " + result);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
                //displayDatabaseInfo();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteData();
                //displayDatabaseInfo();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    /*
    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        // SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        // Cursor cursor = db.rawQuery("SELECT * FROM " + PetEntry.TABLE_NAME, null);
/**
        public android.database.Cursor query(
            String table,
            String[] columns,
            String selection,
            String[] selectionArgs,
            String groupBy,
            String having,
            String orderBy);

 table	        String: The table name to compile the query against.
 columns	    String: A list of which columns to return. Passing null will return all columns, which is discouraged to prevent reading data from storage that isn't going to be used.
 selection	    String: A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
 selectionArgs	String: You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
 groupBy	    String: A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself). Passing null will cause the rows to not be grouped.
 having	        String: A filter declare which row groups to include in the cursor, if row grouping is being used, formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be included, and is required when row grouping is not being used.
 orderBy	    String: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.

        // array of columns to be returned
        String[] projection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
        };

        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);


        //Cursor cursor = db.query(PetEntry.TABLE_NAME, projection, null, null, null, null, null);


        try {
            PetCursorAdapter cursorAdapter = new PetCursorAdapter(this, cursor);
            petListView.setAdapter(cursorAdapter);
        } finally {
            // close the cursor to release resources
            //cursor.close();
        }

        /*
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount());

            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            if (cursor.getCount() > 0) {
                displayView.append("\n\n");
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String breed = cursor.getString(breedColumnIndex);
                int gender = cursor.getInt(genderColumnIndex);
                int weight = cursor.getInt(weightColumn);

                displayView.append("\n" + "ID: " + id + " Name: " + name + " Breed: " + breed + " GENDER: " + gender + " WEIGHT: " + weight);
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

    }
    */

    // Called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        return new CursorLoader(this, PetEntry.CONTENT_URI, PROJECTION, null, null, null);
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
