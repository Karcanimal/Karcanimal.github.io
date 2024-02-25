package com.example.project2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * DynamicFieldsAdapter.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * DynamicFieldsAdapter is an adapter class for managing dynamic EditText fields in a RecyclerView.
 * It provides functionality to add new EditText fields with associated column names, retrieve EditText fields by column names,
 * and bind data to the views.
 *
 * Components:
 * - List<String> columnNames: A list to hold column names.
 * - Map<String, EditText> editTextMap: A map to associate column names with EditText fields.
 *
 * Functions:
 * - Constructor: Initializes the DynamicFieldsAdapter with an empty list of column names and an empty map of EditText fields.
 * - addEditText(String columnName, EditText editText): Adds a new EditText field with its associated column name to the adapter.
 * - getEditText(String columnName): Retrieves the EditText field associated with the specified column name.
 * - onCreateViewHolder(ViewGroup parent, int viewType): Creates ViewHolder instances for the RecyclerView items.
 * - getItemCount(): Returns the total number of items in the RecyclerView.
 * - ViewHolder(itemView): Constructor for the ViewHolder class to hold the EditText field.
 * - clear(): Clears all column names and associated EditText fields from the adapter.
 * - onBindViewHolder(ViewHolder holder, int position): Binds data to the views, setting the hint for the EditText field based on the column name.
 *
 * Note:
 * - This adapter is designed to work with RecyclerViews that contain dynamic EditText fields.
 * - It allows the addition of EditText fields with associated column names, facilitating dynamic data entry.
 * - The adapter is responsible for managing the addition, retrieval, and binding of EditText fields in the RecyclerView.
 */
public class DynamicFieldsAdapter extends RecyclerView.Adapter<DynamicFieldsAdapter.ViewHolder> {
    // List to hold column names
    private final List<String> columnNames;
    // Map to associate column names with EditText fields
    private final Map<String, EditText> editTextMap;

    // Constructor
    public DynamicFieldsAdapter() {
        columnNames = new ArrayList<>();
        editTextMap = new HashMap<>();
    }

    // Method to add a new EditText field with its associated column name
    public void addEditText(String columnName, EditText editText) {
        if (!columnNames.contains(columnName)) {
            columnNames.add(columnName); // Add the column name to the list
            editTextMap.put(columnName, editText); // Associate the column name with the EditText field
            notifyItemInserted(columnNames.size() - 1); // Notify RecyclerView of the new item insertion
        }
    }

    // Method to get the EditText field associated with a column name
    public EditText getEditText(String columnName) {
        return editTextMap.get(columnName);
    }

    // Create ViewHolder instances
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dynamic_field, parent, false);
        return new ViewHolder(view);
    }

    // Get the total number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return columnNames.size();
    }

    // ViewHolder class to hold the EditText field
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.editTextDynamicField);
        }
    }

    // Method to clear all column names and associated EditText fields
    public void clear() {
        columnNames.clear();
        editTextMap.clear();
        notifyDataSetChanged(); // Notify RecyclerView of data change
    }

    // Bind data to views
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String columnName = columnNames.get(position);
        holder.editText.setHint(columnName); // Set the hint for the EditText field based on the column name
    }
}
