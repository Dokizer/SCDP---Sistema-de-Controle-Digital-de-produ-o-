# SCDP - Sistema de Controle Digital de Produção 🏭

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-795548.svg?style=flat)](https://m3.material.io/)
[![Room Database](https://img.shields.io/badge/Database-Room%20(SQLite)-FFA000.svg?style=flat)](https://developer.android.com/training/data-storage/room)

O **SCDP** (Sistema de Controle Digital de Produção) é um aplicativo Android nativo desenvolvido para a gestão, apontamento e controle de qualidade operacional no chão de fábrica da **Farinhas CJ**.

Projetado para operar em ambientes industriais com arquitetura 100% offline, garantindo que nenhum dado de produção ou parada seja perdido mesmo em áreas sem conexão com a internet.

---

## 📥 Download Direto do APK

Para baixar e instalar o aplicativo no seu celular ou tablet Android:

👉 **[Clique aqui para Baixar a Versão Mais Recente (Releases)](https://github.com/Dokizer/SCDP---Sistema-de-Controle-Digital-de-produ-o-/releases)**

> O GitHub compila e disponibiliza o arquivo **`SCDP-v1.1-release.apk`** diretamente para download na aba **Releases**.

---

## 📱 Módulos e Funcionalidades

### 1. 📊 Painel Principal (Dashboard)
- Visão unificada com atalhos diretos para todos os setores de apontamento.
- Indicadores de produção do turno em tempo real.
- Suporte a tema Dinâmico, Claro e Escuro (Material 3).

### 2. 📦 Controle de Empacotamento
- Registro de lotes e produtos empacotados (Farinha Especial, Tradicional, Integral, Farelo, etc.).
- Identificação da linha/empacotadora e operador responsável.
- Aferição de peso médio, controle de pesagem e conferência de vedação da embalagem.

### 3. ⏱️ Apontamento de Paradas de Máquinas
- Registro de início, término e duração calculada automaticamente.
- Classificação por códigos padronizados de parada (Manutenção Mecânica, Elétrica, Troca de Bobina, Falta de Insumo, Limpeza, etc.).
- Detalhamento de observações e ações corretivas.

### 4. 🔍 Checagem de Datagem e Qualidade
- Verificação da impressão de lote, data de fabricação e validade na embalagem.
- Checklist de legibilidade e alinhamento do datador ink-jet / laser.
- Registro fotográfico da amostra diretamente pelo app com armazenamento local seguro.

### 5. 📑 Histórico e Consulta
- Lista unificada de todos os apontamentos realizados no dispositivo.
- Filtros por data, categoria de registro e status.
- Detalhamento individual de cada apontamento com visualização gráfica.

### 6. 📄 Exportação de Relatórios em PDF
- Geração nativa de relatórios formatados em PDF.
- Cabeçalho padronizado da Farinhas CJ com resumo do turno e detalhes técnicos.
- Opção de compartilhamento direto (WhatsApp, e-mail, Bluetooth) ou impressão.

---

## 🛠️ Tecnologias e Arquitetura

- **Linguagem:** Kotlin 2.x
- **Interface:** Jetpack Compose (100% declarativo) com Material Design 3
- **Arquitetura:** MVVM (Model-View-ViewModel) com Unidirectional Data Flow (UDF)
- **Persistência Local:** Room Database (SQLite) com KSP (Kotlin Symbol Processing)
- **Assincronia:** Kotlin Coroutines & StateFlow
- **Geração de Documentos:** Android `PdfDocument` nativo
- **Compatibilidade:** Android 7.0 (API 24) até Android 16 (API 36)

---

## 📂 Estrutura do Projeto

```
app/
 ├── src/main/java/com/example/
 │    ├── data/
 │    │    ├── local/          # Banco de dados Room (AppDatabase, DAOs)
 │    │    ├── model/          # Modelos de dados e entidades
 │    │    └── repository/     # Repositório de dados de produção
 │    ├── ui/
 │    │    ├── components/     # Componentes reutilizáveis (TopBars, Cards, Inputs)
 │    │    ├── screens/        # Telas (Dashboard, Empacotamento, Paradas, Datagem, Histórico)
 │    │    ├── theme/          # Sistema de design (Cores, Tipografia, Transições)
 │    │    ├── AppScreen.kt    # Rotas de navegação
 │    │    └── ProductionViewModel.kt # Gerenciador de estado global
 │    ├── util/                # Utilitários (Exportação de PDF, Gerenciador de Imagens)
 │    └── MainActivity.kt      # Ponto de entrada da aplicação
 └── src/main/res/             # Recursos visuais (Ícone adaptativo, Drawables, Strings)
```

---

## 📄 Licença e Propriedade

Desenvolvido para uso operacional interno da **Farinhas CJ**. Todos os direitos reservados.
