package at.rezepturmeister.naehrwertrechner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.rezepturmeister.naehrwertrechner.data.AppDatabase

class NaehrwertViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(NaehrwertViewModel::class.java))
        return NaehrwertViewModel(db) as T
    }
}
