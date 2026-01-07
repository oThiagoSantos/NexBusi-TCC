-- NexBusi - Database Schema
-- Compatível com MySQL 8.0+ e Servidores Gerenciados (Aiven/AWS)
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ------------------------------------------------------
-- 1. TABELA: users
-- ------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(355) NOT NULL,
  `cnpj` varchar(355) NOT NULL,
  `email` varchar(355) NOT NULL,
  `senha` varchar(355) NOT NULL,
  `usuario` varchar(255) NOT NULL,
  `img_caminho` varchar(255) DEFAULT NULL,
  `ficha_cadastro` varchar(255) DEFAULT NULL,
  `flag` int NOT NULL DEFAULT '0',
  `avaliacao` int DEFAULT '0',
  `observacao` varchar(455) DEFAULT 'Ainda não avaliou :/',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Usuário Administrador Padrão
INSERT INTO `users` (`nome`, `cnpj`, `email`, `senha`, `usuario`, `flag`) 
VALUES ('admin', '00.000.000/0001-00', 'admin@nexbusi.com', 'admin123', 'admin', 2);

-- ------------------------------------------------------
-- 2. TABELA: categoria
-- ------------------------------------------------------
DROP TABLE IF EXISTS `categoria`;
CREATE TABLE `categoria` (
  `id_categoria` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `nome_categoria` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `categoria_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------
-- 3. TABELA: produtos
-- ------------------------------------------------------
DROP TABLE IF EXISTS `produtos`;
CREATE TABLE `produtos` (
  `id_produto` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `nome_produto` varchar(255) DEFAULT NULL,
  `caminho_imagem` varchar(255) DEFAULT NULL,
  `descricao` text,
  `codigo_barras` varchar(20) DEFAULT NULL,
  `preco_custo` decimal(10,2) NOT NULL,
  `preco_revenda` decimal(10,2) NOT NULL,
  `alerta_minimo` int NOT NULL,
  `quantidade_produto` int NOT NULL,
  `id_categoria` int DEFAULT NULL,
  `observacao_produto` varchar(255) DEFAULT 'Sem observações.',
  `motivo_saida` varchar(255) DEFAULT 'Sem motivo definido',
  `data_criacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_produto`),
  UNIQUE KEY `codigo_barras` (`codigo_barras`),
  KEY `id_usuario` (`id_usuario`),
  KEY `fk_produto_categoria` (`id_categoria`),
  CONSTRAINT `fk_produto_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`) ON DELETE SET NULL,
  CONSTRAINT `produtos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------
-- 4. TABELA: historico_entradas
-- ------------------------------------------------------
DROP TABLE IF EXISTS `historico_entradas`;
CREATE TABLE `historico_entradas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_produto` int NOT NULL,
  `quantidade` int NOT NULL,
  `custo_unitario` decimal(10,2) NOT NULL,
  `observacao` text,
  `data_entrada` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_historico_produto` (`id_produto`),
  CONSTRAINT `fk_historico_produto` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------
-- 5. TABELA: historico_saidas
-- ------------------------------------------------------
DROP TABLE IF EXISTS `historico_saidas`;
CREATE TABLE `historico_saidas` (
  `id_saida` int NOT NULL AUTO_INCREMENT,
  `id_produto` int NOT NULL,
  `quantidade` int NOT NULL,
  `motivo` text,
  `data_saida` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `observacao` text,
  PRIMARY KEY (`id_saida`),
  KEY `fk_saida_produto` (`id_produto`),
  CONSTRAINT `fk_saida_produto` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------
-- 6. VIEW: atividades_unificadas
-- ------------------------------------------------------
DROP VIEW IF EXISTS `atividades_unificadas`;
CREATE VIEW `atividades_unificadas` AS 
select `id` AS `id`,`id_produto` AS `id_produto`,`quantidade` AS `quantidade`,'entrada' AS `tipo`,`data_entrada` AS `data_atividade` from `historico_entradas` 
union all 
select `id_saida` AS `id`,`id_produto` AS `id_produto`,`quantidade` AS `quantidade`,'saida' AS `tipo`,`data_saida` AS `data_atividade` from `historico_saidas`;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;