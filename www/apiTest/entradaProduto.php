<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de entrada de produtos no app.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

date_default_timezone_set('America/Sao_Paulo');
$db->query("SET time_zone = '-03:00'");

// Verifica conexão
if (!$db) {
    echo json_encode(["error" => "Falha na conexão com o banco."]);
    exit;
}

// Recebe dados do POST
$id_produto  = $_POST["id_produto"] ?? null;
$quantidade  = $_POST["quantidade"] ?? null;
$preco_custo = $_POST["preco_custo"] ?? null;
$observacao  = $_POST["observacao_produto"] ?? null;

if (!$id_produto || !$quantidade) {
    echo json_encode(["error" => "Dados incompletos."]);
    exit;
}

try {
    $db->beginTransaction();

    // 1️⃣ Atualiza quantidade do produto
    $stmt = $db->prepare("
        UPDATE produtos 
        SET quantidade_produto = quantidade_produto + :quantidade 
        WHERE id_produto = :id_produto
    ");
    $stmt->execute([":quantidade" => $quantidade, ":id_produto" => $id_produto]);

    // 2️⃣ Atualiza preço de custo (se informado)
    if ($preco_custo !== null && $preco_custo !== "") {
        $preco_custo = floatval(str_replace(",", ".", $preco_custo)); // garante float
        $stmt2 = $db->prepare("
            UPDATE produtos 
            SET preco_custo = :preco 
            WHERE id_produto = :id_produto
        ");
        $stmt2->execute([":preco" => $preco_custo, ":id_produto" => $id_produto]);
    }

    // 3️⃣ Atualiza observação do produto (se informada)
    if ($observacao !== null && trim($observacao) !== "") {
        $stmt3 = $db->prepare("
            UPDATE produtos 
            SET observacao_produto = CONCAT(IFNULL(observacao_produto, ''), '\nEntrada: ', :obs, ' (', NOW(), ')') 
            WHERE id_produto = :id_produto
        ");
        $stmt3->execute([":obs" => $observacao, ":id_produto" => $id_produto]);
    }

    // 4️⃣ Insere no histórico de entradas (agora com custo_unitario)
    $custo_unitario = $preco_custo ?? 0; // garante que sempre haverá um valor
    $stmtHist = $db->prepare("
        INSERT INTO historico_entradas (id_produto, quantidade, observacao, custo_unitario, data_entrada)
        VALUES (:id_produto, :quantidade, :observacao, :custo_unitario, NOW())
    ");
    if (!$stmtHist->execute([
        ":id_produto"    => $id_produto,
        ":quantidade"    => $quantidade,
        ":observacao"    => $observacao,
        ":custo_unitario"=> $custo_unitario
    ])) {
        $errorInfo = $stmtHist->errorInfo();
        throw new Exception("Falha ao inserir no histórico de entradas: " . implode(", ", $errorInfo));
    }

    $db->commit();
    echo json_encode(["success" => true, "message" => "Entrada registrada com sucesso."]);

} catch (Exception $e) {
    $db->rollback();
    error_log("Erro entradaProduto: " . $e->getMessage());
    echo json_encode(["error" => "Erro ao registrar entrada", "details" => $e->getMessage()]);
}
?>
