package com.example.project2;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import at.favre.lib.crypto.bcrypt.BCrypt;

/*
 * DBHelper.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * DBHelper is a helper class for interacting with the SQLite database. It manages user authentication and database operations.
 *
 * Components:
 * - String DB_NAME: The name of the database.
 * - int DB_VERSION: The version of the database.
 * - String TABLE_NAME: The name of the table storing user data.
 * - String COL_ID: The column name for user ID.
 * - String COL_USERNAME: The column name for username.
 * - String COL_PASSWORD: The column name for password.
 *
 * Functions:
 * - Constructor(Context context): Initializes the DBHelper with the given context, database name, and version.
 * - onCreate(SQLiteDatabase db): Called when the database is created for the first time. It creates the users table.
 * - onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion): Called when the database needs to be upgraded. It drops the existing table and creates a new one.
 * - checkUser(String username, String password): Checks if a user exists in the database and verifies their password.
 * - insertDataIntoDatabase(String newUser, String newPass): Inserts a new user into the database.
 * - hashPassword(String password): Hashes a password using BCrypt.
 *
 * Note:
 * - This class provides essential functionality for user authentication and database operations.
 * - It ensures secure storage of user passwords by hashing them using BCrypt.
 * - The class encapsulates database-related logic, promoting modularity and code organization.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "THE_COUNTER-DB.db"; // Database name
    private static final int DB_VERSION = 1; // Database version
    private static final String TABLE_NAME = "users"; // Table name
    private static final String COL_ID = "id"; // Column name for user ID
    private static final String COL_USERNAME = "username"; // Column name for username
    private static final String COL_PASSWORD = "password"; // Column name for password

    /**
     * Constructor for DBHelper class.
     * @param context The context of the application.
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * @param db The database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT);");
    }

    /**
     * Called when the database needs to be upgraded.
     * @param db The database instance.
     * @param oldVersion The old version of the database.
     * @param newVersion The new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Method to check if a user exists in the database and verify their password.
     * @param username The username to check.
     * @param password The password to verify.
     * @return True if the user exists and the password is correct, false otherwise.
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
                COL_USERNAME + "=?", new String[]{username})) {
            if (cursor.moveToFirst()) {
                int passwordColumnIndex = cursor.getColumnIndex(COL_PASSWORD);
                if (passwordColumnIndex != -1) {
                    String storedPassword = cursor.getString(passwordColumnIndex);
                    return BCrypt.verifyer().verify(password.toCharArray(), storedPassword).verified;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error checking user: " + e.getMessage());
        } finally {
            db.close();
        }

        return false;
    }

    /**
     * Method to insert a new user into the database.
     * @param newUser The username of the new user.
     * @param newPass The password of the new user.
     */
    public void insertDataIntoDatabase(String newUser, String newPass) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            db.execSQL(sql, new String[]{newUser, newPass});
        } catch (SQLException e) {
            Log.e("DBHelper", "Error inserting data: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    /**
     * Method to hash a password using BCrypt.
     * @param password The password to hash.
     * @return The hashed password.
     */
    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
}