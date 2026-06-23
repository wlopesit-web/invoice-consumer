🌐 **[Read this documentation in English / Leia em Inglês](./README.md)**

---

# 🚀 Plataforma de Ingestão de Notas Fiscais em Nuvem OCI

> Plataforma corporativa de alta performance para ingestão, processamento assíncrono e governança de notas fiscais usando Spring Boot, Apache Kafka e Oracle Cloud Infrastructure (OCI).

---

### 👤 Apresentação
*   📄 **[Clique aqui para baixar meu Currículo em PDF](./assets/Curriculo_Wellington-pt.pdf)**
*   💼 **[Clique aqui para baixar meu Portfólio em PDF](./assets/Project_Invoice_Processing_Java_Spring_Boot_Kafka_OCI_Cloud.pdf)**
*   🌐 **[Conecte-se comigo no LinkedIn](https://linkedin.com](https://linkedin.com/in/wellington-lopes-8b4bb4282)** 
---

## 🗺️ Topologia e Fluxo da Arquitetura

![Arquitetura do Sistema](./assets/system_architecture.jpeg) 


[![Java](https://shields.io)](https://openjdk.org)
[![Spring Boot](https://shields.io)](https://spring.io)
[![Apache Kafka](https://shields.io)](https://apache.org)
[![Oracle OCI](https://shields.io)](https://oracle.com)
[![Kubernetes](https://shields.io)](https://k3s.io)


### ⚙️ Funcionalidades Principais e Governança de Dados
*   🔄 **Persistência de Monitoramento (Hydration)**: Sincronização automatizada da interface com o banco de dados via REST API assíncrona no carregamento da página, eliminando perda de estado em atualizações de tela (F5).
*   🗑️ **Purga Atômica Híbrida (Cascading Delete)**: Fluxo de exclusão transacional que limpa os registros do banco de dados relacional e purga o arquivo físico correspondente de dentro do Bucket da Oracle Cloud (OCI Object Storage) de forma síncrona.
*   🛡️ **Segurança Visual Interativa**: Modal customizado em Modo Escuro com desfoque de fundo para confirmações críticas de infraestrutura, mitigando exclusões acidentais no lote.

---

## 🏗️ Arquitetura e Engenharia de Software

O desenho lógico do ecossistema de dados divide-se em componentes especializados e agnósticos:

*   **Microsserviço Produtor (`invoice-producer`)**:
    *   **Porta de Entrada (Edge Service)**: Atua como o API Gateway exposto na porta `8081`.
    *   **Ingestão Flexível**: Controllers REST preparados para cargas de alta vazão via **JSON Individual Bruto** (`/single`) ou processamento assíncrono de **Listas em Lote** (`/batch`).
    *   **Abordagem Pragmática**: Design focado em performance com DTOs autorais bem definidos, evitando overheads desnecessários de codegen de ferramentas externas nesta fase de escopo controlado.
*   **Microsserviço Consumidor (`invoice-consumer`)**:
    *   **Worker Assíncrono Puro**: Inicialização otimizada sem servidor web (`spring.main.web-application-type=none`) para zerar o overhead do Tomcat e economizar memória RAM.
    *   **Estratégia Anti-Poison Pills**: Leitura orientada a `String` pura + deserialização via Jackson Object Mapper sob bloco de proteção `try-catch`, blindando o microsserviço contra mensagens corrompidas na fila.
*   **Kafdrop (Web UI para Apache Kafka)**:
    *   **Observabilidade de Mensageria**: Implementado localmente na porta `9000` para auditoria visual de tópicos e gerenciamento do lag de consumo do `faturamento-group`.
*   **Rancher (Governança Kubernetes)**:
    *   **Orquestração Centralizada**: Central de comando utilizada exclusivamente em ambiente de nuvem (OCI) para gerenciar o ciclo de vida dos Pods e nós do cluster K3s de produção.    
*   **Persistência Avançada (JPA / Hibernate)**:
    *   Uso do **Spring Data JPA** encapsulando o motor do **Hibernate**.
    *   Camada de negócio (`@Service`) protegida com limites transacionais `@Transactional` (Garantia de propriedades ACID).
*   **Invoice Front-end (Camada Cliente)**:
    *   **Tecnologias**: HTML5, Vanilla JavaScript (ES6+), Tailwind CSS (Offline Integrated Style Guide) e JSZip.
    *   **Papel no Ecossistema**: Fornece o painel operacional para disparo de cargas fiscais e monitoramento de auditoria assíncrona, além de realizar a compressão e streaming de downloads diretos da nuvem.
    

---

## 📨 Mensageria e Resiliência (Apache Kafka)

O desacoplamento completo entre a recepção da nota e a gravação física no banco de dados é gerenciado por eventos:
*   **Estratégia de Consumo Mínimo (`earliest`)**: Configurado via `auto-offset-reset=earliest` associado ao grupo `faturamento-group`. 
*   **Tolerância a Falhas**: Se o microsserviço consumidor sofrer quedas ou manutenções, as notas fiscais ficam retidas de forma durável no broker de mensagens do Docker e são consumidas sequencialmente assim que o worker retorna, sem perda de dados.


---

## 💻 Ambiente de Desenvolvimento Híbrido (IntelliJ + WSL2)

Para emular as condições de um ambiente produtivo localmente, o ecossistema utiliza:
* **Containers Isolados**: Apache Kafka e Zookeeper orquestrados via Docker Compose rodando nativamente dentro do Kernel Linux do **WSL2 (Ubuntu)**.
* **Fiação de Rede Interna**: O IntelliJ (ambiente Windows) comunica-se diretamente com o ecossistema Linux através da porta de loopback `localhost:9092`.
* **Segurança Corporativa (Zero-Trust)**: Nenhuma credencial ou chave secreta é armazenada nos arquivos `application.properties`. Todas as senhas sensíveis são injetadas em tempo de execução via Variáveis de Ambiente da JVM.

---

## ☁️ Infraestrutura de Nuvem e DevOps (OCI + Kubernetes)

A sustentação das aplicações utiliza recursos avançados da **Oracle Cloud Infrastructure (OCI)**:
* **Scripts de Automação (OCI CLI)**: Criação de um robô automatizado baseado em CLI que monitorou e capturou **3 instâncias computacionais ARM Ampere** dentro do teto gratuito permanente (*Always Free*), superando a escassez de recursos na nuvem.
* **Cluster Kubernetes de Alta Disponibilidade (K3s)**: Orquestração real utilizando K3s distribuído nas 3 instâncias. A escolha do K3s garante um plano de controle leve para a arquitetura ARM, mantendo suporte total a comandos via **Kubectl**.
* **Failover Automatizado**: O cluster é configurado para que, se um nó sofrer falha de hardware, os outros nós assumam as réplicas das aplicações automaticamente.
* **Segurança com mTLS Obrigatório**: O banco de dados **Oracle Autonomous Database** exige criptografia mTLS ponta a ponta. A conexão do Java é autenticada através de uma **Wallet física de certificados SSL** integrada via drivers nativos de segurança Java (`oraclepki`, `osdt_core`, `osdt_cert`).

---

## 🚀 Como Executar a Infraestrutura e os Microsserviços

### 1. Pré-requisitos
*   Java 17 JDK instalado no WSL2.
*   Docker e Docker Compose ativos no WSL2.
*   Wallet do Oracle DB extraída em `/home/wcl/oracle_wallet`.

### 2. Inicializar a Mensageria (WSL2 Terminal)
```bash
cd ~/portifolioNF
docker compose up -d
```
*   *Nota: A interface visual do **Kafdrop** estará disponível imediatamente em `http://localhost:9000` para acompanhar as filas.*

### 3. Executar o Microsserviço Produtor
Abra o projeto `invoice-producer` no IntelliJ e clique no botão **Run / Play** (Disponível na porta `8081`).

### 4. Executar o Microsserviço Consumidor (Terminal do WSL2)
Navegue até a pasta raiz do consumidor e execute injetando as variáveis de ambiente necessárias para descriptografar a Wallet e autenticar no banco:

```bash
cd ~/portifolioNF/invoice-consumer/invoice-consumer
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password='Estudos#OCI#2026' --oracle.net.wallet_password='Estudos#OCI#2026'"
```

### 5. Inicializar o Painel de Controle Front-end
1. Abra o arquivo `index.html` localizado na pasta `invoice-frontend` através do IntelliJ.
2. Utilize o atalho interno de visualização no canto superior direito para abrir no navegador de sua preferência (Chrome/Edge).
3. Certifique-se de que os microsserviços das portas `8081` (Produtor) e `8082` (Consumidor Web) estejam de pé para que a ingestão e geração de links PAR funcione perfeitamente.

### 6. Auditoria Visual de Mensagens via Kafdrop
Para garantir que o fluxo de eventos está funcionando e inspecionar os payloads trafegando:
1. Acesse `http://localhost:9000` no seu navegador para abrir o painel do **Kafdrop**.
2. Clique no tópico correspondente às notas fiscais (ex: `faturamento-topic` ou equivalente).
3. Clique no botão **View Messages** no canto superior direito para listar os registros que foram postados pelo produtor e consumidos pelo worker.

---

## 🧪 Contratos de Teste da API (Postman / cURL)

### Opção 1: Via cURL (Direto no Terminal)
Abra um novo terminal e execute o comando abaixo para disparar uma nota fiscal de teste em JSON bruto:

```bash
curl -X POST http://localhost:8081/api/invoices/single \
  -H "Content-Type: application/json" \
  -d '{"numeroNota":"12345","cnpjEmitente":"12345678000199","cnpjDestinatario":"98765432000188","valorTotal":1500.50,"itens":"Item A, Item B"}'
```

### Opção 2: Via Postman
* **Mapeamento**: `POST http://localhost:8081/api/invoices/single`
* **Payload (Body JSON)**:
```json
{
  "numeroNota": "77777",
  "cnpjEmitente": "12345678000199",
  "cnpjDestinatario": "98765432000188",
  "valorTotal": 3500.90,
  "itens": "Item A, Item B, Item C"
}
```
