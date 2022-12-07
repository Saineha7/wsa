package com.livinideas.googlemapsdirectionsample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class FeedbackForm : AppCompatActivity() {
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        db = FirebaseFirestore.getInstance()

        val submit = findViewById<View>(R.id.submit) as Button

        submit.setOnClickListener { view: View? ->
            Submit()
        }
    }

    private fun Submit() {
        val user = findViewById<View>(R.id.user) as EditText
        val area = findViewById<View>(R.id.area) as EditText
        val pc = findViewById<View>(R.id.pc) as EditText
        val bar = findViewById<View>(R.id.bar) as EditText
        val time = findViewById<View>(R.id.time) as EditText
        val pf = findViewById<View>(R.id.pf) as EditText
        val zone = findViewById<View>(R.id.zone) as EditText

        val User = user.text.toString().trim()
        val Area1 = area.text.toString().trim()
        val PC1 = pc.text.toString().trim()
        val BAR1 = bar.text.toString().trim()
        val TIME1 = time.text.toString().trim()
        val PF1 = pf.text.toString().trim()
        val zone1 = zone.text.toString().trim()

        if (!User.isEmpty() && !Area1.isEmpty() && !PC1.isEmpty() && !BAR1.isEmpty() && !TIME1.isEmpty() && !PF1.isEmpty() && !zone1.isEmpty()) {
            try {
                data class Fields(
                    val area: String,
                    val is_bar: String,
                    val isPolice_Station: String,
                    val peopleFrequency: String,
                    val time: String,
                    val zone: String,
                )

                val items = Fields(Area1, BAR1, PC1, PF1, TIME1, zone1)

                val data: HashMap<String, Any> = HashMap()
                data.put("feed", Arrays.asList(items))
                db.collection("Feedback").document("${User}").get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            if (document.exists()) {
                                Log.d("TAG", "Document already exists.")

                                db.collection("Feedback").document("${User}")
                                    .update("feed", FieldValue.arrayUnion(items))
                                    .addOnSuccessListener { void: Void? ->
                                        Toast.makeText(this,
                                            "Successfully uploaded",
                                            Toast.LENGTH_LONG).show()
                                        user.text.clear()
                                        area.text.clear()
                                        pc.text.clear()
                                        bar.text.clear()
                                        time.text.clear()
                                        pf.text.clear()
                                        zone.text.clear()
                                    }
                            } else {
                                Log.d("TAG", "Document doesn't exist.")
                                db.collection("Feedback").document("${User}").set(data)
                                    .addOnSuccessListener { void: Void? ->
                                        Toast.makeText(this,
                                            "Successfully uploaded",
                                            Toast.LENGTH_LONG).show()
                                        user.text.clear()
                                        area.text.clear()
                                        pc.text.clear()
                                        bar.text.clear()
                                        time.text.clear()
                                        pf.text.clear()
                                        zone.text.clear()


                                    }
                            }
                        } else {
                            Log.d("TAG", "Error: ", task.exception)
                        }

                    }

                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Please fill up this field", Toast.LENGTH_LONG).show()
        }
    }

}