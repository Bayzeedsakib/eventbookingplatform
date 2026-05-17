package com.example.eventbookingplatform.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.TicketType
import com.example.eventbookingplatform.databinding.ItemTicketTypeOptionBinding

class TicketTypeAdapter(
    private val onTicketTypeSelected: (TicketType) -> Unit,
) : ListAdapter<TicketTypeRow, TicketTypeAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTicketTypeOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTicketTypeOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: TicketTypeRow) {
            val context = binding.root.context
            binding.textName.text = row.ticketType.name
            binding.textPrice.text =
                context.getString(R.string.booking_ticket_price, row.ticketType.price)
            binding.textAvailable.text =
                if (row.ticketType.available > 0) {
                    context.getString(R.string.booking_tickets_left, row.ticketType.available)
                } else {
                    context.getString(R.string.booking_sold_out)
                }
            binding.textAvailable.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (row.ticketType.available > 0) R.color.text_secondary else R.color.error,
                ),
            )

            val backgroundRes =
                if (row.isSelected) R.drawable.bg_ticket_type_selected else R.drawable.bg_ticket_type_default
            binding.cardTicketType.setBackgroundResource(backgroundRes)
            binding.radioSelected.isChecked = row.isSelected

            val enabled = row.ticketType.available > 0
            binding.root.isEnabled = enabled
            binding.root.alpha = if (enabled) 1f else 0.5f
            binding.root.setOnClickListener {
                if (enabled) onTicketTypeSelected(row.ticketType)
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<TicketTypeRow>() {
        override fun areItemsTheSame(oldItem: TicketTypeRow, newItem: TicketTypeRow): Boolean =
            oldItem.ticketType.id == newItem.ticketType.id

        override fun areContentsTheSame(oldItem: TicketTypeRow, newItem: TicketTypeRow): Boolean =
            oldItem == newItem
    }
}

data class TicketTypeRow(
    val ticketType: TicketType,
    val isSelected: Boolean,
)
