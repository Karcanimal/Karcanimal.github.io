package com.example.project2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * CsvImporter.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * CsvImporter is a utility class for importing CSV files into a SQLite database.
 *
 * Components:
 * - Context context: The context of the application.
 * - InventoryDBHelper inventoryDBHelper: An instance of InventoryDBHelper for interacting with the SQLite database.
 * - InputStream selectedFileInputStream: Input stream of the selected CSV file.
 *
 * Functions:
 * - Constructor(Context context): Initializes the CsvImporter with the given context.
 * - showToast(String message): Displays a toast message with the given message.
 * - importCsvFile(InputStream inputStream): Initiates the CSV file import process.
 * - CsvImportTask: Asynchronous task to import CSV data into the database.
 * - doInBackground(InputStream... inputStreams): Imports CSV data into the database in the background.
 * - onPostExecute(Boolean success): Displays a toast message based on the success of the CSV import.
 * - showCsvImportDialog(): Displays a dialog to select and import a CSV file.
 *
 * Note:
 * - CsvImporter provides functionality to import CSV files into the SQLite database.
 * - It handles the asynchronous CSV import process using AsyncTask.
 * - The class ensures proper handling of input/output streams and database interactions during the import process.
 */
public class CsvImporter {

    private static final String TAG = "CsvImporter";
    public static final int REQUEST_CODE_SELECT_FILE = 1001;
    private final Context context;
    private final InventoryDBHelper inventoryDBHelper;
    private InputStream selectedFileInputStream;

    public CsvImporter(Context context) {
        this.context = context;
        inventoryDBHelper = new InventoryDBHelper(context);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initiates the CSV file import process.
     * @param inputStream The input stream of the selected CSV file.
     */
    public void importCsvFile(InputStream inputStream) {
        new CsvImportTask().execute(inputStream);
    }

    /**
     * Asynchronous task to import CSV data into the database.
     */
    private class CsvImportTask extends AsyncTask<InputStream, Void, Boolean> {

        @Override
        protected Boolean doInBackground(InputStream... inputStreams) {
            SQLiteDatabase db = null;
            try {
                db = inventoryDBHelper.getWritableDatabase();
                InputStream inputStream = inputStreams[0];
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Read the header to get column names
                String header = reader.readLine();
                String[] columns = header.split(",");

                // Check and add missing columns
                for (String column : columns) {
                    if (!inventoryDBHelper.columnExists(column.trim())) {
                        inventoryDBHelper.addNewColumn(column.trim());
                    }
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");

                    ContentValues contentValues = new ContentValues();
                    for (int i = 0; i < values.length; i++) {
                        contentValues.put(columns[i].trim(), values[i].trim());
                    }

                    long newRowId = db.insert(InventoryDBHelper.TABLE_NAME, null, contentValues);
                    Log.d(TAG, "Inserted row with ID: " + newRowId);
                }

                inputStream.close();
                Log.d(TAG, "CSV file import completed successfully.");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error importing CSV file: " + e.getMessage());
                return false;
            } finally {
                // Close the database after importing the CSV file
                if (db != null) {
                    db.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                showToast("CSV file imported successfully.");
            } else {
                showToast("Failed to import CSV file.");
            }
        }
    }

    /**
     * Displays a dialog to select and import a CSV file.
     */
    public void showCsvImportDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_csv, null);

        Button btnSelectFile = dialogView.findViewById(R.id.btnSelectFile);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Log.d(TAG, "CSV import dialog dismissed.");
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Log.d(TAG, "CSV import dialog dismissed after file selection.");

                if (selectedFileInputStream != null) {
                    importCsvFile(selectedFileInputStream);
                } else {
                    Log.e(TAG, "No CSV file selected.");
                }
            }
        });

        dialog.show();
        Log.d(TAG, "CSV import dialog shown.");
    }
}
