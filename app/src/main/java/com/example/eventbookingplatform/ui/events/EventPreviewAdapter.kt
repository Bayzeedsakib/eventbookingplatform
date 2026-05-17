package com.example.eventbookingplatform.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.ItemEventPreviewBinding
import java.util.Locale

class EventPreviewAdapter(
    private val onEventClick: (Event) -> Unit,
) : ListAdapter<Event, EventPreviewAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEventPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEventClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemEventPreviewBinding,
        private val onEventClick: (Event) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context
            binding.textTitle.text = event.title
            binding.textDate.text = event.formattedStartDate()
            binding.textLocation.text = event.locationLabel
            binding.textFeatured.visibility =
                if (event.featured) View.VISIBLE else View.GONE

            binding.textCategory.text =
                EventDisplayFormat.categoryLabel(event.category)
                    .uppercase(Locale.getDefault())
            EventDisplayFormat.applyCategoryChipStyle(binding.textCategory, event.category)
            binding.textPrice.text = EventDisplayFormat.priceLabel(context, event)
            binding.root.setOnClickListener { onEventClick(event) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
