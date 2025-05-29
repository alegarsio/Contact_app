package com.alegrarsio.contactapp.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alegrarsio.contactapp.Database.AppDatabase
import com.alegrarsio.contactapp.Preferences.UserPreferencesRepository

class ContactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).contactDao()

            val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)
            @Suppress("UNCHECKED_CAST")

            return ContactViewModel(application, dao, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
    