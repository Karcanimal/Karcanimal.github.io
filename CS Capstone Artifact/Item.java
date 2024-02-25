package com.example.project2;

import android.content.ContentValues;
import java.util.Map;

/*
 * Item.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * Item represents an item in the inventory system. It encapsulates properties such as name, part number, quantity,
 * and dynamic values associated with the item.
 *
 * Components:
 * - String name: The name of the item.
 * - String partNumber: The part number of the item.
 * - int quantity: The quantity of the item.
 * - Map<String, String> dynamicValues: A map containing dynamic values associated with the item.
 *
 * Functionality:
 * - Constructor: Initializes item properties including name, part number, quantity, and dynamic values.
 * - getName(): Getter method to retrieve the name of the item.
 * - getPartNumber(): Getter method to retrieve the part number of the item.
 * - getQuantity(): Getter method to retrieve the quantity of the item.
 * - setDynamicValues(ContentValues values): Sets dynamic values for the item using ContentValues.
 *   It iterates through dynamicValues and adds them to the provided ContentValues object.
 * - getDynamicValues(): Getter method to retrieve dynamic values associated with the item.
 *
 * Note:
 * - This class provides a structured representation of inventory items and their properties.
 * - Dynamic values allow for flexibility in storing additional information associated with each item.
 * - The class encapsulates item-related functionalities and supports integration with database operations.
 */
public class Item {
    private final String name; // Name of the item
    private final String partNumber; // Part number of the item
    private final int quantity; // Quantity of the item
    private Map<String, String> dynamicValues; // Dynamic values associated with the item

    /**
     * Constructor to initialize item properties.
     * @param name Name of the item
     * @param partNumber Part number of the item
     * @param quantity Quantity of the item
     * @param dynamicValues Dynamic values associated with the item
     */
    public Item(String name, String partNumber, int quantity, Map<String, String> dynamicValues) {
        this.name = name;
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.dynamicValues = dynamicValues;
    }

    /**
     * Getter method for retrieving item name.
     * @return Name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method for retrieving item part number.
     * @return Part number of the item
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Getter method for retrieving item quantity.
     * @return Quantity of the item
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Method to set dynamic values for the item.
     * @param values ContentValues object containing dynamic values
     */
    public void setDynamicValues(ContentValues values) {
        if (dynamicValues != null && !dynamicValues.isEmpty()) {
            for (Map.Entry<String, String> entry : dynamicValues.entrySet()) {
                String columnName = entry.getKey();
                String columnValue = entry.getValue();

                // Check if the column name and value are not null or empty
                if (columnName != null && !columnName.isEmpty() && columnValue != null) {
                    values.put(columnName, columnValue);
                }
            }
        }
    }

    /**
     * Getter method for retrieving dynamic values associated with the item.
     * @return Dynamic values associated with the item
     */
    public Map<String, String> getDynamicValues() {
        return dynamicValues;
    }
}
