package com.example.eventbookingplatform.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.FragmentExploreBinding
import com.example.eventbookingplatform.ui.detail.navigateToEventDetail
import com.example.eventbookingplatform.ui.events.EventPreviewAdapter
import com.example.eventbookingplatform.ui.events.EventsListBinder
import com.example.eventbookingplatform.ui.events.EventsUiState
import com.example.eventbookingplatform.ui.events.EventsViewModel

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val eventsViewModel: EventsViewModel by activityViewModels()
    private val eventsAdapter =
        EventPreviewAdapter { event ->
            navigateToEventDetail(event)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
            isNestedScrollingEnabled = false
        }

        eventsViewModel.eventsState.observe(viewLifecycleOwner) { state ->
            renderEvents(state)
        }
    }

    private fun renderEvents(state: EventsUiState) {
        val (loading, empty, error) = EventsListBinder.contextStrings(binding.root)
        EventsListBinder.bindListStatePanel(
            state = state,
            panel = binding.listState.root,
            messageView = binding.listState.textListStateMessage,
            progressView = binding.listState.progressListState,
            retryButton = binding.listState.buttonListStateRetry,
            loadingText = loading,
            emptyText = empty,
            errorText = error,
            onRetry = { eventsViewModel.loadEvents() },
        )
        EventsListBinder.bindRecycler(
            state = state,
            recyclerView = binding.recyclerEvents,
            adapter = eventsAdapter,
        ) { it.events }

        binding.textSubtitle.text =
            when (state) {
                is EventsUiState.Success ->
                    if (state.events.isEmpty()) {
                        getString(R.string.explore_subtitle_empty)
                    } else {
                        getString(R.string.explore_subtitle_count, state.events.size)
                    }
                else -> getString(R.string.explore_subtitle)
            }

        binding.recyclerEvents.isVisible =
            state is EventsUiState.Success && state.events.isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        eventsViewModel.loadEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
