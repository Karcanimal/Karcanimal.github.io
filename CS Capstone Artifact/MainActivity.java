package com.example.project2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/*
 * MainActivity.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * The MainActivity serves as the entry point for user authentication and account management within the application.
 * Users can either log in with existing credentials or create a new account.
 *
 * Components:
 * - Button (login_button, create_account_button): UI elements to initiate login or account creation processes.
 * - EditText (username_edittext, password_edittext): Fields for entering the username and password.
 * - DBHelper: Database helper class instance for managing user data and authentication.
 *
 * Functionality:
 * - onCreate(): Initializes UI elements and sets click listeners for login and account creation buttons.
 *   Upon login button click, it validates user credentials using DBHelper.
 *   If credentials are valid, it launches the SecondActivity.
 *   Upon create account button click, it prompts the user with a confirmation dialog for account creation.
 *   If the user confirms, it inserts the new account data into the database after hashing the password.
 *   Error handling is implemented for database operations and incorrect login attempts.
 * - showToast(): Displays a short toast message.
 * - showConfirmationDialog(): Displays a confirmation dialog for creating a new account.
 *   If the user confirms, it inserts the new account data into the database.
 *   If the user cancels, no action is taken.
 *
 * Note:
 * - This activity relies on the DBHelper class for database operations.
 * - Error handling is implemented for database operations and user input validation.
 * - Passwords are hashed before storing in the database for security.
 * - The activity provides a straightforward interface for user authentication and account creation.
 */

public class MainActivity extends AppCompatActivity {
    private Button login_button; // Button to initiate login process
    private Button create_account_button; // Button to create a new user account
    private EditText username_edittext; // EditText for entering the username
    private EditText password_edittext; // EditText for entering the password
    private DBHelper dbHelper; // Instance of the database helper class

    // Method to display a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_button = findViewById(R.id.login_button);
        username_edittext = findViewById(R.id.username_edittext);
        password_edittext = findViewById(R.id.password_edittext);
        create_account_button = findViewById(R.id.create_account_button);
        dbHelper = new DBHelper(this);

        // Set click listener for login button
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = username_edittext.getText().toString();
                String password = password_edittext.getText().toString();

                // Check if the entered credentials are valid
                if (dbHelper.checkUser(username, password)) {
                    // If valid, start the second activity
                    Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                    startActivity(intent);
                } else {
                    // If not valid, clear the fields and show an error message
                    username_edittext.getText().clear();
                    password_edittext.getText().clear();
                    showToast("Incorrect username or password");
                }
            }
        });

        // Set click listener for create account button
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = username_edittext.getText().toString();
                String newPassword = password_edittext.getText().toString();
                showConfirmationDialog(newUsername, newPassword);
            }
        });
    }

    // Method to display a confirmation dialog for creating a new account
    private void showConfirmationDialog(final String newUsername, final String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to create an account?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    // Insert the new account data into the database
                    dbHelper.insertDataIntoDatabase(newUsername, dbHelper.hashPassword(newPassword));
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Failed to create an account");
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing or handle the cancellation
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
