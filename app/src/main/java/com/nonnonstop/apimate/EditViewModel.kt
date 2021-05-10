package com.nonnonstop.apimate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditViewModel : ViewModel() {
    val fileName by lazy {
        MutableLiveData<String>()
    }
    val script by lazy {
        MutableLiveData<String>()
    }
    val save by lazy {
        MutableLiveData<Boolean>()
    }
    val revert by lazy {
        MutableLiveData<Boolean>()
    }

    fun save() {
        save.value = true
    }

    fun revert() {
        revert.value = true
    }
}