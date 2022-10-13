package com.abdurashidov.contactapp.db

import android.content.Context
import com.abdurashidov.contactapp.models.Contact

interface MyDbInterface {
    fun addContact(contact: Contact)
    fun updateContact(context: Context, contact: Contact)
    fun deleteContact(context: Context, contact: Contact)
    fun searchContact(context: Context ,string: String):ArrayList<Contact>
    fun getAllContacts():ArrayList<Contact>
}