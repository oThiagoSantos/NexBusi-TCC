package com.example.myapplication;

import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class detalhesProduto extends AppCompatActivity {


    private LinearLayout container, container2;
    private int idProdutoAtual;
    private final ArrayList<View> listaCards = new ArrayList<>();
    double precoRevenda = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_produto);

        container = findViewById(R.id.containerEntrada);
        container2 = findViewById(R.id.containerSaida);



        // 🔥 Primeiro: carregar interface e dados fixos
        configurarDrawer();
        carregarDadosMenuLateral();

        // 🔥 Agora sim: ler os dados enviados pelo estoque.java
        Intent intent = getIntent();

        idProdutoAtual = intent.getIntExtra("id", -1);
        String nome = intent.getStringExtra("nome");
        int quantidade = intent.getIntExtra("quantidade", 0);
        double precoCusto = intent.getDoubleExtra("precoCusto", 0.0);
        precoRevenda = intent.getDoubleExtra("precoRevenda", 0.0);
        int alertaMinimo = intent.getIntExtra("alertaMinimo", 0);
        String descricao = intent.getStringExtra("descricao");
        String caminhoImagem = intent.getStringExtra("caminhoImagem");

        int idCategoria = intent.getIntExtra("idCategoria", 0);
        String nomeCategoria = intent.getStringExtra("categoria");

        // 🔥 Agora preencher a tela com esses dados
        preencherDadosDoProduto(nome, quantidade, precoCusto, precoRevenda, alertaMinimo, descricao, caminhoImagem, nomeCategoria);

        // 🔥 Carregar cards do histórico
        carregarCards();

        findViewById(R.id.itemSobre).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
            startActivity(intentSite);
        });
    }


    private void carregarCards() {

        double total = 0.0;
        container.removeAllViews();
        container2.removeAllViews();
        listaCards.clear();

        LayoutInflater inflater = LayoutInflater.from(this);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // =====================================
        // 🔥 1 — FILTRAR E LISTAR AS 3 ÚLTIMAS ENTRADAS DO PRODUTO
        // =====================================

        ArrayList<dashboard.historicoProdutoEntrada> entradasFiltradas = new ArrayList<>();

        for (dashboard.historicoProdutoEntrada h : dashboard.listaHistoricoEntrada) {
            Log.d("TESTE_ENTRADA", "Entrada -> idProduto=" + h.idProduto + " | idAtual=" + idProdutoAtual);
            if (h.idProduto == idProdutoAtual) {
                entradasFiltradas.add(h);
            }
        }

        Collections.reverse(entradasFiltradas);
        int limiteEntrada = Math.min(3, entradasFiltradas.size());

        if(entradasFiltradas.isEmpty()){
            TextView mostrarAviso = findViewById(R.id.avisoSemEntradas);
            mostrarAviso.setVisibility(VISIBLE);
        }


        for (int i = 0; i < limiteEntrada; i++) {

            dashboard.historicoProdutoEntrada h = entradasFiltradas.get(i);

            View card = inflater.inflate(R.layout.item_detalhes_produto, container, false);

            MaterialCardView entradaOuSaidaMCV = card.findViewById(R.id.entrouSaiuMCV);
            entradaOuSaidaMCV.setCardBackgroundColor(Color.parseColor("#0CF25D"));
            entradaOuSaidaMCV.setStrokeColor(Color.TRANSPARENT);
            TextView quantidadeEntrouOuSaiu = card.findViewById(R.id.quantidadeEntrouOuSaiu);
            quantidadeEntrouOuSaiu.setTextColor(Color.parseColor("#038C3E"));
            TextView exibirData = card.findViewById(R.id.exibirData);
            TextView valorCustouEntrada = card.findViewById(R.id.valorQueCustouEntrada);

            quantidadeEntrouOuSaiu.setText("+" + h.quantidade + " unidades");
            exibirData.setText(formatarData(h.dataEntrada));

            total = h.custoUnitario * h.quantidade;
            valorCustouEntrada.setText(nf.format(total));

            container.addView(card);
            listaCards.add(card);
        }


        // =====================================
        // 🔥 2 — FILTRAR E LISTAR AS 3 ÚLTIMAS SAÍDAS DO PRODUTO
        // =====================================

        ArrayList<dashboard.historicoProdutoSaida> saidasFiltradas = new ArrayList<>();
        // ===============================
        // 🔥 3 — CALCULAR ENTRADAS E SAÍDAS NOS ÚLTIMOS 30 DIAS
        // ===============================

        long agora = System.currentTimeMillis();
        long trintaDiasMs = 30L * 24 * 60 * 60 * 1000;
        long limiteMs = agora - trintaDiasMs;

        int totalEntradasMes = 0;
        int totalSaidasMes = 0;

        // ➤ Filtrar ENTRADAS dos últimos 30 dias
        for (dashboard.historicoProdutoEntrada h : dashboard.listaHistoricoEntrada) {
            if (h.idProduto == idProdutoAtual) {
                long dataMs = converterDataParaMillis(h.dataEntrada);
                if (dataMs >= limiteMs) {
                    totalEntradasMes += h.quantidade;
                }
            }
        }

        // ➤ Filtrar SAÍDAS dos últimos 30 dias
        for (dashboard.historicoProdutoSaida s : dashboard.listaHistoricoSaida) {
            if (s.id_produto == idProdutoAtual) {
                long dataMs = converterDataParaMillis(s.dataSaida);
                if (dataMs >= limiteMs) {
                    totalSaidasMes += s.quantidade;
                }
            }
        }

        // ➤ Exibir na tela
        TextView valorEntradasNoMes = findViewById(R.id.valorEntradasNoMes);
        TextView valorSaidasNoMes = findViewById(R.id.valorSaidasNoMes);

        valorEntradasNoMes.setText(totalEntradasMes + " Un.");
        valorSaidasNoMes.setText(totalSaidasMes + " Un.");


        for (dashboard.historicoProdutoSaida s : dashboard.listaHistoricoSaida) {
            Log.d("TESTE_SAIDA", "Saida -> id_produto=" + s.id_produto + " | idAtual=" + idProdutoAtual);
            if (s.id_produto == idProdutoAtual) {
                saidasFiltradas.add(s);
            }
        }

        Collections.reverse(saidasFiltradas);
        int limiteSaida = Math.min(3, saidasFiltradas.size());

        if(saidasFiltradas.isEmpty()){
            TextView mostrarAviso = findViewById(R.id.avisoSemSaidas);
            mostrarAviso.setVisibility(VISIBLE);
        }

        for (int i = 0; i < limiteSaida; i++) {

            dashboard.historicoProdutoSaida s = saidasFiltradas.get(i);

            View card = inflater.inflate(R.layout.item_detalhes_produto, container2, false);


            MaterialCardView entradaOuSaidaMCV = card.findViewById(R.id.entrouSaiuMCV);
            entradaOuSaidaMCV.setCardBackgroundColor(Color.RED);
            entradaOuSaidaMCV.setStrokeColor(Color.TRANSPARENT);
            TextView quantidadeEntrouOuSaiu = card.findViewById(R.id.quantidadeEntrouOuSaiu);
            quantidadeEntrouOuSaiu.setTextColor(Color.WHITE);
            TextView exibirData = card.findViewById(R.id.exibirData);
            TextView valorCustouEntrada = card.findViewById(R.id.valorQueCustouEntrada);

            quantidadeEntrouOuSaiu.setText("-" + s.quantidade + " unidades");
            exibirData.setText(formatarData(s.dataSaida));

            valorCustouEntrada.setText("+"+nf.format(precoRevenda * s.quantidade));

            container2.addView(card);
            listaCards.add(card);
        }
    }

    private void preencherDadosDoProduto(String nome, int quantidade, double precoCusto, double precoRevenda,
                                         int alertaMinimo, String descricao, String caminhoImagem, String categoria) {

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        TextView txtNome = findViewById(R.id.nomeDoProduto);
        TextView txtCategoria = findViewById(R.id.categoriaDoProduto);
        TextView txtQuantidade = findViewById(R.id.valorQuantidadeProduto);
        TextView txtCusto = findViewById(R.id.valorPrecoCusto);
        TextView txtRevenda = findViewById(R.id.valorPrecoRevenda);
        TextView txtDescricao = findViewById(R.id.descricaoDoProduto);
        TextView txtEstoqueMinimo = findViewById(R.id.valorEstoqueMinimo);
        TextView txtValorTotal = findViewById(R.id.valorTotalProdutos);
        MaterialCardView produtoMCV = findViewById(R.id.statusProdutoMCV);
        TextView txtStatusProduto = findViewById(R.id.status);

        txtNome.setText(nome);
        txtCategoria.setText(categoria);
        txtQuantidade.setText(quantidade + " unidades");
        txtCusto.setText(nf.format(precoCusto));
        txtRevenda.setText(nf.format(precoRevenda));
        txtEstoqueMinimo.setText(String.valueOf(alertaMinimo));
        txtDescricao.setText(descricao);
        txtValorTotal.setText(nf.format(precoCusto * quantidade));
        if(alertaMinimo > quantidade && quantidade > 0){

        } else if (quantidade == 0){
            produtoMCV.setCardBackgroundColor(Color.RED);
            produtoMCV.setStrokeColor(Color.TRANSPARENT);
            txtStatusProduto.setText("Crítico");
            txtStatusProduto.setTextColor(Color.WHITE);
        } else{
            produtoMCV.setCardBackgroundColor(Color.parseColor("#98FFA4"));
            produtoMCV.setStrokeColor(Color.TRANSPARENT);
            txtStatusProduto.setText("Normal");
            txtStatusProduto.setTextColor(Color.parseColor("#005906"));
        }

        assert caminhoImagem != null;
        if (!caminhoImagem.startsWith("/")) {
            caminhoImagem = "/" + caminhoImagem;
        }

        // Se estiver usando Glide:

    ImageView img = findViewById(R.id.imagemProduto);
    String urlImagem = "https://" + MainActivity.meuServidor + caminhoImagem;

    Glide.with(this)
        .load(urlImagem)
        .placeholder(R.drawable.placeholder)
        .into(img);
    }
    private String formatarData(String dataBruta) {
        try {
            // tenta ler datas com hora
            java.text.SimpleDateFormat formatoEntrada1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.text.SimpleDateFormat formatoEntrada2 = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.text.SimpleDateFormat formatoSaida = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            java.util.Date date;

            if (dataBruta.contains(":")) {
                date = formatoEntrada1.parse(dataBruta);
            } else {
                date = formatoEntrada2.parse(dataBruta);
            }

            return formatoSaida.format(date);

        } catch (Exception e) {
            return dataBruta; // se falhar, retorna como veio
        }
    }
    private long converterDataParaMillis(String data) {
        try {
            SimpleDateFormat formato1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat formato2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date d;

            if (data.contains(":")) {
                d = formato1.parse(data);
            } else {
                d = formato2.parse(data);
            }

            return d.getTime();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void configurarDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        findViewById(R.id.iconNotification).setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.openDrawer(Gravity.END);
            } else {
                drawerLayout.closeDrawer(Gravity.END);
            }
        });
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

    public void fazerLogout(View V) {

        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();
        editor.apply();

        Intent intent = new Intent(detalhesProduto.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
    public void viajarFeedback(View V){
        Intent intent = new Intent(detalhesProduto.this, feedback_card.class);
        startActivity(intent);
    }

    public void voltarPraEstoque(View V){
        Intent intent = new Intent(detalhesProduto.this, estoque.class);
        startActivity(intent);
        finish();
    }
}
