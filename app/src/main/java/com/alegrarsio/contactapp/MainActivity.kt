package com.alegrarsio.contactapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alegrarsio.contactapp.Screen.AddEditContactDialog
import com.alegrarsio.contactapp.Screen.ContactGridItemView
import com.alegrarsio.contactapp.Screen.ContactItemView
import com.alegrarsio.contactapp.Screen.DeleteConfirmationDialog
import com.alegrarsio.contactapp.Sorting.SortOrder
import com.alegrarsio.contactapp.Themes.AppTheme
import com.alegrarsio.contactapp.Themes.DefaultLightColorScheme
import com.alegrarsio.contactapp.Themes.OrangeLightColorScheme
import com.alegrarsio.contactapp.Themes.TealLightColorScheme
import com.alegrarsio.contactapp.ViewModel.ContactViewModel
import com.alegrarsio.contactapp.ViewModel.ContactViewModelFactory
import com.alegrarsio.contactapp.ui.theme.ContactAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactAppScreen()
                }
            }
        }
    }
}


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

    val appThemeState by contactViewModel.currentTheme.collectAsState()

    val colorScheme = when (appThemeState) {
        AppTheme.DEFAULT -> DefaultLightColorScheme
        AppTheme.ORANGE -> OrangeLightColorScheme
        AppTheme.TEAL -> TealLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.list_kontak)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { contactViewModel.cycleAppTheme() }) {
                            Icon(
                                painter = painterResource(R.drawable.pallete),
                                contentDescription = stringResource(R.string.cd_ganti_tema)
                            )
                        }
                        IconButton(onClick = { contactViewModel.toggleViewMode() }) {
                            Icon(
                                painter = if (isGridView) painterResource(R.drawable.list_view) else painterResource(R.drawable.grid_view),
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
                FloatingActionButton(
                    onClick = { contactViewModel.onAddContactClicked() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.tambah))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
                            IconButton(onClick = {
                                contactViewModel.onSearchQueryChange("")
                                focusManager.clearFocus()
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_bersihkan_pencarian))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    )
                )

                if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) stringResource(R.string.tidak_ada_kontak_pencarian, searchQuery)
                            else stringResource(R.string.belum_ada_kontak),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 72.dp, start = 4.dp, end = 4.dp), // Padding bawah untuk FAB
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp) // Padding bawah untuk FAB
                        ) {
                            items(contacts, key = { contact -> "list-${contact.id}" }) { contact ->
                                ContactItemView(
                                    contact = contact,
                                    onEdit = { contactViewModel.onEditContactClicked(contact) },
                                    onDelete = { contactViewModel.requestDeleteConfirmation(contact) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Membuat divider lebih soft
                                )
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
}
