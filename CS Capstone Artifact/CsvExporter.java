package com.example.project2;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * CsvExporter.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * CsvExporter is a utility class for exporting data from a SQLite database to a CSV file.
 *
 * Components:
 * - Constants:
 *   - TAG: Tag for logging purposes.
 * - exportCsvFile(Context context): Initiates the process of exporting data to a CSV file.
 * - CsvExportTask: Asynchronous task to export data to a CSV file.
 *   - doInBackground(Void... voids): Performs the CSV export operation in the background.
 *   - onPostExecute(Boolean success): Displays a toast message based on the success of the CSV export.
 * - showToast(Context context, String message): Helper method to display a toast message.
 * - showFileNameDialog(Context context): Displays a dialog to set the file name and destination for CSV export.
 *
 * Note:
 * - CsvExporter provides functionality to export data from a SQLite database to a CSV file.
 * - It uses AsyncTask to perform the export operation asynchronously.
 * - The class ensures proper handling of file I/O operations and database interactions during the export process.
 */
public class CsvExporter {

    private static final String TAG = "CsvExporter";

    /**
     * Initiates the process of exporting data to a CSV file.
     * @param context The context of the calling activity or fragment.
     */
    public static void exportCsvFile(Context context) {
        showFileNameDialog(context);
    }

    /**
     * Asynchronous task to export data to a CSV file.
     */
    private static class CsvExportTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final String fileName;
        private final String destination;

        public CsvExportTask(Context context, String fileName, String destination) {
            this.context = context;
            this.fileName = fileName;
            this.destination = destination;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SQLiteDatabase db = null;
            FileWriter fileWriter = null;
            try {
                // Open the database
                InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(context);
                db = inventoryDBHelper.getReadableDatabase();

                // Define the file path
                File csvFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName + ".csv");

                // Create a new FileWriter
                fileWriter = new FileWriter(csvFile);

                // Write the CSV header
                fileWriter.append("Name,Part Number,Quantity\n");

                // Query the database to retrieve all items
                Cursor cursor = db.query(InventoryDBHelper.TABLE_NAME, null, null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // Extract data from cursor if column indexes are valid
                        int nameIndex = cursor.getColumnIndex(InventoryDBHelper.COL_NAME);
                        int partNumberIndex = cursor.getColumnIndex(InventoryDBHelper.COL_PART_NUMBER);
                        int quantityIndex = cursor.getColumnIndex(InventoryDBHelper.COL_QUANTITY);

                        if (nameIndex != -1 && partNumberIndex != -1 && quantityIndex != -1) {
                            String name = cursor.getString(nameIndex);
                            String partNumber = cursor.getString(partNumberIndex);
                            int quantity = cursor.getInt(quantityIndex);

                            // Write item data to CSV
                            fileWriter.append(name).append(",").append(partNumber).append(",").append(String.valueOf(quantity)).append("\n");
                        } else {
                            Log.e(TAG, "One or more column indexes are invalid");
                        }
                    } while (cursor.moveToNext());

                    // Close cursor
                    cursor.close();
                }

                // Close the FileWriter
                fileWriter.flush();
                fileWriter.close();

                Log.d(TAG, "CSV file exported successfully.");
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error exporting CSV file: " + e.getMessage());
                return false;
            } finally {
                // Close the database if open
                if (db != null) {
                    db.close();
                }
                // Close the FileWriter if open
                try {
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error closing FileWriter: " + e.getMessage());
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                showToast(context, "CSV file exported successfully.");
            } else {
                showToast(context, "Failed to export CSV file.");
            }
        }
    }

    /**
     * Helper method to display a toast message.
     * @param context The context of the calling activity or fragment.
     * @param message The message to be displayed in the toast.
     */
    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a dialog to set the file name and destination for CSV export.
     * @param context The context of the calling activity or fragment.
     */
    private static void showFileNameDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_filename, null);
        EditText editFileName = dialogView.findViewById(R.id.editFileName);
        Spinner destinationSpinner = dialogView.findViewById(R.id.destinationSpinner); // Assuming you have a Spinner in your layout
        // Set up the Spinner with destination options, e.g., internal storage, external storage, etc.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.destination_options, android.R.layout.simple_spinner_item); // Define the array of options in strings.xml
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setTitle("Set File Name and Destination");
        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = editFileName.getText().toString().trim();
                String selectedDestination = destinationSpinner.getSelectedItem().toString();
                if (!fileName.isEmpty()) {
                    // Start exporting with the provided file name and destination
                    new CsvExportTask(context, fileName, selectedDestination).execute();
                } else {
                    showToast(context, "Please enter a valid file name.");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}