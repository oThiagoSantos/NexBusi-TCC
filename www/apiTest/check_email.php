<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para verificar se o email já foi cadastrado no app.
 *Projeto de Conclusão de Curso: NexBusi
*/
// Define o tipo de conteúdo como JSON
header('Content-Type: application/json');

// Inclui o arquivo de conexão.
// Ele deve definir a variável $db (objeto PDO ou null em caso de falha).
include 'connect.php';

// Array de resposta padrão (assume que o e-mail não existe até prova em contrário)
$response = ["exists" => false];

if ($db === null) {
    http_response_code(500); // Internal Server Error
    $response = ["error" => true, "message" => "Erro de conexão com o banco de dados."];
    echo json_encode($response);
    exit;
}

// Verifica se o e-mail foi enviado via POST
if (!isset($_POST['email']) || empty(trim($_POST['email']))) {
    http_response_code(400);
    $response = ["error" => true, "message" => "O parâmetro 'email' é obrigatório."];
    echo json_encode($response);
    exit;
}

$email = trim($_POST['email']);


try {
    //  LIMIT 1 pois só preciso saber se existe, não quantos
    $sql = "SELECT id FROM users WHERE email = :email LIMIT 1";
    
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':email', $email, PDO::PARAM_STR);
    $stmt->execute();

    // Verifica se alguma linha foi retornada (e-mail já existe)
    if ($stmt->rowCount() > 0) {
        $response["exists"] = true;
    } 

} catch (PDOException $e) {
    // Erro durante a execução da query
    http_response_code(500); // Internal Server Error
    error_log("Erro na query de verificação de e-mail: " . $e->getMessage());
    $response = ["error" => true, "message" => "Erro interno do servidor."];
}


echo json_encode($response);

?>