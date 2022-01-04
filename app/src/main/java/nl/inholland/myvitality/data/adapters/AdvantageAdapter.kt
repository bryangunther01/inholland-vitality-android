package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.ui.profile.ProfileActivity

class AdvantageAdapter(context: Context) :
    BaseRecyclerAdapter<String, AdvantageAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.advantage_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.advantage.text = currentItem
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val advantage: TextView = itemView.findViewById(R.id.advantage)
    }
}