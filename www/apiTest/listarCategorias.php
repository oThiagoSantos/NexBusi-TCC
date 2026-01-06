<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para listar as categorias.
 *Projeto de Conclusão de Curso: NexBusi
*/
include "connect.php";

header("Content-Type: application/json; charset=UTF-8");

if (!$db) {
    echo json_encode(["status" => "erro", "msg" => "Falha na conexão com o banco"]);
    exit;
}
if (!isset($_POST["id_usuario"])) {
    echo json_encode(["status" => "erro", "msg" => "id_usuario não enviado"]);
    exit;
}

$id_usuario = intval($_POST["id_usuario"]);

try {
    $sql = "SELECT id_categoria, nome_categoria 
            FROM categoria 
            WHERE id_usuario = :id_usuario
            ORDER BY nome_categoria ASC";

    $stmt = $db->prepare($sql);
    $stmt->bindParam(":id_usuario", $id_usuario, PDO::PARAM_INT);
    $stmt->execute();

    $categorias = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => "ok",
        "categorias" => $categorias
    ]);

} catch (PDOException $e) {
    echo json_encode([
        "status" => "erro",
        "msg" => "Erro SQL",
        "erro" => $e->getMessage()
    ]);
}
