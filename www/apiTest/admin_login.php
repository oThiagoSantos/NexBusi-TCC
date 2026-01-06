<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de login para acessar a página de admin.
 *Projeto de Conclusão de Curso: NexBusi
*/
// Inicia a sessão para controle de acesso
session_start();

// Redireciona se o usuário já estiver logado
if (isset($_SESSION['admin_logged_in']) && $_SESSION['admin_logged_in'] === true) {
    header("Location: admin_panel.php");
    exit;
}

// Inclui o arquivo de conexão
include 'connect.php'; // Assume que 'connect.php' define a variável $db

$error_message = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = isset($_POST['email']) ? trim($_POST['email']) : '';
    $senha = isset($_POST['senha']) ? $_POST['senha'] : '';

    if (empty($email) || empty($senha)) {
        $error_message = "Por favor, preencha todos os campos.";
    } elseif ($db === null) {
        $error_message = "Erro de conexão com o banco de dados. Tente mais tarde.";
    } else {
        try {
            // E use uma coluna ou FLAG (ex: flag = 99) para identificar administradores
            $sql = "SELECT id, email, senha FROM users WHERE email = :email AND flag = 2 LIMIT 1"; 
            // Se você não tem uma coluna 'flag' para admins, remova "AND flag = 99"

            $stmt = $db->prepare($sql);
            $stmt->bindParam(':email', $email, PDO::PARAM_STR);
            $stmt->execute();
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($user && $senha === $user['senha']) {
                // Login bem-sucedido
                $_SESSION['admin_logged_in'] = true;
                $_SESSION['admin_user_id'] = $user['id'];
                
                header("Location: admin_panel.php");
                exit;
            } else {
                $error_message = "Email ou senha incorretos.";
            }

        } catch (PDOException $e) {
            error_log("Erro de autenticação: " . $e->getMessage());
            $error_message = "Ocorreu um erro interno. Tente novamente.";
        }
    }
}
?>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Acesso Administrativo</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f4f4; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .login-container { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); width: 300px; }
        h2 { text-align: center; color: #333; margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
        input[type="email"], input[type="password"] { width: 100%; padding: 10px; margin-bottom: 15px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        button { width: 100%; padding: 10px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        button:hover { background-color: #0056b3; }
        .error { color: #dc3545; text-align: center; margin-bottom: 10px; }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>Acesso Painel Admin</h2>
        <?php if ($error_message): ?>
            <p class="error"><?= htmlspecialchars($error_message) ?></p>
        <?php endif; ?>
        <form method="POST">
            <label for="email">Email:</label>
            <input type="email" id="email" name="email" required value="<?= htmlspecialchars($email) ?>">
            
            <label for="senha">Senha:</label>
            <input type="password" id="senha" name="senha" required>
            
            <button type="submit">Entrar</button>
        </form>
    </div>
</body>
</html>