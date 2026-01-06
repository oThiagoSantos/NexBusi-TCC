<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para enviar imagem tirada no app ao servidor.
 *Projeto de Conclusão de Curso: NexBusi
*/
header('Content-Type: application/json; charset=UTF-8');

// Pasta onde as imagens serão salvas
$targetDir = "../ImagensProdutos/";

// Verifica se a pasta existe, se não cria
if (!file_exists($targetDir)) {
    if (!mkdir($targetDir, 0777, true)) {
        echo json_encode([
            "success" => false,
            "message" => "Não foi possível criar a pasta de destino."
        ]);
        exit;
    }
}

if (isset($_FILES['imagem']) && $_FILES['imagem']['error'] === UPLOAD_ERR_OK) {
    $fileTmpPath = $_FILES['imagem']['tmp_name'];
    $fileName = $_FILES['imagem']['name'];

    // Evita sobrescrever arquivos existentes
    $fileName = time() . "_" . preg_replace("/[^a-zA-Z0-9.]/", "_", $fileName);

    $destPath = $targetDir . $fileName;

    if (move_uploaded_file($fileTmpPath, $destPath)) {
        echo json_encode([
            "success" => true,
            "message" => "Imagem enviada com sucesso!",
            "caminho" => "ImagensProdutos/" . $fileName
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Erro ao mover a imagem para a pasta de destino."
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "Nenhuma imagem enviada ou ocorreu um erro."
    ]);
}
?>
