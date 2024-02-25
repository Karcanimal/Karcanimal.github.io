package com.example.project2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.telephony.SmsManager;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;



public class SecondActivity extends AppCompatActivity {
    //Button and Inventory intializations
    private Button btnPlus;
    private Button btnSendSMS;
    private InventoryDBHelper inventoryHelper;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        // Initialize the inventory helper
        inventoryHelper = new InventoryDBHelper(this);

        // Button Listening
        btnPlus = findViewById(R.id.btnPlus);
        btnSendSMS = findViewById(R.id.btnSendSMS);

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
                builder.setView(dialogView);

                EditText editTextName = dialogView.findViewById(R.id.editItemName);
                EditText editTextPartNumber = dialogView.findViewById(R.id.editPartNumber);
                EditText editTextQuantity = dialogView.findViewById(R.id.editQuantity);

                builder.setPositiveButton("Add", (dialog, which) -> {
                    String name = editTextName.getText().toString();
                    String partNumber = editTextPartNumber.getText().toString();
                    int quantity = Integer.parseInt(editTextQuantity.getText().toString());
                    inventoryHelper.insertItem(name, partNumber, quantity);
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle cancellation
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if SMS permission is granted
                if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request SMS permission
                    ActivityCompat.requestPermissions(SecondActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    // Permission already granted, send the SMS
                    sendSms();
                }
            }
        });

        // Set the adapter for the GridView
        GridView gridView = findViewById(R.id.gridView);
        List<Item> itemList = inventoryHelper.getAllItems();
        ItemAdapter adapter = new ItemAdapter(this, itemList);
        gridView.setAdapter(adapter);
    }

    public class InventoryDBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "inventory.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "items";
        private static final String COL_ID = "_id";
        private static final String COL_NAME = "item_name";
        private static final String COL_PART_NUMBER = "part_number";
        private static final String COL_QUANTITY = "quantity";

        private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT, " +
                        COL_PART_NUMBER + " TEXT, " +
                        COL_QUANTITY + " INTEGER);";

        public InventoryDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        //Inserting Items method
        public void insertItem(String itemName, String partNumber, int itemQuantity) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_NAME, itemName);
            values.put(COL_PART_NUMBER, partNumber);
            values.put(COL_QUANTITY, itemQuantity);

            long newRowId = db.insert(TABLE_NAME, null, values);
            db.close();
        }
        //Displaying Items in Gridview
        public List<Item> getAllItems() {
            List<Item> itemList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String[] columns = {COL_NAME, COL_PART_NUMBER, COL_QUANTITY};
            Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
            int nameColumnIndex = cursor.getColumnIndex(COL_NAME);
            int partNumberColumnIndex = cursor.getColumnIndex(COL_PART_NUMBER);
            int quantityColumnIndex = cursor.getColumnIndex(COL_QUANTITY);

            while (cursor.moveToNext()) {
                if (nameColumnIndex >= 0) {
                    String name = cursor.getString(nameColumnIndex);
                } else {
                    // Handle the case where the column doesn't exist or is null
                    String name = "Unknown";
                }

                if (partNumberColumnIndex >= 0) {
                    String partNumber = cursor.getString(partNumberColumnIndex);
                } else {
                    // Handle the case where the column doesn't exist or is null
                    String partNumber = "Unknown";
                }

                if (quantityColumnIndex >= 0) {
                    int quantity = cursor.getInt(quantityColumnIndex);
                } else {
                    // Handle the case where the column doesn't exist or is null
                    int quantity = 0; // Or any default value
                }

            }

            cursor.close();
            db.close();
            return itemList;
        }
    }
    public class Item {
        private String name;
        private String partNumber;
        private int quantity;

        public Item(String name, String partNumber, int quantity) {
            this.name = name;
            this.partNumber = partNumber;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public int getQuantity() {
            return quantity;
        }
    }
    public class ItemAdapter extends BaseAdapter {
        private List<Item> itemList;
        private Context context;

        public ItemAdapter(Context context, List<Item> itemList) {
            this.context = context;
            this.itemList = itemList;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.grid_item_layout, null);
            }

            // Bind data to the views within the grid item layout
            TextView nameTextView = view.findViewById(R.id.nameTextView);
            TextView partNumberTextView = view.findViewById(R.id.partNumberTextView);
            TextView quantityTextView = view.findViewById(R.id.quantityTextView);

            Item item = itemList.get(position);
            nameTextView.setText(item.getName());
            partNumberTextView.setText(item.getPartNumber());
            quantityTextView.setText(String.valueOf(item.getQuantity()));

            return view;
        }
    }
    private void sendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        String phoneNumber = "5555215556"; // Replace with the recipient's phone number
        String message = "Your inventory item is running low on stock.";
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}