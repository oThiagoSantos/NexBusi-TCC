<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para aba de atividades recentes.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

header("Content-Type: application/json");

date_default_timezone_set('America/Sao_Paulo');
$db->query("SET time_zone = '-03:00'");

try {

    // Permite pegar email via POST ou GET
    $email = $_POST['email'] ?? $_GET['email'] ?? '';
    if (empty($email)) { 
        echo json_encode(["success" => false, "error" => "Email não fornecido"]); 
        exit; 
    }

    // Busca id do usuário pelo email
    $stmtUser = $db->prepare("SELECT id FROM users WHERE email = :email");
    $stmtUser->bindParam(':email', $email);
    $stmtUser->execute();
    $user = $stmtUser->fetch(PDO::FETCH_ASSOC);

    if (!$user) { 
        echo json_encode(["success" => false, "error" => "Usuário não encontrado"]); 
        exit; 
    }

    $userId = $user['id'];

    // Query corrigida: cada parâmetro tem nome único para o PDO
    $sql = "
    SELECT * FROM (
        (SELECT 
            he.id AS id,
            COALESCE(p.nome_produto, 'Produto removido') AS produto,
            'Entrada' AS tipo,
            he.quantidade AS quantidade,
            he.data_entrada AS data,
            he.observacao AS observacao
        FROM historico_entradas he
        LEFT JOIN produtos p ON he.id_produto = p.id_produto
        WHERE he.data_entrada IS NOT NULL AND p.id_usuario = :id_usuario_entrada)

        UNION ALL

        (SELECT 
            hs.id_saida AS id,
            COALESCE(p.nome_produto, 'Produto removido') AS produto,
            'Saída' AS tipo,
            hs.quantidade AS quantidade,
            hs.data_saida AS data,
            hs.observacao AS observacao
        FROM historico_saidas hs
        LEFT JOIN produtos p ON hs.id_produto = p.id_produto
        WHERE hs.data_saida IS NOT NULL AND p.id_usuario = :id_usuario_saida)
    ) AS atividades
    ORDER BY data DESC, id DESC
    LIMIT 3;
    ";

    $stmt = $db->prepare($sql);
    $stmt->bindParam(':id_usuario_entrada', $userId, PDO::PARAM_INT);
    $stmt->bindParam(':id_usuario_saida', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $atividades = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "atividades" => $atividades ?: []
    ]);

} catch (PDOException $e) {
    echo json_encode([
        "success" => false,
        "error" => "Erro ao buscar atividades",
        "details" => $e->getMessage()
    ]);
}
?>
