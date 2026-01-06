package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class cadastro extends AppCompatActivity {

    private Uri imageUri;
    private String caminhoImagemServidor = "";

    AutoCompleteTextView autoCategoria;
    Button btnCriarCategoria;

    ArrayList<Categoria> listaCategorias = new ArrayList<>();
    ArrayAdapter<Categoria> categoriaAdapter;

    int idCategoriaSelecionada = -1;
    int idUsuario;

    String URL_LISTAR = "https://" + meuServidor + "/apiTest/listarCategorias.php";
    String URL_ADD_CAT = "https://" + meuServidor + "/apiTest/criarCategoria.php";
    String URL_ADD_PROD = "https://" + meuServidor + "/apiTest/inserirProduto.php";
    String URL_UPLOAD_IMG = "https://" + meuServidor + "/apiTest/uploadImagem.php";

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        // Pegar idUsuario do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        idUsuario = prefs.getInt("userId", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Erro: usuário não encontrado!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // configurar componentes e listeners
        configurarLaunchers();
        configurarDrawer();
        inicializarComponentes();
        configurarAutoComplete();
        carregarCategorias();
        carregarDadosMenuLateral();

        MaterialCardView adicionarFotoBotao = findViewById(R.id.adicionarFoto);
        if (adicionarFotoBotao != null) {
            adicionarFotoBotao.setOnClickListener(V -> abrirCameraGaleria());
        }
        MaterialCardView cameraButton = findViewById(R.id.cameraButton);
        if (cameraButton != null) {
            cameraButton.setOnClickListener(V -> abrirCameraGaleria());
        }
        // Ajuste do status bar padding: CORREÇÃO sintática aqui
        Toolbar toolbar = findViewById(R.id.toolbar3);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.iconNotification).setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.openDrawer(Gravity.END);
            } else {
                drawerLayout.closeDrawer(Gravity.END);
            }
        });
        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            return insets;
        });
        findViewById(R.id.itemSobre).setOnClickListener(v -> {
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            intentSite.setData(Uri.parse("https://nexbusi.byethost24.com/tcc/"));
            startActivity(intentSite);
        });
    }

    // ---------------- Launchers ----------------
    private void configurarLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && imageUri != null) {
                        uploadImagemParaServidor(imageUri);
                    } else {
                        Toast.makeText(this, "Erro ao capturar imagem", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) uploadImagemParaServidor(imageUri);
                    }
                });

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) abrirCamera();
                    else Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
                });
    }

    // ---------------- Camera / Galeria ----------------
    private void abrirCameraGaleria() {
        new AlertDialog.Builder(this)
                .setTitle("Selecionar imagem")
                .setItems(new CharSequence[]{"Câmera", "Galeria"}, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermission.launch(Manifest.permission.CAMERA);
                        } else abrirCamera();
                    } else abrirGaleria();
                }).show();
    }

    private void abrirCamera() {
        try {
            File fotoArquivo = criarArquivoImagem();
            imageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", fotoArquivo);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao criar arquivo da imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    private File criarArquivoImagem() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomeArquivo = "IMG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nomeArquivo, ".jpg", storageDir);
    }

    // ---------------- Upload ----------------
    // ---------------- Upload ----------------
    private void uploadImagemParaServidor(Uri imageUri) {

        // ********** ALTERAÇÃO CHAVE: COMPRESSÃO DE IMAGEM **********
        // Tenta comprimir a imagem com qualidade 70.
        // Se o erro 413 persistir, reduza a qualidade (ex: 50).
        final byte[] imagemComprimida = compressImage(imageUri, 50);

        if (imagemComprimida == null || imagemComprimida.length == 0) {
            Toast.makeText(this, "Falha na compressão da imagem ou arquivo vazio.", Toast.LENGTH_LONG).show();
            return;
        }
        // ************************************************************

        StringRequestMultipart uploadRequest = new StringRequestMultipart(Request.Method.POST,
                URL_UPLOAD_IMG,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            caminhoImagemServidor = json.getString("caminho");
                            // Exibir o novo tamanho para debug:
                            Toast.makeText(this, "Imagem (" + imagemComprimida.length / 1024 + "KB) enviada!", Toast.LENGTH_LONG).show();
                        } else {
                            // Este é o lugar onde o erro 413 apareceria como um JSON de erro
                            Toast.makeText(this, "Erro: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Geralmente, o erro 413 retorna HTML, não JSON, causando uma exceção aqui.
                        Toast.makeText(this, "Erro de servidor (JSON Inválido/413?)", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // O erro de rede do Volley é capturado aqui
                    Log.e("UPLOAD_ERROR", "Erro de Volley: " + error.getMessage());
                    Toast.makeText(this, "Falha no envio da imagem. Verifique o tamanho do arquivo!", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                // ********** USO DOS BYTES COMPRIMIDOS **********
                // Envia a imagem já comprimida para o servidor
                params.put("imagem", new DataPart("produto_comprimido.jpg", imagemComprimida));
                // ***********************************************

                return params;
            }
        };

        uploadRequest.setShouldCache(false); // Boa prática para uploads
        Volley.newRequestQueue(this).add(uploadRequest);
    }

    // ---------------- Drawer ----------------
    private void configurarDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        if (toolbar != null) setSupportActionBar(toolbar);

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.open_drawer, R.string.close_drawer
            );

            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    // ---------------- Inicializa ----------------
    private void inicializarComponentes() {
        autoCategoria = findViewById(R.id.autoCategoria);
        btnCriarCategoria = findViewById(R.id.btnCriarCategoria);

        bloquearPonto(findViewById(R.id.precoCusto));
        bloquearPonto(findViewById(R.id.precoVenda));

        if (btnCriarCategoria != null) {
            btnCriarCategoria.setOnClickListener(v -> abrirDialogCriarCategoria());
        }
    }

    // ---------------- AutoComplete ----------------
    private void configurarAutoComplete() {
        categoriaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listaCategorias
        );

        if (autoCategoria != null) {
            autoCategoria.setAdapter(categoriaAdapter);

            autoCategoria.setOnItemClickListener((parent, view, position, id) -> {
                Categoria cat = categoriaAdapter.getItem(position);
                if (cat != null) idCategoriaSelecionada = cat.getId();
            });
        }
    }

    // ---------------- Carregar Categorias ----------------
    private void carregarCategorias() {
        StringRequest req = new StringRequest(Request.Method.POST, URL_LISTAR,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("ok")) {
                            listaCategorias.clear();
                            JSONArray arr = json.getJSONArray("categorias");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                int id = obj.getInt("id_categoria");
                                String nome = obj.getString("nome_categoria");
                                listaCategorias.add(new Categoria(id, nome));
                            }
                            if (categoriaAdapter != null) categoriaAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar categorias", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erro de conexão ao carregar categorias", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_usuario", String.valueOf(idUsuario));
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
    /**
     * Carrega a imagem da Uri, decodifica, comprime e retorna os bytes comprimidos.
     * @param imageUri Uri da imagem.
     * @param quality Qualidade da compressão (0 a 100).
     * @return Array de bytes da imagem comprimida ou null em caso de falha.
     */
    private byte[] compressImage(Uri imageUri, int quality) {
        try {
            // 1. Obtém o InputStream da Uri
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // 2. Decodifica o InputStream em um Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            // 3. Comprime o Bitmap para um ByteArrayOutputStream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // Comprime no formato JPEG com a qualidade definida (ex: 70)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

            // 4. Converte o Stream para um Array de Bytes
            byte[] compressedBytes = bos.toByteArray();

            // 5. Libera a memória do Bitmap (boa prática)
            bitmap.recycle();

            return compressedBytes;

        } catch (Exception e) {
            Log.e("IMAGEM_COMPRESSAO", "Erro ao comprimir imagem: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- Criar Categoria ----------------
    private void abrirDialogCriarCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Criar nova categoria");

        EditText input = new EditText(this);
        input.setHint("Nome da categoria");
        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) -> {
            String nome = input.getText().toString().trim();
            if (!nome.isEmpty()) criarCategoriaNoServidor(nome);
            else Toast.makeText(this, "Digite um nome válido!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void criarCategoriaNoServidor(String nomeCategoria) {
        StringRequest req = new StringRequest(Request.Method.POST, URL_ADD_CAT,
                response -> {
                    Toast.makeText(this, "Categoria criada!", Toast.LENGTH_SHORT).show();
                    carregarCategorias();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erro ao criar categoria", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_usuario", String.valueOf(idUsuario));
                p.put("nome_categoria", nomeCategoria);
                return p;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    // ---------------- Validação Categoria ----------------
    private boolean validarCategoria() {
        if (autoCategoria == null) return false;
        String nomeDigitado = autoCategoria.getText().toString().trim();
        for (Categoria c : listaCategorias) {
            if (c.getNome().equals(nomeDigitado)) {
                idCategoriaSelecionada = c.getId();
                return true;
            }
        }
        autoCategoria.setError("Selecione uma categoria válida da lista!");
        return false;
    }

    // ---------------- Inserir Produto ----------------
    public void inserirProdutos(android.view.View v) {
        EditText nomeProduto = findViewById(R.id.nomeProdutoSelecionado);
        EditText descricao = findViewById(R.id.descricaoProduto2);
        EditText codigo = findViewById(R.id.codigoBarras);
        EditText custo = findViewById(R.id.precoCusto);
        EditText venda = findViewById(R.id.precoVenda);
        EditText quantidade = findViewById(R.id.quantidadeInicial);
        EditText minimo = findViewById(R.id.estoqueMinimo);

        String precoCustoRaw = custo.getText().toString().trim();
        String precoVendaRaw = venda.getText().toString().trim();
        String quantidadeRaw = quantidade.getText().toString().trim();
        String minimoRaw = minimo.getText().toString().trim();

        // Validações básicas
        if (precoCustoRaw.isEmpty() || precoVendaRaw.isEmpty() || quantidadeRaw.isEmpty() || minimoRaw.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        String precoCusto = precoCustoRaw.replace(",", ".");
        String precoVenda = precoVendaRaw.replace(",", ".");

        try {
            Double.parseDouble(precoCusto);
            Double.parseDouble(precoVenda);
        } catch (Exception e) {
            Toast.makeText(this, "Preços inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarCategoria()) return;
        if (nomeProduto.getText().toString().trim().isEmpty()) {
            nomeProduto.setError("Digite o nome do produto");
            return;
        }
        if (caminhoImagemServidor.isEmpty()) {
            Toast.makeText(this, "Envie uma imagem antes de inserir o produto!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("nome_produto", nomeProduto.getText().toString().trim());
        params.put("descricao", descricao.getText().toString().trim());
        params.put("codigo_barras", codigo.getText().toString().trim());
        params.put("preco_custo", precoCusto);
        params.put("preco_revenda", precoVenda);
        params.put("quantidade_produto", quantidadeRaw);
        params.put("alerta_minimo", minimoRaw);
        params.put("id_categoria", String.valueOf(idCategoriaSelecionada));
        params.put("id_usuario", String.valueOf(idUsuario));
        params.put("caminho_imagem", caminhoImagemServidor);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.d("CADASTRO_PARAMS", entry.getKey() + " = " + entry.getValue());
        }

        StringRequest req = new StringRequest(Request.Method.POST, URL_ADD_PROD,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.optString("message", "Produto inserido!"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar resposta do servidor", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erro ao inserir produto", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    // ---------------- Utilitários ----------------
    private void bloquearPonto(EditText edit) {
        if (edit == null) return;
        edit.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> source.toString().contains(".") ? "" : null
        });
    }

    private void carregarDadosMenuLateral() {
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        String nomeEmpresa = prefs.getString("empresaName", "Minha Empresa");
        String emailEmpresa = prefs.getString("userEmail", "email@empresa.com");

        TextView txtNomeEmpresa = findViewById(R.id.nomeEmpresa);
        TextView txtEmailEmpresa = findViewById(R.id.emailEmpresa);

        if (txtNomeEmpresa != null) txtNomeEmpresa.setText(nomeEmpresa);
        if (txtEmailEmpresa != null) txtEmailEmpresa.setText(emailEmpresa);
    }
    public void viajarFeedback(View V){
        Intent intent = new Intent(cadastro.this, feedback_card.class);
        startActivity(intent);
    }
    // ---------------- Logout e Navegação ----------------
    public void fazerLogout(android.view.View v) {
        SharedPreferences prefs = getSharedPreferences("NexBusiPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(cadastro.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void voltarPraDashBoard(android.view.View v) {
        startActivity(new Intent(cadastro.this, dashboard.class));
    }
}
