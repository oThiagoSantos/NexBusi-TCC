<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para pegar o histórico das saídas.
 *Projeto de Conclusão de Curso: NexBusi
*/

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'connect.php';

error_reporting(E_ALL);
ini_set('display_errors', 1);

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

        // 1️⃣ Buscar ID do usuário
        $stmtUser = $db->prepare("SELECT id FROM users WHERE email = :email");
        $stmtUser->bindParam(':email', $email, PDO::PARAM_STR);
        $stmtUser->execute();
        $user = $stmtUser->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            echo json_encode(["error" => "Usuário não encontrado"]);
            exit;
        }

        $userId = $user['id'];

        // 2️⃣ Buscar histórico de SAÍDAS
        $stmt = $db->prepare("
            SELECT
                s.id_saida AS id_saida,
                s.id_produto,
                s.quantidade,
                s.motivo,
                s.observacao,
                s.data_saida,
                p.nome_produto,
                p.caminho_imagem
            FROM historico_saidas s
            INNER JOIN produtos p ON s.id_produto = p.id_produto
            WHERE p.id_usuario = :id_usuario
            ORDER BY s.data_saida ASC
        ");

        $stmt->bindParam(':id_usuario', $userId, PDO::PARAM_INT);
        $stmt->execute();

        $historico = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // 🟢 IMPORTANTE → AGORA O JSON É RETORNADO
        echo json_encode(["historico" => $historico], JSON_UNESCAPED_UNICODE);

    } catch (PDOException $e) {
        echo json_encode(["error" => "Erro na consulta: " . $e->getMessage()]);
    }

} else {
    echo json_encode(["error" => "Método não permitido"]);
}
?>
