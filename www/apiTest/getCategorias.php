<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para exibir categorias.
 *Projeto de Conclusão de Curso: NexBusi
*/
require_once "connect.php";

if (!$db) {
    echo json_encode(["success" => false, "message" => "Erro na conexão com banco"]);
    exit;
}

$id_usuario = $_GET['id_usuario'] ?? null;

if (!$id_usuario) {
    echo json_encode(["success" => false, "message" => "ID do usuário não fornecido"]);
    exit;
}

try {
    $stmt = $db->prepare("SELECT id_categoria, nome_categoria FROM categoria WHERE id_usuario = ?");
    $stmt->execute([$id_usuario]);
    $categorias = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode(["success" => true, "categorias" => $categorias]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
