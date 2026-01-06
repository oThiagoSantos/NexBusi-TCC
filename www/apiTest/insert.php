<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para registro digital da empresa do usuário
 *Projeto de Conclusão de Curso: NexBusi
*/
require 'connect.php';

$nomeEmpresa = $_POST["nomeEmpresa"];
$cnpj = $_POST["cnpj"];
$email = $_POST["email"];
$senha = $_POST["senha"];
$usuario = $_POST["usuario"];

try {
    $sql = "INSERT INTO users(nome,cnpj,email,senha,usuario) VALUES (:nome, :cnpj, :email, :senha, :usuario)";
    $stmt = $db->prepare($sql);

    $stmt->bindParam(':nome', $nomeEmpresa);
    $stmt->bindParam(':cnpj', $cnpj);
    $stmt->bindParam(':email', $email);
    $stmt->bindParam(':senha', $senha);
    $stmt->bindParam(':usuario', $usuario);

    $stmt->execute();

    $userId = $db->lastInsertId(); // ✅ pega ID no PDO

    echo $userId; // ✅ envie só o ID
}
catch(Exception $e) {
    echo "ERROR";
}
?>
