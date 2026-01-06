package com.example.myapplication;

import static android.graphics.Typeface.BOLD;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.myapplication.MainActivity.meuServidor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class dashboard extends AppCompatActivity {
    private boolean entradasCarregadas = false;
    private boolean saidasCarregadas = false;

    private TextView tvTotalProdutos, tvLucroLiquido, tvAlertas, mudancas, tvExibirAlertas, tvVariacaoLucro, tvVariacaoSaidasHoje, tvVariacaoTotalProduto, tvExibirSaidasHoje;
    private int valorLucroLiquido = 0;
    private int contaAlertas = 0;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private String userEmail;

    private String lucroReal = "";
    private String saidasHojeReal = "";
    private String alertasReal = "";
    private String totalProdutosReal = "";
    private boolean dadosVisiveis = true;

    private LinearLayout linearLayout, containerAlerta, containerAtividadesRecentes;

    private double variacaoLucro, variacaoTotal, variacaoSaidas;


    public static final List<Produto> listaProdutos = new ArrayList<>();
    public static final List<historicoProdutoEntrada> listaHistoricoEntrada = new ArrayList<>();
    public static final List<historicoProdutoSaida> listaHistoricoSaida = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            return insets;
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar2);
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
        containerAtividadesRecentes = findViewById(R.id.containerAtividadesRecentes);
        containerAlerta = findViewById(R.id.containerAtividade);
        tvTotalProdutos = findViewById(R.id.totalProdutos);
        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layoutdados);
        tvAlertas = findViewById(R.id.tvAlertas);
        tvLucroLiquido = findViewById(R.id.lucroLiquido);
        tvExibirAlertas = findViewById(R.id.exibirAlertasEstoque);
        tvVariacaoLucro = findViewById(R.id.tvVariacaoLucro);
        tvVariacaoSaidasHoje = findViewById(R.id.tvVariacaoSaidasHoje);
        tvVariacaoTotalProduto = findViewById(R.id.tvVariacaoTotalProduto);
        tvExibirSaidasHoje = findViewById(R.id.exibirSaidasHoje);
        mudancas = findViewById(R.id.mudancas);
        linearLayout.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);

        findViewById(R.id.cardCadastrar).setOnClickListener(v ->
                startActivity(new Intent(this, cadastro.class))
        );

        findViewById(R.id.cardEstoque).setOnClickListener(v ->
                startActivity(new Intent(this, estoque.class))
        );

        findViewById(R.id.entradaCardLayout2).setOnClickListener(v ->
                startActivity(new Intent(this, entrada.class))
        );

        findViewById(R.id.saidaCardLayout).setOnClickListener(v ->
                startActivity(new Intent(this, saida.class))
        );

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", null);

        carregarDadosMenuLateral();

        if (userEmail == null) {
            Toast.makeText(this, "Erro: Faça login novamente.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadUserProducts(() -> {
                carregarNumeroDeSaidasHoje(() -> {
                    carregarVariacoesDia();
                    loadHistoricoEntradas(() -> {
                        entradasCarregadas = true;});

                    loadHistoricoSaidas(() -> saidasCarregadas = true);

                });
            });
            carregarAtividades(); // pode continuar em paralelo
            ImageView iconEye = findViewById(R.id.iconEye);

            iconEye.setOnClickListener(v -> {

                if (dadosVisiveis) {
                    iconEye.setImageResource(R.drawable.iconclosedeye);

                    tvLucroLiquido.setText("•••••");
                    tvExibirSaidasHoje.setText("•••••");
                    tvAlertas.setText("•••••");
                    tvTotalProdutos.setText("•••••");
                    tvVariacaoTotalProduto.setText("•••••");
                    tvVariacaoSaidasHoje.setText("•••••");
                    tvVariacaoLucro.setText("•••••");

                    // APLICAR BLUR
                    aplicarBlur(tvLucroLiquido, true);
                    aplicarBlur(tvExibirSaidasHoje, true);
                    aplicarBlur(tvAlertas, true);
                    aplicarBlur(tvTotalProdutos, true);
                    aplicarBlur(tvVariacaoTotalProduto, true);
                    aplicarBlur(tvVariacaoSaidasHoje, true);
                    aplicarBlur(tvVariacaoLucro, true);

                } else {
                    iconEye.setImageResource(R.drawable.iconopenedeye);

                    tvLucroLiquido.setText(lucroReal);
                    tvExibirSaidasHoje.setText(saidasHojeReal);
                    tvExibirAlertas.setText(alertasReal);
                    tvTotalProdutos.setText(totalProdutosReal);
                    tvAlertas.setText(String.valueOf(contaAlertas));
                    tvVariacaoLucro.setText(formatarVariacao(variacaoLucro, tvVariacaoLucro));
                    tvVariacaoTotalProduto.setText(formatarVariacao(variacaoTotal, tvVariacaoTotalProduto));
                    tvVariacaoSaidasHoje.setText(formatarVariacao(variacaoSaidas, tvVariacaoSaidasHoje));

                    // REMOVER BLUR
                    aplicarBlur(tvLucroLiquido, false);
                    aplicarBlur(tvExibirSaidasHoje, false);
                    aplicarBlur(tvExibirAlertas, false);
                    aplicarBlur(tvAlertas, false);
                    aplicarBlur(tvTotalProdutos, false);
                    aplicarBlur(tvVariacaoSaidasHoje, false);
                    aplicarBlur(tvVariacaoTotalProduto, false);
                    aplicarBlur(tvVariacaoLucro, false);
                }

                dadosVisiveis = !dadosVisiveis;
            });
            findViewById(R.id.itemSobre).setOnClickListener(v -> {
                Intent intentSite = new Intent(Intent.ACTION_VIEW);
                intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
                startActivity(intentSite);
            });


        }

    }
    private void loadUserProducts(Runnable callback) {
        valorLucroLiquido = 0;
        contaAlertas = 0;
        listaProdutos.clear();

        String url = "https://" + meuServidor + "/apiTest/get_user_products.php";

        Log.d("DEBUG_LOAD", "Chamando URL: " + url + " com email: " + userEmail);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DEBUG_LOAD", "Resposta recebida: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        // Se houver erro, exibe e garante callback
                        if (jsonObject.has("error")) {
                            Toast.makeText(this, jsonObject.optString("error", "Erro desconhecido"), Toast.LENGTH_SHORT).show();
                            if (callback != null) callback.run();
                            return;
                        }

                        JSONArray produtosArray = jsonObject.optJSONArray("produtos");
                        if (produtosArray == null) produtosArray = new JSONArray();

                        List<Produto> produtosComAlerta = new ArrayList<>();
                        containerAlerta.removeAllViews();

                        for (int i = 0; i < produtosArray.length(); i++) {
                            JSONObject obj = produtosArray.optJSONObject(i);
                            if (obj == null) continue;

                            Produto p = new Produto();
                            p.idUsuario = obj.optInt("id_usuario", 0);
                            p.id = obj.optInt("id_produto", 0);
                            p.nome = obj.optString("nome_produto", "");
                            p.caminhoImagem = obj.optString("caminho_imagem", "");
                            p.descricao = obj.optString("descricao", "");
                            p.codigoBarras = obj.optString("codigo_barras", "");
                            p.precoCusto = obj.optDouble("preco_custo", 0.0);
                            p.precoRevenda = obj.optDouble("preco_revenda", 0.0);
                            p.alertaMinimo = obj.optInt("alerta_minimo", 0);
                            p.quantidade = obj.optInt("quantidade_produto", 0);
                            p.idCategoria = obj.optInt("id_categoria", 0);
                            p.nomeCategoria = obj.optString("nome_categoria", "");

                            listaProdutos.add(p);
                            valorLucroLiquido += (int) ((p.precoRevenda - p.precoCusto) * p.quantidade);

                            if (p.alertaMinimo > p.quantidade) {
                                produtosComAlerta.add(p);
                                ImageView iconAlerta = findViewById(R.id.imageView3);
                                iconAlerta.setVisibility(VISIBLE);

                                TextView exibirAlertasEstoque = findViewById(R.id.exibirAlertasEstoque);
                                Drawable bg = exibirAlertasEstoque.getBackground();
                                bg.setTint(Color.RED);
                                TextView semAlertasEstoque = findViewById(R.id.semAlertas);
                                semAlertasEstoque.setVisibility(GONE);
                            }
                        }

                        contaAlertas = produtosComAlerta.size();
                        carregarCards(produtosComAlerta);

                        progressBar.setVisibility(GONE);
                        linearLayout.setVisibility(VISIBLE);

                        tvAlertas.setText(String.valueOf(contaAlertas));
                        tvExibirAlertas.setText(String.valueOf(contaAlertas));

                        if (contaAlertas > 0) {
                            mudancas.setTextColor(Color.RED);
                            mudancas.setTypeface(null, BOLD);
                            mudancas.setText("Verifique seus produtos!");
                        }

                        tvLucroLiquido.setText("R$" + mostraLucroTotal(valorLucroLiquido));
                        tvTotalProdutos.setText(String.valueOf(listaProdutos.size()));

                        lucroReal = tvLucroLiquido.getText().toString();
                        totalProdutosReal = tvTotalProdutos.getText().toString();
                        alertasReal = tvExibirAlertas.getText().toString();


                        if (callback != null) callback.run(); // chama callback sempre
                    } catch (JSONException e) {
                        Log.e("DEBUG_LOAD", "Erro JSON: " + e.getMessage(), e);
                        Toast.makeText(this, "Erro ao processar JSON", Toast.LENGTH_SHORT).show();
                        if (callback != null) callback.run();
                    }
                },
                error -> {
                    Log.e("DEBUG_LOAD", "Erro de rede: " + error.getMessage(), error);
                    Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.run();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail != null ? userEmail : "");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
    private void loadHistoricoEntradas(Runnable callback) {

        listaHistoricoEntrada.clear();

        String url = "https://" + meuServidor + "/apiTest/getHistoricoEntradas.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DEBUG_ENTRADAS", "Resposta: " + response);

                    try {
                        JSONObject mainObj = new JSONObject(response);

                        if (mainObj.has("error")) {
                            Log.e("DEBUG_ENTRADAS", "Erro API: " + mainObj.getString("error"));
                            if (callback != null) callback.run();
                            return;
                        }

                        JSONArray array = mainObj.getJSONArray("historico");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            historicoProdutoEntrada h = new historicoProdutoEntrada();

                            h.id = obj.optInt("id", 0);
                            h.idProduto = obj.optInt("id_produto", 0);
                            h.quantidade = obj.optInt("quantidade", 0);
                            h.custoUnitario = obj.optDouble("custo_unitario", 0.0);
                            h.observacao = obj.optString("observacao", "");
                            h.dataEntrada = obj.optString("data_entrada", "");

                            listaHistoricoEntrada.add(h);
                        }

                        Log.d("DEBUG_ENTRADAS", "Total carregado: " + listaHistoricoEntrada.size());

                    } catch (Exception e) {
                        Log.e("DEBUG_ENTRADAS", "Erro JSON: " + e.getMessage());
                    }

                    if (callback != null) callback.run();
                },
                error -> {
                    Log.e("DEBUG_ENTRADAS", "Erro de rede: " + error.toString());
                    if (callback != null) callback.run();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail != null ? userEmail : "");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
    private void loadHistoricoSaidas(Runnable callback) {

        listaHistoricoSaida.clear();

        String url = "https://" + meuServidor + "/apiTest/getHistoricoSaidas.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SAIDAS_RAW", "Resposta completa: " + response);

                    try {
                        JSONObject mainObj = new JSONObject(response);

                        if (mainObj.has("error")) {
                            Log.e("DEBUG_SAIDAS", "Erro API: " + mainObj.getString("error"));
                            if (callback != null) callback.run();
                            return;
                        }

                        JSONArray array = mainObj.getJSONArray("historico");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            historicoProdutoSaida h = new historicoProdutoSaida();

                            h.id = obj.optInt("id_saida", 0);
                            h.id_produto = obj.optInt("id_produto", 0);
                            h.quantidade = obj.optInt("quantidade", 0);
                            h.motivo = obj.optString("motivo", "");  // ⬅ novo campo vindo do banco
                            h.dataSaida = obj.optString("data_saida", "");
                            h.observacao = obj.optString("observacao", "");

                            listaHistoricoSaida.add(h);
                        }

                        Log.d("DEBUG_SAIDAS", "Total carregado: " + listaHistoricoSaida.size());

                    } catch (Exception e) {
                        Log.e("SAIDAS_ERROR", "Erro na requisição: " + e.getMessage());
                    }

                    if (callback != null) callback.run();
                },
                error -> {
                    Log.e("DEBUG_SAIDAS", "Erro de rede: " + error.toString());
                    if (callback != null) callback.run();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail != null ? userEmail : "");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }



    private void carregarAtividades() {
        String url = "https://" + meuServidor + "/apiTest/getAtividadesRecentes.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        containerAtividadesRecentes.removeAllViews();

                        if (!json.optBoolean("success", false)) {
                            Toast.makeText(this, "Erro ao carregar atividades", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray array = json.optJSONArray("atividades");
                        if (array == null || array.length() == 0) {
                            // Array vazio: mostra uma mensagem amigável
                            TextView tvVazio = findViewById(R.id.semAtividades);
                            tvVazio.setVisibility(VISIBLE);
                            return;
                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String nome = obj.optString("produto", "Produto removido");
                            String tipo = obj.optString("tipo", "Entrada");
                            String dataBruta = obj.optString("data", "");
                            String qtd = obj.optString("quantidade", "0");

                            View item = getLayoutInflater().inflate(
                                    R.layout.item_atividade_recente,
                                    containerAtividadesRecentes,
                                    false
                            );

                            TextView tvNome = item.findViewById(R.id.textView17);
                            TextView tvTipo = item.findViewById(R.id.textView16);
                            TextView tvTempo = item.findViewById(R.id.textView15);
                            TextView tvAtividade = item.findViewById(R.id.atividade);
                            TextView tvQuantidade = item.findViewById(R.id.textView19);
                            MaterialCardView containerMCV = item.findViewById(R.id.containerMCV);

                            tvNome.setText(nome);
                            tvTipo.setText(tipo + " de estoque");
                            tvTempo.setText(tempoRelativo(dataBruta));
                            tvAtividade.setText(tipo);

                            if (tipo.equals("Entrada")) {
                                containerMCV.setCardBackgroundColor(Color.parseColor("#98FFA4"));
                                tvAtividade.setTextColor(Color.parseColor("#005906"));
                            } else {
                                containerMCV.setCardBackgroundColor(Color.RED);
                                tvAtividade.setTextColor(Color.WHITE);
                            }

                            tvQuantidade.setText("Qntd: " + qtd);

                            containerAtividadesRecentes.addView(item);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar atividades", Toast.LENGTH_SHORT).show();
                    }

                }, error -> Toast.makeText(this, "Erro na conexão", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail != null ? userEmail : "");
                return params;
            }
        };

        queue.add(request);
    }

    private void carregarNumeroDeSaidasHoje(Runnable callback) {
        TextView tvExibirSaidasHoje = findViewById(R.id.exibirSaidasHoje);
        String url = "https://" + meuServidor + "/apiTest/saidasHoje.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            int total = obj.getInt("total_saidas");
                            tvExibirSaidasHoje.setText(String.valueOf(total));
                            saidasHojeReal = String.valueOf(total);
                        } else {
                            tvExibirSaidasHoje.setText("Erro ao carregar");
                        }
                        if (callback != null) callback.run();
                    } catch (Exception e) {
                        tvExibirSaidasHoje.setText("Erro");
                        e.printStackTrace();
                        if (callback != null) callback.run();
                    }
                },
                error -> {
                    tvExibirSaidasHoje.setText("Erro de rede");
                    error.printStackTrace();
                    if (callback != null) callback.run();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void carregarVariacoesDia() {
        String url = "https://" + meuServidor + "/apiTest/variacoesDia.php";

        Log.d("DEBUG_VARIACOES", "Chamando URL: " + url + " com email: " + userEmail);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DEBUG_VARIACOES", "Resposta recebida: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.optBoolean("success", false)) {
                             variacaoLucro = obj.optDouble("variacaoLucro", 0);
                             variacaoTotal = obj.optDouble("variacaoProdutos", 0);
                             variacaoSaidas = obj.optDouble("variacaoSaidas", 0);

                            tvVariacaoLucro.setText(formatarVariacao(variacaoLucro, tvVariacaoLucro));
                            tvVariacaoTotalProduto.setText(formatarVariacao(variacaoTotal, tvVariacaoTotalProduto));
                            tvVariacaoSaidasHoje.setText(formatarVariacao(variacaoSaidas, tvVariacaoSaidasHoje));

                        } else {
                            Log.e("DEBUG_VARIACOES", "JSON success=false");
                            tvVariacaoLucro.setText("Erro");
                            tvVariacaoTotalProduto.setText("Erro");
                            tvVariacaoSaidasHoje.setText("Erro");
                        }
                    } catch (JSONException e) {
                        Log.e("DEBUG_VARIACOES", "Erro JSON: " + e.getMessage(), e);
                        tvVariacaoLucro.setText("Erro");
                        tvVariacaoTotalProduto.setText("Erro");
                        tvVariacaoSaidasHoje.setText("Erro");
                    }
                },
                error -> {
                    Log.e("DEBUG_VARIACOES", "Erro de rede: " + error.getMessage(), error);
                    tvVariacaoLucro.setText("Erro");
                    tvVariacaoTotalProduto.setText("Erro");
                    tvVariacaoSaidasHoje.setText("Erro");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail != null ? userEmail : "");
                return params;
            }
        };

        requestQueue.add(request);
    }


    /**
     * Formata a variação como porcentagem com sinal e cor
     * Cada TextView recebe cor individual
     */
    private String formatarVariacao(double valor, TextView textView) {
        String texto = String.format(Locale.getDefault(), "%.2f%%", valor);

        if (valor > 0) textView.setTextColor(Color.parseColor("#18C45A")); // verde
        else if (valor < 0) textView.setTextColor(Color.RED);
        else textView.setTextColor(Color.GRAY);

        return texto;
    }


    /**
     * Formata a variação como porcentagem com sinal e cor:
     * positivo -> verde, negativo -> vermelho, zero -> cinza
     */


    private void carregarDadosMenuLateral() {
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);

        ((TextView) findViewById(R.id.nomeEmpresa))
                .setText(prefs.getString("empresaName", "Minha Empresa"));

        ((TextView) findViewById(R.id.emailEmpresa))
                .setText(prefs.getString("userEmail", "email@empresa.com"));
    }

    private void carregarCards(List<Produto> produtos) {

        LayoutInflater inflater = LayoutInflater.from(this);

        int limite = Math.min(3, produtos.size());


        for (int i = 0; i < limite; i++) {

            Produto produto = produtos.get(i);

            View card = inflater.inflate(R.layout.item_alerta_estoque, containerAlerta, false);

            TextView tvNome = card.findViewById(R.id.nomeProduto);
            TextView tvEstoque = card.findViewById(R.id.exibirValorEstoque);
            TextView tvMinimo = card.findViewById(R.id.exibirValorEstoqueMinimo);
            TextView tvSituacao = card.findViewById(R.id.situacaoProduto);
            MaterialCardView container = card.findViewById(R.id.containerMCV);

            if (produto.quantidade == 0) {
                container.setCardBackgroundColor(Color.RED);
                tvSituacao.setText("Crítico");
                tvSituacao.setTextColor(Color.WHITE);
            }

            tvNome.setText(produto.nome);
            tvEstoque.setText("Estoque: " + produto.quantidade + " |");
            tvMinimo.setText("Mínimo: " + produto.alertaMinimo);

            containerAlerta.addView(card);
        }
    }
    private String tempoRelativo(String dataString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

            long tempoEvento = Objects.requireNonNull(sdf.parse(dataString)).getTime();
            long agora = System.currentTimeMillis();
            long diff = agora - tempoEvento;

            long segundos = diff / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            long dias = horas / 24;

            if (segundos < 60) return "há " + segundos + " segundos";
            if (minutos < 60) return "há " + minutos + " minutos";
            if (horas < 24) return "há " + horas + " horas";
            return "há " + dias + " dias";

        } catch (Exception e) {
            return dataString;
        }
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
    public void viajarFeedback(View V){
        Intent intent = new Intent(dashboard.this, feedback_card.class);
        startActivity(intent);
    }



    private String mostraLucroTotal(int val) {
        if (val < 1000) return String.valueOf(val);
        else if (val < 1_000_000) return String.format("%.1fK", val / 1000.0);
        else if (val < 1_000_000_000) return val / 1_000_000 + "M";
        else return val / 1_000_000_000 + "B";
    }
    public void viajarMinhaConta(View V){
        Intent intent = new Intent (dashboard.this, perfil.class);
        startActivity(intent);
    }

    public void fazerLogout(View V) {
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(dashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public static class historicoProdutoEntrada {
        public int id;
        public int idProduto;
        public int quantidade;
        public double custoUnitario;
        public String observacao;
        public String dataEntrada;
    }


    public static class Produto {
        public int id;
        public String nome;
        public String caminhoImagem;
        public String descricao;
        public String codigoBarras;
        public double precoCusto;
        public double precoRevenda;
        public int alertaMinimo;
        public int quantidade;
        public String nomeCategoria;

        public int idCategoria;
        public int idUsuario;
    }
    public static class historicoProdutoSaida {
        public int id;
        public int id_produto;
        public int quantidade;
        public String motivo;      // ⬅ novo atributo solicitado
        public String dataSaida;

        public String observacao;
    }
}
