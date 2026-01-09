package com.autolocalize.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.core.content.res.ResourcesCompat
import com.autolocalize.android.AutoLocalize
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

/**
 * A customizable dropdown View component for selecting app language.
 * 
 * Usage in XML:
 * ```xml
 * <com.autolocalize.views.AutoLocalizeLanguagePickerView
 *     android:id="@+id/languagePicker"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:al_languages="@array/supported_languages"
 *     app:al_showLabelMode="nativeName"
 *     app:al_cornerRadius="12dp"
 *     app:al_strokeWidth="2dp" />
 * ```
 * 
 * Usage programmatically:
 * ```kotlin
 * languagePicker.setLanguages(listOf(
 *     LanguageOption("en", "English", "English"),
 *     LanguageOption("es", "Spanish", "Español")
 * ))
 * 
 * languagePicker.setOnLanguageChangedListener { option ->
 *     // Handle language change
 * }
 * ```
 */
class AutoLocalizeLanguagePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {
    
    private val autoCompleteTextView: MaterialAutoCompleteTextView
    private var languages: List<LanguageOption> = emptyList()
    private var adapter: ArrayAdapter<String>? = null
    private var onLanguageChangedListener: ((LanguageOption) -> Unit)? = null
    
    private var showLabelMode: ShowLabelMode = ShowLabelMode.DISPLAY_NAME
    private var showFlag: Boolean = false
    private var displayMode: DisplayMode = DisplayMode.NAME_ONLY
    
    private var scope: CoroutineScope? = null
    private var localeObserverJob: Job? = null
    
    init {
        // Inflate the inner layout
        LayoutInflater.from(context).inflate(R.layout.al_language_picker_view, this, true)
        autoCompleteTextView = findViewById(R.id.al_auto_complete_text_view)
        
        // Set up as exposed dropdown menu
        endIconMode = END_ICON_DROPDOWN_MENU
        
        // Configure AutoCompleteTextView to show all items
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.isFocusable = false
        autoCompleteTextView.isClickable = true
        
        // Apply default style
        applyDefaultStyle()
        
        // Read XML attributes
        attrs?.let { readAttributes(context, it) }
        
        // Set up item click listener
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selected = languages.getOrNull(position)
            if (selected != null) {
                AutoLocalize.setLocale(selected.tag)
                onLanguageChangedListener?.invoke(selected)
            }
        }
        
        // Also handle click on the text view itself to show dropdown
        autoCompleteTextView.setOnClickListener {
            // Show dropdown - the custom filter will ensure all items are shown
            autoCompleteTextView.showDropDown()
        }
        
        // Handle focus to show dropdown
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }
    }
    
    private fun applyDefaultStyle() {
        boxBackgroundMode = BOX_BACKGROUND_OUTLINE
        setBoxCornerRadii(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
        boxStrokeWidth = dpToPx(1f).toInt()
    }
    
    private fun readAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AutoLocalizeLanguagePickerView
        )
        
        try {
            // Read language tags from array resource
            val languagesArrayResId = typedArray.getResourceId(
                R.styleable.AutoLocalizeLanguagePickerView_al_languages,
                0
            )
            
            val labelsArrayResId = typedArray.getResourceId(
                R.styleable.AutoLocalizeLanguagePickerView_al_languageLabels,
                0
            )
            
            if (languagesArrayResId != 0) {
                val tags = context.resources.getStringArray(languagesArrayResId)
                val labels = if (labelsArrayResId != 0) {
                    context.resources.getStringArray(labelsArrayResId)
                } else null
                
                languages = tags.mapIndexed { index, tag ->
                    if (labels != null && index < labels.size) {
                        val locale = Locale.forLanguageTag(tag)
                        LanguageOption(
                            tag = tag,
                            label = labels[index],
                            nativeLabel = locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
                        )
                    } else {
                        LanguageOption.fromTag(tag)
                    }
                }
            }
            
            // Display mode
            val displayModeValue = typedArray.getInt(
                R.styleable.AutoLocalizeLanguagePickerView_al_displayMode,
                0
            )
            displayMode = when (displayModeValue) {
                0 -> DisplayMode.NAME_ONLY
                1 -> DisplayMode.FLAG_ONLY
                2 -> DisplayMode.FLAG_AND_NAME
                else -> DisplayMode.NAME_ONLY
            }
            
            // Show flag (legacy attribute, maps to display mode)
            showFlag = typedArray.getBoolean(
                R.styleable.AutoLocalizeLanguagePickerView_al_showFlag,
                false
            )
            if (showFlag && displayMode == DisplayMode.NAME_ONLY) {
                displayMode = DisplayMode.FLAG_AND_NAME
            }
            
            // Text color
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_textColor)) {
                val textColor = typedArray.getColor(
                    R.styleable.AutoLocalizeLanguagePickerView_al_textColor,
                    Color.BLACK
                )
                autoCompleteTextView.setTextColor(textColor)
            }
            
            // Hint text
            val hintText = typedArray.getString(R.styleable.AutoLocalizeLanguagePickerView_al_hintText)
            if (hintText != null) {
                hint = hintText
            } else {
                hint = "Select language"
            }
            
            // Hint color
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_hintColor)) {
                val hintColor = typedArray.getColor(
                    R.styleable.AutoLocalizeLanguagePickerView_al_hintColor,
                    Color.GRAY
                )
                defaultHintTextColor = ColorStateList.valueOf(hintColor)
            }
            
            // Dropdown background color
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_dropdownBackgroundColor)) {
                val bgColor = typedArray.getColor(
                    R.styleable.AutoLocalizeLanguagePickerView_al_dropdownBackgroundColor,
                    Color.WHITE
                )
                autoCompleteTextView.setDropDownBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(bgColor)
                )
            }
            
            // Stroke color
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_strokeColor)) {
                val strokeColor = typedArray.getColor(
                    R.styleable.AutoLocalizeLanguagePickerView_al_strokeColor,
                    Color.GRAY
                )
                setBoxStrokeColorStateList(ColorStateList.valueOf(strokeColor))
            }
            
            // Stroke width
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_strokeWidth)) {
                val strokeWidth = typedArray.getDimensionPixelSize(
                    R.styleable.AutoLocalizeLanguagePickerView_al_strokeWidth,
                    dpToPx(1f).toInt()
                )
                boxStrokeWidth = strokeWidth
                boxStrokeWidthFocused = strokeWidth
            }
            
            // Corner radius
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_cornerRadius)) {
                val cornerRadius = typedArray.getDimension(
                    R.styleable.AutoLocalizeLanguagePickerView_al_cornerRadius,
                    dpToPx(8f)
                )
                setBoxCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)
            }
            
            // Text size
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_textSize)) {
                val textSize = typedArray.getDimension(
                    R.styleable.AutoLocalizeLanguagePickerView_al_textSize,
                    spToPx(16f)
                )
                autoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
            
            // Font family
            val fontFamily = typedArray.getString(R.styleable.AutoLocalizeLanguagePickerView_al_fontFamily)
            val fontResId = typedArray.getResourceId(
                R.styleable.AutoLocalizeLanguagePickerView_al_fontFamily,
                0
            )
            if (fontResId != 0) {
                val typeface = ResourcesCompat.getFont(context, fontResId)
                autoCompleteTextView.typeface = typeface
            } else if (fontFamily != null) {
                autoCompleteTextView.typeface = Typeface.create(fontFamily, Typeface.NORMAL)
            }
            
            // Icon tint
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_iconTint)) {
                val iconTint = typedArray.getColor(
                    R.styleable.AutoLocalizeLanguagePickerView_al_iconTint,
                    Color.GRAY
                )
                setEndIconTintList(ColorStateList.valueOf(iconTint))
            }
            
            // Popup elevation
            if (typedArray.hasValue(R.styleable.AutoLocalizeLanguagePickerView_al_popupElevation)) {
                val elevation = typedArray.getDimension(
                    R.styleable.AutoLocalizeLanguagePickerView_al_popupElevation,
                    dpToPx(8f)
                )
                autoCompleteTextView.dropDownVerticalOffset = elevation.toInt()
            }
            
            // Show label mode
            val labelModeValue = typedArray.getInt(
                R.styleable.AutoLocalizeLanguagePickerView_al_showLabelMode,
                0
            )
            showLabelMode = when (labelModeValue) {
                0 -> ShowLabelMode.DISPLAY_NAME
                1 -> ShowLabelMode.NATIVE_NAME
                2 -> ShowLabelMode.CUSTOM_LABEL
                else -> ShowLabelMode.DISPLAY_NAME
            }
            
        } finally {
            typedArray.recycle()
        }
        
        // Initialize adapter with languages
        updateAdapter()
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        
        // Observe locale changes
        if (AutoLocalize.isInitialized) {
            localeObserverJob = scope?.launch {
                AutoLocalize.observeLocale().collectLatest { locale ->
                    updateSelectedLanguage(locale)
                }
            }
        }
        
        // Set initial selection
        if (AutoLocalize.isInitialized) {
            updateSelectedLanguage(AutoLocalize.getLocale())
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        localeObserverJob?.cancel()
        scope?.cancel()
        scope = null
    }
    
    /**
     * Sets the list of available languages.
     */
    fun setLanguages(languages: List<LanguageOption>) {
        this.languages = languages
        updateAdapter()
        
        // Update selection if already initialized
        if (AutoLocalize.isInitialized) {
            updateSelectedLanguage(AutoLocalize.getLocale())
        }
    }
    
    /**
     * Sets the style configuration programmatically.
     */
    fun setStyle(style: PickerStyle) {
        style.textColor?.let { autoCompleteTextView.setTextColor(it) }
        style.hintText?.let { hint = it }
        style.hintColor?.let { defaultHintTextColor = ColorStateList.valueOf(it) }
        style.dropdownBackgroundColor?.let {
            autoCompleteTextView.setDropDownBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(it)
            )
        }
        style.strokeColor?.let { setBoxStrokeColorStateList(ColorStateList.valueOf(it)) }
        style.strokeWidthPx?.let {
            boxStrokeWidth = it
            boxStrokeWidthFocused = it
        }
        style.cornerRadiusPx?.let {
            setBoxCornerRadii(it.toFloat(), it.toFloat(), it.toFloat(), it.toFloat())
        }
        style.textSizePx?.let {
            autoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        style.typeface?.let { autoCompleteTextView.typeface = it }
        style.iconTint?.let { setEndIconTintList(ColorStateList.valueOf(it)) }
        style.popupElevationPx?.let {
            autoCompleteTextView.dropDownVerticalOffset = it.toInt()
        }
        style.showFlag?.let { showFlag = it }
        style.showLabelMode?.let {
            showLabelMode = it
            updateAdapter()
        }
    }
    
    /**
     * Sets a listener for language selection changes.
     */
    fun setOnLanguageChangedListener(listener: ((LanguageOption) -> Unit)?) {
        onLanguageChangedListener = listener
    }
    
    /**
     * Gets the currently selected language, or null if none selected.
     */
    fun getSelectedLanguage(): LanguageOption? {
        val currentTag = if (AutoLocalize.isInitialized) {
            AutoLocalize.getLocaleTag()
        } else {
            Locale.getDefault().toLanguageTag()
        }
        return languages.find { 
            it.tag.equals(currentTag, ignoreCase = true) ||
            currentTag.startsWith(it.tag, ignoreCase = true)
        }
    }
    
    /**
     * Sets the selected language by tag.
     */
    fun setSelectedLanguage(languageTag: String) {
        val language = languages.find { 
            it.tag.equals(languageTag, ignoreCase = true)
        }
        if (language != null) {
            AutoLocalize.setLocale(languageTag)
        }
    }
    
    private fun updateAdapter() {
        val displayTexts = languages.map { language ->
            formatLanguageDisplay(language)
        }
        // Create a custom adapter with a no-op filter that always shows all items
        adapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_dropdown_item_1line,
            displayTexts
        ) {
            private val allItems = displayTexts.toList()
            
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        // Always return all items, don't filter
                        val results = FilterResults()
                        results.values = allItems
                        results.count = allItems.size
                        return results
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        if (results != null && results.count > 0) {
                            clear()
                            addAll(results.values as List<String>)
                            notifyDataSetChanged()
                        } else {
                            notifyDataSetInvalidated()
                        }
                    }
                }
            }
        }
        autoCompleteTextView.setAdapter(adapter)
    }
    
    private fun formatLanguageDisplay(language: LanguageOption): String {
        val name = language.getDisplayText(showLabelMode)
        val flag = getFlagEmoji(language.tag)
        
        return when (displayMode) {
            DisplayMode.NAME_ONLY -> name
            DisplayMode.FLAG_ONLY -> flag ?: name
            DisplayMode.FLAG_AND_NAME -> if (flag != null) "$flag $name" else name
        }
    }
    
    private fun getFlagEmoji(languageTag: String): String? {
        // Map language tags to flag emojis
        val flagMap = mapOf(
            "en" to "🇺🇸", "es" to "🇪🇸", "fr" to "🇫🇷", "de" to "🇩🇪",
            "it" to "🇮🇹", "pt" to "🇵🇹", "zh" to "🇨🇳", "ja" to "🇯🇵",
            "ko" to "🇰🇷", "ar" to "🇸🇦", "ru" to "🇷🇺", "hi" to "🇮🇳",
            "el" to "🇬🇷", "tr" to "🇹🇷", "nl" to "🇳🇱", "pl" to "🇵🇱",
            "vi" to "🇻🇳", "th" to "🇹🇭", "id" to "🇮🇩", "uk" to "🇺🇦"
        )
        val code = languageTag.split("-", "_").first().lowercase()
        return flagMap[code]
    }
    
    private fun updateSelectedLanguage(locale: Locale) {
        val languageTag = locale.toLanguageTag()
        val selected = languages.find { 
            it.tag.equals(languageTag, ignoreCase = true) ||
            languageTag.startsWith(it.tag, ignoreCase = true)
        }
        
        if (selected != null) {
            val displayText = formatLanguageDisplay(selected)
            // Temporarily disable filtering, set text, then re-enable
            val currentThreshold = autoCompleteTextView.threshold
            autoCompleteTextView.threshold = Int.MAX_VALUE // Disable filtering
            autoCompleteTextView.setText(displayText, false)
            autoCompleteTextView.threshold = currentThreshold // Re-enable
            // Ensure adapter is set
            if (adapter != null) {
                autoCompleteTextView.setAdapter(adapter)
            }
        }
    }
    
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }
    
    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            resources.displayMetrics
        )
    }
}

