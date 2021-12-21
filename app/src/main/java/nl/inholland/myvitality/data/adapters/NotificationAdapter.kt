package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Notification
import nl.inholland.myvitality.data.entities.NotificationType
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.ui.challenge.ChallengeActivity
import nl.inholland.myvitality.ui.profile.ProfileActivity
import nl.inholland.myvitality.ui.timelinepost.TimelinePostActivity
import nl.inholland.myvitality.util.DateUtils

class NotificationAdapter(context: Context) :
    BaseRecyclerAdapter<Notification, NotificationAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        var clickListener: View.OnClickListener? = null

        when(currentItem.type){
            NotificationType.LIKE -> {
                holder.image.load(currentItem.profileImage)
                holder.title.text = Html.fromHtml(context.getString(R.string.notification_liked, currentItem.fullName), Html.FROM_HTML_MODE_LEGACY)
                holder.subtitle.text = DateUtils.formatDateToTimeAgo(context, currentItem.date)

                clickListener = View.OnClickListener { view ->
                    view.context.startActivity(
                        Intent(view.context, TimelinePostActivity::class.java)
                            .putExtra("POST_ID", currentItem.timelinePostId)
                    )
                }
            }
            NotificationType.COMMENT -> {
                holder.image.load(currentItem.profileImage)
                holder.title.text = Html.fromHtml(context.getString(R.string.notification_comment, currentItem.fullName), Html.FROM_HTML_MODE_LEGACY)
                holder.subtitle.text = DateUtils.formatDateToTimeAgo(context, currentItem.date)

                clickListener = View.OnClickListener { view ->
                    view.context.startActivity(
                        Intent(view.context, TimelinePostActivity::class.java)
                            .putExtra("POST_ID", currentItem.timelinePostId)
                    )
                }
            }
            NotificationType.FOLLOW -> {
                holder.image.load(currentItem.profileImage)
                holder.title.text = Html.fromHtml(context.getString(R.string.notification_follow, currentItem.fullName), Html.FROM_HTML_MODE_LEGACY)
                holder.subtitle.text = DateUtils.formatDateToTimeAgo(context, currentItem.date)

                if(currentItem.isFollowing == false){
                    // TODO: Show follow icon if not following
                }

                clickListener = View.OnClickListener { view ->
                    view.context.startActivity(
                        Intent(view.context, ProfileActivity::class.java)
                            .putExtra("USER_ID", currentItem.toUser)
                    )
                }
            }
            NotificationType.OTHER -> {
                holder.image.setImageResource(R.drawable.challenge_placeholder)
                holder.title.text = context.getString(R.string.notification_new_activity_title)
                holder.subtitle.text = context.getString(R.string.notification_new_activity_subtitle)
                holder.subtitle.setTextColor(context.getColor(R.color.primary))

                clickListener = View.OnClickListener { view ->
                    view.context.startActivity(
                        Intent(view.context, ChallengeActivity::class.java)
                            .putExtra("CHALLENGE_ID", currentItem.challengeId)
                    )
                }
            }
        }

        holder.itemView.setOnClickListener(clickListener)
        holder.image.setOnClickListener { view ->
            if (currentItem.type == NotificationType.OTHER) {
                view.context.startActivity(
                    Intent(view.context, ChallengeActivity::class.java)
                        .putExtra("CHALLENGE_ID", currentItem.challengeId)
                )
            } else {
                view.context.startActivity(
                    Intent(view.context, ProfileActivity::class.java)
                        .putExtra("USER_ID", currentItem.toUser)
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val image: ImageView = itemView.findViewById(R.id.notification_image)
        internal val title: TextView = itemView.findViewById(R.id.notification_title)
        internal val subtitle: TextView = itemView.findViewById(R.id.notification_subtitle)
    }
}