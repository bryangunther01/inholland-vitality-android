package nl.inholland.myvitality.architecture.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * The base recycler adapter for handling the base functionalities of an adapter
 */
abstract class BaseRecyclerAdapter<T, VH : RecyclerView.ViewHolder> @JvmOverloads internal constructor(val context: Context, private val mItems: MutableList<T> = ArrayList()) : RecyclerView.Adapter<VH>() {

    /**
     * ClickListener triggered when item in list is clicked
     */
    private var listener: OnItemClickListener<T>? = null

    var items: List<T>
        get() = mItems
        set(items) {
            mItems.clear()
            addItems(items)
        }

    /**
     * Check if the listener is attached
     */
    private val isListenerAttached: Boolean
        get() = listener != null

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener { view ->
            if (isListenerAttached) {
                listener!!.onItemClick(view, items[position], position)
            }
        }
    }

    /**
     * Check the amount of items available in the list
     *
     * @return int Amount of items
     */
    override fun getItemCount(): Int {
        return mItems.size
    }

    /**
     * Function to add a bunch of items to the list
     */
    fun addItem(item: T) {
        // Add the items to the list
        mItems.add(item)
        notifyDataSetChanged()
    }

    /**
     * Function to add a bunch of items to the list
     */
    fun addItems(items: List<T>) {
        // Add the items to the list
        mItems.addAll(items)

        // Get the unique values from the list
        val distinctList = mItems.distinct()

        // Clear the list and add the unique values
        mItems.clear()
        mItems.addAll(distinctList)

        notifyDataSetChanged()
    }

    /**
     * Function to clear the list of items and update the view
     */
    fun clearItems(){
        mItems.clear()

        notifyDataSetChanged()
    }

    interface OnItemClickListener<T> {
        fun onItemClick(view: View, item: T, position: Int)
    }
}