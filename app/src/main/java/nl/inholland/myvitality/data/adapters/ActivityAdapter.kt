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
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Activity
import nl.inholland.myvitality.data.entities.ActivityCategory
import nl.inholland.myvitality.ui.activity.detail.ActivityDetailActivity
import nl.inholland.myvitality.ui.activity.overview.ActivityOverviewActivity
import nl.inholland.myvitality.util.DateUtils

class ActivityAdapter(context: Context) :
    BaseRecyclerAdapter<Activity, ActivityAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Load the challenge image with a fallback image
        if(currentItem.isActive) {
            Glide.with(context)
                .load(currentItem.imageLink)
                .into(holder.activityImage)
        } else {
            Glide.with(context)
                .load(currentItem.imageLink)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(22)))
                .into(holder.activityImage)
        }

        holder.activityTitle.text = currentItem.title

        if(!currentItem.isActive){
            holder.activityDate.text = context.getString(R.string.activity_start_date_text, DateUtils.formatDate(currentItem.startDate, "dd-MM-yyyy"))
        } else {
            holder.activityDate.text = context.getString(R.string.activity_end_date_text, DateUtils.formatDate(currentItem.endDate, "dd-MM-yyyy"))
        }

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
        internal val activityDate: TextView = itemView.findViewById(R.id.activity_date)
    }
}