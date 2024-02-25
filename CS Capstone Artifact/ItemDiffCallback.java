package com.example.project2;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

/*
 * ItemDiffCallback.java
 * Developer: Christopher Karchella
 * Version: 1.1
 *
 * Description:
 * ItemDiffCallback is a callback class used for calculating the difference between two lists of items,
 * enabling efficient updates of RecyclerView contents. It facilitates the implementation of DiffUtil
 * for optimized list updates in RecyclerView adapters.
 *
 * Components:
 * - List<Item> oldList: The old list of items.
 * - List<Item> newList: The new list of items.
 *
 * Functionality:
 * - Constructor: Initializes the ItemDiffCallback with the old and new lists of items.
 * - getOldListSize(): Retrieves the size of the old list.
 * - getNewListSize(): Retrieves the size of the new list.
 * - areItemsTheSame(int oldItemPosition, int newItemPosition): Compares whether the items at the specified positions
 *   in the old and new lists are the same based on their equality.
 * - areContentsTheSame(int oldItemPosition, int newItemPosition): Compares whether the contents of the items at the specified positions
 *   in the old and new lists are the same based on their equality. This method typically involves a deeper comparison of item properties.
 *
 * Note:
 * - This class is essential for implementing efficient updates in RecyclerView adapters, particularly when dealing with large datasets.
 * - It ensures that RecyclerView updates are performed only where necessary, minimizing UI flickering and improving performance.
 */
public class ItemDiffCallback extends DiffUtil.Callback {

    private final List<Item> oldList; // The old list of items
    private final List<Item> newList; // The new list of items

    /**
     * Constructor for ItemDiffCallback class.
     * @param oldList The old list of items.
     * @param newList The new list of items.
     */
    public ItemDiffCallback(List<Item> oldList, List<Item> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    /**
     * Get the size of the old list.
     * @return The size of the old list.
     */
    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    /**
     * Get the size of the new list.
     * @return The size of the new list.
     */
    @Override
    public int getNewListSize() {
        return newList.size();
    }

    /**
     * Check if items in the old and new lists are the same.
     * @param oldItemPosition The position of the item in the old list.
     * @param newItemPosition The position of the item in the new list.
     * @return True if items are the same, false otherwise.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    /**
     * Check if contents of items in the old and new lists are the same.
     * @param oldItemPosition The position of the item in the old list.
     * @param newItemPosition The position of the item in the new list.
     * @return True if contents of items are the same, false otherwise.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Implement logic to check if item contents are the same
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}