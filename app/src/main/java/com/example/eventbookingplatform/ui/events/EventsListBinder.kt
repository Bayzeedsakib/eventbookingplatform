package com.example.eventbookingplatform.ui.events

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event

object EventsListBinder {

    fun bindStatus(
        state: EventsUiState,
        statusView: TextView,
        loadingText: String,
        emptyText: String,
        errorText: String,
    ) {
        when (state) {
            is EventsUiState.Loading -> {
                statusView.visibility = View.VISIBLE
                statusView.text = loadingText
            }
            is EventsUiState.Error -> {
                statusView.visibility = View.VISIBLE
                statusView.text = state.message.ifBlank { errorText }
            }
            is EventsUiState.Success -> {
                if (state.events.isEmpty()) {
                    statusView.visibility = View.VISIBLE
                    statusView.text = emptyText
                } else {
                    statusView.visibility = View.GONE
                }
            }
        }
    }

    fun bindRecycler(
        state: EventsUiState,
        recyclerView: RecyclerView,
        adapter: EventPreviewAdapter,
        eventsSelector: (EventsUiState.Success) -> List<Event>,
    ) {
        val events =
            when (state) {
                is EventsUiState.Success -> eventsSelector(state)
                else -> emptyList()
            }
        recyclerView.visibility =
            if (events.isNotEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(events)
    }

    fun contextStrings(view: View): Triple<String, String, String> {
        val context = view.context
        return Triple(
            context.getString(R.string.events_loading),
            context.getString(R.string.events_empty),
            context.getString(R.string.events_error),
        )
    }
}
