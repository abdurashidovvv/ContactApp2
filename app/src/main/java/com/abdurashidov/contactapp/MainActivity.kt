 package com.abdurashidov.contactapp

import RvAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
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
                         Color.WHITE,
                         object : MyButtonClickListener {
                             override fun onClick(position: Int) {
                                 makeCall(position)
                             }
                         })
                 )

                 buffer.add(
                     MyButton(this@MainActivity,
                         "SMS",
                         30,
                         R.drawable.ic_sms,
                         Color.WHITE,
                         object : MyButtonClickListener {
                             override fun onClick(position: Int) {
                                 sendSms(position)
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

     private fun sendSms(position: Int){
         askPermission(Manifest.permission.SEND_SMS){
             val phoneNumber=list[position].number
             val dialog=AlertDialog.Builder(this).create()
             val view=layoutInflater.inflate(R.layout.dialog_send_sms, null, false)
             val name=view.findViewById<TextView>(R.id.name)
             val number=view.findViewById<TextView>(R.id.number)
             val text=view.findViewById<TextInputEditText>(R.id.msg)
             val sendBtn=view.findViewById<CardView>(R.id.send_msg)
             name.text=list[position].name
             number.text=list[position].number
             dialog.setView(view)
             dialog.show()
             val sms=SmsManager.getDefault()
             sendBtn.setOnClickListener {
                 try {
                 sms.sendTextMessage(number.text.toString(), null, text.text.toString(), null, null)
                     dialog.cancel()
             } catch (ex: Exception) {
             }
             }
         }
     }

     private fun makeCall(position: Int) {
        askPermission(Manifest.permission.CALL_PHONE){
            val phoneNumber=list[position].number
            val intent=Intent(Intent(Intent.ACTION_CALL))
            intent.data= Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }.onDeclined {
                e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(this)
                    .setMessage("Ruxsat bermasangiz ilova ishlay olmaydi ruxsat bering...")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if(e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'

                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
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