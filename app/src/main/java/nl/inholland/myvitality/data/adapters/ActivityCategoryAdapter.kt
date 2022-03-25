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
import nl.inholland.myvitality.data.entities.ActivityCategory
import nl.inholland.myvitality.ui.activity.overview.ActivityOverviewActivity
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity

class ActivityCategoryAdapter(context: Context) :
    BaseRecyclerAdapter<ActivityCategory, ActivityCategoryAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_category_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        if(position == 0){
            holder.activityImage.setImageResource(R.drawable.my_activity_image)

            holder.itemView.setOnClickListener { view ->
                val intent = Intent(view.context, ProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                view.context.startActivity(intent)
            }
        } else {
            Glide.with(context)
                .load(currentItem.imageLink)
                .into(holder.activityImage)

            holder.itemView.setOnClickListener { view ->
                val intent = Intent(view.context, ActivityOverviewActivity::class.java)
                intent.putExtra("CATEGORY_ID", currentItem.categoryId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                view.context.startActivity(intent)
            }
        }

        holder.activityTitle.text = currentItem.title
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val activityImage: ImageView = itemView.findViewById(R.id.activity_category_image)
        internal val activityTitle: TextView = itemView.findViewById(R.id.activity_category_title)
    }
}