<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para pegar o histórico das entradas.
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

        // 2️⃣ Buscar histórico de entradas SOMENTE de produtos desse usuário
        $stmt = $db->prepare("
            SELECT 
                h.id,
                h.id_produto,
                h.quantidade,
                h.custo_unitario,
                h.observacao,
                h.data_entrada,
                p.nome_produto,
                p.caminho_imagem
            FROM historico_entradas h
            INNER JOIN produtos p ON h.id_produto = p.id_produto
            WHERE p.id_usuario = :id_usuario
            ORDER BY h.data_entrada ASC
        ");

        $stmt->bindParam(':id_usuario', $userId, PDO::PARAM_INT);
        $stmt->execute();

        $historico = $stmt->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['historico' => $historico]);

    } catch (PDOException $e) {
        echo json_encode(["error" => "Erro na consulta: " . $e->getMessage()]);
    }

} else {
    echo json_encode(["error" => "Método não permitido"]);
}
?>
