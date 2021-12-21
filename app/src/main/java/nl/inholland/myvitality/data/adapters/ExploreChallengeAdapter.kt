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
import coil.load
import coil.transform.RoundedCornersTransformation
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ChallengeType
import nl.inholland.myvitality.ui.challenge.ChallengeActivity

class ExploreChallengeAdapter(context: Context, var showButtons: Boolean? = true) : BaseRecyclerAdapter<Challenge, ExploreChallengeAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.challenge_explore_view_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Load the challenge image with a fallback image
        holder.challengeImage.load(currentItem.imageLink) {
            transformations(RoundedCornersTransformation(20f))
        }

        // Set the challenge type
        when (currentItem.challengeType) {
            ChallengeType.EXERCISE ->
                holder.challengeType.text = context.getString(R.string.challenge_type_exercise)
            ChallengeType.DIET ->
                holder.challengeType.text = context.getString(R.string.challenge_type_diet)
            ChallengeType.MIND ->
                holder.challengeType.text = context.getString(R.string.challenge_type_mind)
        }

        if(showButtons == true){
            holder.challengeButton.visibility = View.VISIBLE

            holder.challengeButton.setOnClickListener { view ->
                val intent = Intent(view.context, ChallengeActivity::class.java)
                intent.putExtra("CHALLENGE_ID", currentItem.challengeId)
                view.context.startActivity(intent)
            }
        }

        holder.challengeTitle.text = currentItem.title
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ChallengeActivity::class.java)
            intent.putExtra("CHALLENGE_ID", currentItem.challengeId)
            view.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val challengeImage: ImageView = itemView.findViewById(R.id.challenge_image)
        internal val challengeType: TextView = itemView.findViewById(R.id.challenge_type)
        internal val challengeTitle: TextView = itemView.findViewById(R.id.challenge_title)
        internal val challengeButton: Button = itemView.findViewById(R.id.challenge_view_challenge)

    }
}