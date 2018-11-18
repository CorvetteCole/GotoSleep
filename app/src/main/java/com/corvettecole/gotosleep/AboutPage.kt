package com.corvettecole.gotosleep

import android.app.Fragment
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

/**
 * The main class of this library with many predefined methods to add Elements for common items in
 * an About page. This class creates a [android.view.View] that can be passed as the root view
 * in [Fragment.onCreateView] or passed to the [android.app.Activity.setContentView]
 * in an activity's [android.app.Activity.onCreate] method
 *
 *
 * To create a custom item in the about page, pass an instance of [Element]
 * to the [AboutPage.addItem] method.
 *
 * @see Element
 */
class AboutPage
/**
 * The AboutPage requires a context to perform it's functions. Give it a context associated to an
 * Activity or a Fragment. To avoid memory leaks, don't pass a
 * [Context.getApplicationContext()][android.content.Context.getApplicationContext] here.
 *
 * @param context
 */
(private val mContext: Context) {
    private val mInflater: LayoutInflater
    private val mView: View
    private var mDescription: String? = null
    private var mImage = 0
    private var mIsRTL = false
    private val mCustomFont: Typeface? = null

    private val separator: View
        get() = mInflater.inflate(R.layout.about_page_separator, null)

    init {
        this.mInflater = LayoutInflater.from(mContext)
        this.mView = mInflater.inflate(R.layout.about_page, null)
    }

    /**
     * Add a custom [Element] to this AboutPage
     *
     * @param element
     * @return this AboutPage instance for builder pattern support
     * @see Element
     */
    fun addItem(element: Element): AboutPage {
        val wrapper = mView.findViewById<View>(R.id.about_providers) as LinearLayout
        wrapper.addView(createItem(element))
        wrapper.addView(separator, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.resources.getDimensionPixelSize(R.dimen.about_separator_height)))
        return this
    }

    /**
     * Set the header image to display in this AboutPage
     *
     * @param resource the resource id of the image to display
     * @return this AboutPage instance for builder pattern support
     */
    fun setImage(@DrawableRes resource: Int): AboutPage {
        this.mImage = resource
        return this
    }

    /**
     * Add a new group that will display a header in this AboutPage
     *
     *
     * A header will be displayed in the order it was added. For e.g:
     *
     *
     * `
     * new AboutPage(this)
     * .addItem(firstItem)
     * .addGroup("Header")
     * .addItem(secondItem)
     * .create();
    ` *
     *
     *
     * Will display the following
     * [First item]
     * [Header]
     * [Second item]
     *
     * @param name the title for this group
     * @return this AboutPage instance for builder pattern support
     */
    fun addGroup(name: String): AboutPage {

        val textView = TextView(mContext)
        textView.text = name
        TextViewCompat.setTextAppearance(textView, R.style.about_groupTextAppearance)
        val textParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        if (mCustomFont != null) {
            textView.typeface = mCustomFont
        }

        val padding = mContext.resources.getDimensionPixelSize(R.dimen.about_group_text_padding)
        textView.setPadding(padding, padding, padding, padding)


        if (mIsRTL) {
            textView.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            textParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        } else {
            textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            textParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        textView.layoutParams = textParams

        (mView.findViewById<View>(R.id.about_providers) as LinearLayout).addView(textView)
        return this
    }

    /**
     * Turn on the RTL mode.
     *
     * @param value
     * @return this AboutPage instance for builder pattern support
     */
    fun isRTL(value: Boolean): AboutPage {
        this.mIsRTL = value
        return this
    }

    fun setDescription(description: String): AboutPage {
        this.mDescription = description
        return this
    }

    /**
     * Create and inflate this AboutPage. After this method is called the AboutPage
     * cannot be customized any more.
     *
     * @return the inflated [View] of this AboutPage
     */
    fun create(): View {
        val description = mView.findViewById<View>(R.id.description) as TextView
        val image = mView.findViewById<View>(R.id.image) as ImageView
        if (mImage > 0) {
            image.setImageResource(mImage)
        }

        if (!TextUtils.isEmpty(mDescription)) {
            description.text = mDescription
        }

        description.gravity = Gravity.CENTER

        if (mCustomFont != null) {
            description.typeface = mCustomFont
        }

        return mView
    }

    private fun createItem(element: Element): View {
        val wrapper = LinearLayout(mContext)
        wrapper.orientation = LinearLayout.HORIZONTAL
        wrapper.isClickable = true

        if (element.onClickListener != null) {
            wrapper.setOnClickListener(element.onClickListener)
        } else if (element.intent != null) {
            wrapper.setOnClickListener {
                try {
                    mContext.startActivity(element.intent)
                } catch (e: Exception) {
                }
            }

        }

        val outValue = TypedValue()
        mContext.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        wrapper.setBackgroundResource(outValue.resourceId)

        val padding = mContext.resources.getDimensionPixelSize(R.dimen.about_text_padding)
        wrapper.setPadding(padding, padding, padding, padding)
        val wrapperParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wrapper.layoutParams = wrapperParams


        val textView = TextView(mContext)
        TextViewCompat.setTextAppearance(textView, R.style.about_elementTextAppearance)
        val textParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.layoutParams = textParams
        if (mCustomFont != null) {
            textView.typeface = mCustomFont
        }

        var iconView: ImageView? = null

        if (element.iconDrawable != null) {
            iconView = ImageView(mContext)
            val size = mContext.resources.getDimensionPixelSize(R.dimen.about_icon_size)
            val iconParams = LinearLayout.LayoutParams(size, size)
            iconView.layoutParams = iconParams
            val iconPadding = mContext.resources.getDimensionPixelSize(R.dimen.about_icon_padding)
            iconView.setPadding(iconPadding, 0, iconPadding, 0)

            if (Build.VERSION.SDK_INT < 21) {
                val drawable = VectorDrawableCompat.create(iconView.resources, element.iconDrawable!!, iconView.context.theme)
                iconView.setImageDrawable(drawable)
            } else {
                iconView.setImageResource(element.iconDrawable!!)
            }

            var wrappedDrawable = DrawableCompat.wrap(iconView.drawable)
            wrappedDrawable = wrappedDrawable.mutate()
            if (element.getAutoApplyIconTint()!!) {
                val currentNightMode = mContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                    if (element.getIconTint() != null) {
                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, element.getIconTint()!!))
                    } else {
                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, R.color.moonPrimary))
                    }
                } else if (element.getIconNightTint() != null) {
                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, element.getIconNightTint()!!))
                } else {
                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, R.color.moonPrimary))
                }
            }

        } else {
            val iconPadding = mContext.resources.getDimensionPixelSize(R.dimen.about_icon_padding)
            textView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        }


        textView.text = element.title


        if (mIsRTL) {

            val gravity = if (element.gravity != null) element.gravity else Gravity.END

            wrapper.gravity = gravity!! or Gravity.CENTER_VERTICAL

            textParams.gravity = gravity or Gravity.CENTER_VERTICAL
            wrapper.addView(textView)
            if (element.iconDrawable != null) {
                wrapper.addView(iconView)
            }

        } else {
            val gravity = if (element.gravity != null) element.gravity else Gravity.START
            wrapper.gravity = gravity!! or Gravity.CENTER_VERTICAL

            textParams.gravity = gravity or Gravity.CENTER_VERTICAL
            if (element.iconDrawable != null) {
                wrapper.addView(iconView)
            }
            wrapper.addView(textView)
        }

        return wrapper
    }
}
