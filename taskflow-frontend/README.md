# TaskFlow — Frontend

Frontend React (Vite) da instanciação **Controle de Tarefas** do `rachai-framework`.

## Funcionalidades

- Criar projetos
- Adicionar colaboradores a um projeto, com nível de ocupação (0–100%)
- Criar tarefas com título, prioridade (ALTA/MEDIA/BAIXA) e esforço estimado
- Concluir tarefas
- Gerar o prompt de sugestão de tarefas montado pelo `ContextManager` do framework
- Importar uma lista de tarefas via imagem (OCR/Tabscanner) e disparar a delegação
  automática de carga entre os colaboradores selecionados, usando o fluxo completo
  do framework: `OCRModule` → `ContextManager` → `ResourceSplitter`

## Como rodar

```bash
npm install
npm run dev
```

A aplicação sobe em `http://localhost:3002` e espera o backend `taskflow-instance`
rodando em `http://localhost:8083`.

Para rodar o backend:

```bash
cd ../rachai-multi
mvn -pl taskflow-instance -am spring-boot:run
```

> A importação via OCR depende de uma chave válida da Tabscanner configurada em
> `taskflow-instance/src/main/resources/application.yml` e de acesso à internet
> para a chamada externa.
