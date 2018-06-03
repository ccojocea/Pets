package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by ccojo on 6/2/2018.
 */

public final class PetContract {

    private PetContract() {
    }

    public class PetEntry implements BaseColumns {

        // name of the table
        public static final String TABLE_NAME = "pets";

        // columns including _ID
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        // constants used as values in column gender
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}
