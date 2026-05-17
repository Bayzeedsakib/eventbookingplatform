package com.example.eventbookingplatform.ui.detail

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event

fun Fragment.navigateToEventDetail(event: Event) {
    navigateToEventDetail(event.id)
}

fun Fragment.navigateToEventDetail(eventId: String) {
    findNavController().navigate(
        currentDestinationEventDetailAction() ?: return,
        bundleOf(EventDetailFragment.ARG_EVENT_ID to eventId),
    )
}

@IdRes
private fun Fragment.currentDestinationEventDetailAction(): Int? =
    when (findNavController().currentDestination?.id) {
        R.id.homeFragment -> R.id.action_homeFragment_to_eventDetailFragment
        R.id.exploreFragment -> R.id.action_exploreFragment_to_eventDetailFragment
        else -> null
    }
