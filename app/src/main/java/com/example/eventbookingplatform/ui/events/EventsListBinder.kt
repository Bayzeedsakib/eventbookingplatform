package com.example.eventbookingplatform.ui.events

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
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
        progressView: View? = null,
        retryButton: View? = null,
        onRetry: (() -> Unit)? = null,
    ) {
        retryButton?.setOnClickListener { onRetry?.invoke() }
        when (state) {
            is EventsUiState.Loading -> {
                progressView?.isVisible = true
                statusView.isVisible = true
                statusView.text = loadingText
                retryButton?.isVisible = false
            }
            is EventsUiState.Error -> {
                progressView?.isVisible = false
                statusView.isVisible = true
                statusView.text = state.message.ifBlank { errorText }
                retryButton?.isVisible = onRetry != null
            }
            is EventsUiState.Success -> {
                progressView?.isVisible = false
                retryButton?.isVisible = false
                if (state.events.isEmpty()) {
                    statusView.isVisible = true
                    statusView.text = emptyText
                } else {
                    statusView.isVisible = false
                }
            }
        }
    }

    fun bindListStatePanel(
        state: EventsUiState,
        panel: View,
        messageView: TextView,
        progressView: View,
        retryButton: View,
        loadingText: String,
        emptyText: String,
        errorText: String,
        onRetry: () -> Unit,
    ) {
        val showPanel =
            when (state) {
                is EventsUiState.Loading,
                is EventsUiState.Error,
                -> true
                is EventsUiState.Success -> state.events.isEmpty()
            }
        panel.isVisible = showPanel
        if (!showPanel) return

        bindStatus(
            state = state,
            statusView = messageView,
            loadingText = loadingText,
            emptyText = emptyText,
            errorText = errorText,
            progressView = progressView,
            retryButton = retryButton,
            onRetry = onRetry,
        )
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
