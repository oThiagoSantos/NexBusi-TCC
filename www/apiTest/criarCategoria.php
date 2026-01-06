<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de criação de categorias.
 *Projeto de Conclusão de Curso: NexBusi
*/
include "connect.php";

header("Content-Type: application/json; charset=UTF-8");

if (!$db) {
    echo json_encode(["status" => "erro", "msg" => "Falha na conexão"]);
    exit;
}

if (!isset($_POST["id_usuario"]) || !isset($_POST["nome_categoria"])) {
    echo json_encode(["status" => "erro", "msg" => "Dados não enviados"]);
    exit;
}

$id_usuario = intval($_POST["id_usuario"]);
$nome_categoria = trim($_POST["nome_categoria"]);

try {
    // Verifica se já existe
    $check = $db->prepare("SELECT id_categoria FROM categoria WHERE nome_categoria = ? AND id_usuario = ?");
    $check->execute([$nome_categoria, $id_usuario]);

    if ($check->rowCount() > 0) {
        echo json_encode(["status" => "existe", "msg" => "Categoria já existe"]);
        exit;
    }

    // Insere nova categoria
    $stmt = $db->prepare("INSERT INTO categoria (id_usuario, nome_categoria) VALUES (?, ?)");
    $stmt->execute([$id_usuario, $nome_categoria]);

    echo json_encode([
        "status" => "ok",
        "msg" => "Categoria criada com sucesso",
        "id_categoria" => $db->lastInsertId()
    ]);

} catch (PDOException $e) {
    echo json_encode([
        "status" => "erro",
        "msg" => "Erro SQL",
        "erro" => $e->getMessage()
    ]);
}
?>
