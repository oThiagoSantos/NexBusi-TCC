package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class recuperarSenha extends AppCompatActivity {

    EditText edtEmail;
    Button btnEnviar;   // <-- ADICIONADO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_senha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ⚠️ INICIALIZA AQUI
        edtEmail = findViewById(R.id.edttxtEmail);
        btnEnviar = findViewById(R.id.enviarEmail);

        // CLIQUE DO BOTÃO
        btnEnviar.setOnClickListener(v -> {
            enviarEmailRecuperacao();
        });
    }

    private void enviarEmailRecuperacao() {

        String emailEnviado = edtEmail.getText().toString().trim();

        if (emailEnviado.isEmpty()) {
            Toast.makeText(this, "Digite seu email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔒 DESATIVA BOTÃO PARA EVITAR CLIQUES MÚLTIPLOS
        btnEnviar.setEnabled(false);
        btnEnviar.setAlpha(0.5f); // efeito visual

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://" + MainActivity.meuServidor + "/apiTest/recuperarSenha.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    // 🔓 REABILITA O BOTÃO
                    btnEnviar.setEnabled(true);
                    btnEnviar.setAlpha(1f);
                },
                error -> {
                    Toast.makeText(this, "Erro: " + error.toString(), Toast.LENGTH_LONG).show();
                    // 🔓 REABILITA O BOTÃO MESMO SE DER ERRO
                    btnEnviar.setEnabled(true);
                    btnEnviar.setAlpha(1f);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailEnviado);
                return params;
            }
        };

        queue.add(req);
    }
}
