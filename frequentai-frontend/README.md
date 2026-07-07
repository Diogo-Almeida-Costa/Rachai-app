# FrequentAI — Frontend

Frontend React (Vite) da instanciação **Controle de Frequência** do `rachai-framework`.

## Funcionalidades

- Criar turmas
- Matricular alunos em uma turma
- Registrar presença/falta por aluno e data
- Ver o histórico de registros da turma
- Ver a assiduidade de cada aluno (% de faltas) com indicação visual de risco (>25% de faltas)
- Gerar o prompt de análise de risco montado pelo `ContextManager` do framework (ponto fixo), pronto para ser enviado a um LLM

## Como rodar

```bash
npm install
npm run dev
```

A aplicação sobe em `http://localhost:3001` e espera o backend `frequentai-instance` rodando em `http://localhost:8082`.

Para rodar o backend:

```bash
cd ../rachai-multi
mvn -pl frequentai-instance -am spring-boot:run
```
