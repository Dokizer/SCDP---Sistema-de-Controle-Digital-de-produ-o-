# SCDP - Sistema de Controle Digital de Produção 🏭

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-795548.svg?style=flat)](https://m3.material.io/)
[![Room Database](https://img.shields.io/badge/Database-Room%20(SQLite)-FFA000.svg?style=flat)](https://developer.android.com/training/data-storage/room)

O **SCDP** (Sistema de Controle Digital de Produção) é um aplicativo Android nativo desenvolvido para a gestão, apontamento e controle de qualidade operacional no chão de fábrica da **Farinhas CJ**.

Projetado para operar em ambientes industriais com arquitetura 100% offline, garantindo que nenhum dado de produção ou parada seja perdido mesmo em áreas sem conexão com a internet.

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

## 🚀 Como Compilar e Gerar o APK

### Pré-requisitos
- Android Studio Ladybug ou superior
- JDK 17 ou 21
- Android SDK instalado (API 34+)

### Passos de Compilação
1. Clone o repositório:
   ```bash
   git clone https://github.com/SEU-USUARIO/SCDP.git
   cd SCDP
   ```

2. Compile o APK de depuração:
   ```bash
   ./gradlew assembleDebug
   ```

3. O APK gerado estará disponível no caminho:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. Para instalar diretamente em um dispositivo conectado via USB (com depuração USB ativada):
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📦 Como Publicar o APK nas Releases do GitHub

A melhor prática recomendada pelo GitHub é disponibilizar o arquivo `.apk` na aba **Releases**:

1. Acesse o seu repositório no GitHub.
2. Na barra lateral direita, clique em **Releases** (ou **Create a new release**).
3. Clique em **Draft a new release**.
4. Defina uma tag de versão (ex: `v1.1.0`) e um título (ex: `SCDP v1.1.0 - Versão Oficial`).
5. No campo de anexo de arquivos (**Attach binaries by dropping them here or selecting them**), arraste o arquivo `app-debug.apk`.
6. Adicione as notas da versão descrevendo as novidades e clique em **Publish release**.
7. Pronto! Qualquer operador ou usuário poderá baixar e instalar o APK diretamente pelo link da Release.

---

## 📄 Licença e Propriedade

Desenvolvido para uso operacional interno da **Farinhas CJ**. Todos os direitos reservados.
