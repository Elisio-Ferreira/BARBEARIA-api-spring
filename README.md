# Barbearia — a API

Projeto da disciplina **Introdução ao Spring Boot**. É a API de uma barbearia:
guarda os serviços, os barbeiros e os clientes, e entrega esses dados para
quem pedir.

Estrutura inspirada no repositório de referência
[lequedevagas-api-spring](https://github.com/Um-Leque-de-Tecnologia/lequedevagas-api-spring)
(NickDev), aplicada a outro tema.

## Equipe

| Frente | Pessoa | Recurso | Arquivos |
| --- | --- | --- | --- |
| 1 | **Elisio** | Serviço | `Servico`, `ServicoEntrada`, `ServicoResposta`, `ServicoRepository`, `ServicoService`, `ServicoController` |
| 2 | **Alessandra** | Barbeiro | `Barbeiro`, `BarbeiroEntrada`, `BarbeiroJaExisteException`, `BarbeiroRepository`, `BarbeiroService`, `BarbeiroController` |
| 3 | **Cintia** | Cliente | `Cliente`, `ClienteEntrada`, `ClienteRepository`, `ClienteService`, `ClienteController` |
| 4 | **Ana Claudia** | Busca e números | `BuscaService`, `BuscaController`, `EstatisticasService`, `EstatisticasController`, `TratadorDeErros` |

A frente 4 não tem recurso próprio: ela lê o `ServicoRepository` da frente 1.
Buscar é uma responsabilidade, não uma tabela.

## Rodar

```bash
./mvnw spring-boot:run     # Mac e Linux
mvnw.cmd spring-boot:run   # Windows
```

Precisa de **JDK 21**. Sobe em <http://localhost:8080>. Depois abra o
`requisicoes.http` e clique em **Send Request** em cada chamada (IntelliJ
direto; no VS Code, instale a extensão REST Client).

## Rotas

| Rota | Frente | Devolve | Status |
| --- | --- | --- | --- |
| `GET /servicos` | 1 | os 12 serviços | 200 |
| `GET /servicos/{id}` | 1 | um serviço | 200 · 404 |
| `POST /servicos` | 1 | o serviço criado, com `Location` | 201 · 400 |
| `PUT /servicos/{id}` | 1 | o serviço trocado | 200 · 400 · 404 |
| `DELETE /servicos/{id}` | 1 | sem corpo | 204 · 404 |
| `GET /barbeiros` | 2 | os 5 barbeiros | 200 |
| `GET /barbeiros/{slug}` | 2 | um barbeiro | 200 · 404 |
| `POST /barbeiros` | 2 | o barbeiro criado, com `Location` no slug | 201 · 400 · 409 |
| `DELETE /barbeiros/{slug}` | 2 | sem corpo | 204 · 404 |
| `GET /clientes` | 3 | os 6 clientes | 200 |
| `GET /clientes/{id}` | 3 | um cliente | 200 · 404 |
| `POST /clientes` | 3 | o cliente criado, com `Location` | 201 · 400 |
| `PUT /clientes/{id}` | 3 | o cliente trocado | 200 · 400 · 404 |
| `DELETE /clientes/{id}` | 3 | sem corpo | 204 · 404 |
| `GET /servicos/busca` | 4 | serviços filtrados por `categoria`, `barbeiro`, `precoMax`, `promocao` | 200 (inclusive `[]`) |
| `GET /estatisticas` | 4 | total, promoções, preço médio, duração média, contagem por categoria e por barbeiro | 200 |

Erros de validação e de conflito voltam no formato Problem Details
(`application/problem+json`), com a lista de campos inválidos em `campos`.

## Decisões da equipe

- **Camadas:** Controller (só HTTP) → Service (regra e tradução) → Repository
  (onde o dado está). Dependência sempre pelo construtor, em campo `final`.
- **Entrada e saída separadas:** `ServicoEntrada` (sem id) entra, `ServicoResposta`
  (sem `descricao`) sai. O modelo `Servico` fica do lado de dentro.
- **Barbeiro se identifica pelo slug**, escolhido por quem cria, e por isso o
  `Location` do POST aponta para o slug. Slug repetido dá 409.
- **Barbeiro não tem PUT:** trocar o slug seria trocar o endereço do recurso.
- **Busca sem resultado é 200 com `[]`;** id inexistente é 404.
- **Dados em memória**, em `CopyOnWriteArrayList`, porque várias requisições
  podem mexer na lista ao mesmo tempo. Quando entrar banco de dados, só os
  `Repository` mudam.
- **Cada `barbeiroSlug` dos serviços precisa existir** como `slug` no
  `BarbeiroRepository`, escrito igual.

## O que ainda não tem

Banco de dados, relacionamento de verdade entre Serviço e Barbeiro (hoje é um
texto solto), agendamento de horários, autenticação e testes além do
`contextLoads`.

## Como cada pessoa faz o seu commit

O `git log` é a evidência de quem fez o quê, então **cada pessoa commita da sua
própria máquina**, com o próprio nome e e-mail do Git.

**1. Uma pessoa cria o repositório** (por exemplo, a Ana Claudia), sobe a base e
compartilha com as outras três:

```bash
git init
git add pom.xml mvnw mvnw.cmd .mvn .gitignore .gitattributes README.md \
        src/main/resources src/main/java/br/edu/faculdade/barbearia/BarbeariaApplication.java \
        src/test
git commit -m "chore: base do projeto Spring Boot"
git branch -M main
git remote add origin <URL-DO-REPOSITORIO>
git push -u origin main
```

**2. Cada pessoa clona, cria a sua branch e commita a sua frente:**

```bash
git clone <URL-DO-REPOSITORIO> && cd barbearia-api-spring
git config user.name  "Seu Nome"
git config user.email "seu-email@exemplo.com"
git checkout -b frente-1-servico      # troque pelo nome da sua frente
```

Elisio (frente 1):
```bash
git add src/main/java/br/edu/faculdade/barbearia/Servico*.java
git commit -m "feat(servicos): CRUD de serviços com entrada, resposta e validação"
```

Alessandra (frente 2):
```bash
git add src/main/java/br/edu/faculdade/barbearia/Barbeiro*.java
git commit -m "feat(barbeiros): recurso identificado por slug, com slug único"
```

Cintia (frente 3):
```bash
git add src/main/java/br/edu/faculdade/barbearia/Cliente*.java idempotencia.http
git commit -m "feat(clientes): CRUD completo e evidência de idempotência"
```

Ana Claudia (frente 4):
```bash
git add src/main/java/br/edu/faculdade/barbearia/Busca*.java \
        src/main/java/br/edu/faculdade/barbearia/Estatisticas*.java \
        src/main/java/br/edu/faculdade/barbearia/TratadorDeErros.java requisicoes.http
git commit -m "feat(busca): busca por filtros, estatísticas e tratamento de erros"
```

Depois: `git push -u origin <sua-branch>` e abra um Pull Request para `main`.

> Atenção: as quatro partes se conectam. `BuscaService` e `EstatisticasService`
> usam o `ServicoRepository`; o `TratadorDeErros` conhece o
> `BarbeiroJaExisteException`; os slugs dos serviços precisam bater com os dos
> barbeiros. Combinem essas assinaturas antes de cada um mexer no seu pedaço, e
> rodem a aplicação inteira depois do merge.
