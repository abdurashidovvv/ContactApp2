 package com.abdurashidov.contactapp

import RvAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.abdurashidov.contactapp.Helper.MyButton
import com.abdurashidov.contactapp.Helper.MySwipeHelper
import com.abdurashidov.contactapp.Listener.MyButtonClickListener
import com.abdurashidov.contactapp.db.MyDbHelper
import com.abdurashidov.contactapp.models.Contact
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*

 class MainActivity : AppCompatActivity() {

     private lateinit var adapter:RvAdapter
     private lateinit var list:ArrayList<Contact>
     private lateinit var myDbHelper: MyDbHelper

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)


         myDbHelper= MyDbHelper(this)
         list= ArrayList()
         list.addAll(myDbHelper.getAllContacts())

         rv.setHasFixedSize(true)

         //add swipe
         val swipe = object : MySwipeHelper(this, rv, 120) {
             override fun instantiateMyButton(
                 viewHolder: RecyclerView.ViewHolder,
                 buffer: MutableList<MyButton>,
             ) {
                 buffer.add(
                     MyButton(this@MainActivity,
                         "Call",
                         30,
                         R.drawable.ic_call,
                         Color.RED,
                         object : MyButtonClickListener {
                             override fun onClick(position: Int) {
                                 Toast.makeText(this@MainActivity, "Call", Toast.LENGTH_SHORT)
                                     .show()
                             }
                         })
                 )

                 buffer.add(
                     MyButton(this@MainActivity,
                         "SMS",
                         30,
                         R.drawable.ic_sms,
                         Color.RED,
                         object : MyButtonClickListener {
                             override fun onClick(position: Int) {
                                 Toast.makeText(this@MainActivity, "SMS", Toast.LENGTH_SHORT).show()
                             }
                         })
                 )
             }

         }

         readContact()

     }

     override fun onResume() {
         super.onResume()

         add.setOnClickListener {
             val dialog=AlertDialog.Builder(this).create()
             val view=layoutInflater.inflate(R.layout.dialog_add_contact, null, false)
             val editTextName=view.findViewById<TextInputEditText>(R.id.name)
             val editTextNumber=view.findViewById<TextInputEditText>(R.id.number)
             val btnSave=view.findViewById<Button>(R.id.btn_save)
             dialog.setView(view)
             dialog.show()

             btnSave.setOnClickListener {
                 if (editTextName.text.toString().isNotEmpty() && editTextNumber.text.toString().isNotEmpty()){
                     val contact=Contact(
                         name = editTextName.text.toString(),
                         number = editTextNumber.text.toString()
                     )
                     list.add(contact)
                     adapter.notifyDataSetChanged()
                     dialog.cancel()
                 }else{
                     Toast.makeText(this, "Iltimos hamma maydonlarni toldiring.", Toast.LENGTH_SHORT).show()
                 }
             }
         }
     }

     override fun onStop() {
         super.onStop()
         list.forEach {
             if (myDbHelper.getAllContacts().contains(it)){
             }else{
                 myDbHelper.addContact(it)
             }
         }
     }

     private fun makeCall(position: Int) {

     }

     @SuppressLint("NewApi", "Range")
     private fun readContact() {
         val contactList = ArrayList<Contact>()
         askPermission(Manifest.permission.READ_CONTACTS){
            val contacts=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null)
            while (contacts!!.moveToNext()){
                val contact=Contact(
                    contacts!!.getString(contacts!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    contacts.getString(contacts!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                )
                contactList.add(contact)
            }
             contacts.close()

             contactList.forEach {
                 if (!myDbHelper.getAllContacts().contains(it)){
                     list.add(it)
                 }
             }

             adapter=RvAdapter(list)
             rv.adapter=adapter
         }.onDeclined { e->
             if(e.hasDenied()){
                 AlertDialog.Builder(this)
                     .setMessage("Ruxsat bermasangiz ilova ishlay olmaydi ruxsat bering...")
                     .setPositiveButton("yes"){dialog, which->
                         e.askAgain()
                     }
                     .setNegativeButton("no"){dialog, which->
                         dialog.dismiss()
                     }
                     .show()
             }
             if (e.hasForeverDenied()){
                 e.goToSettings()
             }
         }
     }
 }