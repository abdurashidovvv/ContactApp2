 package com.abdurashidov.contactapp

import RvAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.abdurashidov.contactapp.Helper.MyButton
import com.abdurashidov.contactapp.Helper.MySwipeHelper
import com.abdurashidov.contactapp.Listener.MyButtonClickListener
import com.abdurashidov.contactapp.models.Contact
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import kotlinx.android.synthetic.main.activity_main.*

 class MainActivity : AppCompatActivity() {

     private lateinit var adapter:RvAdapter

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)


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
                         R.drawable.ic_launcher_background,
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
                         R.drawable.ic_launcher_background,
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
             adapter=RvAdapter(contactList)
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