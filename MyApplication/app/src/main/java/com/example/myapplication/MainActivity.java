package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //servidor em nuvem
    //para rodar no celular é o ip 192.168.100.209 e no emulador 10.0.2.2
    public static String meuServidor = "nexbusi-fkepbgdcc2bmgyd7.brazilsouth-01.azurewebsites.net";

    private final validaDados validaDados = new validaDados(); // Assumindo que essa classe existe e tem métodos de validação
    private static final String URL_API = "https://"+ meuServidor +"/apiTest/insert.php"; // Constante para URL
    private static final String URL_CHECK_EMAIL = "https://" + meuServidor + "/apiTest/check_email.php"; // *** NOVO ENDPOINT ***

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Removi EdgeToEdge se não for essencial; adicione de volta se precisar

        // 1. Inicialização dos Views
        EditText nomeEmpresaEt = findViewById(R.id.nomeEmpresa_Et);
        EditText cnpjEt = findViewById(R.id.cnpj_Et);
        EditText emailEt = findViewById(R.id.email_Et);
        EditText senhaEt = findViewById(R.id.senha_Et);
        EditText usernameEt = findViewById(R.id.userName_Et);
        Button button = findViewById(R.id.submit_btn);
        TextView loginEt = findViewById(R.id.facaLogin);
        EditText confSenhaEt = findViewById(R.id.confSenha_Et);
        CheckBox checkBox = findViewById(R.id.checkBox);



        // 2. Configuração do Link de Login (SpannableString)
        String textoLink = "Ja possui uma Conta? Faça Login";
        SpannableString spannableString = new SpannableString(textoLink);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View v) {
                // Vai para a Activity de Login (MainActivity3)
                Intent intentLogin = new Intent(MainActivity.this, MainActivity3.class);
                startActivity(intentLogin);
                finish();
            }
        };
        // "Faça Login" começa no índice 20 (contagem: "Ja possui uma Conta? " = 20 chars)
        spannableString.setSpan(clickableSpan, 20, textoLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginEt.setText(spannableString);
        loginEt.setMovementMethod(LinkMovementMethod.getInstance());

        // 3. Listener do Botão de Submit (Inserção no Banco)
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Captura dos valores dos campos
                String nomeEmpresa = nomeEmpresaEt.getText().toString().trim();
                String cnpj = cnpjEt.getText().toString().trim();
                String email = emailEt.getText().toString().trim();
                String senha = senhaEt.getText().toString().trim();
                String usuario = usernameEt.getText().toString().trim();
                String confSenha = confSenhaEt.getText().toString().trim();

                // Validação expandida (ajuste métodos da validaDados conforme sua implementação)
                if (nomeEmpresa.isEmpty() || !validaDados.validaNomeEmpresa(nomeEmpresa)) {
                    Toast.makeText(MainActivity.this, "Nome da empresa é obrigatório.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cnpj.isEmpty() || !validaDados.validaCNPJ(cnpj)) {
                    Toast.makeText(MainActivity.this, "CNPJ inválido ou vazio.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (email.isEmpty() || !validaDados.validaEmail(email)) {
                    Toast.makeText(MainActivity.this, "Email inválido ou vazio.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (senha.isEmpty() || senha.length() < 6) { // Exemplo simples para senha; use validaDados se tiver
                    Toast.makeText(MainActivity.this, "Senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (usuario.isEmpty() || !validaDados.validaUsuario(usuario)){
                    Toast.makeText(MainActivity.this, "Nome de usuário inválido ou vazio.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!confSenha.equals(senha)){
                    Toast.makeText(MainActivity.this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!checkBox.isChecked()){
                    Toast.makeText(MainActivity.this, "Por favor, marque a caixa para continuar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Se todas validações passarem, insere no banco
                verificarEmailNoBanco(nomeEmpresa, cnpj, email, senha, usuario);
            }
        });
    }
    private void verificarEmailNoBanco(final String nomeEmpresa, final String cnpj, final String email, final String senha, final String usuario) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_CHECK_EMAIL, // Usa o novo endpoint
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean emailExists = jsonResponse.getBoolean("exists");

                            if (emailExists) {
                                // E-mail já existe
                                Toast.makeText(MainActivity.this, "Este e-mail já está em uso. Por favor, faça login ou use outro e-mail.", Toast.LENGTH_LONG).show();
                            } else {
                                // E-mail NÃO existe: Procede com a inserção
                                inserirNoBanco(nomeEmpresa, cnpj, email, senha, usuario);
                            }

                        } catch (Exception e) {
                            // Se a resposta não for um JSON válido ou houver outro erro
                            Toast.makeText(MainActivity.this, "Erro ao processar a verificação de e-mail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Erro de conexão/servidor na verificação
                        Toast.makeText(MainActivity.this, "Erro de rede ao verificar o e-mail. Tente novamente.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email); // Apenas envia o e-mail para verificação
                return params;
            }
        };

        // Enfileira a requisição de verificação
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Método separado para inserir no banco (torna o código mais limpo e reutilizável)
    private void inserirNoBanco(String nomeEmpresa, String cnpj, String email, String senha, String usuario) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_API,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            try {
                                String userId = response.trim();

                                // verifica se retornou número
                                if (!userId.matches("\\d+")) {
                                    Toast.makeText(MainActivity.this, "Erro no servidor: " + response, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                                prefs.edit().putString("user_id", userId).apply();

                                Toast.makeText(MainActivity.this, "ID registrado: " + userId, Toast.LENGTH_SHORT).show();

                                Intent intentConfirm = new Intent(MainActivity.this, ConfirmId.class);
                                startActivity(intentConfirm);

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Erro ao processar resposta!", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }


                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Erro ao processar resposta!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Erro: Mostra mensagem detalhada
                        String errorMessage = "Erro na conexão: " + error.getMessage();
                        if (error.networkResponse != null) {
                            errorMessage += " (Código: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nomeEmpresa", nomeEmpresa);
                params.put("cnpj", cnpj);
                params.put("email", email);
                params.put("senha", senha);
                params.put("usuario", usuario);
                return params;
            }
        };

        // Enfileira a requisição
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Método auxiliar para limpar campos (evita repetição)
    private void limparCampos() {
        EditText nomeEmpresaEt = findViewById(R.id.nomeEmpresa_Et);
        EditText cnpjEt = findViewById(R.id.cnpj_Et);
        EditText emailEt = findViewById(R.id.email_Et);
        EditText senhaEt = findViewById(R.id.senha_Et);
        EditText userNameEt = findViewById(R.id.userName_Et);

        nomeEmpresaEt.setText("");
        cnpjEt.setText("");
        emailEt.setText("");
        senhaEt.setText("");
        userNameEt.setText("");
    }

}
