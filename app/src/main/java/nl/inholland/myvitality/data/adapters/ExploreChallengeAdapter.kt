package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ActivityType
import nl.inholland.myvitality.ui.activity.detail.ActivityDetailActivity

class ExploreChallengeAdapter(context: Context, var showButtons: Boolean? = true) : BaseRecyclerAdapter<Challenge, ExploreChallengeAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.challenge_explore_view_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Load the challenge image with a fallback image
        Glide.with(context)
            .load(currentItem.imageLink)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
            .into(holder.challengeImage)

        // Set the challenge type
        when (currentItem.activityType) {
            ActivityType.EXERCISE ->
                holder.challengeType.text = context.getString(R.string.activity_type_exercise)
            ActivityType.DIET ->
                holder.challengeType.text = context.getString(R.string.activity_type_diet)
            ActivityType.MIND ->
                holder.challengeType.text = context.getString(R.string.activity_type_mind)
        }

        if(showButtons == true){
            holder.challengeButton.visibility = View.VISIBLE

            holder.challengeButton.setOnClickListener { view ->
                val intent = Intent(view.context, ActivityDetailActivity::class.java)
                intent.putExtra("CHALLENGE_ID", currentItem.challengeId)
                view.context.startActivity(intent)
            }
        }

        holder.challengeTitle.text = currentItem.title
        val onClickAction = View.OnClickListener { view ->
            val intent = Intent(view.context, ActivityDetailActivity::class.java)
            intent.putExtra("CHALLENGE_ID", currentItem.challengeId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            view.context.startActivity(intent)
        }

        if (showButtons == true) {
            holder.challengeButton.visibility = View.VISIBLE
            holder.challengeButton.setOnClickListener(onClickAction)
        }

        holder.itemView.setOnClickListener(onClickAction)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val challengeImage: ImageView = itemView.findViewById(R.id.challenge_image)
        internal val challengeType: TextView = itemView.findViewById(R.id.challenge_type)
        internal val challengeTitle: TextView = itemView.findViewById(R.id.challenge_title)
        internal val challengeButton: Button = itemView.findViewById(R.id.challenge_view_challenge)

    }
}