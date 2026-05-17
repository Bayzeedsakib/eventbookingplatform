package com.example.eventbookingplatform.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.ui.events.EventsUiState
import java.util.Locale

object HomeFilters {
    const val CATEGORY_ALL = "all"
}

data class HomeDisplayState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val featuredEvent: Event? = null,
    val upcomingEvents: List<Event> = emptyList(),
    val showNoMatches: Boolean = false,
)

class HomeViewModel : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedCategory = MutableLiveData(HomeFilters.CATEGORY_ALL)
    val selectedCategory: LiveData<String> = _selectedCategory

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }

    fun buildDisplayState(
        eventsState: EventsUiState,
        searchQuery: String,
        selectedCategory: String,
    ): HomeDisplayState {
        return when (eventsState) {
            is EventsUiState.Loading ->
                HomeDisplayState(isLoading = true)
            is EventsUiState.Error ->
                HomeDisplayState(
                    isLoading = false,
                    errorMessage = eventsState.message,
                )
            is EventsUiState.Success -> {
                val filtered =
                    eventsState.events
                        .filterByCategory(selectedCategory)
                        .filterBySearch(searchQuery)
                val featured =
                    filtered.firstOrNull { it.featured } ?: filtered.firstOrNull()
                val upcoming =
                    if (featured != null) {
                        filtered.filter { it.id != featured.id }
                    } else {
                        filtered
                    }
                HomeDisplayState(
                    isLoading = false,
                    featuredEvent = featured,
                    upcomingEvents = upcoming,
                    showNoMatches = eventsState.events.isNotEmpty() && filtered.isEmpty(),
                )
            }
        }
    }

    private fun List<Event>.filterByCategory(categoryId: String): List<Event> {
        if (categoryId == HomeFilters.CATEGORY_ALL) return this
        return filter { it.category == categoryId }
    }

    private fun List<Event>.filterBySearch(query: String): List<Event> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return this
        val needle = trimmed.lowercase(Locale.getDefault())
        return filter { event ->
            event.title.lowercase(Locale.getDefault()).contains(needle) ||
                event.venue.lowercase(Locale.getDefault()).contains(needle) ||
                event.city.lowercase(Locale.getDefault()).contains(needle) ||
                event.description.lowercase(Locale.getDefault()).contains(needle) ||
                event.category.lowercase(Locale.getDefault()).contains(needle)
        }
    }
}
