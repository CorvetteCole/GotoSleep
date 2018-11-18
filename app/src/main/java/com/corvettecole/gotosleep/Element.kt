package com.corvettecole.gotosleep

import android.content.Intent
import android.view.View

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Element class represents an about item in the about page.
 * Use [AboutPage.addItem] to add your
 * custom items to the AboutPage. This class can be constructed in a builder pattern type fashion.
 */
class Element {

    internal var title: String? = null
    internal var iconDrawable: Int? = null
    private var colorDay: Int? = null
    private var colorNight: Int? = null
    private var value: String? = null
    internal var intent: Intent? = null
    internal var gravity: Int? = null
    private var autoIconColor: Boolean? = true

    internal var onClickListener: View.OnClickListener? = null

    constructor() {

    }

    constructor(title: String, iconDrawable: Int?) {
        this.title = title
        this.iconDrawable = iconDrawable
    }

    /**
     * Get the onClickListener for this Element
     *
     * @return
     * @see android.view.View.OnClickListener
     */
    fun getOnClickListener(): View.OnClickListener? {
        return onClickListener
    }

    /**
     * Set the onClickListener for this Element. It will be invoked when this particular element
     * is clicked on the AboutPage. This method has higher priority than
     * [Element.setIntent] when both methods are used
     *
     * @param onClickListener
     * @return this Element instance for builder pattern support
     * @see android.view.View.OnClickListener
     */
    fun setOnClickListener(onClickListener: View.OnClickListener): Element {
        this.onClickListener = onClickListener
        return this
    }

    /**
     * Get the gravity of the content of this Element
     *
     * @return See [android.view.Gravity]
     */
    fun getGravity(): Int? {
        return gravity
    }

    /**
     * Set the Gravity of the content for this Element
     *
     * @param gravity See [android.view.Gravity]
     * @return this Element instance for builder pattern support
     */
    fun setGravity(gravity: Int?): Element {
        this.gravity = gravity
        return this
    }

    /**
     * @return the title for this Element
     */
    fun getTitle(): String? {
        return title
    }

    /**
     * Set the title for this Element
     *
     * @param title the string value to set
     * @return this Element instance for builder pattern support
     */
    fun setTitle(title: String): Element {
        this.title = title
        return this
    }

    /**
     * Get the icon drawable for this Element that appears to the left of the title or to the
     * right of the title in RTL layout mode.
     *
     * @return the icon drawable resource of this Element
     */
    @DrawableRes
    fun getIconDrawable(): Int? {
        return iconDrawable
    }

    /**
     * Set the icon drawable for this Element that appears to the left of the title or to the
     * right of the title in RTL layout mode.
     *
     * @param iconDrawable the icon drawable resource to set
     * @return this Element instance for builder pattern support
     */
    fun setIconDrawable(@DrawableRes iconDrawable: Int?): Element {
        this.iconDrawable = iconDrawable
        return this
    }

    /**
     * @return the color resource identifier for this Elements icon
     */
    @ColorRes
    //@Nullable
    fun getIconTint(): Int? {
        return colorDay
    }

    /**
     * Set the color resource identifier for this Elements icon
     *
     * @param color the color resource identifier to use for this Element
     * @return this Element instance for builder pattern support
     */
    fun setIconTint(@ColorRes color: Int?): Element {
        this.colorDay = color
        return this
    }

/**
 * Get the color resource identifier for this Elements icon when in night mode
 *
 * @return
**/

@ColorRes
fun getIconNightTint():Int? {
return colorNight
}

/**
 * Set the icon tint to be used for this Elements icon when in night mode. If no color
 * is specified the accent color of the current theme will be used in night mode.
 *
 * @param colorNight
 * @return
*/
fun setIconNightTint(@ColorRes colorNight:Int?):Element {
this.colorNight = colorNight
return this
}

fun getValue():String? {
return value
}

fun setValue(value:String):Element {
this.value = value
return this
}

/**
 * Get the intent to be used for when this Element
 *
 * @return
 * @see Element.setIntent
*/
fun getIntent():Intent? {
return intent
}

/**
 * Set the intent to pass to the
 * [android.content.Context.startActivity] method when this item
 * is clicked. This method has lower priority than
 * [Element.setOnClickListener]
 * when both are used.
 *
 * @param intent the intent to be used
 * @return this Element instance for builder pattern support
 * @see android.content.Intent
*/
fun setIntent(intent:Intent):Element {
this.intent = intent
return this
}

/**
 * @return the AutoIcon
*/
fun getAutoApplyIconTint():Boolean? {
return autoIconColor
}

/**
 * Automatically apply tint to this Elements icon.
 *
 * @param autoIconColor
 * @return this Element instance for builder pattern support
*/
fun setAutoApplyIconTint(autoIconColor:Boolean?):Element {
this.autoIconColor = autoIconColor
return this
}
}