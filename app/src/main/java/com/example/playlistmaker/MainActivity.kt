package com.example.playlistmaker
//d
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ← ДОБАВЛЕНО: вызов super.onCreate
        enableEdgeToEdge() // ← ДОБАВЛЕНО: если используете edge-to-edge
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.poisk)

        val clickListener = View.OnClickListener { v ->
            val intent = Intent(this, PoiskActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener(clickListener)

        val button_2 = findViewById<Button>(R.id.mediateka)

        button_2.setOnClickListener{
            val intent = Intent(this, MediatekaActivity::class.java)
            startActivity(intent)
        }

        val button_3 = findViewById<Button>(R.id.nastroiki)

        button_3.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}