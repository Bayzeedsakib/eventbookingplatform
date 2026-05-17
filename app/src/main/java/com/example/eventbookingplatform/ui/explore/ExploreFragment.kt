package com.example.eventbookingplatform.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
        EventsListBinder.bindStatus(
            state = state,
            statusView = binding.textEventsStatus,
            loadingText = loading,
            emptyText = empty,
            errorText = error,
        )
        EventsListBinder.bindRecycler(
            state = state,
            recyclerView = binding.recyclerEvents,
            adapter = eventsAdapter,
        ) { it.events }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
