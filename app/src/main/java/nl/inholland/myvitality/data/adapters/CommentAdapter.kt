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
import nl.inholland.myvitality.architecture.base.BaseRecyclerAdapter
import nl.inholland.myvitality.data.entities.Comment
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.util.DateUtils

class CommentAdapter(context: Context) :
    BaseRecyclerAdapter<Comment, CommentAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.post_comment_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        Glide.with(context)
            .load(currentItem.imageUrl)
            .placeholder(R.drawable.person_placeholder)
            .into(holder.profileImage)

        holder.userName.text = currentItem.fullName
        holder.postDate.text = DateUtils.formatDateToTimeAgo(currentItem.timestamp)
        holder.content.text = currentItem.text

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ProfileActivity::class.java)
            intent.putExtra("USER_ID", currentItem.userId)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        internal val userName: TextView = itemView.findViewById(R.id.comment_user_name)
        internal val postDate: TextView = itemView.findViewById(R.id.comment_date)
        internal val content: TextView = itemView.findViewById(R.id.comment_content)
    }
}