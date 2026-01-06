<?php

/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para exibir os dados do usuário
 *Projeto de Conclusão de Curso: NexBusi
*/
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); // Permite requisições do app (ajuste para produção)
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Incluir o arquivo de conexão existente
require_once 'connect.php';

// Verificar se a conexão foi estabelecida
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
        // Consulta segura com prepared statement usando PDO
        $stmt = $db->prepare("SELECT nome, senha, cnpj, email, usuario FROM users WHERE email = :email");
        $stmt->bindParam(':email', $email, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($result) {
            echo json_encode($result);
        } else {
            echo json_encode(["error" => "Usuário não encontrado"]);
        }
    } catch (PDOException $e) {
        echo json_encode(["error" => "Erro na consulta: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Método não permitido"]);
}
?>
