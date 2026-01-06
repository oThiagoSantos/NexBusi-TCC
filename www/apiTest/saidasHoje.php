<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para listar as saídas do dia de hoje.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

date_default_timezone_set('America/Sao_Paulo');
$db->query("SET time_zone = '-03:00'");

try {
    $email = $_POST['email'] ?? '';
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

    // Conta saídas apenas do usuário
    $stmt = $db->prepare("
        SELECT COUNT(*) AS total_saidas
        FROM historico_saidas hs
        INNER JOIN produtos p ON hs.id_produto = p.id_produto
        WHERE DATE(hs.data_saida) = CURDATE() AND p.id_usuario = :id_usuario
    ");
    $stmt->bindParam(':id_usuario', $userId, PDO::PARAM_INT);
    $stmt->execute();

    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "total_saidas" => $result['total_saidas'] ?? 0
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}
?>
