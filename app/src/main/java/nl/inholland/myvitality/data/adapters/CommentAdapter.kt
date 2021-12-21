package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Comment
import nl.inholland.myvitality.util.DateUtils
import nl.inholland.myvitality.util.TextViewUtils

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

        holder.profileImage.load(currentItem.imageUrl)
        holder.userName.text = currentItem.fullName
        holder.postDate.text = DateUtils.formatDateToTimeAgo(context, currentItem.timestamp)
        holder.content.text = currentItem.text

        holder.itemView.setOnClickListener { view ->
//            val intent = Intent(view.context, ProfileActivity::class.java)
//            intent.putExtra("USER_ID", currentItem.userId)
//            view.context.startActivity(intent)
            Toast.makeText(view.context, "WIP - Open Profile",  Toast.LENGTH_LONG).show()
           // TODO: Open profile
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