package com.example.eventbookingplatform.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.MainActivity
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.FragmentHomeBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.example.eventbookingplatform.ui.detail.navigateToEventDetail
import com.example.eventbookingplatform.ui.events.EventDisplayFormat
import com.example.eventbookingplatform.ui.events.EventsUiState
import com.example.eventbookingplatform.ui.events.EventsViewModel
import com.example.eventbookingplatform.ui.events.bindFeaturedCardImage

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val categoryAdapter = CategoryChipAdapter { chip ->
        homeViewModel.setSelectedCategory(chip.id)
    }
    private val upcomingAdapter =
        UpcomingEventAdapter { event ->
            navigateToEventDetail(event)
        }

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

        val chipsWithLabels =
            listOf(
                CategoryChipItem(HomeFilters.CATEGORY_ALL, getString(R.string.home_category_all)),
                CategoryChipItem(Event.CATEGORY_MUSIC, getString(R.string.home_category_music)),
                CategoryChipItem(Event.CATEGORY_TECH, getString(R.string.home_category_tech)),
                CategoryChipItem(Event.CATEGORY_ART, getString(R.string.home_category_art)),
            )

        binding.recyclerCategories.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
        }
        categoryAdapter.submitChips(chipsWithLabels, HomeFilters.CATEGORY_ALL)

        binding.recyclerUpcoming.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingAdapter
            isNestedScrollingEnabled = false
        }

        binding.editSearch.addTextChangedListener { editable ->
            homeViewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        binding.textSeeAll.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavDestination(R.id.exploreFragment)
        }

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            val displayName =
                when (state) {
                    is AuthUiState.SignedIn -> state.profile.greetingName
                    else -> getString(R.string.guest_name)
                }
            binding.textGreeting.text = getString(R.string.home_greeting, displayName)
        }

        eventsViewModel.eventsState.observe(viewLifecycleOwner) { eventsState ->
            renderHome(eventsState)
        }

        homeViewModel.searchQuery.observe(viewLifecycleOwner) {
            eventsViewModel.eventsState.value?.let { renderHome(it) }
        }

        homeViewModel.selectedCategory.observe(viewLifecycleOwner) { categoryId ->
            categoryAdapter.submitChips(chipsWithLabels, categoryId)
            eventsViewModel.eventsState.value?.let { renderHome(it) }
        }
    }

    private fun renderHome(eventsState: EventsUiState) {
        val displayState =
            homeViewModel.buildDisplayState(
                eventsState = eventsState,
                searchQuery = homeViewModel.searchQuery.value.orEmpty(),
                selectedCategory = homeViewModel.selectedCategory.value ?: HomeFilters.CATEGORY_ALL,
            )

        bindStatus(displayState, eventsState)
        bindFeaturedCard(displayState.featuredEvent)
        bindUpcomingList(displayState)
    }

    private fun bindStatus(displayState: HomeDisplayState, eventsState: EventsUiState) {
        binding.textEventsStatus.visibility = View.GONE
        binding.recyclerUpcoming.visibility = View.GONE
        binding.featuredCard.root.visibility = View.GONE

        when {
            displayState.isLoading -> {
                binding.textEventsStatus.visibility = View.VISIBLE
                binding.textEventsStatus.text = getString(R.string.events_loading)
            }
            displayState.errorMessage != null -> {
                binding.textEventsStatus.visibility = View.VISIBLE
                binding.textEventsStatus.text = displayState.errorMessage
            }
            eventsState is EventsUiState.Success && eventsState.events.isEmpty() -> {
                binding.textEventsStatus.visibility = View.VISIBLE
                binding.textEventsStatus.text = getString(R.string.events_empty)
            }
            displayState.showNoMatches -> {
                binding.textEventsStatus.visibility = View.VISIBLE
                binding.textEventsStatus.text = getString(R.string.events_no_matches)
            }
            else -> {
                binding.recyclerUpcoming.visibility =
                    if (displayState.upcomingEvents.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun bindFeaturedCard(event: Event?) {
        if (event == null) {
            binding.featuredCard.root.visibility = View.GONE
            return
        }

        bindFeaturedCardImage(
            imageView = binding.featuredCard.imageFeatured,
            scrimView = binding.featuredCard.imageFeaturedScrim,
            contentView = binding.featuredCard.layoutFeaturedContent,
            imageUrl = event.imageUrl,
        )
        binding.featuredCard.textFeaturedTitle.text = event.title
        binding.featuredCard.textFeaturedDate.text = EventDisplayFormat.shortDate(event)
        binding.featuredCard.textFeaturedLocation.text = event.locationLabel
        binding.featuredCard.buttonGetTickets.setOnClickListener {
            navigateToEventDetail(event)
        }
        binding.featuredCard.root.setOnClickListener {
            navigateToEventDetail(event)
        }
        binding.featuredCard.root.visibility = View.VISIBLE
    }

    private fun bindUpcomingList(displayState: HomeDisplayState) {
        upcomingAdapter.submitList(displayState.upcomingEvents)
        if (displayState.upcomingEvents.isNotEmpty() && !displayState.isLoading) {
            binding.recyclerUpcoming.visibility = View.VISIBLE
        }
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
