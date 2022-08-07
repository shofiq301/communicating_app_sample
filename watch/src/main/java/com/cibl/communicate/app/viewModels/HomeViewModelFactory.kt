package com.cibl.communicate.app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class HomeViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("unknown View Model class")
    }
}