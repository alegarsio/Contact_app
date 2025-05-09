package com.alegrarsio.contactapp.ViewModel


import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alegrarsio.contactapp.Model.Contact
import com.alegrarsio.contactapp.DAO.ContactDao
import com.alegrarsio.contactapp.Database.AppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.alegrarsio.contactapp.Sorting.SortOrder

class ContactViewModel(private val contactDao: ContactDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> =
        contactDao.getAllContacts()
            .combine(_searchQuery) { contacts, query ->
                if (query.isBlank()) {
                    contacts
                } else {
                    contacts.filter { contact ->
                        contact.name.contains(query, ignoreCase = true) ||
                                contact.phoneNumber.contains(query, ignoreCase = true) ||
                                (contact.email?.contains(query, ignoreCase = true) == true)
                    }
                }
            }
            .combine(_sortOrder) { filteredByName, currentOrder ->
                when (currentOrder) {
                    SortOrder.ASCENDING -> filteredByName.sortedBy { it.name.lowercase() }
                    SortOrder.DESCENDING -> filteredByName.sortedByDescending { it.name.lowercase() }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    var currentName by mutableStateOf("")
    var currentPhoneNumber by mutableStateOf("")
    var currentEmail by mutableStateOf("")
    var contactToEdit by mutableStateOf<Contact?>(null)
    var showDialog by mutableStateOf(false)
        private set

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    var showDeleteConfirmDialog by mutableStateOf(false)
        private set
    private var contactPendingDeletion by mutableStateOf<Contact?>(null)


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
    }

    fun onNameChange(name: String) { currentName = name }
    fun onPhoneNumberChange(phone: String) { currentPhoneNumber = phone }
    fun onEmailChange(email: String) { currentEmail = email }

    fun onAddContactClicked() {
        clearInputFieldsAndEditState()
        showDialog = true
    }

    fun onEditContactClicked(contact: Contact) {
        contactToEdit = contact
        currentName = contact.name
        currentPhoneNumber = contact.phoneNumber
        currentEmail = contact.email ?: ""
        showDialog = true
    }

    fun onDismissDialog() {
        showDialog = false
        clearInputFieldsAndEditState()
    }

    fun saveOrUpdateContact() {
        val name = currentName.trim()
        val phone = currentPhoneNumber.trim()
        val email = currentEmail.trim().ifEmpty { null }

        if (name.isNotEmpty() && phone.isNotEmpty()) {
            viewModelScope.launch {
                if (contactToEdit == null) {
                    contactDao.insertContact(Contact(name = name, phoneNumber = phone, email = email))
                } else {
                    val updatedContact = contactToEdit!!.copy(
                        name = name,
                        phoneNumber = phone,
                        email = email
                    )
                    contactDao.updateContact(updatedContact)
                }
                onDismissDialog()
            }
        }
    }

    fun requestDeleteConfirmation(contact: Contact) {
        contactPendingDeletion = contact
        showDeleteConfirmDialog = true
    }

    fun confirmDelete() {
        contactPendingDeletion?.let { contact ->
            viewModelScope.launch {
                contactDao.deleteContact(contact)
                if (contactToEdit?.id == contact.id && showDialog) {
                    onDismissDialog()
                }
            }
        }
        dismissDeleteConfirmationDialog()
    }

    fun dismissDeleteConfirmationDialog() {
        showDeleteConfirmDialog = false
        contactPendingDeletion = null
    }

    private fun clearInputFieldsAndEditState() {
        currentName = ""
        currentPhoneNumber = ""
        currentEmail = ""
        contactToEdit = null
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun getContactNameToDelete(): String {
        return contactPendingDeletion?.name ?: ""
    }
}

class ContactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).contactDao()
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}