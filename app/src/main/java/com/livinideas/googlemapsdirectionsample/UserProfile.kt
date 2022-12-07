package com.livinideas.googlemapsdirectionsample

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_userprofile.*
import kotlinx.android.synthetic.main.activity_userprofile.view.*
import org.checkerframework.checker.units.qual.Length
import org.w3c.dom.Text
import kotlin.math.log

var count = 1
var c1 = sharedpreferences.getString("c1p","")
var c2 = sharedpreferences.getString("c2p","")
var c3 = sharedpreferences.getString("c3p","")


class UserProfile : AppCompatActivity() {
    var editor =  sharedpreferences.edit()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userprofile)
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

        Log.d("contacts1", "onCreate: $c1")
        Log.d("contacts2", "onCreate: $c2")
        Log.d("contacts3", "onCreate: $c3")


        addContacts()

        Log.d("valueee", "onCreate: ${sharedpreferences.getString("c1n","Emergency Contact1")}")
        if(sharedpreferences.getString("c1n","Emergency Contact1") != "Emergency Contact1") {
            findViewById<TextView>(R.id.contactTv1).text=" Name: "
            findViewById<TextView>(R.id.contactTv1).append(
                sharedpreferences.getString("c1n", "Emergency Contact 1"))
            findViewById<TextView>(R.id.contactTv1).append("\n Contact: ")
            findViewById<TextView>(R.id.contactTv1).append(
                sharedpreferences.getString(
                    "c1p",
                    "Emergency Contact Number"
                )
            )
        }

        if(sharedpreferences.getString("c2n","Emergency Contact2") != "Emergency Contact2") {
            findViewById<TextView>(R.id.contactTv2).text=" Name: "
            findViewById<TextView>(R.id.contactTv2).append (
                sharedpreferences.getString("c2n", "Emergency Contact 2"))
            findViewById<TextView>(R.id.contactTv2).append("\n Contact: ")
            findViewById<TextView>(R.id.contactTv2).append(
                sharedpreferences.getString(
                    "c2p",
                    "Emergency Contact Number"
                )
            )
        }

        if(sharedpreferences.getString("c3n","Emergency Contact3") != "Emergency Contact3") {
            findViewById<TextView>(R.id.contactTv3).text=" Name: "
            findViewById<TextView>(R.id.contactTv3).append(
                sharedpreferences.getString("c3n", "Emergency Contact 3"))
            findViewById<TextView>(R.id.contactTv3).append("\n Contact: ")
            findViewById<TextView>(R.id.contactTv3).append(
                sharedpreferences.getString(
                    "c3p",
                    "Emergency Contact Number"
                )
            )
        }
        findViewById<TextView>(R.id.contactTv1).setOnClickListener() {
            if( findViewById<TextView>(R.id.contactTv1).text == "") {
                Toast.makeText(this,"No contact exists to edit!",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Select new contact", Toast.LENGTH_SHORT).show()
                //findViewById<TextView>(R.id.contactTv1).text = ""
                count = 1
                resultLauncher.launch(intent)
            }
        }

        findViewById<TextView>(R.id.contactTv2).setOnClickListener() {
            if( findViewById<TextView>(R.id.contactTv2).text == "") {
                Toast.makeText(this,"No contact exists to edit!",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Select new contact", Toast.LENGTH_SHORT).show()
              //  findViewById<TextView>(R.id.contactTv2).text = ""
                count = 2
                resultLauncher.launch(intent)
            }
        }

        findViewById<TextView>(R.id.contactTv3).setOnClickListener() {
            if( findViewById<TextView>(R.id.contactTv3).text == "") {
                Toast.makeText(this,"No contact exists to edit!",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Select new contact", Toast.LENGTH_SHORT).show()
              //  findViewById<TextView>(R.id.contactTv3).text = ""
                count = 3
                resultLauncher.launch(intent)
            }
        }

    }

    @SuppressLint("Range")
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {

            //val editor =  sharedpreferences.edit()


    var key: String
    var tv: TextView
             when(count) {
                1 -> {
                    tv = findViewById(R.id.contactTv1)
                    key = "c1n"
                }
                2 -> {
                    tv = findViewById(R.id.contactTv2)
                    key = "c2n"
                }
                3 -> {
                    tv = findViewById(R.id.contactTv3)
                    key = "c3n"
                }
                else -> {Toast.makeText(this,"Only 3 emergency contacts can be added!",Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
                }
            }
            val uri = result.data?.data
            val cursor2: Cursor?
            val cursor1 = contentResolver.query(uri!!, null, null, null, null)!!

            if (cursor1.moveToFirst()) {
                //get contact details
                val contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                val idResultHold = idResults.toInt()

                tv.text = ""
               // findViewById<TextView>(R.id.contactTv1)
                    tv.append (" Name: $contactName")
                editor.putString(key,contactName)
                editor.apply()
                Log.d("taggg", ": $contactName")


                if (idResultHold == 1){
                    cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,
                        null,
                        null
                    )
                    //a contact may have multiple phone numbers
                    if (cursor2!!.moveToFirst()){
                        val contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                       // findViewById<TextView>(R.id.contactTv1)
                            tv.append("\n Contact: $contactNumber")
                        when(count) {
                            1 -> {c1 = contactNumber
                                editor.putString("c1p",c1)}
                            2 -> {c2 = contactNumber
                                editor.putString("c2p",c2)}
                            3 -> {c3 = contactNumber
                                editor.putString("c3p",c3)}
                        }
                        editor.apply()
                    }
                    cursor2.close()
                }
                count++
                cursor1.close()
            }
        }
    }

    @SuppressLint("Range")
    fun addContacts() {
        findViewById<FloatingActionButton>(R.id.addContactFab).setOnClickListener() {

            if(findViewById<TextView>(R.id.contactTv1).text != "" && findViewById<TextView>(R.id.contactTv2).text != "" && findViewById<TextView>(R.id.contactTv3).text != "") {
                Toast.makeText(this, "Only 3 emergency contacts can be added!", Toast.LENGTH_SHORT).show()
            } else {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            resultLauncher.launch(intent)}
        }

//        findViewById<Button>(R.id.smsBtn).setOnClickListener() {
//            var i = 1
//            while(i in 1..3) {
//                val msgIntent = Intent(Intent.ACTION_SENDTO)
//                msgIntent.data = when(i) {
//                    1 -> Uri.parse("smsto:$c1")
//                    2 -> Uri.parse("smsto:$c2")
//                    3 -> Uri.parse("smsto:$c3")
//                    else -> {Uri.parse("smsto:")}
//                }
//               // msgIntent.data = Uri.parse("smsto:")
//                msgIntent.putExtra("sms_body", "Live location coordinates: ($lat,$lng)")
//                i++
//                this.startActivity(msgIntent)
//            }
//        }
    }

//    fun addContacts1() {
//        var editContact1 = findViewById<EditText>(R.id.contactEt1)
//        findViewById<TextView>(R.id.contactTv1).setOnClickListener() {
//            if(editContact1.visibility == View.INVISIBLE) {
//                editContact1.visibility = View.VISIBLE
////                editContact1.focusable = View.NOT_FOCUSABLE
//            }
//                else
//                editContact1.visibility = View.INVISIBLE
//        }
//
//        var editContact2 = findViewById<EditText>(R.id.contactEt2)
//        findViewById<TextView>(R.id.contactTv2).setOnClickListener() {
//            if (editContact2.visibility == View.INVISIBLE){
//                editContact2.visibility = View.VISIBLE
////            editContact2.focusable = View.NOT_FOCUSABLE
//        }
//            else
//                editContact2.visibility = View.INVISIBLE
//        }
//
//        var editContact3 = findViewById<EditText>(R.id.contactEt3)
//        findViewById<TextView>(R.id.contactTv3).setOnClickListener() {
//            if(editContact3.visibility == View.INVISIBLE) {
//                editContact3.visibility = View.VISIBLE
////                editContact3.focusable = View.NOT_FOCUSABLE
//            }
//            else
//                editContact3.visibility = View.INVISIBLE
//        }
//
//        editContact1.setOnClickListener() {
////            editContact1.focusable = View.FOCUSABLE
//            var text = editContact1.text
//            findViewById<TextView>(R.id.contactTv1).text = text
//            editContact1.visibility = View.INVISIBLE
//        }
//
//        editContact2.setOnClickListener() {
////            editContact2.focusable = View.FOCUSABLE
//            var text = editContact2.text
//            findViewById<TextView>(R.id.contactTv2).text = text
//            editContact2.visibility = View.INVISIBLE
//        }
//
//        editContact3.setOnClickListener() {
////            editContact3.focusable = View.FOCUSABLE
//            var text = editContact3.text
//            findViewById<TextView>(R.id.contactTv3).text = text
//            editContact3.visibility = View.INVISIBLE
//
//        }
//
//        findViewById<Button>(R.id.smsBtn).setOnClickListener() {
//            this.getSystemService(SmsManager::class.java).sendTextMessage("+919962130932", "+919962130932", "test message", null, null)
//                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
//            Log.d("yoyo", "addContacts1: sdent")
//            }
//    }
}