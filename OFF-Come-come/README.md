# Microsserviço de Informações Nutricionais — Backend da Aplicação: Come-come

## Sobre o Projeto
Este repositório faz parte do projeto da disciplina Projeto de Desenvolvimento de Software e implementa o microsserviço de informações nutricionais do backend da aplicação Come-come — um aplicativo mobile que auxilia usuários a analisarem informações nutricionais de produtos alimentícios e compararem produtos em diversos aspectos.

O backend é desenvolvido em Spring Boot, seguindo a arquitetura de microsserviços, e será consumido pela aplicação mobile.

## Objetivo
O objetivo deste microsserviço é atuar como a interface central para a obtenção de dados de produtos alimentícios. Ele é responsável por se conectar à API externa do OpenFoodFacts, buscar, processar e (opcionalmente) cachear as informações nutricionais, disponibilizando-as de forma padronizada e resiliente para os demais serviços da aplicação Come-come.

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.5.6
- **Spring WebClient** (ou RestTemplate, para consumo da API externa)
- **Spring Data JPA** (Para cache local dos produtos consultados)
- **PostgreSQL** (Para armazenamento do cache)
- Maven
