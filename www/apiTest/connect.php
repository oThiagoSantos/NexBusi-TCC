<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de conexão ao banco de dados.
 *Projeto de Conclusão de Curso: NexBusi
*/
ob_clean();

error_reporting(0);  // Desabilita exibição de erros (use error_log em produção)
ob_start();

// Função simples para carregar variáveis do .env
function loadEnv($path) {
    if (!file_exists($path)) return;
    $lines = file($path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($name, $value) = explode('=', $line, 2);
        $_ENV[trim($name)] = trim($value, '" ');
    }
}

loadEnv(__DIR__ . '/.env');

// Tenta ler do $_ENV (local) ou do sistema (Azure/Hospedagem)
$uri = $_ENV['DB_URI'] ?? getenv('DB_URI') ?? '';
$fields = parse_url($uri);

// Monta o DSN usando as variáveis de ambiente
$conn = "mysql:host=" . ($fields["host"] ?? '') . 
        ";port=" . ($fields["port"] ?? '') . 
        ";dbname=" . ($_ENV['DB_NAME'] ?? 'defaultdb') . 
        ";sslmode=verify-ca;sslrootcert=ca.pem";

try {
    $db = new PDO($conn, $fields["user"] ?? '', $fields["pass"] ?? '');
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
} catch (PDOException $e) {
    $db = null;
    error_log("Erro de conexão: " . $e->getMessage());
}

ob_end_clean();
?>
?>