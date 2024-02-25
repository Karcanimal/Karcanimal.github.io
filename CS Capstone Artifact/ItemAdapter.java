package com.example.project2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * ItemAdapter.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * ItemAdapter is an adapter class responsible for populating a RecyclerView with items.
 * It facilitates the binding of item data to corresponding views and supports filtering functionality.
 *
 * Components:
 * - List<Item> itemList: The list of all items.
 * - List<Item> filteredList: The list of filtered items.
 * - Context context: Reference to the application context for inflating layouts.
 *
 * Functionality:
 * - Constructor: Initializes the adapter with a list of items and the application context.
 * - onCreateViewHolder(): Inflates the item layout and returns a new ViewHolder instance.
 * - getItemCount(): Retrieves the count of items in the filtered list.
 * - updateData(List<Item> items): Updates the adapter data with the provided list of items.
 * - ViewHolder: Inner static class for holding the views of each item.
 * - onBindViewHolder(): Binds item data to the views of each item in the RecyclerView.
 *   It also logs dynamic values associated with each item.
 * - filterList(Map<String, String> filterMap): Updates the filteredList based on provided filter criteria.
 *   It iterates through all items and adds those matching the filter criteria to the filteredList.
 *   If no filters are provided, it adds all items from the original list.
 *   It notifies the adapter about the data set change after filtering.
 *
 * Note:
 * - This adapter supports dynamic filtering based on user-defined criteria.
 * - It efficiently handles the binding of item data to views and updates RecyclerView contents.
 * - The adapter is designed to be versatile and adaptable to various item layouts and filtering requirements.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final List<Item> itemList; // List of all items
    private final List<Item> filteredList; // List of filtered items
    private final Context context; // Context reference for inflating layout

    // Constructor to initialize the adapter with a list of items and the context
    public ItemAdapter(List<Item> itemList, Context context) {
        this.context = context;
        this.itemList = itemList;
        this.filteredList = new ArrayList<>(itemList); // Initialize filtered list with all items
    }

    // Method to create ViewHolder instances
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and return a new ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    // Method to get the count of items in the filtered list
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateData(List<Item> items) {
    }

    // ViewHolder class for holding the views of each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView partNumberTextView;
        TextView quantityTextView;

        // Constructor to initialize views
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            partNumberTextView = itemView.findViewById(R.id.textViewPartNumber);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
        }
    }

    // Method to bind data to views of each item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = filteredList.get(position); // Get item from filtered list

        // Bind item data to views
        holder.nameTextView.setText(item.getName());

        // Construct the part number text with a placeholder
        String partNumberText = context.getString(R.string.part_number_placeholder, item.getPartNumber());
        holder.partNumberTextView.setText(partNumberText);

        // Construct the quantity text with a placeholder
        String quantityText = context.getString(R.string.quantity_placeholder, item.getQuantity());
        holder.quantityTextView.setText(quantityText);

        // Log dynamic values
        Map<String, String> dynamicValues = item.getDynamicValues();
        for (Map.Entry<String, String> entry : dynamicValues.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();
            Log.d("ItemAdapter", "Dynamic Column: " + columnName + ", Value: " + columnValue);
        }
    }

    // Method to update the filtered list based on provided filters
    public void filterList(Map<String, String> filterMap) {
        filteredList.clear(); // Clear the filtered list

        // If no filters provided, add all items from the original list
        if (filterMap.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            // Iterate through all items and add those matching the filter criteria
            for (Item item : itemList) {
                boolean isMatch = true;
                for (Map.Entry<String, String> entry : filterMap.entrySet()) {
                    String columnName = entry.getKey();
                    String columnValue = entry.getValue();
                    if (!item.getDynamicValues().containsKey(columnName) || !item.getDynamicValues().get(columnName).equalsIgnoreCase(columnValue)) {
                        isMatch = false;
                        break;
                    }
                }
                if (isMatch) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter about data set change
    }
}
