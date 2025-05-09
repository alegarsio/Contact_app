package com.alegrarsio.contactapp.Screen

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alegrarsio.contactapp.Model.Contact
import com.alegrarsio.contactapp.R
import com.alegrarsio.contactapp.Sorting.SortOrder
import com.alegrarsio.contactapp.ViewModel.ContactViewModel
import com.alegrarsio.contactapp.ViewModel.ContactViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactAppScreen(
    contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val contacts by contactViewModel.filteredContacts.collectAsState()
    val isGridView by contactViewModel.isGridView.collectAsState()
    val searchQuery by contactViewModel.searchQuery.collectAsState()
    val currentSortOrder by contactViewModel.sortOrder.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_kontak)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { contactViewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Filled.Menu else Icons.Filled.MoreVert,
                            contentDescription = if (isGridView) stringResource(R.string.list_view) else stringResource(R.string.grid_view)
                        )
                    }
                    IconButton(onClick = { contactViewModel.toggleSortOrder() }) {
                        Icon(
                            imageVector = if (currentSortOrder == SortOrder.ASCENDING) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = stringResource(R.string.cd_urutkan) + " " +
                                    if (currentSortOrder == SortOrder.ASCENDING) stringResource(R.string.tooltip_urutkan_az)
                                    else stringResource(R.string.tooltip_urutkan_za)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { contactViewModel.onAddContactClicked() }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.tambah))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { contactViewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(stringResource(R.string.label_cari_kontak)) },
                placeholder = { Text(stringResource(R.string.placeholder_cari)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_ikon_cari)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { contactViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_bersihkan_pencarian))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                })
            )

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty())  stringResource(R.string.tidak_ada_kontak) + " $searchQuery\""
                        else stringResource(R.string.belum_ada_kontak),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(all = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(contacts, key = { contact -> "grid-${contact.id}" }) { contact ->
                            ContactGridItemView(
                                contact = contact,
                                onEdit = { contactViewModel.onEditContactClicked(contact) },
                                onDelete = { contactViewModel.requestDeleteConfirmation(contact) }
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(contacts, key = { contact -> "list-${contact.id}" }) { contact ->
                            ContactItemView(
                                contact = contact,
                                onEdit = { contactViewModel.onEditContactClicked(contact) },
                                onDelete = { contactViewModel.requestDeleteConfirmation(contact) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }

        if (contactViewModel.showDialog) {
            AddEditContactDialog(
                contactViewModel = contactViewModel,
                onDismiss = { contactViewModel.onDismissDialog() }
            )
        }

        if (contactViewModel.showDeleteConfirmDialog) {
            DeleteConfirmationDialog(
                contactName = contactViewModel.getContactNameToDelete(),
                onConfirm = { contactViewModel.confirmDelete() },
                onDismiss = { contactViewModel.dismissDeleteConfirmationDialog() }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    contactName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.hapus_confirm)) },
        text = { Text(stringResource(R.string.hapus_pesan, contactName)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.Iya))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.Tidak))
            }
        }
    )
}

@Composable
fun ContactGridItemView(contact: Contact, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            contact.email?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.hapus), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddEditContactDialog(
    contactViewModel: ContactViewModel,
    onDismiss: () -> Unit
) {
    val isEditing = contactViewModel.contactToEdit != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.edit) else stringResource(R.string.tambah),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                OutlinedTextField(
                    value = contactViewModel.currentName,
                    onValueChange = { contactViewModel.onNameChange(it) },
                    label = { Text(stringResource(R.string.nama)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contactViewModel.currentPhoneNumber,
                    onValueChange = { contactViewModel.onPhoneNumberChange(it) },
                    label = { Text(stringResource(R.string.Nomor_telepon)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contactViewModel.currentEmail,
                    onValueChange = { contactViewModel.onEmailChange(it) },
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.batal))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { contactViewModel.saveOrUpdateContact() }) {
                        Text(if (isEditing) stringResource(R.string.update) else stringResource(R.string.simpan))
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItemView(contact: Contact, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(contact.phoneNumber, fontSize = 16.sp)
            contact.email?.let {
                if (it.isNotEmpty()) {
                    Text(it, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.hapus), tint = MaterialTheme.colorScheme.error)
        }
    }
}