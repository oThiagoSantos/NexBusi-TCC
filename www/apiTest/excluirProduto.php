<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de exclusão de produtos do app.
 *Projeto de Conclusão de Curso: NexBusi
*/
include "connect.php"; // aqui você cria a variável $db (PDO)

header("Content-Type: application/json; charset=utf-8");

if (!isset($_POST["id"])) {
    echo json_encode(["status" => "erro", "msg" => "ID não enviado"]);
    exit;
}

$id = intval($_POST["id"]);

if ($db === null) {
    echo json_encode(["status" => "erro", "msg" => "Falha na conexão"]);
    exit;
}

try {
    $sql = "DELETE FROM produtos WHERE id_produto = :id";
    $stmt = $db->prepare($sql);
    $stmt->bindParam(":id", $id, PDO::PARAM_INT);

    if ($stmt->execute()) {
        echo json_encode(["status" => "ok"]);
    } else {
        echo json_encode(["status" => "erro", "msg" => "Falha no execute()"]);
    }

} catch (Exception $e) {
    echo json_encode(["status" => "erro", "msg" => $e->getMessage()]);
}
?>
