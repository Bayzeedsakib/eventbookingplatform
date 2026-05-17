package com.example.eventbookingplatform.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eventbookingplatform.MainActivity
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.FragmentEventDetailBinding
import com.example.eventbookingplatform.ui.booking.BookingFragment
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.example.eventbookingplatform.ui.events.EventDisplayFormat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventDetailViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonBookNow.setOnClickListener {
            handleBookNow()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }

        viewModel.loadEvent(eventId)
    }

    private fun renderState(state: EventDetailUiState) {
        binding.textStatus.isVisible = state !is EventDetailUiState.Success
        binding.scrollContent.isVisible = state is EventDetailUiState.Success
        binding.buttonBookNow.isVisible = state is EventDetailUiState.Success

        when (state) {
            EventDetailUiState.Loading -> {
                binding.textStatus.text = getString(R.string.event_detail_loading)
            }
            EventDetailUiState.NotFound -> {
                binding.textStatus.text = getString(R.string.event_detail_not_found)
            }
            is EventDetailUiState.Error -> {
                binding.textStatus.text = state.message
            }
            is EventDetailUiState.Success -> bindEvent(state.event)
        }
    }

    private fun bindEvent(event: Event) {
        binding.toolbar.title = event.title
        binding.headerGradient.setBackgroundResource(
            EventDisplayFormat.headerBackgroundRes(event.category),
        )
        binding.imageEvent.isVisible = event.imageUrl.isNotBlank()

        binding.textCategory.text =
            EventDisplayFormat.categoryLabel(event.category).uppercase(Locale.getDefault())
        EventDisplayFormat.applyCategoryChipStyle(binding.textCategory, event.category)
        binding.textFeaturedBadge.isVisible = event.featured

        binding.textTitle.text = event.title
        binding.textDate.text = event.formattedStartDate()
        binding.textLocation.text = event.locationLabel
        binding.textCapacity.text = EventDisplayFormat.capacityLabel(requireContext(), event)
        binding.textDescription.text = event.description
    }

    private fun handleBookNow() {
        when (authViewModel.authState.value) {
            is AuthUiState.SignedIn -> {
                val eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()
                if (eventId.isNotBlank()) {
                    findNavController().navigate(
                        R.id.action_eventDetailFragment_to_bookingFragment,
                        androidx.core.os.bundleOf(BookingFragment.ARG_EVENT_ID to eventId),
                    )
                }
            }
            else -> showSignInPrompt()
        }
    }

    private fun showSignInPrompt() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.book_now_sign_in_title)
            .setMessage(R.string.book_now_sign_in_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.book_now_sign_in_action) { _, _ ->
                findNavController().navigateUp()
                (requireActivity() as MainActivity).selectBottomNavDestination(R.id.profileFragment)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EVENT_ID = "eventId"
    }
}
