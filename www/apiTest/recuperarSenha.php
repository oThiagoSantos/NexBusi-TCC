<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo para exibir a senha do usuário para recuperá-la no app.
 *Projeto de Conclusão de Curso: NexBusi
*/
require "connect.php"; // sua conexão PDO

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// IMPORTS PHPMailer
require 'PHPMailer/src/PHPMailer.php';
require 'PHPMailer/src/SMTP.php';
require 'PHPMailer/src/Exception.php';

// 1️⃣ Verifica se enviou o email
if (!isset($_POST["email"])) {
    echo "Email não enviado.";
    exit;
}

$email = $_POST["email"];

// 2️⃣ Consulta o usuário no banco
$stmt = $db->prepare("SELECT nome, senha FROM users WHERE email = ?");
$stmt->execute([$email]);

if ($stmt->rowCount() == 0) {
    echo "Email não encontrado.";
    exit;
}

$usuario = $stmt->fetch(PDO::FETCH_ASSOC);
$nome = $usuario["nome"];
$senha = $usuario["senha"];

// 3️⃣ Monta mensagem
$assunto = "Recuperar sua senha - NexBusi";
$mensagemHTML = "
    <h2>Recuperação de Senha</h2>
    <p>Olá <b>$nome</b>,</p>
    <p>Sua senha cadastrada é:</p>
    <h3>$senha</h3>
    <p>Recomendamos alterá-la após o login.</p>
";

$mensagemTXT = "Olá $nome,\nSua senha é: $senha";

// 4️⃣ Envio com PHPMailer
$mail = new PHPMailer(true);

try {

    // CONFIG SMTP
    $mail->isSMTP();
    $mail->Host = "smtp.gmail.com";
    $mail->SMTPAuth = true;
    $mail->Username = "nexbusi3@gmail.com";
    $mail->Password = "yuac lqrg cwei endt"; // senha de app
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS; 
    $mail->Port = 587;

    // REMETENTE e DESTINATÁRIO
    $mail->setFrom("nexbusi3@gmail.com", "NexBusi - Recuperar Senha");
    $mail->addAddress($email, $nome);

    // CONTEÚDO
    $mail->isHTML(true);
    $mail->Subject = $assunto;
    $mail->Body    = $mensagemHTML;
    $mail->AltBody = $mensagemTXT;

    // ENVIA
    if ($mail->send()) {
        echo "Senha enviada para seu email!";
    } else {
        echo "Erro ao enviar o email.";
    }

} catch (Exception $e) {
    echo "Erro ao enviar: {$mail->ErrorInfo}";
}
?>
