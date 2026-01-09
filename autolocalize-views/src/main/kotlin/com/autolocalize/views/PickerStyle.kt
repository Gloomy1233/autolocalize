package com.autolocalize.views

import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px

/**
 * Style configuration for AutoLocalizeLanguagePickerView.
 * Use this for programmatic styling.
 */
data class PickerStyle(
    /**
     * Text color for the selected language
     */
    @ColorInt val textColor: Int? = null,
    
    /**
     * Hint text shown when no language is selected
     */
    val hintText: String? = null,
    
    /**
     * Hint text color
     */
    @ColorInt val hintColor: Int? = null,
    
    /**
     * Background color for the dropdown menu
     */
    @ColorInt val dropdownBackgroundColor: Int? = null,
    
    /**
     * Stroke/border color
     */
    @ColorInt val strokeColor: Int? = null,
    
    /**
     * Stroke width in pixels
     */
    @Px val strokeWidthPx: Int? = null,
    
    /**
     * Corner radius in pixels
     */
    @Px val cornerRadiusPx: Int? = null,
    
    /**
     * Text size in pixels
     */
    @Px val textSizePx: Float? = null,
    
    /**
     * Font typeface
     */
    val typeface: Typeface? = null,
    
    /**
     * Tint for dropdown icon
     */
    @ColorInt val iconTint: Int? = null,
    
    /**
     * Elevation for popup menu in pixels
     */
    @Px val popupElevationPx: Float? = null,
    
    /**
     * Whether to show flag icons
     */
    val showFlag: Boolean? = null,
    
    /**
     * How to display language names
     */
    val showLabelMode: ShowLabelMode? = null
) {
    class Builder {
        private var textColor: Int? = null
        private var hintText: String? = null
        private var hintColor: Int? = null
        private var dropdownBackgroundColor: Int? = null
        private var strokeColor: Int? = null
        private var strokeWidthPx: Int? = null
        private var cornerRadiusPx: Int? = null
        private var textSizePx: Float? = null
        private var typeface: Typeface? = null
        private var iconTint: Int? = null
        private var popupElevationPx: Float? = null
        private var showFlag: Boolean? = null
        private var showLabelMode: ShowLabelMode? = null
        
        fun textColor(@ColorInt color: Int) = apply { this.textColor = color }
        fun hintText(text: String) = apply { this.hintText = text }
        fun hintColor(@ColorInt color: Int) = apply { this.hintColor = color }
        fun dropdownBackgroundColor(@ColorInt color: Int) = apply { this.dropdownBackgroundColor = color }
        fun strokeColor(@ColorInt color: Int) = apply { this.strokeColor = color }
        fun strokeWidthPx(@Px width: Int) = apply { this.strokeWidthPx = width }
        fun cornerRadiusPx(@Px radius: Int) = apply { this.cornerRadiusPx = radius }
        fun textSizePx(@Px size: Float) = apply { this.textSizePx = size }
        fun typeface(typeface: Typeface) = apply { this.typeface = typeface }
        fun iconTint(@ColorInt color: Int) = apply { this.iconTint = color }
        fun popupElevationPx(@Px elevation: Float) = apply { this.popupElevationPx = elevation }
        fun showFlag(show: Boolean) = apply { this.showFlag = show }
        fun showLabelMode(mode: ShowLabelMode) = apply { this.showLabelMode = mode }
        
        fun build() = PickerStyle(
            textColor = textColor,
            hintText = hintText,
            hintColor = hintColor,
            dropdownBackgroundColor = dropdownBackgroundColor,
            strokeColor = strokeColor,
            strokeWidthPx = strokeWidthPx,
            cornerRadiusPx = cornerRadiusPx,
            textSizePx = textSizePx,
            typeface = typeface,
            iconTint = iconTint,
            popupElevationPx = popupElevationPx,
            showFlag = showFlag,
            showLabelMode = showLabelMode
        )
    }
    
    companion object {
        fun builder() = Builder()
    }
}

