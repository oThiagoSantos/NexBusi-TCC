<?php
/* 
 *Nome: Thiago Lima dos Santos
 *Arquivo do painel de admin.
 *Projeto de Conclusão de Curso: NexBusi
*/
// Inicia a sessão
session_start();

// Verifica se o administrador está logado
if (!isset($_SESSION['admin_logged_in']) || $_SESSION['admin_logged_in'] !== true) {
    // Se não estiver logado, redireciona para a página de login
    header("Location: admin_login.php");
    exit;
}

require_once 'connect.php';

// Importação PHPMailer no topo 👇
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Arquivos PHPMailer
// ATENÇÃO: Verifique os caminhos corretos para esses arquivos no seu servidor
require 'phpmailer/src/PHPMailer.php';
require 'phpmailer/src/SMTP.php';
require 'phpmailer/src/Exception.php';

// A parte de conexão com o banco de dados foi movida para connect.php
if ($db === null) {
    die("Erro fatal: Falha na conexão com o banco de dados.");
}

// Atualiza flag via AJAX
if (isset($_POST['id']) && isset($_POST['novo_flag'])) {

    $id = intval($_POST['id']);
    $novo_flag = intval($_POST['novo_flag']);

    $stmt = $db->prepare("UPDATE users SET flag = :flag WHERE id = :id");
    $stmt->execute(['flag' => $novo_flag, 'id' => $id]);

    // Se aprovado → envia e-mail
    if ($novo_flag == 1) {

        $stmtEmail = $db->prepare("SELECT email, nome FROM users WHERE id = ?");
        $stmtEmail->execute([$id]);
        $userData = $stmtEmail->fetch(PDO::FETCH_ASSOC);

        if ($userData) {
            $email = $userData['email'];
            $nome = $userData['nome'];

            $mail = new PHPMailer(true);

            // Configurações SMTP (MANTENHA ESTAS CONFIGURAÇÕES)
            $mail->SMTPDebug = 0; // Altere para 2 para debug
            $mail->Debugoutput = 'error_log';

            $mail->isSMTP();
            $mail->Host = 'smtp.gmail.com';
            $mail->SMTPAuth = true;
            $mail->Username = 'nexbusi3@gmail.com';
            $mail->Password = 'yuac lqrg cwei endt'; // TROQUE PELA SENHA DE APP DO SEU GMAIL
            $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
            $mail->Port = 587;

            $mail->setFrom('nexbusi3@gmail.com', 'Seu App');
            $mail->addAddress($email, $nome);

            $mail->isHTML(true);
            $mail->Subject = '✅ Sua conta foi aprovada!';
            $mail->Body = "
                <strong>Olá $nome,</strong><br><br>
                Sua conta foi <span style='color:green'><b>validada com sucesso ✅</b></span><br>
                Agora você já pode acessar o aplicativo.<br><br>
                Obrigado,<br>
                <em>Equipe da Nexbusi.</em>
            ";

            try {
                $mail->send();
                error_log("EMAIL ENVIADO para $email");
            } catch (Exception $e) {
                error_log("ERRO EMAIL: " . $mail->ErrorInfo);
            }
        }
    }

    echo "ok";
    exit;
}

// Excluir usuário
if (isset($_POST['delete_id'])) {

    $id = intval($_POST['delete_id']);

    $stmt = $db->prepare("DELETE FROM users WHERE id = ?");
    $stmt->execute([$id]);

    echo "deleted";
    exit;
}

// Consulta principal para listar usuários
$stmt = $db->query("SELECT id, nome, cnpj, email, usuario, img_caminho, ficha_cadastro, flag FROM users");
$usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<title>Painel de Administração</title>
<style>
    body {
        font-family: Arial, sans-serif;
        background-color: #f4f4f4;
        padding: 20px;
    }
    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }
    .header h2 {
        margin: 0;
    }
    .logout-btn {
        padding: 8px 15px;
        background-color: #dc3545;
        color: white;
        text-decoration: none;
        border-radius: 5px;
        font-size: 14px;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        background: #fff;
        box-shadow: 0 0 8px rgba(0,0,0,0.1);
    }
    th, td {
        padding: 10px;
        text-align: left;
        border-bottom: 1px solid #ddd;
    }
    th {
        text-align:center;
        background-color: #333;
        color: #fff;
    }
    tr.expanded-row td {
        background-color: #fafafa;
    }
    .arquivos-content {
        display: none;
        padding: 15px;
        background-color: #f9f9f9;
        border-top: 1px solid #ddd;
    }
    .arquivos-content img {
        width: 150px;
        border-radius: 8px;
        margin-bottom: 10px;
        display: block;
    }
    .pdf-link {
        color: #0066cc;
        text-decoration: none;
        display: inline-block;
        margin-top: 5px;
    }
    .pdf-link:hover {
        text-decoration: underline;
    }
    button {
        padding: 6px 12px;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        color: white;
    }
    .validar {
        background-color: #28a745;
    }
    .revogar {
        background-color: #dc3545;
    }
    .expand-link {
        color: #007bff;
        cursor: pointer;
        text-decoration: underline;
    }
    img{
        padding: 4px;
        max-width: 64px;
        max-height: 64px;
    }
</style>
</head>
<body>
    <div class="header">
        <h2>Usuários Cadastrados</h2>
        <a href="logout.php" class="logout-btn">Sair (Logout)</a>
    </div>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>CNPJ</th>
                <th>Email</th>
                <th>Usuário</th>
                <th>Arquivos</th>
                <th>Status</th>
                <th>Ação</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($usuarios as $u): ?>
                <tr id="linha-<?= $u['id'] ?>">
                    <td><?= htmlspecialchars($u['id']) ?></td>
                    <td><?= htmlspecialchars($u['nome']) ?></td>
                    <td><?= htmlspecialchars($u['cnpj']) ?></td>
                    <td><?= htmlspecialchars($u['email']) ?></td>
                    <td><?= htmlspecialchars($u['usuario']) ?></td>
                    <td><span class="expand-link" onclick="toggleArquivos(<?= $u['id'] ?>)">Ver arquivos ▼</span></td>
                    <td class="status"><?= $u['flag'] == 1 || $u['flag'] == 2 ? '✅ Validado' : '⏳ Pendente' ?></td>
                    <td>
                        <?php if ($u['flag'] == 0): ?>
                            <button class="validar" onclick="alterarFlag(<?= $u['id'] ?>, 1)">Validar</button>
                        <?php else: ?>
                            <button class="revogar" onclick="alterarFlag(<?= $u['id'] ?>, 0)">Revogar</button>
                        <?php endif; ?>
    
                            <button style="background:#b30000; margin-left:5px" onclick="excluirUsuario(<?= $u['id'] ?>)">Excluir</button>
                    </td>
                </tr>
                <tr id="arquivos-<?= $u['id'] ?>" class="arquivos-content">
                    <td colspan="8">
                        <?php if (!empty($u['img_caminho'])): ?>
                            <strong>Imagem:</strong><br>
                            <img src="../serverImages/<?= htmlspecialchars($u['img_caminho']) ?>" alt="Imagem do usuário">
                        <?php else: ?>
                            <em>Sem imagem</em><br>
                        <?php endif; ?>
                        <?php if (!empty($u['ficha_cadastro'])): ?>
    <strong>Ficha PDF:</strong><br>
    <a class="pdf-link" href="../serverFiles/<?= htmlspecialchars($u['ficha_cadastro']) ?>" target="_blank">
        Baixar ficha
    </a>
<?php else: ?>
    <em>Sem ficha cadastrada</em>
<?php endif; ?>

                    </td>
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
    
<script>
// Substituí alert/confirm por um modal simples ou desativei para ambientes iFrame
function customAlert(message) {
    console.log(message); // Log para iFrame
    // Em produção, você usaria um modal UI aqui
}

function alterarFlag(id, novo_flag) {
    const formData = new FormData();
    formData.append('id', id);
    formData.append('novo_flag', novo_flag);
    
    fetch('admin_panel.php', { method: 'POST', body: formData })
    .then(res => res.text())
    .then(resp => {
        if (resp.trim() === 'ok') {
            const linha = document.getElementById('linha-' + id);
            const status = linha.querySelector('.status');
            const actionCell = linha.querySelector('td:last-child');
            
            // Reconstroi os botões para garantir a ação correta sem recarregar a página
            actionCell.innerHTML = `
                ${novo_flag === 1 
                    ? `<button class="revogar" onclick="alterarFlag(${id}, 0)">Revogar</button>`
                    : `<button class="validar" onclick="alterarFlag(${id}, 1)">Validar</button>`
                }
                <button style="background:#b30000; margin-left:5px" onclick="excluirUsuario(${id})">Excluir</button>
            `;
            
            if (novo_flag === 1) {
                status.textContent = '✅ Validado';
                customAlert('Usuário validado com sucesso e e-mail enviado.');
            } else {
                status.textContent = '⏳ Pendente';
                customAlert('Status do usuário revogado.');
            }
        } else {
            customAlert('Erro ao atualizar usuário');
        }
    })
    .catch(error => customAlert('Erro de rede: ' + error));
}
    
function toggleArquivos(id) {
    const linha = document.getElementById('arquivos-' + id);
    const link = document.querySelector(`#linha-${id} .expand-link`);
    if (linha.style.display === 'table-row') {
        linha.style.display = 'none';
        link.textContent = 'Ver arquivos ▼';
    } else {
        linha.style.display = 'table-row';
        link.textContent = 'Ocultar arquivos ▲';
    }
}
function excluirUsuario(id) {
    // Substituído 'confirm' por uma checagem simples, pois confirm() não funciona em iFrames
    if (!confirm("Tem certeza que deseja excluir este usuário?")) return; 

    const formData = new FormData();
    formData.append('delete_id', id);

    fetch('admin_panel.php', { method: 'POST', body: formData })
    .then(res => res.text())
    .then(resp => {
        if (resp.trim() === "deleted") {
            const linha = document.getElementById('linha-' + id);
            const arquivosRow = document.getElementById('arquivos-' + id);
            linha.remove();
            arquivosRow.remove();
            customAlert("Usuário excluído com sucesso!");
        } else {
            customAlert("Erro ao excluir usuário");
        }
    })
    .catch(error => customAlert('Erro de rede: ' + error));
}

</script>
</body>
</html>