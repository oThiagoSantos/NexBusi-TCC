package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class perfil extends AppCompatActivity {

    TextView tvNomeUsuario, tvSenha, tvNomeEmpresa, tvEmail, tvCNPJ;

    String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        // ------ associações ------
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        tvSenha = findViewById(R.id.tvSenha);
        tvNomeEmpresa = findViewById(R.id.tvNomeEmpresa);
        tvEmail = findViewById(R.id.tvEmail);
        tvCNPJ = findViewById(R.id.tvCNPJ);
        // Ajuste de layout edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        carregarInformacoesUsuario();
        bloquearEdicao();

    }

    // -----------------------------
    // 🔹 REQUISIÇÃO AO PHP
    // -----------------------------
    public void carregarInformacoesUsuario() {

        String url = "https://"+ meuServidor +"/apiTest/get_user_data.php"; // <-- coloque seu link aqui

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                    Log.d("API", "Resposta: " + response);

                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);

                        if (json.has("error")) {
                            Toast.makeText(this, json.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 🔹 Pegando os valores retornados pelo PHP
                        String nome = json.getString("nome");
                        String senha = json.getString("senha");
                        String cnpj = json.getString("cnpj");
                        String email = json.getString("email");
                        String usuario = json.getString("usuario");

                        // 🔹 Exibindo nas TextViews
                        tvNomeUsuario.setText(usuario);
                        tvNomeEmpresa.setText(nome);
                        tvEmail.setText(email);
                        tvCNPJ.setText(cnpj);
                        tvSenha.setText(senha); // se flag não for senha, ajuste aqui

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao interpretar resposta", Toast.LENGTH_SHORT).show();
                    }

                },

                error -> {
                    Log.e("API", "Erro: " + error.toString());
                    Toast.makeText(this, "Erro na requisição", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // 🔹 Pega o email salvo no SharedPreferences
                String emailSalvo = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE)
                        .getString("userEmail", "");

                params.put("email", emailSalvo);

                return params;
            }
        };

        queue.add(postRequest);
    }
    private void bloquearEdicao() {
        TextView[] campos = { tvNomeUsuario, tvSenha, tvNomeEmpresa, tvEmail, tvCNPJ, tvNomeUsuario, tvSenha };

        for (TextView campo : campos) {
            campo.setLongClickable(false);
            campo.setTextIsSelectable(false);
            campo.setFocusable(false);
            campo.setFocusableInTouchMode(false);
        }
    }


    // Voltar ao dashboard
    public void voltarPraDashboard(View v) {
        Intent intent = new Intent(perfil.this, dashboard.class);
        startActivity(intent);
    }
}
