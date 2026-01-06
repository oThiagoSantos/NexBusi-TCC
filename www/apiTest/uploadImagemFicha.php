<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para enviar imagem e ficha enviadas ao servidor.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

file_put_contents("debug.txt", "FILES:\n".print_r($_FILES, true)."\nPOST:\n".print_r($_POST, true));

$user_id = $_POST["user_id"];

$resp = ["success" => false, "message" => "Nenhum arquivo recebido"];

// --- UPLOAD DE IMAGEM ---
if (isset($_FILES["file"])) {

    $targetDir = "../serverImages/";
    if (!file_exists($targetDir)) mkdir($targetDir, 0777, true);

    $fileName = time() . "_" . basename($_FILES["file"]["name"]);
    $targetFile = $targetDir . $fileName;

    if (move_uploaded_file($_FILES["file"]["tmp_name"], $targetFile)) {

        $stmt = $db->prepare("UPDATE users SET img_caminho = ? WHERE id = ?");
        $stmt->execute([$fileName, $user_id]);

        $resp = ["success" => true, "message" => "Imagem enviada!"];
    }
    echo json_encode($resp);
    exit;
}

// --- UPLOAD DA FICHA ---
if (isset($_FILES["ficha"])) {

    $targetDir = "../serverFiles/";
    if (!file_exists($targetDir)) mkdir($targetDir, 0777, true);

    $fileName = time() . "_" . basename($_FILES["ficha"]["name"]);
    $targetFile = $targetDir . $fileName;

    if (move_uploaded_file($_FILES["ficha"]["tmp_name"], $targetFile)) {

        $stmt = $db->prepare("UPDATE users SET ficha_cadastro = ? WHERE id = ?");
        $stmt->execute([$fileName, $user_id]);

        $resp = ["success" => true, "message" => "PDF enviado com sucesso!"];
    }

    echo json_encode($resp);
    exit;
}

echo json_encode($resp);
?>