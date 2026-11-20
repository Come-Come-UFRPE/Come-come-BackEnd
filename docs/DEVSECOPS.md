# Pipeline DevSecOps

O workflow `.github/workflows/devsecops.yml` roda em todo pull request, em push na `main` e na `dev`, e manualmente pela aba Actions (workflow_dispatch). Ele não depende de nenhuma nuvem.

| Job | Ferramenta | O que verifica | Bloqueia? |
| --- | --- | --- | --- |
| Segredos | Gitleaks 8.30.1 | Chaves, tokens e senhas no código e no histórico do Git | Sim |
| SAST | CodeQL (`security-extended`) | Falhas no código Java, como injeção e uso inseguro de APIs | Não, reporta |
| SAST | Semgrep (`p/java`, `p/secrets`) | Padrões inseguros em Java e Spring | Não, reporta |
| SCA + config | Trivy `fs` | CVEs nas dependências dos `pom.xml` e más configurações nos Dockerfiles | Sim, para dependências CRITICAL/HIGH com correção |
| Imagens | Build + Trivy `image` (8 serviços) | CVEs no SO base e nas bibliotecas de cada imagem, além de gerar o SBOM em CycloneDX | Sim, para CRITICAL/HIGH com correção |

Os resultados aparecem em **Security › Code scanning**. Os SBOMs ficam como artefatos de cada execução. Em push na `main`, as imagens são publicadas no GHCR como `ghcr.io/come-come-ufrpe/comecome-<serviço>`, com as tags `main` e o hash do commit.

O `.github/dependabot.yml` abre PRs semanais para atualizar as dependências Maven, as imagens base e as próprias actions.

## Cadeia de suprimentos da própria pipeline

- Toda action está fixada pelo hash do commit, não pela tag.
- Os binários baixados (Gitleaks) têm o checksum SHA-256 conferido antes de rodar.
- O Trivy está fixado na v0.69.3 e a `trivy-action` na v0.35.0.

O motivo: em março de 2026, quase todas as tags da `aquasecurity/trivy-action` foram reescritas para apontar para código malicioso ([CVE-2026-33634](https://nvd.nist.gov/vuln/detail/CVE-2026-33634); [nota da Aqua](https://www.aquasec.com/blog/trivy-supply-chain-attack-what-you-need-to-know)). Só a v0.35.0 ficou íntegra. Quem usava a action por tag executou o código do atacante.

## Configuração no GitHub (uma vez)

1. Code scanning: em repositório público ele é gratuito e os resultados aparecem sozinhos. Se o repositório virar privado, é preciso o GitHub Advanced Security.
2. Para bloquear o merge: em **Settings › Branches**, criar uma regra para a `main` exigindo os checks `Segredos (Gitleaks)`, `Dependências e Dockerfiles (Trivy)` e os jobs `Imagem *`.
3. Para publicar no GHCR, a organização precisa permitir que o `GITHUB_TOKEN` crie pacotes (Settings › Actions › Workflow permissions).

## Falso positivo do Gitleaks

Adicione o fingerprint mostrado no log ao arquivo `.gitleaksignore` na raiz, com uma justificativa no PR.
