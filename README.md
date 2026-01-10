# NexBusi 🚀

**Projeto de Conclusão de Curso (TCC)** *Gestão de estoque e fluxo de caixa na palma da sua mão.*

O **NexBusi** é uma solução de gestão inteligente focada em **controle de estoque e fluxo de caixa para pequenos negócios**. O sistema permite que o gestor tenha acesso remoto e em tempo real aos dados da sua empresa, priorizando a mobilidade e a facilidade de uso através de uma interface intuitiva em Android.

O projeto foi construído com enfoque em **segurança robusta, escalabilidade e boas práticas de desenvolvimento**, utilizando uma infraestrutura moderna e desacoplada.

---

## 📱 Fluxo de Acesso e Validação

Para garantir a que haja proteção e integridade dos dados empresariais, o NexBusi implementa um fluxo de segurança rigoroso para novos registros:

1.  **Registro:** O usuário preenche os dados da empresa no app.
2.  **Verificação de Identidade:** É necessário o envio de uma foto de documento e a ficha cadastral em PDF (disponível para download na mesma tela).
3.  **Aprovação Manual:** Os dados e arquivos são enviados para um painel administrativo exclusivo onde o administrador valida sua autoria.
    * **Painel Admin:** `(seu-servidor)/apiTest/admin_panel.php`
    * **Login Admin:** `admin@nexbusi.com` | **Senha:** `admin123`
4.  **Acesso Liberado:** Somente após a validação manual do administrador, o login na aplicação é permitido.

> **💡 Para testes rápidos utilizando o .apk** Utilize o login de demonstração:  
> **E-mail:** `emaildeteste@email.com` | **Senha:** `123456`

---

## 🛠️ Stack Tecnológica e Arquitetura

O projeto utiliza uma **Arquitetura Desacoplada** (*Cloud Separation*), separando a lógica de aplicação do armazenamento de dados para maior segurança e performance.



* **Mobile:** Java (Android Nativo) com bibliotecas `Retrofit`, `OkHttp3` e `Volley` para consumo de API.
* **Backend:** PHP 8.x com arquitetura baseada em `PDO` e `Prepared Statements`.
* **Banco de Dados:** MySQL 8.0 (Nuvem).
* **Infraestrutura Cloud:**
    * **Servidor de Aplicação:** Microsoft Azure.
    * **Base de Dados Gerenciada (DBaaS):** Aiven.
* **Segurança:**
    * Criptografia de ligação via **SSL/TLS**.
    * Gestão de credenciais via variáveis de ambiente (`.env`).
    * **Isolamento de Dados:** Base de dados protegida fisicamente ao residir num cluster diferente do servidor web.

---

## 📦 Configuração do Ambiente Local

### 1. Pré-requisitos
* PHP >= 8.0
* MySQL 8.0
* Servidor local (XAMPP, WAMP ou PHP Built-in server)

### 2. Configuração da Base de Dados
O esquema está localizado em `/SchemaSQL/schema.sql`.
1. Crie um banco de dados (ex: `abcd`).
2. Importe o arquivo `schema.sql`. 
   *(Nota: O script foi otimizado para servidores na nuvem, removendo restrições de "Super User" que causam o erro 1227).*

### 3. Variáveis de Ambiente
1. Na raiz do projeto, copia o arquivo `.env.example` para `.env`:
   ```bash
   cp .env.example .env
2. Configure-o de acordo com a sua string de ligação:

```bash
   DB_URI="mysql://usuario:senha@host:porta/nome_do_banco?ssl-mode=REQUIRED"
   DB_NAME="nome_do_seu_banco"
```
### 4. Ligação Segura (SSL)

- Se seu banco em nuvem exigir SSL (como o Aiven):

1. Coloque seu certificado **ca.pem** na raiz do projeto (no mesmo lugar do arquivo `ca-certificate.example.pem`).

2. O sistema detectará o certificado automaticamente e ativará a ligação segura.

## 📁 Estrutura do Repositório
- `/MyApplication` Lógica do funcionamento do app em Java e Front-end.

- `/SchemaSQL:` Script consolidado de criação de tabelas, relações e views.

- `/www:` Backend e diretórios da API.

- `connect.php:` Lógica de ligação centralizada com suporte a SSL e .env.

- `.env.example:` Template para configuração das variáveis de ambiente.
  
- `NexBusi.apk` Arquivo do app funcional, disponível para teste.

## 🔒 Segurança e Boas Práticas

- **SQL Injection:** Proteção total através do uso de Prepared Statements.

- **Privacidade:** Ficheiros sensíveis (.env, ca.pem) estão protegidos via .gitignore.

- **Integridade:** Uso de Chaves Estrangeiras com ON DELETE CASCADE para evitar dados órfãos.

- **Verificação de registro manual**: Análise de imagens e documentos fornecidos pelo usuário para validar seu login.

---

## 📌 Observações finais
**Esse projeto foi desenvolvido para fins de estudo, com enfoque em:**

- Familiarização com ambiente cloud **(Aiven, Azure)** e IDE `Android Studio`.
- Utilização de  bibliotecas ``Java`` para consumo de APIs.
- Configuração de **variáveis de ambiente** e descentralização de servidores.
- Integração de servidor e Banco de Dados com ``Android```.
- Aprimoramento de segurança através de certificado de autenticação.

--- 

## 👨‍💻 Autor

- Thiago Lima dos Santos
  
> **⭐ Se esse projeto te ajudou, me ajude com uma estrela no repositório! **
