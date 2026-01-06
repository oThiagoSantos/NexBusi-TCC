<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para logout da página de admin.
 *Projeto de Conclusão de Curso: NexBusi
*/
// Inicia a sessão
session_start();

// Destrói todas as variáveis de sessão
$_SESSION = array();

if (ini_get("session.use_cookies")) {
    $params = session_get_cookie_params();
    setcookie(session_name(), '', time() - 42000,
        $params["path"], $params["domain"],
        $params["secure"], $params["httponly"]
    );
}

session_destroy();

// Redireciona para a página de login
header("Location: admin_login.php");
exit;
?>