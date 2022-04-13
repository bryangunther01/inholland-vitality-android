package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.PersonalScoreboardResult
import nl.inholland.myvitality.ui.activity.detail.ActivityDetailActivity
import nl.inholland.myvitality.util.DateUtils

class PersonalScoreboardAdapter(context: Context) :
    BaseRecyclerAdapter<PersonalScoreboardResult, PersonalScoreboardAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Load the challenge image with a fallback image
        Glide.with(context)
            .load(currentItem.imageLink)
            .placeholder(R.drawable.activity_placeholder)
            .into(holder.activityImage)

        holder.activityTitle.text = currentItem.title
        holder.activitySubtitle.text = context.getString(R.string.scoreboard_personal_points, currentItem.points)

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ActivityDetailActivity::class.java)
            intent.putExtra("ACTIVITY_ID", currentItem.activityId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val activityImage: ImageView = itemView.findViewById(R.id.activity_image)
        internal val activityTitle: TextView = itemView.findViewById(R.id.activity_title)
        internal val activitySubtitle: TextView = itemView.findViewById(R.id.activity_subtitle)
    }
}