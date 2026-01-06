package com.example.myapplication;

import static com.example.myapplication.MainActivity.meuServidor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ConfirmId extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_FILE_PICK = 2;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_id);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), this::onApplyWindowInsets);
        // Para gerar link de baixar o arquivo:
        TextView Baixar = findViewById(R.id.baixarDoc);
        String textoLink = "Baixe o arquivo aqui.";
        // Define a String como clicável
        SpannableString spannableString = new SpannableString(textoLink);
        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(@NonNull View V) {
                // Seta o link da onde o download deverá ser feito
                String urlExterna = "https://" + meuServidor + "/FichaCadastral.pdf";
                String nomeArquivo = "FichaCadastral.pdf";
                downloadFile(urlExterna, nomeArquivo);
            }
        };

        spannableString.setSpan(clickableSpan, 0, textoLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Baixar.setText(spannableString);
        Baixar.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // Método para abrir o explorador de arquivos e selecionar um arquivo de ficha cadastral (.xlsx)
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // agora PDF
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Selecione o PDF preenchido"), REQUEST_FILE_PICK);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Nenhum app de arquivos encontrado!", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para iniciar a câmera e tirar foto (salva localmente no diretório de arquivos externos do app)
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Câmera não disponível", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para criar o arquivo da imagem
    private File createImageFile() throws IOException {
        // Cria um timestamp único para o nome do arquivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefixo */
                ".jpg",         /* sufixo */
                storageDir      /* diretório */
        );

        // Salva o caminho completo da foto para uso posterior
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Manipula o resultado da captura de imagem ou seleção de arquivo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // A foto foi salva localmente no caminho currentPhotoPath
            uploadPhotoToServer(currentPhotoPath);
            // Aqui você pode adicionar lógica adicional, como exibir a imagem ou fazer upload
        } else if (requestCode == REQUEST_FILE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedFile = data.getData();

            uploadFichaToServer(selectedFile);
        }
    }

    private void uploadPhotoToServer(String filePath) {
        File file = new File(filePath);
        String originalFileName = file.getName(); // Mantém o nome original do arquivo

        // ********** ALTERAÇÃO CHAVE: COMPRESSÃO **********
        // Tenta comprimir a imagem com qualidade 70 (um bom ponto de partida)
        // Se o upload ainda falhar com 413, tente diminuir esse valor (ex: 50)
        byte[] compressedBytes = compressAndConvertImage(filePath, 70);

        if (compressedBytes == null || compressedBytes.length == 0) {
            Toast.makeText(this, "Erro: Falha ao comprimir ou imagem vazia.", Toast.LENGTH_LONG).show();
            return;
        }
        // *************************************************

        // 1. Cria o RequestBody a partir dos bytes comprimidos
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/jpeg"), compressedBytes);

        // 2. Cria o MultipartBody.Part, usando o nome original do arquivo (originalFileName)
        // e o RequestBody de bytes comprimidos (requestFile)
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", originalFileName, requestFile);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "").trim();

        RequestBody userIdBody =
                RequestBody.create(MultipartBody.FORM, userId);


        Retrofit retrofit = RetrofitClient.getClient("https://" + meuServidor);
        ApiService apiService = retrofit.create(ApiService.class);



        Call<ResponseBody> call = apiService.enviarArquivo(body, userIdBody);


        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String msg;

                    try {
                        msg = response.body() != null ? response.body().string() : "Sem resposta";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // Exibe o novo tamanho do corpo da requisição para debug
                    Toast.makeText(ConfirmId.this, "Sucesso! Tamanho do upload: " + compressedBytes.length / 1024 + " KB", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ConfirmId.this, "Erro no envio! Status: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(ConfirmId.this, "Falha ao enviar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadFichaToServer(Uri fileUri) {
        try {
            byte[] fileBytes;

            try (InputStream is = getContentResolver().openInputStream(fileUri);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                if (is == null) {
                    Toast.makeText(this, "Erro ao abrir PDF!", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] data = new byte[4096];
                int nRead;
                while ((nRead = is.read(data)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                fileBytes = buffer.toByteArray();
            }

            // Nome do arquivo REAL
            String originalName = getFileName(fileUri);

            // Corpo da requisição
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("application/pdf"),  // MIME correto!
                    fileBytes
            );

            MultipartBody.Part fichaPart =
                    MultipartBody.Part.createFormData("ficha", originalName, requestFile);

            // ID do usuário
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            String userId = prefs.getString("user_id", "");

            RequestBody userIdBody =
                    RequestBody.create(MultipartBody.FORM, userId);

            // Retrofit
            Retrofit retrofit = RetrofitClient.getClient("https://" + meuServidor + "/");
            ApiService apiService = retrofit.create(ApiService.class);


            Call<ResponseBody> call = apiService.enviarArquivo(fichaPart, userIdBody);


            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ConfirmId.this, "PDF enviado com sucesso!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ConfirmId.this, "Erro ao enviar PDF!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(ConfirmId.this, "Falha: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao ler PDF!", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Carrega uma imagem de um caminho de arquivo, comprime-a e retorna como um array de bytes.
     * @param filePath O caminho absoluto do arquivo de imagem.
     * @param quality A qualidade da compressão (0 a 100).
     * @return Array de bytes da imagem comprimida ou null em caso de falha.
     */
    private byte[] compressAndConvertImage(String filePath, int quality) {
        // 1. Decodifica o arquivo em um Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if (bitmap == null) {
            return null; // Falha ao decodificar a imagem
        }

        // 2. Comprime o Bitmap para um ByteArrayOutputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Define o formato de compressão (JPEG é ideal para fotos) e a qualidade (ex: 70)
        // O valor 'quality' (qualidade) deve ser entre 0 (máxima compressão, pior qualidade) e 100 (mínima compressão, melhor qualidade)
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

        // 3. Converte o Stream para um Array de Bytes
        byte[] compressedImageBytes = bos.toByteArray();

        // 4. Libera a memória do Bitmap (importante!)
        bitmap.recycle();

        return compressedImageBytes;
    }

    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            result = new File(uri.getPath()).getName();
        }

        // Fallback caso não tenha extensão
        if (!result.endsWith(".pdf")) {
            result = "FichaCadastral_" + System.currentTimeMillis() + ".pdf";
        }

        return result;
    }


    // Manipula o resultado da solicitação de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permissão de câmera negada. Não é possível tirar fotos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // metodo de download private para não ser acessado
    private void downloadFile(String url, String fileName) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        // Configurações
        request.setTitle("Baixando " + fileName);
        request.setDescription("Download em andamento...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Permite download via wifi ou dados móveis
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        // Define MIME type correto para PDF
        request.setMimeType("application/pdf");

        // Salva na pasta Downloads
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Executa o download
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
        } else {
            Toast.makeText(this, "Erro ao iniciar download", Toast.LENGTH_SHORT).show();
        }
    }


    public void navegaParaEspera(View V) {
        Intent intentConfirm = new Intent(ConfirmId.this, liberaAcessoUm.class);
        startActivity(intentConfirm);
    }

    private WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        TextView textView = findViewById(R.id.textView4);
        // Texto completo
        String texto = "Baixe o arquivo para registro e edite pelo\nAdobe Acrobat, depois faça upload aqui.";
        // Cria um SpannableString a partir do texto
        SpannableString spannable = new SpannableString(texto);
        Button btnFile = findViewById(R.id.btnFile);
        Button btnCamera = findViewById(R.id.btnCamera);
        // Define o estilo negrito para a palavra "negrito"
        int start = texto.indexOf("Adobe Acrobat");
        int end = start + "Adobe Acrobat".length();
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica o texto formatado na TextView
        textView.setText(spannable);

        String textoLink = "Ja possui uma Conta? Faça Login";
        SpannableString spannableString = new SpannableString(textoLink);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View v) {
                // Vai para a Activity de Login (MainActivity3)
                Intent intentLogin = new Intent(ConfirmId.this, MainActivity3.class);
                startActivity(intentLogin);
            }
        };
        // "Faça Login" começa no índice 20 (contagem: "Ja possui uma Conta? " = 20 chars)
        TextView strLogin = findViewById(R.id.txt);
        spannableString.setSpan(clickableSpan, 20, textoLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        strLogin.setText(spannableString);
        strLogin.setMovementMethod(LinkMovementMethod.getInstance());

        // Adiciona o listener para o botão da câmera (sem modificar funcionalidade existente)
        btnCamera.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(ConfirmId.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ConfirmId.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                dispatchTakePictureIntent();
            }
        });

        // Adiciona o listener para o botão de arquivo (abre o explorador para selecionar ficha cadastral)
        btnFile.setOnClickListener(view -> openFilePicker());

        return insets;
    }
}
