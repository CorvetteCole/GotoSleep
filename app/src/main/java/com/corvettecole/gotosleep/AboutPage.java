/**
 *         Go to Sleep is an open source app to manage a healthy sleep schedule
 *         Copyright (C) 2019 Cole Gerdemann
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.corvettecole.gotosleep;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

/**
 * The main class of this library with many predefined methods to add Elements for common items in
 * an About page. This class creates a {@link android.view.View} that can be passed as the root view
 * in {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)} or passed to the {@link android.app.Activity#setContentView(View)}
 * in an activity's {@link android.app.Activity#onCreate(Bundle)} method
 * <p>
 * To create a custom item in the about page, pass an instance of {@link Element}
 * to the {@link AboutPage#addItem(Element)} method.
 *
 * @see Element
 */
public class AboutPage {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final View mView;
    private String mDescription;
    private int mImage = 0;
    private boolean mIsRTL = false;
    private Typeface mCustomFont;

    /**
     * The AboutPage requires a context to perform it's functions. Give it a context associated to an
     * Activity or a Fragment. To avoid memory leaks, don't pass a
     * {@link android.content.Context#getApplicationContext() Context.getApplicationContext()} here.
     *
     * @param context
     */
    public AboutPage(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mView = mInflater.inflate(R.layout.about_page, null);
    }

    /**
     * Add a custom {@link Element} to this AboutPage
     *
     * @param element
     * @return this AboutPage instance for builder pattern support
     * @see Element
     */
    public AboutPage addItem(Element element) {
        LinearLayout wrapper = (LinearLayout) mView.findViewById(R.id.about_providers);
        wrapper.addView(createItem(element));
        wrapper.addView(getSeparator(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources().getDimensionPixelSize(R.dimen.about_separator_height)));
        return this;
    }

    /**
     * Set the header image to display in this AboutPage
     *
     * @param resource the resource id of the image to display
     * @return this AboutPage instance for builder pattern support
     */
    public AboutPage setImage(@DrawableRes int resource) {
        this.mImage = resource;
        return this;
    }

    /**
     * Add a new group that will display a header in this AboutPage
     * <p>
     * A header will be displayed in the order it was added. For e.g:
     * <p>
     * <code>
     * new AboutPage(this)
     * .addItem(firstItem)
     * .addGroup("Header")
     * .addItem(secondItem)
     * .create();
     * </code>
     * <p>
     * Will display the following
     * [First item]
     * [Header]
     * [Second item]
     *
     * @param name the title for this group
     * @return this AboutPage instance for builder pattern support
     */
    public AboutPage addGroup(String name) {

        TextView textView = new TextView(mContext);
        textView.setText(name);
        TextViewCompat.setTextAppearance(textView, R.style.about_groupTextAppearance);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (mCustomFont != null) {
            textView.setTypeface(mCustomFont);
        }

        int padding = mContext.getResources().getDimensionPixelSize(R.dimen.about_group_text_padding);
        textView.setPadding(padding, padding, padding, padding);


        if (mIsRTL) {
            textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            textParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        } else {
            textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            textParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        }
        textView.setLayoutParams(textParams);

        ((LinearLayout) mView.findViewById(R.id.about_providers)).addView(textView);
        return this;
    }

    /**
     * Turn on the RTL mode.
     *
     * @param value
     * @return this AboutPage instance for builder pattern support
     */
    public AboutPage isRTL(boolean value) {
        this.mIsRTL = value;
        return this;
    }

    public AboutPage setDescription(String description) {
        this.mDescription = description;
        return this;
    }

    /**
     * Create and inflate this AboutPage. After this method is called the AboutPage
     * cannot be customized any more.
     *
     * @return the inflated {@link View} of this AboutPage
     */
    public View create() {
        TextView description = (TextView) mView.findViewById(R.id.description);
        ImageView image = (ImageView) mView.findViewById(R.id.image);
        if (mImage > 0) {
            image.setImageResource(mImage);
        }

        if (!TextUtils.isEmpty(mDescription)) {
            description.setText(mDescription);
        }

        description.setGravity(Gravity.CENTER);

        if (mCustomFont != null) {
            description.setTypeface(mCustomFont);
        }

        return mView;
    }

    private View createItem(final Element element) {
        LinearLayout wrapper = new LinearLayout(mContext);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);
        wrapper.setClickable(true);

        if (element.getOnClickListener() != null) {
            wrapper.setOnClickListener(element.getOnClickListener());
        } else if (element.getIntent() != null) {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mContext.startActivity(element.getIntent());
                    } catch (Exception e) {
                    }
                }
            });

        }

        TypedValue outValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        wrapper.setBackgroundResource(outValue.resourceId);

        int padding = mContext.getResources().getDimensionPixelSize(R.dimen.about_text_padding);
        wrapper.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(wrapperParams);


        TextView textView = new TextView(mContext);
        TextViewCompat.setTextAppearance(textView, R.style.about_elementTextAppearance);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(textParams);
        if (mCustomFont != null) {
            textView.setTypeface(mCustomFont);
        }

        ImageView iconView = null;

        if (element.getIconDrawable() != null) {
            iconView = new ImageView(mContext);
            int size = mContext.getResources().getDimensionPixelSize(R.dimen.about_icon_size);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
            iconView.setLayoutParams(iconParams);
            int iconPadding = mContext.getResources().getDimensionPixelSize(R.dimen.about_icon_padding);
            iconView.setPadding(iconPadding, 0, iconPadding, 0);

            if (Build.VERSION.SDK_INT < 21) {
                Drawable drawable = VectorDrawableCompat.create(iconView.getResources(), element.getIconDrawable(), iconView.getContext().getTheme());
                iconView.setImageDrawable(drawable);
            } else {
                iconView.setImageResource(element.getIconDrawable());
            }

            Drawable wrappedDrawable = DrawableCompat.wrap(iconView.getDrawable());
            wrappedDrawable = wrappedDrawable.mutate();
            if (element.getAutoApplyIconTint()) {
                int currentNightMode = mContext.getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
                if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                    if (element.getIconTint() != null) {
                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, element.getIconTint()));
                    } else {
                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, R.color.moonPrimary));
                    }
                } else if (element.getIconNightTint() != null) {
                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, element.getIconNightTint()));
                } else {
                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(mContext, R.color.moonPrimary));
                }
            }

        } else {
            int iconPadding = mContext.getResources().getDimensionPixelSize(R.dimen.about_icon_padding);
            textView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        }


        textView.setText(element.getTitle());


        if (mIsRTL) {

            final int gravity = element.getGravity() != null ? element.getGravity() : Gravity.END;

            wrapper.setGravity(gravity | Gravity.CENTER_VERTICAL);
            //noinspection ResourceType
            textParams.gravity = gravity | Gravity.CENTER_VERTICAL;
            wrapper.addView(textView);
            if (element.getIconDrawable() != null) {
                wrapper.addView(iconView);
            }

        } else {
            final int gravity = element.getGravity() != null ? element.getGravity() : Gravity.START;
            wrapper.setGravity(gravity | Gravity.CENTER_VERTICAL);
            //noinspection ResourceType
            textParams.gravity = gravity | Gravity.CENTER_VERTICAL;
            if (element.getIconDrawable() != null) {
                wrapper.addView(iconView);
            }
            wrapper.addView(textView);
        }

        return wrapper;
    }

    private View getSeparator() {
        return mInflater.inflate(R.layout.about_page_separator, null);
    }
}
