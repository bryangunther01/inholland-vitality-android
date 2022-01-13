package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.ScoreboardUser
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity

class ScoreboardAdapter(context: Context) :
    BaseRecyclerAdapter<ScoreboardUser, ScoreboardAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.scoreboard_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        val userPosition = position+1

        if(userPosition == 1) {
            holder.position.setTextColor(context.getColor(R.color.primary))
            holder.fullName.setTextColor(context.getColor(R.color.primary))
            holder.points.setTextColor(context.getColor(R.color.primary))
        }

        if(userPosition <= 3){
            holder.position.typeface = Typeface.DEFAULT_BOLD
            holder.fullName.typeface = Typeface.DEFAULT_BOLD
            holder.points.typeface = Typeface.DEFAULT_BOLD
        }


        holder.position.text = userPosition.toString()
        Glide.with(context)
            .load(currentItem.profileImage)
            .into(holder.profileImage)
        holder.fullName.text = currentItem.fullName
        holder.points.text = currentItem.points.toString()

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ProfileActivity::class.java)
            intent.putExtra("USER_ID", currentItem.userId)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val position: TextView = itemView.findViewById(R.id.scoreboard_position)
        internal val profileImage: ImageView = itemView.findViewById(R.id.scoreboard_profile_image)
        internal val fullName: TextView = itemView.findViewById(R.id.scoreboard_full_name)
        internal val points: TextView = itemView.findViewById(R.id.scoreboard_points)
    }
}