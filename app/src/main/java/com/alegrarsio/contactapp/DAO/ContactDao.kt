package com.alegrarsio.contactapp.DAO


import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.alegrarsio.contactapp.Model.Contact
@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>
}