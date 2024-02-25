package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.content.ContentValues;


public class MainActivity extends AppCompatActivity {
    //Button initializations
    private Button login_button;
    private Button create_account_button;
    private EditText username_edittext;
    private EditText password_edittext;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button listening
        login_button = findViewById(R.id.login_button);
        username_edittext = findViewById(R.id.username_edittext);
        password_edittext = findViewById(R.id.password_edittext);
        create_account_button = findViewById(R.id.create_account_button);
        dbHelper = new DBHelper(this);

        //Handles Login Buttons clicks
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = username_edittext.getText().toString();
                String password = password_edittext.getText().toString();

                //If successful goes to the main screen
                if (dbHelper.checkUser(username, password)) {
                    Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                    startActivity(intent);
                } else {
                    username_edittext.getText().clear();
                    password_edittext.getText().clear();
                }
            }
        });
        //handles Create Account button clicks
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the registration here
                String newUsername = username_edittext.getText().toString();
                String newPassword = password_edittext.getText().toString();
                // Insert userInput into the SQLite database
                insertDataIntoDatabase(newUsername, newPassword);
            }
        });
    }
    //DataBase handler class
    public class DBHelper extends SQLiteOpenHelper{
        //Database variables
        private static final String DB_NAME = "THE_COUNTER-DB.db";
        private static final int DB_VERSION = 1;
        private static final String TABLE_NAME = "users";
        private static final String COL_ID = "id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";

        public DBHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT, " +
                    COL_PASSWORD + " TEXT);");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        //Verifies users in the DataBase
        public boolean checkUser(String username, String password) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
                    COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});
            int count = cursor.getCount();
            cursor.close();
            return count > 0;
        }
    }
    //Adds users into the Database
    private void insertDataIntoDatabase(String newUser, String newPass) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define the values you want to insert
        ContentValues values = new ContentValues();
        values.put("username", newUser);
        values.put("password", newPass);

        // Insert data into the table
        long newRowId = db.insert("users", null, values);

        // Handle the result of the insertion
        if (newRowId != -1) {
            username_edittext.getText().clear();
            password_edittext.getText().clear();
        } else {
            username_edittext.getText().clear();
        }
        // Close the database to release resources
        db.close();
    }
}

