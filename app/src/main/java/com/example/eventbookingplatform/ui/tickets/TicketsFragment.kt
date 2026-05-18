package com.example.eventbookingplatform.ui.tickets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.MainActivity
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.DialogTicketQrBinding
import com.example.eventbookingplatform.databinding.FragmentTicketsBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class TicketsFragment : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val ticketsViewModel: TicketsViewModel by viewModels()

    private lateinit var ticketAdapter: TicketCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ticketAdapter =
            TicketCardAdapter { item ->
                showQrDialog(item)
            }

        binding.recyclerTickets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketAdapter
            setHasFixedSize(true)
        }

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.tickets_tab_upcoming),
            true,
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.tickets_tab_past),
        )
        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    ticketsViewModel.setSelectedTab(
                        if (tab.position == 0) TicketsTab.UPCOMING else TicketsTab.PAST,
                    )
                    refreshTicketList()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            },
        )

        binding.buttonSignIn.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavDestination(R.id.profileFragment)
        }

        binding.buttonRetry.setOnClickListener {
            ticketsViewModel.loadTickets()
        }

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            val signedIn = state is AuthUiState.SignedIn
            binding.layoutGuest.isVisible = !signedIn && state !is AuthUiState.Loading
            binding.layoutContent.isVisible = signedIn
            if (signedIn) {
                ticketsViewModel.loadTickets()
            } else {
                ticketAdapter.submitList(emptyList())
            }
        }

        ticketsViewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (authViewModel.authState.value !is AuthUiState.SignedIn) return@observe
            renderTicketsState(state)
        }

        ticketsViewModel.selectedTab.observe(viewLifecycleOwner) {
            if (authViewModel.authState.value is AuthUiState.SignedIn) {
                refreshTicketList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (authViewModel.authState.value is AuthUiState.SignedIn) {
            ticketsViewModel.loadTickets()
        }
    }

    private fun renderTicketsState(state: TicketsUiState) {
        binding.progressLoading.isVisible = state is TicketsUiState.Loading
        binding.buttonRetry.isVisible = state is TicketsUiState.Error
        binding.recyclerTickets.isVisible = state is TicketsUiState.Content

        when (state) {
            TicketsUiState.Loading -> {
                binding.textStatus.isVisible = false
                binding.recyclerTickets.isVisible = false
            }
            is TicketsUiState.Error -> {
                binding.textStatus.isVisible = true
                binding.textStatus.text =
                    state.message.ifBlank { getString(R.string.tickets_error) }
                binding.recyclerTickets.isVisible = false
            }
            is TicketsUiState.Content -> refreshTicketList(state)
        }
    }

    private fun refreshTicketList(content: TicketsUiState.Content? = null) {
        val state = content ?: ticketsViewModel.uiState.value as? TicketsUiState.Content ?: return
        val list = currentListForTab(state)
        ticketAdapter.submitList(list)
        val empty = list.isEmpty() && ticketsViewModel.uiState.value !is TicketsUiState.Loading
        binding.textStatus.isVisible = empty
        binding.recyclerTickets.isVisible = list.isNotEmpty()
        if (empty) {
            binding.textStatus.text =
                getString(
                    if (ticketsViewModel.selectedTab.value == TicketsTab.PAST) {
                        R.string.tickets_empty_past
                    } else {
                        R.string.tickets_empty_upcoming
                    },
                )
        }
    }

    private fun currentListForTab(state: TicketsUiState.Content): List<TicketDisplayItem> =
        when (ticketsViewModel.selectedTab.value) {
            TicketsTab.PAST -> state.past
            else -> state.upcoming
        }

    private fun showQrDialog(item: TicketDisplayItem) {
        val booking = item.booking
        val dialogBinding = DialogTicketQrBinding.inflate(layoutInflater)
        dialogBinding.textEventTitle.text = booking.eventTitle
        dialogBinding.textTicketSummary.text =
            getString(R.string.tickets_line, booking.quantity, booking.ticketTypeName)
        dialogBinding.textBookingId.text =
            getString(R.string.tickets_qr_booking_id, booking.id)

        val qrSizePx = 512
        val payload = QrCodeGenerator.encodeBookingId(booking.id)
        dialogBinding.imageQr.setImageBitmap(
            QrCodeGenerator.createBitmap(payload, qrSizePx),
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tickets_qr_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
