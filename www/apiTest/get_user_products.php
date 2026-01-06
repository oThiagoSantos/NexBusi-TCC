<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para listar os produtos do usuário.
 *Projeto de Conclusão de Curso: NexBusi
*/
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'connect.php';

if ($db === null) {
    echo json_encode(["error" => "Falha na conexão com o banco de dados"]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $email = $_POST['email'] ?? '';

    if (empty($email)) {
        echo json_encode(["error" => "Email não fornecido"]);
        exit;
    }

    try {

        // 1️⃣ Buscar o ID do usuário pelo email
        $stmtUser = $db->prepare("SELECT id FROM users WHERE email = :email");
        $stmtUser->bindParam(':email', $email, PDO::PARAM_STR);
        $stmtUser->execute();
        $user = $stmtUser->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            echo json_encode(["error" => "Usuário não encontrado"]);
            exit;
        }

        $userId = $user['id'];

        // 2️⃣ Buscar produtos do usuário (QUERY CORRIGIDA)
        $stmtProducts = $db->prepare("
    SELECT 
        produtos.id_produto, 
        produtos.nome_produto, 
        produtos.caminho_imagem, 
        produtos.descricao, 
        produtos.codigo_barras, 
        produtos.preco_custo, 
        produtos.preco_revenda, 
        produtos.alerta_minimo, 
        produtos.quantidade_produto,
        produtos.id_categoria,
        categoria.nome_categoria AS nome_categoria,
        produtos.id_usuario
    FROM produtos
    LEFT JOIN categoria ON produtos.id_categoria = categoria.id_categoria
    WHERE produtos.id_usuario = :id_usuario
");


        $stmtProducts->bindParam(':id_usuario', $userId, PDO::PARAM_INT);
        $stmtProducts->execute();

        $products = $stmtProducts->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['produtos' => $products]);

    } catch (PDOException $e) {
        echo json_encode(["error" => "Erro na consulta: " . $e->getMessage()]);
    }

} else {
    echo json_encode(["error" => "Método não permitido"]);
}
?>
