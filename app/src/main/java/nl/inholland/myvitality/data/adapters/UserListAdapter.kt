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

        holder.profileImage.load(currentItem.profileImage)
        holder.userName.text = currentItem.fullName

        holder.userDetails.text = ""
        holder.userDetails.append(currentItem.jobTitle)
        currentItem.location.let{
            holder.userDetails.append(", $it")
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