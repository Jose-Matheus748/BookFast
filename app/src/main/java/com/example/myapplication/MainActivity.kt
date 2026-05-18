package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.util.Log

class MainActivity: AppCompatActivity() {
    lateinit var btnNavegar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_inicial)

        val btnNavegar = findViewById<Button>(R.id.btnNavegar)

        // activity_tela_inicial -> activity_wellcome
        btnNavegar.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        var fb = Firebase.firestore

//        fb.collection("Pessoas")
//            .add(mapOf(
//                "nome" to "Jose2",
//                "idade" to 20,
//                "sexo" to "Masculino",
//                "altura" to 1.90
//            ))
//        fb.collection("Pessoas")
//            .document("pwhOumhMchNuWl7KOhT4")
//            .update(mapOf(
//                "nome" to "Jose Matheus"
//            ))
//        fb.collection("Pessoas")
//            .document("pwhOumhMchNuWl7KOhT4")
//            .delete()

//        fb.collection("Pessoas")
//            .document("pwhOumhMchNuWl7KOhT4")
//            .get().addOnSuccessListener {
//                result ->
//                Log.d("BookFast", result.get("altura").toString())
//                Log.d("BookFast", result.get("idade").toString())
//                Log.d("BookFast", result.get("nome").toString())
//                Log.d("BookFast", result.get("sexo").toString())
//            }
    }
}
