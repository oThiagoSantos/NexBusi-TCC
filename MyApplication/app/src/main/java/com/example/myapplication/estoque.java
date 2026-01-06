package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class estoque extends AppCompatActivity {

    private LinearLayout container;
    double porcentagemLucro = 0.0;

    private ImageView iconEye;

    private boolean dadosVisiveis;
    private final ArrayList<View> listaCards = new ArrayList<>();

    public double valorTotalEmEstq = 0.0;
    public double valorRevendaTotal = 0.0;

    private int categoriaSelecionada = 0; // 0 = todas
    private String textoPesquisa = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estoque);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);



        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        carregarDadosMenuLateral();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
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

        container = findViewById(R.id.cointanerEntrada);


        carregarCards(); // cria os cards

        AutoCompleteTextView dropdown = findViewById(R.id.autoCategoria);
        carregarCategorias(dropdown); // carrega categorias no dropdown

        EditText campoPesquisa = findViewById(R.id.buscarProduto);

        iconEye = findViewById(R.id.iconEye);

        iconEye.setOnClickListener(v -> {

            TextView valorTotalEstq = findViewById(R.id.valorTotalEstq);
            TextView valorMargem = findViewById(R.id.valorMargem);

            if (dadosVisiveis) {
                iconEye.setImageResource(R.drawable.iconclosedeye);

                valorTotalEstq.setText("•••••");
                valorMargem.setText("•••••");

                // APLICAR BLUR
                aplicarBlur(valorTotalEstq, true);
                aplicarBlur(valorMargem, true);

            } else {
                iconEye.setImageResource(R.drawable.iconopenedeye);

                valorTotalEstq.setText(String.format("R$%s", mostraValorTotal((int) valorTotalEmEstq)));
                valorMargem.setText(Math.round(porcentagemLucro) + "%");

                // REMOVER BLUR
                aplicarBlur(valorTotalEstq, false);
                aplicarBlur(valorMargem,false);
            }

            dadosVisiveis = !dadosVisiveis;
        });
        findViewById(R.id.itemSobre).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
            startActivity(intentSite);
        });

        campoPesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoPesquisa = s.toString();
                aplicarFiltros();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    public void fazerLogout(View V) {

        // Apaga sessão salva
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear(); // Apaga tudo: email, user_id, etc.
        editor.apply();

        // Vai para tela de login
        Intent intent = new Intent(estoque.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Impede voltar para dashboard
    }

    private void carregarCards() {
        container.removeAllViews();
        listaCards.clear();
        valorTotalEmEstq = 0;
        valorRevendaTotal = 0.0;

        TextView custoTotal = findViewById(R.id.valorTotalEstq);
        TextView valorMargem = findViewById(R.id.valorMargem);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (dashboard.Produto produto : dashboard.listaProdutos) {

            View card = inflater.inflate(R.layout.item_produto_estoque, container, false);

            card.setOnClickListener(v -> abrirDetalhesProduto(produto));

            TextView nome = card.findViewById(R.id.nomeProduto2);
            TextView txtViewEstqMin = card.findViewById(R.id.estqMin);
            TextView txtQuantidade = card.findViewById(R.id.quantidadeProduto);
            TextView txtPreco = card.findViewById(R.id.precoProduto);
            TextView txtCusto = card.findViewById(R.id.categoriaProdutoSelecionado);
            TextView txtValorTotal = card.findViewById(R.id.ultimoCusto);
            ProgressBar progressBar = card.findViewById(R.id.progressEstoque);

            TextView txtIdCategoria = card.findViewById(R.id.idCategoriaProduto);
            txtIdCategoria.setText(String.valueOf(produto.idCategoria));
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            nome.setText(produto.nome);
            txtQuantidade.setText(produto.quantidade + " Unidades");
            txtPreco.setText(nf.format(produto.precoRevenda));
            txtCusto.setText(nf.format(produto.precoCusto));


            double valorTotal = produto.precoCusto * produto.quantidade;

            txtViewEstqMin.setText("Estoque Mínimo: " + produto.alertaMinimo);
            txtValorTotal.setText(nf.format(valorTotal));

            valorTotalEmEstq += valorTotal;

            progressBar.setProgress(calcularPorcentagem(produto.quantidade, produto.alertaMinimo));

            double valorRevenda = produto.precoRevenda * produto.quantidade;

             valorRevendaTotal += valorRevenda;

            container.addView(card);
            listaCards.add(card);

            ImageButton excluirBtn = card.findViewById(R.id.excluirProdutoBtn);
            excluirBtn.setOnClickListener(v -> confirmarExclusao(produto.id, card));
        }

        custoTotal.setText(String.format("R$%s", mostraValorTotal((int) valorTotalEmEstq)));

        double lucroTotal = valorRevendaTotal - valorTotalEmEstq;


        if(valorTotalEmEstq > 0){
            porcentagemLucro = (lucroTotal / valorTotalEmEstq) * 100;
        }

        valorMargem.setText(Math.round(porcentagemLucro) + "%");


    }

    private void carregarCategorias(AutoCompleteTextView dropdown) {

        String url = "https://" + meuServidor + "/apiTest/listarCategorias.php";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {

                    try {
                        JSONObject json = new JSONObject(response);

                        if (!json.getString("status").equals("ok")) {
                            return;
                        }

                        JSONArray arr = json.getJSONArray("categorias");

                        ArrayList<String> nomesCategorias = new ArrayList<>();
                        ArrayList<Integer> idsCategorias = new ArrayList<>();

                        // Categoria 0 = todas
                        nomesCategorias.add("Todas");
                        idsCategorias.add(0);

                        // Lê cada categoria do JSON corretamente
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject cat = arr.getJSONObject(i);

                            nomesCategorias.add(cat.getString("nome_categoria"));
                            idsCategorias.add(cat.getInt("id_categoria"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                nomesCategorias
                        );

                        dropdown.setAdapter(adapter);

                        dropdown.setOnItemClickListener((parent, view, position, id) -> {
                            categoriaSelecionada = idsCategorias.get(position);
                            aplicarFiltros();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                },
                Throwable::printStackTrace
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);

                // ⚠️ AQUI ESTÁ O ERRO ORIGINAL:
                // você está tentando pegar "idUsuario", mas seu login salva "userId"
                int idUsuario = prefs.getInt("userId", -1);

                params.put("id_usuario", String.valueOf(idUsuario));

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }



    private void aplicarFiltros() {
        for (View card : listaCards) {

            TextView txtIdCategoria = card.findViewById(R.id.idCategoriaProduto);
            int idCat = Integer.parseInt(txtIdCategoria.getText().toString());

            TextView nome = card.findViewById(R.id.nomeProduto2);
            String nomeProduto = nome.getText().toString().toLowerCase();

            boolean passaCategoria = (categoriaSelecionada == 0 || idCat == categoriaSelecionada);
            boolean passaTexto = nomeProduto.contains(textoPesquisa.toLowerCase());

            card.setVisibility(passaCategoria && passaTexto ? View.VISIBLE : View.GONE);
        }
    }

    private void confirmarExclusao(int idProduto, View cardView) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir Produto")
                .setMessage("Tem certeza de que deseja excluir este produto?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto(idProduto, cardView))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void excluirProduto(int idProduto, View cardView) {

        String url = "https://" + meuServidor + "/apiTest/excluirProduto.php";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    if (response.contains("ok")) {
                        container.removeView(cardView);
                    }
                },
                Throwable::printStackTrace
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(idProduto));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
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
    public void voltarPraDashBoard(View V){
        Intent intent = new Intent(estoque.this, dashboard.class);
        startActivity(intent);
    }
    private void abrirDetalhesProduto(dashboard.Produto produto) {
        Intent intent = new Intent(estoque.this, detalhesProduto.class);

        intent.putExtra("id", produto.id);
        intent.putExtra("nome", produto.nome);
        intent.putExtra("quantidade", produto.quantidade);
        intent.putExtra("precoCusto", produto.precoCusto);
        intent.putExtra("precoRevenda", produto.precoRevenda);
        intent.putExtra("alertaMinimo", produto.alertaMinimo);
        intent.putExtra("descricao", produto.descricao);
        intent.putExtra("caminhoImagem", produto.caminhoImagem);
        intent.putExtra("idCategoria", produto.idCategoria);
        intent.putExtra("categoria", produto.nomeCategoria);

        startActivity(intent);
        finish();
    }
    public void viajarFeedback(View V){
        Intent intent = new Intent(estoque.this, feedback_card.class);
        startActivity(intent);
    }
    private void aplicarBlur(TextView tv, boolean aplicar) {
        if (aplicar) {
            tv.setAlpha(0.3f);
            tv.setShadowLayer(18f, 0, 0, Color.BLACK);
        } else {
            tv.setAlpha(1f);
            tv.setShadowLayer(0f, 0, 0, Color.TRANSPARENT);
        }
    }

    private String mostraValorTotal(int valordeEstoque) {
        if (valordeEstoque < 1000) return String.valueOf(valordeEstoque);
        else if (valordeEstoque < 1_000_000) return valordeEstoque / 1000 + "K";
        else if (valordeEstoque < 1_000_000_000) return valordeEstoque / 1_000_000 + "M";
        else return valordeEstoque / 1_000_000_000 + "B";
    }

    private int calcularPorcentagem(int quantidade, int alerta) {
        if (alerta == 0) alerta = 1;
        int porcentagem = (int) ((quantidade * 100f) / alerta);
        return Math.max(0, Math.min(porcentagem, 100));
    }
}
