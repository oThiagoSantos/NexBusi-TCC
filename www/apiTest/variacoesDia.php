<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para exibir a porcentagem de variação dos dados.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php";

date_default_timezone_set('America/Sao_Paulo');
$db->query("SET time_zone = '-03:00'");

try {
    // Recebe email do POST ou GET
    $email = $_POST['email'] ?? $_GET['email'] ?? '';
    if (empty($email)) {
        echo json_encode(["success" => false, "error" => "Email não fornecido"]);
        exit;
    }

    // Busca id do usuário
    $stmtUser = $db->prepare("SELECT id FROM users WHERE email = :email");
    $stmtUser->bindParam(':email', $email);
    $stmtUser->execute();
    $user = $stmtUser->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode(["success" => false, "error" => "Usuário não encontrado"]);
        exit;
    }

    $userId = $user['id'];

    // --- 1️⃣ Total de produtos e variação por produtos cadastrados ---
    // Total de produtos atualmente
    $stmt = $db->prepare("SELECT COUNT(*) FROM produtos WHERE id_usuario = :userId");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $totalProdutos = intval($stmt->fetchColumn() ?? 0);

    // Quantos produtos foram cadastrados hoje
    $stmt = $db->prepare("SELECT COUNT(*) FROM produtos WHERE id_usuario = :userId AND DATE(data_criacao) = CURDATE()");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $novosHoje = intval($stmt->fetchColumn() ?? 0);

    // Total até ontem
    $totalOntem = $totalProdutos - $novosHoje;

    // Variação em %
    $variacaoProdutos = $totalOntem == 0 ? ($novosHoje > 0 ? 100 : 0) : ($novosHoje / $totalOntem) * 100;

    // --- 2️⃣ Lucro líquido ---
    // Lucro hoje
    $stmt = $db->prepare("
        SELECT SUM((p.preco_revenda - p.preco_custo) * hs.quantidade) 
        FROM produtos p
        LEFT JOIN historico_saidas hs 
               ON p.id_produto = hs.id_produto 
              AND DATE(hs.data_saida) = CURDATE()
        WHERE p.id_usuario = :userId
    ");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $lucroHoje = floatval($stmt->fetchColumn() ?? 0);

    // Lucro ontem
    $stmt = $db->prepare("
        SELECT SUM((p.preco_revenda - p.preco_custo) * hs.quantidade) 
        FROM produtos p
        LEFT JOIN historico_saidas hs 
               ON p.id_produto = hs.id_produto 
              AND DATE(hs.data_saida) = CURDATE() - INTERVAL 1 DAY
        WHERE p.id_usuario = :userId
    ");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $lucroOntem = floatval($stmt->fetchColumn() ?? 0);

    $variacaoLucro = $lucroOntem == 0 ? ($lucroHoje > 0 ? 100 : 0) : (($lucroHoje - $lucroOntem) / $lucroOntem) * 100;

    // --- 3️⃣ Número de saídas ---
    // Saídas hoje
    $stmt = $db->prepare("
        SELECT COUNT(*) 
        FROM historico_saidas hs
        INNER JOIN produtos p ON hs.id_produto = p.id_produto
        WHERE p.id_usuario = :userId AND DATE(hs.data_saida) = CURDATE()
    ");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $saidasHoje = intval($stmt->fetchColumn() ?? 0);

    // Saídas ontem
    $stmt = $db->prepare("
        SELECT COUNT(*) 
        FROM historico_saidas hs
        INNER JOIN produtos p ON hs.id_produto = p.id_produto
        WHERE p.id_usuario = :userId AND DATE(hs.data_saida) = CURDATE() - INTERVAL 1 DAY
    ");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();
    $saidasOntem = intval($stmt->fetchColumn() ?? 0);

    $variacaoSaidas = $saidasOntem == 0 ? ($saidasHoje > 0 ? 100 : 0) : (($saidasHoje - $saidasOntem) / $saidasOntem) * 100;

    // --- Retorna JSON ---
    echo json_encode([
        "success" => true,
        "totalHoje" => $totalProdutos,
        "totalOntem" => $totalOntem,
        "novosHoje" => $novosHoje,
        "variacaoProdutos" => round($variacaoProdutos, 2),
        "lucroHoje" => round($lucroHoje, 2),
        "lucroOntem" => round($lucroOntem, 2),
        "variacaoLucro" => round($variacaoLucro, 2),
        "saidasHoje" => $saidasHoje,
        "saidasOntem" => $saidasOntem,
        "variacaoSaidas" => round($variacaoSaidas, 2)
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "error" => "Erro ao calcular variações",
        "details" => $e->getMessage()
    ]);
}
?>
