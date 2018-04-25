package com.melodispel.dpgame.utitlities;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.melodispel.dpgame.R;
import com.melodispel.dpgame.data.DBContract;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Andrea on 2018-04-13.
 */

public final class DataUtils {

    public static final String MATERIAL_TEXT_SEPARATOR = ",";

    public static final int INDEX_SENTENCE_ID = 0;
    public static final int INDEX_ITEM_START = 1;
    public static final int INDEX_CORRECT = 2;
    public static final int INDEX_WRONG = 3;
    public static final int INDEX_ITEM_END = 4;
    public static final int INDEX_LEVEL = 5;



    public static void FillDbWithMaterialsFromTxt(Context context, SQLiteDatabase db) {
        ArrayList<String> material = ReadTextResource(context, R.raw.material);



        for (String line : material) {
            String[] cells = line.split(MATERIAL_TEXT_SEPARATOR);

            ContentValues cv = new ContentValues();
            cv.put(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID, cells[INDEX_SENTENCE_ID]);
            cv.put(DBContract.MaterialsEntry.COLUMN_ITEM_START, cells[INDEX_ITEM_START]);
            cv.put(DBContract.MaterialsEntry.COLUMN_ITEM_END, cells[INDEX_ITEM_END]);
            cv.put(DBContract.MaterialsEntry.COLUMN_CORRECT, cells[INDEX_CORRECT]);
            cv.put(DBContract.MaterialsEntry.COLUMN_WRONG, cells[INDEX_WRONG]);
            cv.put(DBContract.MaterialsEntry.COLUMN_LEVEL, cells[INDEX_LEVEL]);

            db.insert(DBContract.MaterialsEntry.TABLE_NAME, null, cv);
        }


    }

    /**
     * Reads a raw textfile resource.
     * @param context
     * @param textResourceId the resource id of the text file
     * @return an array list with each line of text as an element
     */
    public static ArrayList<String> ReadTextResource(Context context, int textResourceId) {


        ArrayList<String> lines = new ArrayList<String>();
        InputStream inputStream = context.getResources().openRawResource(textResourceId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;

        try {
            while (( line = buffreader.readLine()) != null) {
                lines.add(line);
            }
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Reading database material from text resource, no readable text material found: " + e.getMessage());
        }

        return lines;
    }
}
