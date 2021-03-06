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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import nl.inholland.myvitality.R
import nl.inholland.myvitality.architecture.base.BaseRecyclerAdapter
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.timeline.liked.TimelineLikedActivity
import nl.inholland.myvitality.ui.timelinepost.view.TimelinePostActivity
import nl.inholland.myvitality.util.DateUtils

class TimelinePostAdapter(context: Context, private val onClickedLike: (TimelinePost, ViewHolder) -> Unit) :
    BaseRecyclerAdapter<TimelinePost, TimelinePostAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.timeline_post_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        holder.image.visibility = View.GONE

        Glide.with(context)
            .load(currentItem.profilePicture)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.profileImage)
        holder.userName.text = currentItem.fullName
        holder.postDate.text = DateUtils.formatDateToTimeAgo(currentItem.publishDate)
        holder.content.text = currentItem.text

        currentItem.imageUrl?.let {
            holder.image.visibility = View.VISIBLE
            Glide.with(context)
                .load(it)
                .into(holder.image)
        }

        if(currentItem.countOfLikes > 0){
            holder.likeCount.visibility = View.VISIBLE
            holder.likeIcon.visibility = View.VISIBLE
            holder.likeCount.text = currentItem.countOfLikes.toString()
        }

        if(currentItem.countOfComments > 0){
            holder.commentCount.visibility = View.VISIBLE
            holder.commentCount.text = null
            holder.commentCount.append(currentItem.countOfComments.toString() + " ")
            holder.commentCount.append(context.getString(R.string.post_text_comments))
        }

        if (currentItem.iLikedPost) {
            holder.likedButton.setIconResource(R.drawable.ic_thumbsup_fill)
            holder.likedButton.setIconTintResource(R.color.primary)

            if(currentItem.countOfLikes - 1 <= 0){
                holder.likeCount.text = context.getString(R.string.post_like_count_you)
            } else {
                holder.likeCount.text = context.getString(R.string.post_like_count, (currentItem.countOfLikes - 1).toString())
            }
        } else {
            holder.likedButton.setIconResource(R.drawable.ic_thumbsup)
            holder.likedButton.setIconTintResource(R.color.black)
        }

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, TimelinePostActivity::class.java)
            intent.putExtra("POST_ID", currentItem.postId)
            view.context.startActivity(intent)
        }

        val userClickListener = View.OnClickListener { view ->
            view.context.startActivity(
                Intent(view.context, ProfileActivity::class.java)
                    .putExtra("USER_ID", currentItem.userId))
        }

        holder.profileImage.setOnClickListener(userClickListener)
        holder.userName.setOnClickListener(userClickListener)

        val likeClickListener = View.OnClickListener { view ->
            view.context.startActivity(
                Intent(view.context, TimelineLikedActivity::class.java)
                    .putExtra("POST_ID", currentItem.postId))
        }

        holder.likeIcon.setOnClickListener(likeClickListener)
        holder.likeCount.setOnClickListener(likeClickListener)

        holder.likedButton.setOnClickListener {
            onClickedLike(currentItem, holder)

            currentItem.iLikedPost = !currentItem.iLikedPost
            currentItem.countOfLikes = if(currentItem.iLikedPost) currentItem.countOfLikes-- else currentItem.countOfLikes++
         }

        holder.commentButton.setOnClickListener { view ->
            val intent = Intent(view.context, TimelinePostActivity::class.java)
            intent.putExtra("POST_ID", currentItem.postId)
            intent.putExtra("COMMENT", true)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val profileImage: ImageView = itemView.findViewById(R.id.post_profile_image)
        internal val userName: TextView = itemView.findViewById(R.id.post_user_name)
        internal val postDate: TextView = itemView.findViewById(R.id.post_date)
        internal val content: TextView = itemView.findViewById(R.id.post_content)
        internal val image: ImageView = itemView.findViewById(R.id.post_image)
        internal val likeIcon: ImageView = itemView.findViewById(R.id.post_like_count_icon)
        internal val likeCount: TextView = itemView.findViewById(R.id.post_like_count)
        internal val commentCount: TextView = itemView.findViewById(R.id.post_comment_count)
        internal val likedButton: MaterialButton = itemView.findViewById(R.id.post_like_button)
        internal val commentButton: MaterialButton = itemView.findViewById(R.id.post_comment_button)
    }
}