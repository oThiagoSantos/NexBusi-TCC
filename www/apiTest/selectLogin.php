<?php

/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de login para acessar o app.
 *Projeto de Conclusão de Curso: NexBusi
*/

// Verifica usuário e senha e valida a flag
// Flag deve ser alterada manualmente para 1 no BD para ativar o usuário


error_reporting(0);  // Desabilita exibição de erros na tela (mas permite logging)
ob_start();  // Buffer para capturar output indesejado

require 'connect.php';  // Arquivo de conexão (retorna $db como PDO)

ob_end_clean();  // Limpa qualquer output anterior

// Headers para JSON (antes de qualquer output)
header('Content-Type: application/json');

// Verifica se é POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Método não permitido']);
    exit;
}

// Recebe dados do POST (de EditText no Android: login_field para email/usuario, password para senha)
$login_field = trim($_POST['login_field'] ?? '');  // Email ou usuario
$password = $_POST['password'] ?? '';  // Senha

// Validação básica
if (empty($login_field) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Campos obrigatórios não preenchidos']);
    exit;
}

// Verifica se $db está definida (evita erro se connect.php falhar)
if (!isset($db) || !$db) {
    echo json_encode(['success' => false, 'message' => 'Erro de conexão com o banco']);
    exit;
}

// Prepared statement para segurança (evita SQL Injection)
try {
    // SQL: Verifica por 'usuario' OU 'email'
    $sql = "SELECT id, nome, cnpj, email, senha, usuario, flag FROM users WHERE (usuario = :user_param OR email = :email_param) LIMIT 1";
    $stmt = $db->prepare($sql);
    
    // Bind DUAS VEZES: Uma para cada placeholder, com o MESMO valor ($login_field)
    $stmt->bindParam(':user_param', $login_field);
    $stmt->bindParam(':email_param', $login_field);
    
    $stmt->execute();

    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user && $user['senha'] === $password) {  // Primeiro: Verifica usuário e senha
        // Verifica flag (assume 1 = ativo, 0 = inativo)
        if ($user['flag'] == 1) {  // Flag ativa
            echo json_encode([
                'success' => true,
                'message' => 'Login bem-sucedido',
                'user_id' => $user['id'],
                'nome' => $user['nome'],
                'cnpj' => $user['cnpj'],
                'email' => $user['email'],
                'usuario' => $user['usuario'],
                'flag' => $user['flag']
            ]);
        } else {  // Flag inativa (0)
            echo json_encode([
                'success' => false,
                'message' => 'Sua conta ainda não foi validada.'
            ]);
        }
    } else {
        // Dados inválidos (usuário não existe ou senha errada)
        echo json_encode(['success' => false, 'message' => 'Email/Usuário ou senha incorretos']);
    }
} catch (Exception $e) {
    // Captura erros SQL ou PDO sem expor detalhes ao cliente (segurança)
    // manda o erro real para o log do servidor (pasta tmp no laragon)
    error_log("ERRO NO LOGIN - selectLogin.php: " . $e->getMessage() . " | SQL: " . $sql . " | Login Field: " . $login_field);
    echo json_encode(['success' => false, 'message' => 'Erro interno no servidor']);
}

// Fecha conexão
$db = null;
?>