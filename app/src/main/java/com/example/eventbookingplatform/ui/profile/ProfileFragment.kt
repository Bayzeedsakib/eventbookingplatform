package com.example.eventbookingplatform.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.FragmentProfileBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    private var isRegisterMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAuthModeUi()
        setupListeners()
        observeAuth()
    }

    private fun setupListeners() {
        binding.buttonPrimaryAuth.setOnClickListener {
            authViewModel.clearError()
            val email = binding.editEmail.text?.toString().orEmpty()
            val password = binding.editPassword.text?.toString().orEmpty()
            if (isRegisterMode) {
                val name = binding.editName.text?.toString().orEmpty()
                authViewModel.register(name, email, password)
            } else {
                authViewModel.login(email, password)
            }
        }

        binding.buttonLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.textToggleAuthMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            authViewModel.clearError()
            binding.textAuthError.isVisible = false
            setupAuthModeUi()
        }
    }

    private fun setupAuthModeUi() {
        binding.inputNameLayout.isVisible = isRegisterMode
        binding.buttonPrimaryAuth.setText(
            if (isRegisterMode) R.string.action_register else R.string.action_login
        )
        binding.textToggleAuthMode.setText(
            if (isRegisterMode) R.string.profile_switch_to_login else R.string.profile_switch_to_register
        )
    }

    private fun observeAuth() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            val signedIn = state is AuthUiState.SignedIn
            binding.layoutSignedIn.isVisible = signedIn
            binding.layoutSignedOut.isVisible = !signedIn && state !is AuthUiState.Loading

            binding.textProfileSubtitle.setText(
                if (signedIn) R.string.profile_subtitle_signed_in
                else R.string.profile_subtitle_guest
            )

            if (state is AuthUiState.SignedIn) {
                val profile = state.profile
                binding.textUserName.text = profile.name
                binding.textUserEmail.text = profile.email
                binding.textUserRole.setText(R.string.role_attendee_label)
                binding.textAvatarInitial.text =
                    profile.greetingName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressAuth.isVisible = loading
            binding.buttonPrimaryAuth.isEnabled = !loading
            binding.buttonLogout.isEnabled = !loading
            binding.textToggleAuthMode.isEnabled = !loading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.textAuthError.isVisible = !message.isNullOrBlank()
            binding.textAuthError.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
