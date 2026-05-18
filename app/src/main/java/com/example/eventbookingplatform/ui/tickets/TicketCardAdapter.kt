package com.example.eventbookingplatform.ui.tickets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.databinding.ItemTicketCardBinding
import com.example.eventbookingplatform.ui.events.EventDisplayFormat
import java.util.Locale

class TicketCardAdapter(
    private val onShowQr: (TicketDisplayItem) -> Unit,
) : ListAdapter<TicketDisplayItem, TicketCardAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTicketCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onShowQr)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemTicketCardBinding,
        private val onShowQr: (TicketDisplayItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketDisplayItem) {
            val context = binding.root.context
            val booking = item.booking

            binding.headerAccent.setBackgroundResource(
                EventDisplayFormat.headerBackgroundRes(item.category),
            )
            binding.textTitle.text = booking.eventTitle

            val dateText = item.formattedEventDate()
            binding.textDate.isVisible = dateText.isNotBlank()
            binding.textDate.text = dateText

            val location = item.locationLabel
            binding.textLocation.isVisible = location.isNotBlank()
            binding.textLocation.text = location

            binding.textTicketLine.text =
                context.getString(
                    com.example.eventbookingplatform.R.string.tickets_line,
                    booking.quantity,
                    booking.ticketTypeName,
                )
            binding.textTotal.text =
                context.getString(
                    com.example.eventbookingplatform.R.string.tickets_total,
                    booking.totalPrice,
                )
            binding.textBookingId.text =
                context.getString(
                    com.example.eventbookingplatform.R.string.tickets_booking_id,
                    booking.id,
                )

            binding.buttonShowQr.setOnClickListener { onShowQr(item) }
            binding.root.setOnClickListener { onShowQr(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TicketDisplayItem>() {
        override fun areItemsTheSame(oldItem: TicketDisplayItem, newItem: TicketDisplayItem): Boolean =
            oldItem.booking.id == newItem.booking.id

        override fun areContentsTheSame(oldItem: TicketDisplayItem, newItem: TicketDisplayItem): Boolean =
            oldItem == newItem
    }
}
