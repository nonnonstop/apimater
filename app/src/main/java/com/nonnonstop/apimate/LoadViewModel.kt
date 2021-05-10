package com.nonnonstop.apimate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoadViewModel : ViewModel() {
    val htmlUrl by lazy {
        MutableLiveData<String>()
    }
    val isRunning by lazy {
        MutableLiveData<Boolean>()
    }
}