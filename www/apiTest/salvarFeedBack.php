<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para feedback do usuário em relação ao app.
 *Projeto de Conclusão de Curso: NexBusi
*/
header("Content-Type: application/json; charset=utf-8");
ob_clean();

require_once "connect.php"; // usa seu arquivo de conexão PDO

if (!$db) {
    echo json_encode(["status" => "erro_conexao"]);
    exit;
}

// Coleta dados enviados pelo app
$avaliacao  = isset($_POST["avaliacao"])  ? intval($_POST["avaliacao"]) : 0;
$observacao = isset($_POST["observacao"]) ? trim($_POST["observacao"]) : "";
$id_usuario = isset($_POST["id_usuario"]) ? intval($_POST["id_usuario"]) : 0;

// Validação simples
if ($id_usuario <= 0 || $avaliacao <= 0) {
    echo json_encode(["status" => "dados_invalidos"]);
    exit;
}

try {

    $sql = "UPDATE abcd.users 
            SET avaliacao = :avaliacao, observacao = :observacao
            WHERE id = :id";

    $stmt = $db->prepare($sql);

    $stmt->bindParam(":avaliacao", $avaliacao, PDO::PARAM_INT);
    $stmt->bindParam(":observacao", $observacao, PDO::PARAM_STR);
    $stmt->bindParam(":id", $id_usuario, PDO::PARAM_INT);

    $stmt->execute();

    echo json_encode(["status" => "ok"]);

} catch (PDOException $e) {
    error_log("Erro ao salvar avaliação: " . $e->getMessage());
    echo json_encode(["status" => "erro_query"]);
}
?>
