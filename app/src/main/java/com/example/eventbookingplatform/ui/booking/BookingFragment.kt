package com.example.eventbookingplatform.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.MainActivity
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.FragmentBookingBinding
import com.google.android.material.snackbar.Snackbar

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by viewModels()

    private val ticketTypeAdapter =
        TicketTypeAdapter { ticketType ->
            viewModel.selectTicketType(ticketType.id)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()

        binding.toolbar.setNavigationOnClickListener {
            handleBack()
        }

        binding.recyclerTicketTypes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketTypeAdapter
        }

        binding.buttonDecrease.setOnClickListener { viewModel.decrementQuantity() }
        binding.buttonIncrease.setOnClickListener { viewModel.incrementQuantity() }

        binding.buttonPrimary.setOnClickListener {
            when (val state = viewModel.uiState.value) {
                is BookingUiState.SelectTickets -> viewModel.continueToPayment()
                is BookingUiState.Payment -> viewModel.confirmPayment()
                is BookingUiState.Success -> {
                    findNavController().popBackStack(R.id.eventDetailFragment, true)
                    (requireActivity() as MainActivity).selectBottomNavDestination(R.id.ticketsFragment)
                }
                else -> Unit
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }

        viewModel.userMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.clearUserMessage()
            }
        }

        viewModel.loadEvent(eventId)
    }

    private fun handleBack() {
        when (viewModel.uiState.value) {
            is BookingUiState.Payment -> viewModel.backToSelectTickets()
            is BookingUiState.Success -> {
                findNavController().popBackStack(R.id.eventDetailFragment, true)
            }
            else -> findNavController().navigateUp()
        }
    }

    private fun renderState(state: BookingUiState) {
        binding.textStatus.isVisible =
            state is BookingUiState.Loading ||
                state is BookingUiState.NotFound ||
                state is BookingUiState.Error
        binding.scrollContent.isVisible =
            state !is BookingUiState.Loading &&
                state !is BookingUiState.NotFound
        binding.layoutBottomActions.isVisible =
            state is BookingUiState.SelectTickets ||
                state is BookingUiState.Payment ||
                state is BookingUiState.Processing ||
                state is BookingUiState.Success

        binding.progressProcessing.isVisible = state is BookingUiState.Processing
        binding.buttonPrimary.isEnabled = state !is BookingUiState.Processing

        when (state) {
            BookingUiState.Loading -> {
                binding.textStatus.text = getString(R.string.booking_loading)
                binding.layoutSelectTickets.isVisible = false
                binding.cardPayment.isVisible = false
                binding.cardSuccess.isVisible = false
            }
            BookingUiState.NotFound -> {
                binding.textStatus.text = getString(R.string.booking_not_found)
                binding.layoutSelectTickets.isVisible = false
                binding.cardPayment.isVisible = false
                binding.cardSuccess.isVisible = false
                binding.layoutBottomActions.isVisible = false
            }
            is BookingUiState.Error -> {
                binding.textStatus.text = state.message
                binding.layoutSelectTickets.isVisible = false
                binding.cardPayment.isVisible = false
                binding.cardSuccess.isVisible = false
                binding.layoutBottomActions.isVisible = false
            }
            is BookingUiState.SelectTickets -> bindSelectTickets(state)
            is BookingUiState.Payment -> bindPayment(state)
            BookingUiState.Processing -> bindProcessing()
            is BookingUiState.Success -> bindSuccess(state)
        }
    }

    private fun bindSelectTickets(state: BookingUiState.SelectTickets) {
        binding.textStatus.isVisible = false
        binding.layoutSelectTickets.isVisible = true
        binding.cardPayment.isVisible = false
        binding.cardSuccess.isVisible = false

        binding.textEventTitle.text = state.event.title
        binding.textEventDate.text = state.event.formattedStartDate()

        ticketTypeAdapter.submitList(
            state.event.ticketTypes.map { type ->
                TicketTypeRow(
                    ticketType = type,
                    isSelected = type.id == state.selectedTicketTypeId,
                )
            },
        )

        binding.textQuantity.text = state.quantity.toString()
        binding.buttonDecrease.isEnabled = state.quantity > 1
        binding.buttonIncrease.isEnabled =
            state.selectedTicketType != null && state.quantity < state.maxQuantity

        val ticketName = state.selectedTicketType?.name.orEmpty()
        binding.textCapacityHint.text =
            if (state.maxQuantity > 0) {
                getString(R.string.booking_max_quantity, state.maxQuantity, ticketName)
            } else {
                getString(R.string.booking_sold_out)
            }

        binding.textTotalBar.isVisible = true
        binding.textTotalBar.text = getString(R.string.booking_total, state.totalPrice)
        binding.buttonPrimary.text = getString(R.string.booking_continue_payment)
        binding.buttonPrimary.isEnabled = state.canContinue
    }

    private fun bindPayment(state: BookingUiState.Payment) {
        binding.textStatus.isVisible = false
        binding.layoutSelectTickets.isVisible = false
        binding.cardPayment.isVisible = true
        binding.cardSuccess.isVisible = false

        binding.textEventTitle.text = state.event.title
        binding.textEventDate.text = state.event.formattedStartDate()

        binding.textPaymentLine.text =
            getString(
                R.string.booking_payment_line,
                state.quantity,
                state.ticketType.name,
                state.ticketType.price,
            )
        binding.textPaymentTotal.text = getString(R.string.booking_total, state.totalPrice)

        binding.textTotalBar.isVisible = true
        binding.textTotalBar.text = getString(R.string.booking_total, state.totalPrice)
        binding.buttonPrimary.text = getString(R.string.booking_pay_now)
        binding.buttonPrimary.isEnabled = true
    }

    private fun bindProcessing() {
        binding.buttonPrimary.isEnabled = false
    }

    private fun bindSuccess(state: BookingUiState.Success) {
        binding.layoutSelectTickets.isVisible = false
        binding.cardPayment.isVisible = false
        binding.cardSuccess.isVisible = true
        binding.textSuccessMessage.text =
            getString(R.string.booking_success_message, state.eventTitle)
        binding.textTotalBar.isVisible = false
        binding.buttonPrimary.text = getString(R.string.booking_view_tickets)
        binding.buttonPrimary.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EVENT_ID = "eventId"
    }
}
