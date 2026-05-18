package com.example.eventbookingplatform.ui.events

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import coil.dispose
import coil.load
import com.example.eventbookingplatform.R

/**
 * Binds a list/detail header: remote image when [imageUrl] is set, otherwise [categoryGradientRes].
 * Image is drawn under a scrim overlay so category chips stay readable.
 */
fun bindEventHeaderImage(
    imageView: ImageView,
    overlayView: View,
    imageUrl: String?,
    @DrawableRes categoryGradientRes: Int,
) {
    fun showCategoryGradient() {
        imageView.dispose()
        imageView.setImageDrawable(null)
        imageView.isVisible = false
        overlayView.setBackgroundResource(categoryGradientRes)
    }

    val url = imageUrl?.trim().orEmpty()
    if (url.isEmpty()) {
        showCategoryGradient()
        return
    }

    imageView.isVisible = true
    overlayView.setBackgroundResource(R.drawable.bg_header_image_scrim)
    imageView.load(url) {
        crossfade(true)
        listener(
            onSuccess = { _, _ ->
                imageView.isVisible = true
                overlayView.setBackgroundResource(R.drawable.bg_header_image_scrim)
            },
            onError = { _, _ -> showCategoryGradient() },
            onCancel = { _ -> showCategoryGradient() },
        )
    }
}

/** Featured hero card: full-bleed image with scrim, or default gradient card background. */
fun bindFeaturedCardImage(
    imageView: ImageView,
    scrimView: View,
    contentView: View,
    imageUrl: String?,
) {
    fun showDefaultCard() {
        imageView.dispose()
        imageView.setImageDrawable(null)
        imageView.isVisible = false
        scrimView.isVisible = false
        contentView.setBackgroundResource(R.drawable.bg_featured_card)
    }

    val url = imageUrl?.trim().orEmpty()
    if (url.isEmpty()) {
        showDefaultCard()
        return
    }

    imageView.isVisible = true
    scrimView.isVisible = true
    contentView.background = null
    imageView.load(url) {
        crossfade(true)
        listener(
            onSuccess = { _, _ ->
                imageView.isVisible = true
                scrimView.isVisible = true
                contentView.background = null
            },
            onError = { _, _ -> showDefaultCard() },
            onCancel = { _ -> showDefaultCard() },
        )
    }
}
