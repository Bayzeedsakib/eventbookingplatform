package com.example.eventbookingplatform.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.FragmentHomeBinding
import com.example.eventbookingplatform.ui.auth.AuthUiState
import com.example.eventbookingplatform.ui.auth.AuthViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            val displayName = when (state) {
                is AuthUiState.SignedIn -> state.profile.greetingName
                else -> getString(R.string.guest_name)
            }
            binding.textGreeting.text = getString(R.string.home_greeting, displayName)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
