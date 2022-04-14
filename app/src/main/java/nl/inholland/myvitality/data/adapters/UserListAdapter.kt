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
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity

class UserListAdapter(context: Context) :
    BaseRecyclerAdapter<SimpleUser, UserListAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.user_list_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        holder.userDetails.text = null

        Glide.with(context)
            .load(currentItem.profileImage)
            .into(holder.profileImage)

        holder.userName.text = currentItem.fullName

        holder.userDetails.text = ""

        if(currentItem.jobTitle != null && currentItem.location != null){
            holder.userDetails.append("${currentItem.jobTitle}, ${currentItem.location}")
        } else if (currentItem.jobTitle != null) {
            holder.userDetails.append(currentItem.jobTitle)
        } else if (currentItem.location != null) {
            holder.userDetails.append(currentItem.location)
        }

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ProfileActivity::class.java)
            intent.putExtra("USER_ID", currentItem.userId)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        internal val userName: TextView = itemView.findViewById(R.id.user_name)
        internal val userDetails: TextView = itemView.findViewById(R.id.user_details)
    }
}