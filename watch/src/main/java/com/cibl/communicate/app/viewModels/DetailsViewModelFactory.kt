package com.cibl.communicate.app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class DetailsViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountDetailsViewModel::class.java)){
            return AccountDetailsViewModel() as T
        }
        throw IllegalArgumentException("unknown View Model class")
    }
}