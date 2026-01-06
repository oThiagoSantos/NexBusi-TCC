package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class feedback_card extends AppCompatActivity {

    ImageView estrela1, estrela2, estrela3, estrela4, estrela5;
    EditText edtComentario;
    TextView botaoVoltar;

    Button btnAvaliar;

    int avaliacaoSelecionada = 0; // quantidade de estrelas escolhidas (1 a 5)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_card);

        botaoVoltar = findViewById(R.id.botaopravoltar);
        estrela1 = findViewById(R.id.estrela1);
        estrela2 = findViewById(R.id.estrela2);
        estrela3 = findViewById(R.id.estrela3);
        estrela4 = findViewById(R.id.estrela4);
        estrela5 = findViewById(R.id.estrela5);
        edtComentario = findViewById(R.id.edtComentario);
        btnAvaliar = findViewById(R.id.btnAvaliar);

        configurarClicksEstrelas();

// só depois
        botaoVoltar.setOnClickListener(v -> voltarDashboard());
        btnAvaliar.setOnClickListener(v -> salvarAvaliacao());



    }

    // ---------------------------------------------
    // CONFIGURAÇÃO DOS CLIQUES NAS ESTRELAS
    // ---------------------------------------------
    private void configurarClicksEstrelas() {

        estrela1.setOnClickListener(v -> atualizarEstrelas(1));
        estrela2.setOnClickListener(v -> atualizarEstrelas(2));
        estrela3.setOnClickListener(v -> atualizarEstrelas(3));
        estrela4.setOnClickListener(v -> atualizarEstrelas(4));
        estrela5.setOnClickListener(v -> atualizarEstrelas(5));
    }

    // Atualiza o visual das estrelas e guarda a avaliação
    private void atualizarEstrelas(int valor) {

        avaliacaoSelecionada = valor;

        ImageView[] estrelas = {estrela1, estrela2, estrela3, estrela4, estrela5};

        for (int i = 0; i < estrelas.length; i++) {
            if (i < valor) {
                estrelas[i].setImageResource(R.drawable.estrelapreenchidaicone);
            } else {
                estrelas[i].setImageResource(R.drawable.iconeestrelavazia);
            }
        }
    }

    public void voltarDashboard(){
        Intent intent = new Intent(feedback_card.this, dashboard.class);
        startActivity(intent);
    }

    // ---------------------------------------------
    // ENVIA PARA O SERVIDOR PHP
    // ---------------------------------------------
    private void salvarAvaliacao() {

        if (avaliacaoSelecionada == 0) {
            Toast.makeText(this, "Selecione uma quantidade de estrelas!", Toast.LENGTH_SHORT).show();
            return;
        }

        String observacao = edtComentario.getText().toString().trim();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://" + meuServidor + "/apiTest/salvarFeedback.php";

        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", 0);

        StringRequest requisicao = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Erro ao enviar avaliação: " + error, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("avaliacao", String.valueOf(avaliacaoSelecionada));
                params.put("observacao", observacao);
                params.put("id_usuario", String.valueOf(userId));

                return params;
            }
        };

        queue.add(requisicao);
    }
}
