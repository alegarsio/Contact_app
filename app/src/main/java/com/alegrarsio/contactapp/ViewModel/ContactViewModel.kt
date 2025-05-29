package com.alegrarsio.contactapp.ViewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alegrarsio.contactapp.Model.Contact
import com.alegrarsio.contactapp.DAO.ContactDao
import com.alegrarsio.contactapp.Preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.alegrarsio.contactapp.R
import com.alegrarsio.contactapp.Sorting.SortOrder
import com.alegrarsio.contactapp.Themes.AppTheme
import java.util.regex.Pattern

private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

class ContactViewModel(
    private val application: Application,
    private val contactDao: ContactDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _currentTheme = MutableStateFlow(AppTheme.DEFAULT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.appTheme.collect { theme ->
                _currentTheme.value = theme
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.isGridView.collect { isGrid ->
                _isGridView.value = isGrid
            }
        }
    }

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

    var showDeleteConfirmDialog by mutableStateOf(false)
        private set
    private var contactPendingDeletion by mutableStateOf<Contact?>(null)


    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()


    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
    }

    fun onNameChange(name: String) {
        currentName = name
        if (name.isNotBlank()) {
            _nameError.value = null
        }
    }

    fun onPhoneNumberChange(phone: String) {
        currentPhoneNumber = phone
        if (phone.isNotBlank()) {
            _phoneError.value = null
        }
    }

    fun onEmailChange(email: String) {
        currentEmail = email
        if (email.isBlank()) {
            _emailError.value = null
        } else if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            _emailError.value = application.getString(R.string.error_email_tidak_valid)
        } else {
            _emailError.value = null
        }
    }

    private fun validateInputs(): Boolean {
        val nameIsValid = currentName.trim().isNotEmpty()
        val phoneIsValid = currentPhoneNumber.trim().isNotEmpty()
        val emailIsActuallyEmpty = currentEmail.trim().isEmpty()
        val emailFormatIsValid = EMAIL_ADDRESS_PATTERN.matcher(currentEmail.trim()).matches()

        _nameError.value = if (!nameIsValid) application.getString(R.string.error_nama_kosong) else null
        _phoneError.value = if (!phoneIsValid) application.getString(R.string.error_telepon_kosong) else null

        if (!emailIsActuallyEmpty) {
            _emailError.value = if (!emailFormatIsValid) application.getString(R.string.error_email_tidak_valid) else null
        } else {
            _emailError.value = null
        }

        return nameIsValid && phoneIsValid && (emailIsActuallyEmpty || emailFormatIsValid)
    }


    fun onAddContactClicked() {
        clearInputFieldsAndErrors()
        showDialog = true
    }

    fun onEditContactClicked(contact: Contact) {
        clearInputFieldsAndErrors()
        contactToEdit = contact
        currentName = contact.name
        currentPhoneNumber = contact.phoneNumber
        currentEmail = contact.email ?: ""
        showDialog = true
    }

    fun onDismissDialog() {
        showDialog = false
        clearInputFieldsAndErrors()
    }

    fun saveOrUpdateContact() {
        if (!validateInputs()) {
            return
        }

        val name = currentName.trim()
        val phone = currentPhoneNumber.trim()
        val email = currentEmail.trim().ifEmpty { null }

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

    private fun clearInputFieldsAndErrors() {
        currentName = ""
        currentPhoneNumber = ""
        currentEmail = ""
        contactToEdit = null
        _nameError.value = null
        _phoneError.value = null
        _emailError.value = null
    }

    fun toggleViewMode() {
        val newViewMode = !_isGridView.value
        viewModelScope.launch {
            userPreferencesRepository.saveGridViewPreference(newViewMode)
        }
    }

    fun getContactNameToDelete(): String { return contactPendingDeletion?.name ?: "" }

    fun cycleAppTheme() {
        val current = _currentTheme.value
        val allThemes = AppTheme.entries.toTypedArray()
        val nextThemeIndex = (allThemes.indexOf(current) + 1) % allThemes.size
        val newTheme = allThemes[nextThemeIndex]
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(newTheme)
        }
    }
}
