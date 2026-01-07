<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo de conexão ao banco de dados.
 *Projeto de Conclusão de Curso: NexBusi
*/
ob_clean();

error_reporting(0);  // Desabilita exibição de erros
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

$uri = $_ENV['DB_URI'] ?? getenv('DB_URI') ?? '';
$fields = parse_url($uri);

// Extrai o nome do banco da própria URI caso DB_NAME não exista
$dbName = $_ENV['DB_NAME'] ?? ltrim($fields["path"] ?? '', '/');

$dsn = "mysql:host=" . ($fields["host"] ?? '') . 
       ";port=" . ($fields["port"] ?? '3306') . 
       ";dbname=" . $dbName;

// Opções do PDO
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_EMULATE_PREPARES => false,
];

// Só adiciona SSL se o arquivo existir
if (file_exists(__DIR__ . '/ca.pem')) {
    $options[PDO::MYSQL_ATTR_SSL_CA] = __DIR__ . '/ca.pem';
}

try {
    $db = new PDO($dsn, $fields["user"] ?? '', $fields["pass"] ?? '', $options);
} catch (PDOException $e) {
    error_log("Erro de conexão: " . $e->getMessage());
    die("Erro ao conectar ao banco de dados. Verifique o log.");
}

ob_end_clean();
?>