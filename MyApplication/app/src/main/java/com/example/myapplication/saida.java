package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class saida extends AppCompatActivity {

    private LinearLayout produtoSelecionadoLayout;
    private LinearLayout detalhesEntradaLayout;

    private TextView nomeProdutoSelecionado;
    private TextView categoriaProdutoSelecionado;
    private TextView valorEmEstoqueSelecionado;

    private EditText qntdProduto2;
    private EditText motivoSaida;
    private EditText descricaoProduto;
    private dashboard.Produto produtoSelecionadoObj;


    private ProdutoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saida);

        MaterialButton btnRegistrar = findViewById(R.id.btnSaidaProduto);
        btnRegistrar.setOnClickListener(v -> registrarSaidaNoServidor());

        produtoSelecionadoLayout = findViewById(R.id.produtoSelecionadoLayout);
        detalhesEntradaLayout = findViewById(R.id.detalhesEntradaLayout);

        nomeProdutoSelecionado = findViewById(R.id.nomeProdutoSelecionado);
        categoriaProdutoSelecionado = findViewById(R.id.categoriaProdutoSelecionado);
        valorEmEstoqueSelecionado = findViewById(R.id.valorEmEstoqueSelecionado);

        qntdProduto2 = findViewById(R.id.qntdProduto3);
        motivoSaida = findViewById(R.id.motivoSaida);
        descricaoProduto = findViewById(R.id.descricaoProduto2);

        // Toolbar e Drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar6);
        setSupportActionBar(toolbar);
        carregarDadosMenuLateral();



        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        findViewById(R.id.iconNotification).setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.openDrawer(Gravity.END);
            } else {
                drawerLayout.closeDrawer(Gravity.END);
            }
        });
        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.cointanerEntrada);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter com listener
        adapter = new ProdutoAdapter(dashboard.listaProdutos, this::selecionarProduto);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.itemSobre).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://rianrodrigues.byethost12.com/Bartotech/"));
            startActivity(intentSite);
        });

        // Campo de pesquisa
        EditText campoPesquisa = findViewById(R.id.buscarProduto2);
        campoPesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().toLowerCase();
                List<dashboard.Produto> filtrados = new ArrayList<>();
                for (dashboard.Produto p : dashboard.listaProdutos) {
                    if (p.nome.toLowerCase().contains(texto)) filtrados.add(p);
                }
                // Atualiza o mesmo adapter com a lista filtrada
                adapter = new ProdutoAdapter(filtrados, produto -> selecionarProduto(produto));
                recyclerView.setAdapter(adapter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    private void registrarSaidaNoServidor() {

        if (produtoSelecionadoObj == null) {
            Toast.makeText(this, "Selecione um produto!", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantidadeDigitada = qntdProduto2.getText().toString().trim();
        String motivoDigitado = motivoSaida.getText().toString().trim();



        if (motivoDigitado.isEmpty()) {
            Toast.makeText(this, "Digite o motivo da saída!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Agora já é seguro converter
        String observacao = descricaoProduto.getText().toString().trim();

        if (quantidadeDigitada.isEmpty()) {
            Toast.makeText(this, "Preencha quantidade e custo!", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantidadeSaida = Integer.parseInt(quantidadeDigitada);
        // Impedir saída maior que o estoque
        if (quantidadeSaida > produtoSelecionadoObj.quantidade) {
            Toast.makeText(this, "Quantidade solicitada maior que o estoque disponível!", Toast.LENGTH_SHORT).show();
            return;
        }


        String url = "https://" + meuServidor + "/apiTest/saidaProduto.php";

        StringRequest requisicao = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Saída registrada!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Erro ao registrar saída!", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_produto", String.valueOf(produtoSelecionadoObj.id));
                params.put("quantidade", String.valueOf(quantidadeSaida));
                params.put("motivo_saida", motivoDigitado);
                params.put("observacao", observacao);
                return params;
            }

        };

        Volley.newRequestQueue(this).add(requisicao);
    }




    private void selecionarProduto(dashboard.Produto produto) {
        produtoSelecionadoObj = produto;
        nomeProdutoSelecionado.setText(produto.nome);
        categoriaProdutoSelecionado.setText(produto.nomeCategoria);
        valorEmEstoqueSelecionado.setText("Estoque atual: " + produto.quantidade);

        qntdProduto2.setText("");
        motivoSaida.setText("");
        descricaoProduto.setText("");

        produtoSelecionadoLayout.setVisibility(LinearLayout.VISIBLE);
        detalhesEntradaLayout.setVisibility(LinearLayout.VISIBLE);
        produtoSelecionadoLayout.requestFocus();
    }

    private void carregarDadosMenuLateral() {
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);

        String nomeEmpresa = prefs.getString("empresaName", "Minha Empresa");
        String emailEmpresa = prefs.getString("userEmail", "email@empresa.com");

        TextView txtNomeEmpresa = findViewById(R.id.nomeEmpresa);
        TextView txtEmailEmpresa = findViewById(R.id.emailEmpresa);

        txtNomeEmpresa.setText(nomeEmpresa);
        txtEmailEmpresa.setText(emailEmpresa);
    }
    public void viajarFeedback(View V){
        Intent intent = new Intent(saida.this, feedback_card.class);
        startActivity(intent);
    }
    public void voltarPraDashBoard(android.view.View V){
        startActivity(new Intent(saida.this, dashboard.class));
    }
}
