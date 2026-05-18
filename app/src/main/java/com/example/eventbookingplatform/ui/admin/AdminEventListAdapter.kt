package com.example.eventbookingplatform.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.ItemAdminEventBinding
import java.util.Locale

class AdminEventListAdapter(
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit,
) : ListAdapter<Event, AdminEventListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAdminEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAdminEventBinding,
        private val onEditClick: (Event) -> Unit,
        private val onDeleteClick: (Event) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context
            binding.textTitle.text = event.title
            binding.textMeta.text = event.formattedStartDate()
            binding.textLocation.text = event.locationLabel
            binding.textStatus.text = statusLabel(context, event.status)
            binding.textFeatured.isVisible = event.featured
            binding.root.setOnClickListener { onEditClick(event) }
            binding.buttonEdit.setOnClickListener { onEditClick(event) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(event) }
        }

        private fun statusLabel(context: android.content.Context, status: String): String =
            when (status) {
                Event.STATUS_PUBLISHED ->
                    context.getString(R.string.admin_status_published).uppercase(Locale.getDefault())
                Event.STATUS_DRAFT ->
                    context.getString(R.string.admin_status_draft).uppercase(Locale.getDefault())
                Event.STATUS_CANCELLED ->
                    context.getString(R.string.admin_status_cancelled).uppercase(Locale.getDefault())
                else -> status.uppercase(Locale.getDefault())
            }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
