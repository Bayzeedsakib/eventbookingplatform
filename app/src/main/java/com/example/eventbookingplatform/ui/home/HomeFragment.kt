package com.example.eventbookingplatform.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.FragmentHomeBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.example.eventbookingplatform.ui.events.EventPreviewAdapter
import com.example.eventbookingplatform.ui.events.EventsListBinder
import com.example.eventbookingplatform.ui.events.EventsUiState
import com.example.eventbookingplatform.ui.events.EventsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by activityViewModels()

    private val featuredAdapter = EventPreviewAdapter()
    private val allEventsAdapter = EventPreviewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerFeatured.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = featuredAdapter
            isNestedScrollingEnabled = false
        }
        binding.recyclerEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allEventsAdapter
            isNestedScrollingEnabled = false
        }

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            val displayName =
                when (state) {
                    is AuthUiState.SignedIn -> state.profile.greetingName
                    else -> getString(R.string.guest_name)
                }
            binding.textGreeting.text = getString(R.string.home_greeting, displayName)
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
            adapter = allEventsAdapter,
        ) { it.events }

        val featured =
            when (state) {
                is EventsUiState.Success -> state.featuredEvents
                else -> emptyList()
            }
        val showFeatured = featured.isNotEmpty()
        binding.textFeaturedHeading.visibility =
            if (showFeatured) View.VISIBLE else View.GONE
        binding.recyclerFeatured.visibility = if (showFeatured) View.VISIBLE else View.GONE
        featuredAdapter.submitList(featured)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
