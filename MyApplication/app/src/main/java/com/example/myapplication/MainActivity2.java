package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("userEmail", null);
            if (userEmail != null) {
                // Usuário já logado: Vá para DashboardActivity
                Intent intent = new Intent(MainActivity2.this, dashboard.class);
                startActivity(intent);
                finish();  // Fecha MainActivity para evitar voltar
            }
            ImageButton btn = findViewById(R.id.avancar);
            return insets;
        });
        findViewById(R.id.saberMais).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
            startActivity(intentSite);
        });
    };
    public void navegar(View v){
        Intent avancaTela = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(avancaTela);
        finish();
    }
}