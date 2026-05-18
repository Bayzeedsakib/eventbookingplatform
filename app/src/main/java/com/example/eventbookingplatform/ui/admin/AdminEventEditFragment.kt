package com.example.eventbookingplatform.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eventbookingplatform.ui.events.EventsViewModel
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.databinding.FragmentAdminEventEditBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminEventEditFragment : Fragment() {

    private var _binding: FragmentAdminEventEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminEventEditViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by activityViewModels()

    private var startAt: java.util.Date? = null
    private val startAtFormat = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAdminEventEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAdmin()) {
            Snackbar.make(binding.root, R.string.admin_access_denied, Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        val eventId = arguments?.getString(AdminEventsFragment.ARG_EVENT_ID).orEmpty()
        val isNew = eventId.isBlank()

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setTitle(
            if (isNew) R.string.admin_add_event_title else R.string.admin_edit_event_title,
        )

        setupSpinners()
        binding.buttonPickStart.setOnClickListener { showStartDateTimePicker() }
        binding.buttonSave.setOnClickListener { saveCurrentForm() }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AdminEventEditUiState.Loading -> showLoading()
                is AdminEventEditUiState.Editing -> showForm(state.form)
                is AdminEventEditUiState.Error -> showLoadError(state.message)
                AdminEventEditUiState.Saved -> {
                    eventsViewModel.loadEvents()
                    Snackbar.make(binding.root, R.string.admin_event_saved, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.saveError.observe(viewLifecycleOwner) { message ->
            binding.textSaveError.isVisible = !message.isNullOrBlank()
            binding.textSaveError.text = message
        }

        viewModel.load(eventId.takeIf { it.isNotBlank() })
    }

    private fun isAdmin(): Boolean {
        val state = authViewModel.authState.value
        return state is AuthUiState.SignedIn && state.profile.isAdmin
    }

    private fun setupSpinners() {
        val categories =
            listOf(
                Event.CATEGORY_MUSIC to getString(R.string.home_category_music),
                Event.CATEGORY_TECH to getString(R.string.home_category_tech),
                Event.CATEGORY_ART to getString(R.string.home_category_art),
            )
        binding.spinnerCategory.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories.map { it.second },
            )

        val statuses =
            listOf(
                Event.STATUS_DRAFT to getString(R.string.admin_status_draft),
                Event.STATUS_PUBLISHED to getString(R.string.admin_status_published),
                Event.STATUS_CANCELLED to getString(R.string.admin_status_cancelled),
            )
        binding.spinnerStatus.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                statuses.map { it.second },
            )
    }

    private fun categoryValues(): List<String> =
        listOf(Event.CATEGORY_MUSIC, Event.CATEGORY_TECH, Event.CATEGORY_ART)

    private fun statusValues(): List<String> =
        listOf(Event.STATUS_DRAFT, Event.STATUS_PUBLISHED, Event.STATUS_CANCELLED)

    private fun showLoading() {
        binding.layoutForm.isVisible = false
        binding.textLoadStatus.isVisible = true
        binding.textLoadStatus.setText(R.string.admin_loading_event)
    }

    private fun showLoadError(message: String) {
        binding.layoutForm.isVisible = false
        binding.textLoadStatus.isVisible = true
        binding.textLoadStatus.text = message
    }

    private fun showForm(form: AdminEventForm) {
        binding.textLoadStatus.isVisible = false
        binding.layoutForm.isVisible = true

        binding.editTitle.setText(form.title)
        binding.editDescription.setText(form.description)
        binding.editVenue.setText(form.venue)
        binding.editCity.setText(form.city)
        binding.editImageUrl.setText(form.imageUrl)
        binding.switchFeatured.isChecked = form.featured

        val categoryIndex = categoryValues().indexOf(form.category).coerceAtLeast(0)
        binding.spinnerCategory.setSelection(categoryIndex)

        val statusIndex = statusValues().indexOf(form.status).coerceAtLeast(0)
        binding.spinnerStatus.setSelection(statusIndex)

        startAt = form.startAt
        updateStartButtonLabel()

        binding.editGeneralPrice.setText(form.generalTicket.priceText)
        binding.editGeneralAvailable.setText(form.generalTicket.availableText)
        binding.editSecondName.setText(form.secondTicket.name)
        binding.editSecondPrice.setText(form.secondTicket.priceText)
        binding.editSecondAvailable.setText(form.secondTicket.availableText)
    }

    private fun updateStartButtonLabel() {
        val date = startAt
        binding.buttonPickStart.text =
            if (date != null) startAtFormat.format(date)
            else getString(R.string.admin_pick_start)
    }

    private fun showStartDateTimePicker() {
        val cal = Calendar.getInstance()
        startAt?.let { cal.time = it }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        startAt = cal.time
                        updateStartButtonLabel()
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false,
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun readForm(): AdminEventForm {
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val statusIndex = binding.spinnerStatus.selectedItemPosition
        return AdminEventForm(
            title = binding.editTitle.text?.toString().orEmpty(),
            description = binding.editDescription.text?.toString().orEmpty(),
            category = categoryValues().getOrElse(categoryIndex) { Event.CATEGORY_MUSIC },
            venue = binding.editVenue.text?.toString().orEmpty(),
            city = binding.editCity.text?.toString().orEmpty(),
            startAt = startAt,
            imageUrl = binding.editImageUrl.text?.toString().orEmpty(),
            featured = binding.switchFeatured.isChecked,
            status = statusValues().getOrElse(statusIndex) { Event.STATUS_DRAFT },
            generalTicket =
                AdminTicketTypeForm(
                    name = "General",
                    priceText = binding.editGeneralPrice.text?.toString().orEmpty(),
                    availableText = binding.editGeneralAvailable.text?.toString().orEmpty(),
                ),
            secondTicket =
                AdminTicketTypeForm(
                    name = binding.editSecondName.text?.toString().orEmpty(),
                    priceText = binding.editSecondPrice.text?.toString().orEmpty(),
                    availableText = binding.editSecondAvailable.text?.toString().orEmpty(),
                ),
        )
    }

    private fun saveCurrentForm() {
        binding.textSaveError.isVisible = false
        viewModel.save(readForm())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
