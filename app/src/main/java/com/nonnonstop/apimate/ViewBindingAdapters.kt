package com.nonnonstop.apimate

import android.view.View
import androidx.databinding.BindingAdapter

object ViewBindingAdapters {
    @BindingAdapter("android:visibility")
    @JvmStatic
    fun View.setBoolToVisibility(b: Boolean) {
        this.visibility = if (b) View.VISIBLE else View.GONE
    }
}