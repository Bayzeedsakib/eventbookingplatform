package com.example.eventbookingplatform.ui.events

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.data.model.Event
import java.text.SimpleDateFormat
import java.util.Locale

object EventDisplayFormat {

    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    fun categoryLabel(category: String): String =
        category.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

    fun shortDate(event: Event): String = shortDateFormat.format(event.startAt)

    fun priceLabel(context: Context, event: Event): String {
        val minPrice = event.minPrice ?: return context.getString(R.string.event_price_tba)
        return context.getString(R.string.event_price_from, minPrice)
    }

    fun capacityLabel(context: Context, event: Event): String {
        val total = event.totalCapacity
        return if (total > 0) {
            context.getString(R.string.event_detail_capacity_value, total)
        } else {
            context.getString(R.string.event_detail_capacity_unknown)
        }
    }

    fun headerBackgroundRes(category: String): Int =
        when (category) {
            Event.CATEGORY_MUSIC -> R.drawable.bg_card_header_music
            Event.CATEGORY_TECH -> R.drawable.bg_card_header_tech
            Event.CATEGORY_ART -> R.drawable.bg_card_header_art
            else -> R.drawable.bg_card_header_default
        }

    fun applyCategoryChipStyle(textView: TextView, category: String) {
        val context = textView.context
        val (bgRes, textRes) =
            when (category) {
                Event.CATEGORY_TECH -> R.color.tag_tech_bg to R.color.tag_tech_text
                Event.CATEGORY_ART -> R.color.tag_art_bg to R.color.tag_art_text
                Event.CATEGORY_MUSIC -> R.color.tag_music_bg to R.color.tag_music_text
                else -> R.color.bg_elevated to R.color.text_secondary
            }
        textView.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, bgRes))
        textView.setTextColor(ContextCompat.getColor(context, textRes))
    }
}
