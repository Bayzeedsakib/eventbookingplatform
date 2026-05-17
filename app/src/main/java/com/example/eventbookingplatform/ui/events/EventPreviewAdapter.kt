package com.example.eventbookingplatform.ui.events

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.ItemEventPreviewBinding
import java.util.Locale

class EventPreviewAdapter :
    ListAdapter<Event, EventPreviewAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEventPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemEventPreviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context
            binding.textTitle.text = event.title
            binding.textDate.text = event.formattedStartDate()
            binding.textLocation.text = event.locationLabel
            binding.textFeatured.visibility =
                if (event.featured) View.VISIBLE else View.GONE

            val categoryLabel = event.category.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            binding.textCategory.text = categoryLabel.uppercase(Locale.getDefault())
            applyCategoryStyle(event.category)

            val minPrice = event.minPrice
            binding.textPrice.text =
                if (minPrice != null) {
                    context.getString(R.string.event_price_from, minPrice)
                } else {
                    context.getString(R.string.event_price_tba)
                }
        }

        private fun applyCategoryStyle(category: String) {
            val context = binding.root.context
            val (bgRes, textRes) =
                when (category) {
                    Event.CATEGORY_TECH -> R.color.tag_tech_bg to R.color.tag_tech_text
                    Event.CATEGORY_ART -> R.color.tag_art_bg to R.color.tag_art_text
                    Event.CATEGORY_MUSIC -> R.color.tag_music_bg to R.color.tag_music_text
                    else -> R.color.bg_elevated to R.color.text_secondary
                }
            binding.textCategory.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, bgRes))
            binding.textCategory.setTextColor(ContextCompat.getColor(context, textRes))
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
