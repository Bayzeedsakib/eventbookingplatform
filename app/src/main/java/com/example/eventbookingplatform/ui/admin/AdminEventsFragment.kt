package com.example.eventbookingplatform.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.FragmentAdminEventsBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.example.eventbookingplatform.ui.events.EventsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AdminEventsFragment : Fragment() {

    private var _binding: FragmentAdminEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminEventsViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by activityViewModels()

    private lateinit var adapter: AdminEventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAdminEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAdmin()) {
            Snackbar.make(binding.root, R.string.admin_access_denied, Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter =
            AdminEventListAdapter(
                onEditClick = { event -> navigateToEdit(event.id) },
                onDeleteClick = { event -> confirmDelete(event) },
            )
        binding.recyclerEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEvents.adapter = adapter

        binding.fabAddEvent.setOnClickListener { navigateToEdit(null) }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AdminEventsUiState.Loading -> showLoading()
                is AdminEventsUiState.Content -> showContent(state.events)
                is AdminEventsUiState.Error -> showMessage(state.message)
            }
        }

        viewModel.deleteError.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.clearDeleteError()
            }
        }

        viewModel.eventListChanged.observe(viewLifecycleOwner) {
            eventsViewModel.loadEvents()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdmin()) viewModel.loadEvents()
    }

    private fun isAdmin(): Boolean {
        val state = authViewModel.authState.value
        return state is AuthUiState.SignedIn && state.profile.isAdmin
    }

    private fun navigateToEdit(eventId: String?) {
        val bundle =
            Bundle().apply {
                putString(ARG_EVENT_ID, eventId.orEmpty())
            }
        findNavController().navigate(R.id.action_adminEventsFragment_to_adminEventEditFragment, bundle)
    }

    private fun confirmDelete(event: Event) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_delete_title)
            .setMessage(getString(R.string.admin_delete_message, event.title))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.admin_action_delete) { _, _ ->
                viewModel.deleteEvent(event.id)
            }
            .show()
    }

    private fun showLoading() {
        binding.progressLoading.isVisible = true
        binding.textStatus.isVisible = false
        binding.recyclerEvents.isVisible = false
        binding.fabAddEvent.isVisible = false
    }

    private fun showContent(events: List<Event>) {
        binding.progressLoading.isVisible = false
        binding.fabAddEvent.isVisible = true
        binding.recyclerEvents.isVisible = true
        adapter.submitList(events)
        if (events.isEmpty()) {
            binding.textStatus.isVisible = true
            binding.textStatus.setText(R.string.admin_events_empty)
        } else {
            binding.textStatus.isVisible = false
        }
    }

    private fun showMessage(message: String) {
        binding.progressLoading.isVisible = false
        binding.recyclerEvents.isVisible = false
        binding.fabAddEvent.isVisible = true
        binding.textStatus.isVisible = true
        binding.textStatus.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EVENT_ID = "eventId"
    }
}
