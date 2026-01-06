<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para criar um novo produto.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

header("Content-Type: application/json; charset=UTF-8");

if (!$db) {
    echo json_encode(["success" => false, "message" => "Erro na conexão"]);
    exit;
}

// Recebendo dados do POST
$nome = $_POST["nome_produto"] ?? null;
$id_categoria = $_POST["id_categoria"] ?? null;
$caminho_imagem = $_POST["caminho_imagem"] ?? null; // pode ser NULL
$descricao = $_POST["descricao"] ?? null;
$codigo = $_POST["codigo_barras"] ?? null;
$custo = $_POST["preco_custo"] ?? null;
$venda = $_POST["preco_revenda"] ?? null;
$quantidade = $_POST["quantidade_produto"] ?? 0;
$minimo = $_POST["alerta_minimo"] ?? 0;
$id_usuario = $_POST["id_usuario"] ?? null;

// Validação mínima
if (!$nome || !$id_usuario || !$id_categoria) {
    echo json_encode(["success" => false, "message" => "Dados incompletos"]);
    exit;
}

try {
    // Inserir produto usando o ID da categoria enviado pelo app
    // Agora incluímos o campo data_criacao com CURRENT_TIMESTAMP
    $stmt = $db->prepare("
        INSERT INTO produtos 
        (id_usuario, nome_produto, caminho_imagem, descricao, codigo_barras, preco_custo, preco_revenda, alerta_minimo, quantidade_produto, id_categoria, data_criacao) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
    ");

    $result = $stmt->execute([
        $id_usuario,
        $nome,
        $caminho_imagem,
        $descricao,
        $codigo,
        $custo,
        $venda,
        $minimo,
        $quantidade,
        $id_categoria
    ]);

    echo json_encode([
        "success" => $result,
        "message" => $result ? "Produto inserido com sucesso!" : "Erro ao inserir produto."
    ]);

} catch (PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erro SQL",
        "erro" => $e->getMessage()
    ]);
}
?>
