package com.example.eventbookingplatform.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.ItemEventUpcomingBinding
import com.example.eventbookingplatform.ui.events.EventDisplayFormat
import java.util.Locale

class UpcomingEventAdapter :
    ListAdapter<Event, UpcomingEventAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEventUpcomingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemEventUpcomingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context
            binding.headerGradient.setBackgroundResource(
                EventDisplayFormat.headerBackgroundRes(event.category),
            )
            binding.textCategory.text =
                EventDisplayFormat.categoryLabel(event.category).uppercase(Locale.getDefault())
            EventDisplayFormat.applyCategoryChipStyle(binding.textCategory, event.category)
            binding.textTitle.text = event.title
            binding.textDate.text = EventDisplayFormat.shortDate(event)
            binding.textLocation.text = event.venue.ifBlank { event.city }
            binding.textPrice.text = EventDisplayFormat.priceLabel(context, event)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
