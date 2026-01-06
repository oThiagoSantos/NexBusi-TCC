-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: Cloud host    Database: abcd
-- ------------------------------------------------------
-- Server version	8.0.35

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '1446712b-9194-11f0-837d-862ccfb00d01:1-65,
1cd536ee-82a2-11f0-aa6b-862ccfb0083b:1-15,
525145ae-1571-11f0-8755-168cf036a383:1-37,
81295f9d-ae27-11f0-9ab2-862ccfb0153b:1-26,
8ac2d09f-bf06-11f0-ac7c-862ccfb0341c:1-328';

--
-- Temporary view structure for view `atividades_unificadas`
--

DROP TABLE IF EXISTS `atividades_unificadas`;
/*!50001 DROP VIEW IF EXISTS `atividades_unificadas`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `atividades_unificadas` AS SELECT 
 1 AS `id`,
 1 AS `id_produto`,
 1 AS `quantidade`,
 1 AS `tipo`,
 1 AS `data_atividade`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id_categoria` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `nome_categoria` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `categoria_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (4,1,'Energéticos'),(5,57,'Energéticos'),(6,59,'Notebooks'),(8,59,'garrafas'),(9,59,'garrafa');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historico_entradas`
--

DROP TABLE IF EXISTS `historico_entradas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historico_entradas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_produto` int NOT NULL,
  `quantidade` int NOT NULL,
  `custo_unitario` decimal(10,2) NOT NULL,
  `observacao` text,
  `data_entrada` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_historico_produto` (`id_produto`),
  CONSTRAINT `fk_historico_produto` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`) ON DELETE CASCADE,
  CONSTRAINT `historico_entradas_ibfk_1` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historico_entradas`
--

LOCK TABLES `historico_entradas` WRITE;
/*!40000 ALTER TABLE `historico_entradas` DISABLE KEYS */;
INSERT INTO `historico_entradas` VALUES (2,9,130,3.00,'Compra para o mês','2025-11-30 22:38:00'),(3,9,10,160.00,'','2025-11-30 22:55:11'),(4,9,100,1.00,'','2025-11-30 22:55:47'),(5,9,50,2.50,'Renovando o estoque mensal','2025-11-30 23:34:34'),(10,9,10,5.00,'Compra','2025-12-02 01:44:28'),(11,9,10,5.00,'Compra','2025-12-02 01:44:29'),(12,9,10,5.00,'Compra','2025-12-02 01:44:30'),(13,13,15,4.00,'','2025-12-02 01:51:30'),(14,13,20,12.00,'','2025-12-02 02:11:39'),(16,12,10,4.00,'','2025-12-03 03:58:46'),(17,13,16,4.00,'','2025-12-03 04:28:14'),(18,13,5,4.00,'','2025-12-03 04:29:25'),(19,13,5,4.00,'','2025-12-03 04:48:40'),(20,16,20,4.00,'','2025-12-03 04:51:24'),(21,13,4,4.00,'','2025-12-03 19:35:31'),(22,9,10,4.00,'','2025-12-03 19:41:01'),(23,22,25,4.00,'seola','2025-12-03 21:50:07'),(24,13,50,4.00,'','2025-12-04 19:26:28'),(25,13,15,10.00,'Teste entrada','2025-12-05 02:21:49'),(26,29,40,3.50,'','2025-12-05 05:12:59'),(27,12,50,3.50,'','2025-12-05 12:06:52');
/*!40000 ALTER TABLE `historico_entradas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historico_saidas`
--

DROP TABLE IF EXISTS `historico_saidas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historico_saidas`
--

LOCK TABLES `historico_saidas` WRITE;
/*!40000 ALTER TABLE `historico_saidas` DISABLE KEYS */;
INSERT INTO `historico_saidas` VALUES (7,12,100,'Venda','2025-12-01 02:04:23','Venda comum'),(8,12,149,'Venda','2025-12-01 02:22:22','Vendendo quase todos os produtos'),(9,12,1,'Venda','2025-12-01 20:44:17','Tanto Faz'),(15,12,10,'Venda','2025-12-02 01:22:39',''),(16,9,5,'Venda','2025-12-02 01:45:43','Não Importa'),(17,13,50,'Venda','2025-12-02 01:52:39','Saida'),(18,13,5,'Venda','2025-12-02 01:54:16',''),(19,12,2,'Venda','2025-12-02 02:07:30',''),(20,12,3,'Venda','2025-12-02 03:10:55',''),(21,9,10,'Venda','2025-12-02 03:17:29',''),(22,9,10,'Venda','2025-12-02 03:17:33',''),(23,9,10,'Venda','2025-12-02 03:17:35',''),(24,9,10,'Venda','2025-12-02 03:17:36',''),(25,9,10,'Venda','2025-12-02 03:17:37',''),(26,9,10,'Venda','2025-12-02 03:17:37',''),(27,9,10,'Venda','2025-12-02 03:17:38',''),(28,9,10,'Venda','2025-12-02 03:17:38',''),(29,13,19,'Venda','2025-12-02 03:19:16','Sem Observação'),(30,13,1,'Venda','2025-12-03 01:13:45',''),(31,13,10,'Venda','2025-12-03 04:31:02',''),(32,13,10,'Venda','2025-12-03 04:32:19',''),(33,21,5,'Venda','2025-12-03 21:15:52',''),(34,21,35,'Venda','2025-12-03 21:51:37',''),(35,15,32,'Venda','2025-12-04 19:26:53',''),(36,12,30,'Venda','2025-12-04 19:27:41',''),(37,13,70,'Venda','2025-12-05 02:22:14',''),(38,30,2,'venda','2025-12-05 10:56:16',''),(39,30,1,'atraso','2025-12-05 10:57:21',''),(40,30,1,'venda','2025-12-05 15:31:05',''),(41,30,1,'venda','2025-12-05 15:31:48','venci mermo');
/*!40000 ALTER TABLE `historico_saidas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produtos`
--

DROP TABLE IF EXISTS `produtos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  CONSTRAINT `fk_produto_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  CONSTRAINT `produtos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produtos`
--

LOCK TABLES `produtos` WRITE;
/*!40000 ALTER TABLE `produtos` DISABLE KEYS */;
INSERT INTO `produtos` VALUES (9,1,'Redbull','ImagensProdutos/1764542099_produto.jpg','Redbull sabor pomelo','000000000005',4.00,8.50,50,280,4,'Sem observações.\nEntrada: Compra (2025-12-01 22:44:27)\nEntrada: Compra (2025-12-01 22:44:28)\nEntrada: Compra (2025-12-01 22:44:30)','Sem motivo definido','2025-12-03 21:55:53'),(12,1,'RedBull Pomelo','ImagensProdutos/1764554556_produto.jpg','Redbull Sabor Pomelo','00000000049',3.50,9.00,20,55,4,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(13,1,'Monster Ultra Rosa','ImagensProdutos/1764624076_produto.jpg','Monster sabor ultra rosa','',10.00,10.50,15,5,4,'Sem observações.\nEntrada: Teste entrada (2025-12-04 23:21:49)','Sem motivo definido','2025-12-03 21:55:53'),(14,1,'Monster Pacific Punch','ImagensProdutos/1764624127_produto.jpg','Monster de Sabor variado','00000000050',4.00,10.50,20,50,4,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(15,1,'RedBull Melancia','ImagensProdutos/1764624177_produto.jpg','Redbull sabores variados','0000000027',5.00,12.00,10,3,4,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(16,1,'Redbull Cereja','ImagensProdutos/1764624227_produto.jpg','Redbull sabores variados','000000000162',4.00,12.00,30,65,4,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(17,1,'Redbull Tropical','ImagensProdutos/1764624279_produto.jpg','Redbull sabores variados','01749273919',5.00,12.00,25,80,4,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(21,57,'RedBull Cereja','ImagensProdutos/1764796349_produto.jpg','Redbull sabor Cereja','00000000040',4.00,11.00,15,10,5,'Sem observações.','Sem motivo definido','2025-12-03 21:55:53'),(22,57,'Monster Mango Loco','ImagensProdutos/1764797654_produto.jpg','Monster sabores variados','000000002262',4.00,9.00,10,125,5,'Sem observações.\nEntrada: seola (2025-12-03 18:50:07)','Sem motivo definido','2025-12-03 21:55:53'),(23,57,'Redbull Tropical','ImagensProdutos/1764799130_produto.jpg','RedBull sabor Tropical','2964917492',3.50,11.00,30,120,5,'Sem observações.','Sem motivo definido','2025-12-03 21:59:22'),(24,57,'Monster Mango Loco Zero','ImagensProdutos/1764800265_produto.jpg','Um dos melhores tlg','0264926302',4.00,11.00,20,70,5,'Sem observações.','Sem motivo definido','2025-12-03 22:18:16'),(25,1,'Redbull sabores','ImagensProdutos/1764876559_produto.jpg','Tanto Faz','0274927492',15.00,60.00,10,30,4,'Sem observações.','Sem motivo definido','2025-12-04 19:29:46'),(26,57,'meu produto','ImagensProdutos/1764879600_produto.jpg','ksksksksk','0027402849',15.00,50.00,5,100,5,'Sem observações.','Sem motivo definido','2025-12-04 20:20:38'),(29,1,'Monster sem açúcar','ImagensProdutos/1764911518_produto_comprimido.jpg','Monster sem açúcar','000384728',3.50,11.00,80,65,4,'Sem observações.','Sem motivo definido','2025-12-05 05:12:34'),(30,59,'notebook positivo','ImagensProdutos/1764931887_produto_comprimido.jpg','notebook com o positivo do Bryan','111222333',1.00,3.00,2,0,6,'Sem observações.','Sem motivo definido','2025-12-05 10:52:37');
/*!40000 ALTER TABLE `produtos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Adega','00.000.000/0000.00','admin@admin.com','admin1234','adminUserTeste','1764902913_adidas.jpeg','arquivo.pdf',2,5,'App muito bom!'),(57,'user2','00.000.000/0000.00','admin@admin2.com','admin1234','adminUser2','img.png','arquivo.pdf',1,5,'Gostei muito do app!'),(59,'riantec','59.124.965/0001-39','riantec@gmail.com','123456','Riantec',NULL,'1764793264_Baixando FichaCadastralExcel.xlsx',1,0,'Ainda não avaliou :/'),(66,'Empresatal','25.896.160/0001-91','empresatal@gmail.com','MinhaSenha','meuUsuario','1764899210_JPEG_20251204_224641_5548396974100589293.jpg','1764899201_Baixando FichaCadastral.pdf',1,0,'Ainda não avaliou :/'),(84,'minhaempresa','34.429.405/0001-61','thiagonahora@gmail.com','123456','meuUsuario',NULL,NULL,1,0,'Ainda não avaliou :/'),(85,'testeEmpresa','34.429.405/0001-61','thiagobyethost@gmail.com','123456','ThiagoLima','1764969026_JPEG_20251205_181017_2149333667034145122.jpg',NULL,0,0,'Ainda não avaliou :/');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `atividades_unificadas`
--

/*!50001 DROP VIEW IF EXISTS `atividades_unificadas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`avnadmin`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `atividades_unificadas` AS select `historico_entradas`.`id` AS `id`,`historico_entradas`.`id_produto` AS `id_produto`,`historico_entradas`.`quantidade` AS `quantidade`,'entrada' AS `tipo`,`historico_entradas`.`data_entrada` AS `data_atividade` from `historico_entradas` union all select `historico_saidas`.`id_saida` AS `id`,`historico_saidas`.`id_produto` AS `id_produto`,`historico_saidas`.`quantidade` AS `quantidade`,'saida' AS `tipo`,`historico_saidas`.`data_saida` AS `data_atividade` from `historico_saidas` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-05 18:41:48
