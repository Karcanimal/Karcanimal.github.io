package com.example.project2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * InventoryDBHelper.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * InventoryDBHelper provides methods for managing interactions with the SQLite database for inventory management.
 * It includes functionalities such as adding items, adding columns, retrieving items from the database,
 * and providing dialog boxes to handle these functions.
 *
 * Components:
 * - String DATABASE_NAME: The name of the SQLite database.
 * - int DATABASE_VERSION: The version of the SQLite database schema.
 * - String TABLE_NAME: The name of the table storing inventory items.
 * - String COL_ID: The name of the column storing the unique identifier for each item.
 * - String COL_NAME: The name of the column storing the item name.
 * - String COL_PART_NUMBER: The name of the column storing the part number of the item.
 * - String COL_QUANTITY: The name of the column storing the quantity of the item.
 * - ItemAdapter adapter: Adapter to update RecyclerView when data changes.
 * - static final String TABLE_CREATE: SQL query to create the table for storing inventory items.
 *
 * Functions:
 * - Constructor: Initializes the InventoryDBHelper with the database name, version, and other parameters.
 * - onCreate(SQLiteDatabase db): Called when the database is created for the first time. Creates the item table.
 * - onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion): Called when the database needs to be upgraded.
 *   Drops the existing table and creates a new one.
 * - addNewColumn(String columnName): Adds a new column to the table.
 * - addNewColumnDialog(Context context): Displays a dialog for adding a new column.
 * - showAddItemDialog(Context context, DynamicFieldsAdapter dynamicFieldsAdapter): Displays a dialog for adding a new item.
 * - insertItem(String name, String partNumber, int quantity, Map<String, String> dynamicValues): Inserts a new item into the database.
 * - getAllColumnNames(): Retrieves all column names from the table.
 * - getAllItems(): Retrieves all items from the database, including dynamically added columns.
 * - getAllDynamicColumnNames(): Retrieves all dynamic column names from the table.
 * - columnExists(String columnName): Checks if a column exists in the table.
 *
 * Note:
 * - This class encapsulates database operations related to inventory management.
 * - It provides methods to interact with the database, manage schema changes, and handle user inputs.
 * - The class facilitates adding new items and columns to the database, retrieving items, and checking column existence.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {
    // Database constants
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "items";
    private static final String COL_ID = "_id";
    public static final String COL_NAME = "item_name";
    public static final String COL_PART_NUMBER = "part_number";
    public static final String COL_QUANTITY = "quantity";

    private ItemAdapter adapter; // Adapter to update RecyclerView

    // SQL query to create the table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT, " +
                    COL_PART_NUMBER + " TEXT, " +
                    COL_QUANTITY + " INTEGER);";

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        Log.d("InventoryDBHelper", "Database created successfully");
    }

    // Method called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create the table again
        db.execSQL(TABLE_CREATE);
        Log.d("InventoryDBHelper", "Database upgraded successfully");
    }

    // Method to add a new column to the table
    public void addNewColumn(String columnName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String query = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " ";
            Log.d("InventoryDBHelper", "Executing query: " + query);
            db.execSQL(query);
            Log.d("InventoryDBHelper", "Column " + columnName + " added successfully");

            // Notify the adapter about the change in the dataset
            if (adapter != null) {
                adapter.updateData(getAllItems());
                Log.d("InventoryDBHelper", "Adapter notified about dataset change");
            }
        } catch (Exception e) {
            Log.e("InventoryDBHelper", "Error adding column " + columnName + ": " + e.getMessage());
        } finally {
            // Close the database if it's open
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Method to display a dialog for adding a new column
    public void addNewColumnDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_column, null);
        builder.setView(dialogView);

        EditText columnNameEditText = dialogView.findViewById(R.id.editColumnName);
        EditText columnTypeEditText = dialogView.findViewById(R.id.editColumnType);

        builder.setPositiveButton("Add", (dialog, which) -> {
            // Retrieve column name and type from EditText fields and call addNewColumn method
            String columnName = columnNameEditText.getText().toString().trim();
            addNewColumn(columnName);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to display a dialog for adding a new item
    public void showAddItemDialog(Context context, DynamicFieldsAdapter dynamicFieldsAdapter) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_item, null);
            builder.setView(dialogView);

            EditText editTextName = dialogView.findViewById(R.id.editItemName);
            EditText editTextPartNumber = dialogView.findViewById(R.id.editPartNumber);
            EditText editTextQuantity = dialogView.findViewById(R.id.editQuantity);
            RecyclerView dynamicFieldsRecyclerView = dialogView.findViewById(R.id.recyclerViewDynamicFields);

            // Get column names dynamically every time the dialog is shown
            List<String> columnNames = getAllColumnNames();

            // Set the layout manager for the RecyclerView
            dynamicFieldsRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            // Clear existing dynamic fields adapter and add new ones
            dynamicFieldsAdapter.clear();

            for (String columnName : columnNames) {
                View dynamicFieldView = LayoutInflater.from(context).inflate(R.layout.item_dynamic_field, null);
                EditText editText = dynamicFieldView.findViewById(R.id.editTextDynamicField);
                editText.setHint(columnName); // Set the hint with the column name
                dynamicFieldsAdapter.addEditText(columnName, editText);
            }

            // Set the adapter for dynamic fields RecyclerView
            dynamicFieldsRecyclerView.setAdapter(dynamicFieldsAdapter);

            builder.setPositiveButton("Add", (dialog, which) -> {
                // Handle positive button click
                try {
                    String name = editTextName.getText().toString().trim();
                    String partNumber = editTextPartNumber.getText().toString().trim();
                    int quantity = Integer.parseInt(editTextQuantity.getText().toString().trim());

                    // Create a map to store dynamically added columns and their values
                    Map<String, String> dynamicValues = new HashMap<>();
                    for (String columnName : columnNames) {
                        EditText editText = dynamicFieldsAdapter.getEditText(columnName);
                        String columnValue = editText.getText().toString().trim();
                        dynamicValues.put(columnName, columnValue);
                    }

                    // Call the insertItem method with item details and dynamic values
                    long newRowId = insertItem(name, partNumber, quantity, dynamicValues);

                    if (newRowId != -1) {
                        if (adapter != null) {
                            adapter.updateData(getAllItems());
                            Log.d("InventoryDBHelper", "Adapter notified about dataset change");
                        }
                        Log.d("InventoryDBHelper", "Item added successfully");
                    } else {
                        Log.e("InventoryDBHelper", "Failed to add item to database");
                    }
                } catch (Exception e) {
                    Log.e("InventoryDBHelper", "Error adding item: " + e.getMessage());
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Handle negative button click or cancellation
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("InventoryDBHelper", "Error in showAddItemDialog: " + e.getMessage());
        }
    }

    // Method to insert a new item into the database
    public long insertItem(String name, String partNumber, int quantity, Map<String, String> dynamicValues) {
        SQLiteDatabase db = null;
        long newRowId = -1;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_NAME, name);
            values.put(COL_PART_NUMBER, partNumber);
            values.put(COL_QUANTITY, quantity);

            // Create a new Item object and set its dynamic values
            Item item = new Item(name, partNumber, quantity, dynamicValues);
            item.setDynamicValues(values); // Call the setDynamicValues method

            // Log the ContentValues object before insertion
            Log.d("InventoryDBHelper", "ContentValues for insertion: " + values);

            // Perform the insertion
            newRowId = db.insert(TABLE_NAME, null, values);
            Log.d("InventoryDBHelper", "Inserted row with ID: " + newRowId);
        } catch (Exception e) {
            Log.e("InventoryDBHelper", "Error during insertion: " + e.getMessage());
        } finally {
            // Close the database connection if it's open
            if (db != null) {
                db.close();
            }
        }
        return newRowId;
    }

    // Method to retrieve all column names from the table
    public List<String> getAllColumnNames() {
        List<String> columnNames = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null)) {
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    if (nameIndex != -1) {
                        String columnName = cursor.getString(nameIndex);
                        // Exclude specific columns
                        if (!columnName.equals("_id") && !columnName.equals("item_name")
                                && !columnName.equals("part_number") && !columnName.equals("quantity")) {
                            columnNames.add(columnName);
                        }
                    }
                }
                Log.d("InventoryDBHelper", "Column names retrieved: " + columnNames);
            }
        } catch (Exception e) {
            Log.e("InventoryDBHelper", "Error retrieving column names: " + e.getMessage());
        }
        return columnNames;
    }

    // Method to retrieve all items from the database, including only user-defined columns
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Retrieve dynamically added column names
            List<String> dynamicColumnNames = getAllDynamicColumnNames();

            // Create a projection including only user-defined columns
            String[] projection = new String[dynamicColumnNames.size() + 4];
            projection[0] = COL_NAME;
            projection[1] = COL_PART_NUMBER;
            projection[2] = COL_QUANTITY;
            projection[3] = COL_ID;
            for (int i = 0; i < dynamicColumnNames.size(); i++) {
                projection[i + 4] = dynamicColumnNames.get(i);
            }

            cursor = db.query(TABLE_NAME, projection, null, null, null, null, null);

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(COL_NAME);
                int partNumberIndex = cursor.getColumnIndex(COL_PART_NUMBER);
                int quantityIndex = cursor.getColumnIndex(COL_QUANTITY);

                while (cursor.moveToNext()) {
                    // Check if quantity column index is valid
                    if (quantityIndex >= 0) {
                        String name = cursor.getString(nameIndex);
                        String partNumber = cursor.getString(partNumberIndex);
                        int quantity = cursor.getInt(quantityIndex);

                        // Create a map to store dynamically added columns
                        Map<String, String> dynamicColumns = new HashMap<>();

                        // Retrieve dynamically added column values
                        for (String columnName : dynamicColumnNames) {
                            int columnIndex = cursor.getColumnIndex(columnName);
                            if (columnIndex >= 0) {
                                String columnValue = cursor.getString(columnIndex);
                                dynamicColumns.put(columnName, columnValue);
                                Log.d("getAllItems", "Column: " + columnName + ", Value: " + columnValue);
                            } else {
                                Log.e("getAllItems", "Column index for " + columnName + " is not found");
                            }
                        }

                        // Add the item only if all required columns are found
                        if (name != null && partNumber != null && quantity >= 0) {
                            // Create Item object with static and dynamic values
                            Item item = new Item(name, partNumber, quantity, dynamicColumns);
                            itemList.add(item);
                        } else {
                            Log.e("getAllItems", "One or more required columns are missing or quantity is negative");
                        }
                    } else {
                        Log.e("getAllItems", "Quantity column index not found");
                    }
                }
            } else {
                Log.e("getAllItems", "Cursor is null");
            }
        } catch (Exception e) {
            Log.e("getAllItems", "Error retrieving items: " + e.getMessage());
        } finally {
            // Close the cursor and database connection
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return itemList;
    }

    // Method to retrieve all dynamic column names from the table
    public List<String> getAllDynamicColumnNames() {
        List<String> dynamicColumnNames = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null)) {
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    if (nameIndex != -1) {
                        String columnName = cursor.getString(nameIndex);
                        if (!columnName.equals("_id") && !columnName.equals("item_name")
                                && !columnName.equals("part_number") && !columnName.equals("quantity")) {
                            dynamicColumnNames.add(columnName);
                        }
                    }
                }
                Log.d("InventoryDBHelper", "Dynamic column names retrieved: " + dynamicColumnNames);
            }
        } catch (Exception e) {
            Log.e("InventoryDBHelper", "Error retrieving dynamic column names: " + e.getMessage());
        }
        return dynamicColumnNames;
    }

    // Method to check if a column exists in the table
    public boolean columnExists(String columnName) {
        boolean result = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the sqlite_master table to check if the column exists
            cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameColumnIndex = cursor.getColumnIndex("name");
                    if (nameColumnIndex != -1) {
                        String name = cursor.getString(nameColumnIndex);
                        if (name != null && name.equals(columnName)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("InventoryDBHelper", "Error checking if column exists: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return result;
    }
}
