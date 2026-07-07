# Rachai

Este repositório contém um **framework** de divisão inteligente de recursos entre múltiplos usuários (com IA e OCR integrados) e duas aplicações que são instanciações desse framework: o **RachAI** (divisão de despesas) e o **PontoAI** (controle de frequência).

## 🧩 Arquitetura: Framework + Instanciações

O `backend/api/src/main/java/com/rachai/framework` é o **core do framework**: pontos fixos e genéricos que não dependem de nenhum domínio específico.

* `framework.core.context.ContextManager` — monta o prompt de IA a partir de uma configuração (`IAIConfig`) e dos dados de contexto.
* `framework.core.ocr.OCRModule` — orquestra o envio de documentos para OCR (Tabscanner) e delega a interpretação do resultado para uma extensão (`IOCRExtension`).
* `framework.core.splitter.ResourceSplitter` — orquestra a divisão de um recurso (`IResource`) entre participantes (`framework.core.user.User`), delegando a matemática para uma regra (`ISplitRule`).
* `framework.extension.*` — as interfaces de extensão (`IAIConfig`, `IOCRExtension`, `IResource`, `ISplitRule`) que cada aplicação deve implementar.

Cada aplicação (instanciação) vive em seu **próprio pacote**, isolada da outra, implementando essas quatro extensões para o seu domínio:

| Pacote | Aplicação | Recurso dividido | Regra de divisão | OCR | IA |
|---|---|---|---|---|---|
| `com.rachai.api` | **RachAI** — divisão de despesas | `ExpenseResource` (valor da despesa) | `EqualSplitRule` (divisão igualitária) | `ReceiptOCRExtension` (cupom fiscal) | `RachAIConfig` (sugestão de grupos) |
| `com.rachai.attendance` | **PontoAI** — controle de frequência | `WorkloadResource` (carga horária total) | `AttendanceCompensationRule` (compensação por assiduidade real) | `SignedAttendanceListOCRExtension` (lista de presença assinada) | `PontoAIConfig` (atividades extras de compensação) |

Dentro de cada pacote de aplicação, a subpasta `instance/` reúne exatamente essas implementações concretas dos pontos de extensão do framework (`instance/resource`, `instance/rule`, `instance/ocr`, `instance/ai`), enquanto `model`, `repository`, `service` e `controller` contêm as entidades, regras de negócio e endpoints específicos de cada domínio. Os clients de integração genéricos (`GroqApiClient`, `TabscannerApiClient`) ficam em `com.rachai.api.client` e são reutilizados por ambas as instanciações, pois são infraestrutura do framework, não lógica de domínio.

### RachAI
O **RachAl** é uma plataforma desenvolvida para facilitar a organização financeira entre amigos e colegas. O foco é registrar gastos compartilhados, dividir valores e utilizar algoritmos para simplificar a quitação de dívida.

### PontoAI
O **PontoAI** controla a frequência presencial de participantes em uma turma. A partir da carga horária total, ele compensa as horas faltantes com base na assiduidade real detectada, podendo extrair as presenças automaticamente via OCR de listas físicas assinadas, e sugere atividades extras (via IA) para cobrir as faltas.

* Endpoints de domínio: `POST/GET /pontoai/classgroups` (turmas e participantes).
* Endpoint da instanciação do framework: `POST /api/framework/attendance/process` (recebe a foto da lista de presença + `classGroupId` + `sessionDurationHours`, e retorna as horas a compensar por participante junto com a sugestão de atividades extras da IA).

## 🚀 Funcionalidades Planejadas
* **Gestão de Perfil:** Criação e edição de contas de usuário.
* **Gestão de Grupos:** Criação de grupos e consulta de dívidas.
* **Registro de Despesas:** Divisão igual ou personalizada.
* **Inteligência:** Algoritmo de compensação automática e sugestão de eventos.

## 🛠️ Tecnologias
* **Front-end:** React (Vite), HTML5, CSS3, JavaScript.
* **Back-end:** Java com Spring Boot.

## 📅 Cronograma de Sprints
1. **Sprint 1:** Autenticação, Perfil e Grupos.
2. **Sprint 2:** Algoritmo de Compensação e Registro de Despesas.
3. **Sprint 3:** Sugestão de Eventos e Notificações.

## 👥 Equipe
* Filipe de Medeiros Alves 
* Iara Kelly Lima da Silva 
* José Diogo de Almeida Costa

---
*Projeto apresentado em 19/03/2026* 