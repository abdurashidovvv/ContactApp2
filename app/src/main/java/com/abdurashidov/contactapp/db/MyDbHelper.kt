package com.abdurashidov.contactapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.abdurashidov.contactapp.models.Contact

class MyDbHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), MyDbInterface {

    companion object{
        val DB_NAME="db_name"
        val DB_VERSION=1

        val TABLE_NAME="contact_table"
        val ID="id"
        val NAME="name"
        val NUMBER="number"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query="create table $TABLE_NAME($ID integer not null primary key autoincrement unique, $NAME text not null, $NUMBER text not null)"
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query="drop table if exists $TABLE_NAME"
        db?.execSQL(query)
        onCreate(db)
    }

    override fun addContact(contact: Contact) {
        val database=writableDatabase
        val contentValues= ContentValues()
        contentValues.put(NAME, contact.name)
        contentValues.put(NUMBER, contact.number)
        database.insert(TABLE_NAME, null, contentValues)
        database.close()
    }

    override fun updateContact(context: Context, contact: Contact) {
        val database=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(NAME, contact.name)
        contentValues.put(NUMBER, contact.number)
        val result=database.update(TABLE_NAME, contentValues, "id=?", arrayOf(contact.id.toString()))
        if (result==-1){
            Toast.makeText(context, "Failed to Update", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Successfully Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun deleteContact(context: Context, contact: Contact) {
        val database=writableDatabase
        val result=database.delete(TABLE_NAME, "id=?", arrayOf(contact.id.toString()))
        if (result==-1){
            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Successfully Delete!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun searchContact(context: Context ,string: String): ArrayList<Contact> {
        val contactList=ArrayList<Contact>()
        val query="select * from $TABLE_NAME where $NAME like '$string%'"
        val database=readableDatabase
        val cursor=database.rawQuery(query, null)

        if (cursor.moveToFirst()){
            do {
                contactList.add(
                    Contact(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        number = cursor.getString(2)
                    )
                )
            }while (cursor.moveToNext())
        }
        return contactList
    }

    override fun getAllContacts(): ArrayList<Contact> {
        val contactList=ArrayList<Contact>()
        val query="select * from $TABLE_NAME"
        val database=readableDatabase
        val cursor=database.rawQuery(query, null)

        if (cursor.moveToFirst()){
            do {
                contactList.add(
                    Contact(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        number = cursor.getString(2)
                    )
                )
            }while (cursor.moveToNext())
        }
        return contactList
    }

}