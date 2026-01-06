<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para registrar saída do produto.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

date_default_timezone_set('America/Sao_Paulo');
$db->query("SET time_zone = '-03:00'");



if (!$db) {
    echo json_encode(["error" => "Falha na conexão"]);
    exit;
}

$id_produto  = $_POST["id_produto"] ?? null;
$quantidade  = $_POST["quantidade"] ?? null;
$motivo      = $_POST["motivo_saida"] ?? null;
$observacao  = $_POST["observacao"] ?? "";

if (!$id_produto || !$quantidade || !$motivo) {
    echo json_encode(["error" => "Campos obrigatórios faltando"]);
    exit;
}

try {
    $db->beginTransaction();

    // 1. SUBTRAIR quantidade
    $stmt = $db->prepare("
        UPDATE produtos 
        SET quantidade_produto = quantidade_produto - :qntd
        WHERE id_produto = :id
    ");
    $stmt->execute([
        ":qntd" => $quantidade,
        ":id"   => $id_produto
    ]);

    // 2. REGISTRAR SAÍDA
    // data_saida é definida automaticamente pelo MySQL
    $stmt2 = $db->prepare("
        INSERT INTO historico_saidas (id_produto, quantidade, motivo, observacao)
        VALUES (:id, :qnt, :motivo, :obs)
    ");
    $stmt2->execute([
        ":id"     => $id_produto,
        ":qnt"    => $quantidade,
        ":motivo" => $motivo,
        ":obs"    => $observacao
    ]);

    $db->commit();

    echo json_encode(["success" => true]);

} catch (PDOException $e) {
    $db->rollback();
    error_log("Erro saidaProduto: " . $e->getMessage());
    echo json_encode(["error" => "Erro no servidor"]);
}
?>
