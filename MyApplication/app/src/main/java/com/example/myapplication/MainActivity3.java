package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Textos
            String txt = "Esqueci Minha senha";
            String txt2 = "Não possui uma conta? Registre-se";

            TextView textView = findViewById(R.id.EsqueciSenha);
            TextView tvRegistro = findViewById(R.id.Registro);

            // Primeiro texto clicável (Esqueci Minha senha)
            SpannableString spannable = new SpannableString(txt);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(MainActivity3.this, recuperarSenha.class);
                    startActivity(intent);
                }
            };
            spannable.setSpan(clickableSpan, 0, txt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Segundo texto clicável (Registre-se)
            SpannableString spannable2 = new SpannableString(txt2);
            ClickableSpan clickableSpan2 = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent trocarTela = new Intent(MainActivity3.this, MainActivity.class);
                    startActivity(trocarTela);
                    finish();
                }
            };
            // Aplicando somente na palavra "Registre-se"
            int start = txt2.indexOf("Registre-se");
            int end = start + "Registre-se".length();
            spannable2.setSpan(clickableSpan2, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Define os textos nos TextViews
            textView.setText(spannable);
            tvRegistro.setText(spannable2);

            // Permite clicar nos spans
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            tvRegistro.setMovementMethod(LinkMovementMethod.getInstance());



            return insets;
        });
        findViewById(R.id.imageButtonSite).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
            startActivity(intentSite);
        });
    }


    public void fazerLogin(View V) {
        EditText editTextLogin = findViewById(R.id.editTextLogin);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        String loginField = editTextLogin.getText().toString().trim();  // Campo de login (assumo email)

        if (loginField.isEmpty() || editTextPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://"+meuServidor+"/apiTest/selectLogin.php";  // URL do servidor PHP
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {

                            // -------- PEGAR DADOS DO USUÁRIO --------
                            String nome = json.getString("nome");
                            int idUsuario = json.getInt("user_id");  // ← PEGA O ID DO SERVIDOR!

                            Toast.makeText(this, "Bem-vindo, " + nome + "!", Toast.LENGTH_SHORT).show();

                            // -------- SALVAR NO SHARED PREFERENCES --------
                            SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putInt("userId", idUsuario);// ← SALVA O ID
                            editor.putString("userEmail", loginField);  // ← SALVA EMAIL
                            editor.putString("empresaName", nome);
                            editor.apply();

                            Log.d("LoginDebug", "Email salvo: " + loginField);
                            Log.d("LoginDebug", "ID salvo: " + idUsuario);

                            // -------- SEGUIR PARA O DASHBOARD --------
                            Intent intent = new Intent(MainActivity3.this, dashboard.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            String message = json.getString("message");
                            if (message.contains("não")) {
                                Intent intent = new Intent(MainActivity3.this, liberaAcessoDois.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Erro: " + e.getMessage());
                        Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage();
                    if (errorMsg == null) {
                        errorMsg = "Timeout ou o servidor não respondeu corretamente";
                    }
                    Log.e("VolleyError", "Erro de rede: " + errorMsg + " - Resposta da rede: " + (error.networkResponse != null ? error.networkResponse.statusCode : "null"));
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("login_field", loginField);
                params.put("password", editTextPassword.getText().toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout em ms (10s)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(request);
    }

    public void voltarRegistro(View V){
        Intent intent = new Intent(MainActivity3.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}