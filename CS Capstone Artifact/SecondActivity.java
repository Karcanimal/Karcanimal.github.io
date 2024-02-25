package com.example.project2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.ImageButton;

/*
 * SecondActivity.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * This file contains the implementation of the SecondActivity class, which represents the second screen of the application.
 * SecondActivity facilitates the management of inventory items, sending SMS notifications, and handling navigation actions.
 * It integrates with various components such as RecyclerView for displaying items, ActionBarDrawerToggle for navigation drawer control,
 * and SmsManager for sending SMS notifications. Additionally, it interacts with the InventoryDBHelper for database operations,
 * DynamicFieldsAdapter for managing dynamic EditText fields, and CsvImporter for importing CSV files.
 *
 * Components:
 * - RecyclerView: Displays the list of inventory items.
 * - ActionBarDrawerToggle: Manages the navigation drawer's open/close state and synchronization.
 * - NavigationView: Provides navigation options for users.
 * - ImageButton (btnOpenDrawer, btnFilter): Allows users to interact with the navigation drawer and apply filters.
 * - InventoryDBHelper: Handles database operations such as retrieving items, adding new items, and adding columns.
 * - CsvImporter: Manages the import of CSV files into the application.
 *
 * Functionality:
 * - onCreate(): Initializes views, components, and listeners. Retrieves items from the database, logs their details, and sets up the RecyclerView.
 *   Additionally, it dynamically adds EditText fields based on column names retrieved from the database.
 * - onCreateOptionsMenu(): Inflates the menu layout for the navigation drawer.
 * - onPostCreate(): Syncs the toggle state after the activity's state restoration.
 * - sendSms(): Sends an SMS notification to a predefined phone number indicating low stock of inventory items.
 * - handleNavigationItemClick(): Handles clicks on navigation items by executing corresponding actions such as adding items, sending SMS, etc.
 * - onActivityResult(): Handles the result of file selection for CSV import.
 *
 * Note:
 * - The class relies on various resource files such as layout XML (main_screen), menu XML (drawer_menu), and string resources.
 * - It interacts with permissions for sending SMS and accessing files.
 * - Error handling is implemented for SMS sending failures and CSV file import errors.
 * - The class follows a structured approach to manage inventory-related tasks and navigation actions efficiently.
 */

public class SecondActivity extends AppCompatActivity {
    // Variables declaration
    private InventoryDBHelper inventoryDBHelper;
    private DynamicFieldsAdapter dynamicFieldsAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CsvImporter csvImporter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        // Initialize views and components
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        RecyclerView recyclerViewItems = findViewById(R.id.recyclerViewItems);
        ImageButton btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        ImageButton btnFilter = findViewById(R.id.btnFilter);

        // Initialize the InventoryDBHelper
        inventoryDBHelper = new InventoryDBHelper(this);

        // Initialize the CsvImporter
        csvImporter = new CsvImporter(this);

        // Retrieve all items from the database
        List<Item> allItems = inventoryDBHelper.getAllItems();

        // Log the sizes of allItems
        Log.d("SecondActivity", "Total item list size: " + allItems.size());

        // Log the entire database
        for (Item item : allItems) {
            Log.d("SecondActivity", "Item: " + item.getName() + ", Part Number: " + item.getPartNumber() + ", Quantity: " + item.getQuantity() + ", Dynamic Values: " + item.getDynamicValues());
            // Log dynamic values
            Map<String, String> dynamicValues = item.getDynamicValues();
            for (Map.Entry<String, String> entry : dynamicValues.entrySet()) {
                String columnName = entry.getKey();
                String columnValue = entry.getValue();
                Log.d("SecondActivity", columnName + ": " + columnValue);
            }
        }

        // Create and set up the adapter
        ItemAdapter itemAdapter = new ItemAdapter(allItems, this);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);

        // Set up other components and listeners
        dynamicFieldsAdapter = new DynamicFieldsAdapter();

        // Retrieve column names from the database
        List<String> columnNames = inventoryDBHelper.getAllColumnNames();

        // Add EditText fields dynamically with the retrieved column names
        for (String columnName : columnNames) {
            EditText editText = new EditText(this);
            dynamicFieldsAdapter.addEditText(columnName, editText);
        }

        // Set up the ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Set navigation item click listener
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation item clicks
            handleNavigationItemClick(item);
            return true;
        });

        // Set click listener for opening the drawer
        btnOpenDrawer.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        btnFilter.setOnClickListener(v -> {
            // Get user-defined filter criteria
            Map<String, String> filterMap = new HashMap<>();
            // Populate filterMap with user-defined criteria (e.g., from EditText fields)

            // Call filter method on adapter
            itemAdapter.filterList(filterMap);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    // Method to send an SMS
    private void sendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        String phoneNumber = "5555215556"; // Replace with the recipient's phone number
        String message = "Your inventory item is running low on stock.";

        try {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            // SMS sent successfully
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            Log.d("SecondActivity", "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            // Handle exceptions, such as permission issues or invalid phone number
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            Log.e("SecondActivity", "Failed to send SMS to " + phoneNumber, e);
        }
    }

    // Method to handle navigation item clicks
    private void handleNavigationItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            inventoryDBHelper.showAddItemDialog(this, dynamicFieldsAdapter);
        } else if (id == R.id.menu_add_column) {
            inventoryDBHelper.addNewColumnDialog(this);
        } else if (id == R.id.menu_sms) {
            sendSms();
        } else if (id == R.id.menu_add_csv) {
            csvImporter.showCsvImportDialog();
        }
        else if (id == R.id.menu_export_csv) {
            CsvExporter.exportCsvFile(this);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CsvImporter.REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            // Handle file selection result
            Uri selectedFileUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
                csvImporter.importCsvFile(inputStream); // Start CSV import process
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SecondActivity", "Error importing CSV file", e);
            }
        }
    }
}
