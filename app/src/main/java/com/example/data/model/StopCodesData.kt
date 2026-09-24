package com.example.data.model

object StopCodesData {
    val list = listOf(
        // AJUSTE
        StopCode("A1", "AJUSTE", "Ajuste Fundo Chato"),
        StopCode("A2", "AJUSTE", "Ajuste na Esteira"),
        StopCode("A3", "AJUSTE", "Ajuste na Faca / Mordaça"),
        StopCode("A4", "AJUSTE", "Ajuste Picote / Mordaça"),
        StopCode("A5", "AJUSTE", "Ajuste no Cilindro / Mordaça"),

        // BALANÇA / CAÇAMBA
        StopCode("B1", "BALANÇA / CAÇAMBA", "Balança Desativada"),
        StopCode("B2", "BALANÇA / CAÇAMBA", "Balança Descalibrada"),
        StopCode("B3", "BALANÇA / CAÇAMBA", "Caçamba Ajuste"),
        StopCode("B4", "BALANÇA / CAÇAMBA", "Caçamba Folgada"),
        StopCode("B5", "BALANÇA / CAÇAMBA", "Caçamba Solta"),

        // BLOQUEADOR
        StopCode("C1", "BLOQUEADOR", "Limpeza na Balança"),
        StopCode("C2", "BLOQUEADOR", "Troca Célula de Carga"),
        StopCode("C3", "BLOQUEADOR", "Bloqueador Carga Congestionado"),

        // DOSADOR
        StopCode("D1", "DOSADOR", "Ajuste no Dosador"),
        StopCode("D2", "DOSADOR", "Dosador Entupido"),
        StopCode("D3", "DOSADOR", "Dosador sem Copo"),
        StopCode("D4", "DOSADOR", "Dosador com Falha"),
        StopCode("D5", "DOSADOR", "Dosador Fora do Tempo"),

        // CONTADOR / FALTA
        StopCode("E1", "CONTADOR / FALTA", "Falta de Produto"),
        StopCode("E2", "CONTADOR / FALTA", "Sensor Contador de Pacotes"),

        // FILME
        StopCode("F1", "FILME", "Filme Fora de Centro / COF Alto-Baixo"),
        StopCode("F2", "FILME", "Filme Desalinhado"),
        StopCode("F3", "FILME", "Fotocélula Filme"),
        StopCode("F4", "FILME", "Filme Desregulado"),
        StopCode("F5", "FILME", "Filme Fora do Padrão"),
        StopCode("F6", "FILME", "Filme Preso no Colarinho"),

        // DATADOR
        StopCode("G1", "DATADOR", "Datador Ajuste"),
        StopCode("G2", "DATADOR", "Datador Falhando"),
        StopCode("G3", "DATADOR", "Datador Desarmando"),
        StopCode("G4", "DATADOR", "Datador Sujo"),
        StopCode("G5", "DATADOR", "Fita Presa ou Partida"),

        // ENFARDADEIRA
        StopCode("L1", "ENFARDADEIRA", "Falha no Sensor Enfardadeira"),
        StopCode("L2", "ENFARDADEIRA", "Enfardadeira Comporta Travada"),
        StopCode("L3", "ENFARDADEIRA", "Enfardadeira Inversor"),
        StopCode("L4", "ENFARDADEIRA", "Limpeza Enfardadeira"),

        // ELEVADOR / ESTEIRA
        StopCode("M1", "ELEVADOR / ESTEIRA", "Elevador de Fardos"),
        StopCode("M2", "ELEVADOR / ESTEIRA", "Elevador de Pacotes"),
        StopCode("M3", "ELEVADOR / ESTEIRA", "Elevador de Canecas"),
        StopCode("M4", "ELEVADOR / ESTEIRA", "Esteira (Definir Local)"),

        // SISTEMA DE AR
        StopCode("O1", "SISTEMA DE AR", "Ar Fraco (Definir Local)"),
        StopCode("O2", "SISTEMA DE AR", "Vazamento Ar (Definir Local)"),
        StopCode("O3", "SISTEMA DE AR", "Regulador de Fluxo Quebrado"),
        StopCode("O4", "SISTEMA DE AR", "Troca da Conexão de Ar (Definir Local)"),
        StopCode("O5", "SISTEMA DE AR", "Repasse de Produtos"),

        // ROBÔ
        StopCode("P1", "ROBÔ", "Acesso a Porta Robô"),
        StopCode("R1", "ROBÔ", "Acúmulo de Fardos Robô"),
        StopCode("R2", "ROBÔ", "Ajuste no Robô"),
        StopCode("R3", "ROBÔ", "Ajustes Iniciais"),

        // LIMPEZA / TROCA
        StopCode("S1", "LIMPEZA / TROCA", "Troca de Embalagem"),
        StopCode("S2", "LIMPEZA / TROCA", "Troca de Produto"),
        StopCode("S3", "LIMPEZA / TROCA", "Limpeza Fim de Turno"),
        StopCode("S4", "LIMPEZA / TROCA", "Limpeza Meio de Turno"),

        // TROCA DE PEÇAS
        StopCode("T1", "TROCA DE PEÇAS", "Troca Bobina Empacotador"),
        StopCode("T2", "TROCA DE PEÇAS", "Troca Bobina Enfardadeira"),
        StopCode("T3", "TROCA DE PEÇAS", "Teflon (Definir Local)"),
        StopCode("T4", "TROCA DE PEÇAS", "Troca Fita Datadora"),

        // QUALIDADE DO PRODUTO
        StopCode("Q1", "QUALIDADE DO PRODUTO", "Densidade Fora"),
        StopCode("Q2", "QUALIDADE DO PRODUTO", "Produto Infestado"),
        StopCode("Q3", "QUALIDADE DO PRODUTO", "Falha Produto com Gritz"),
        StopCode("Q4", "QUALIDADE DO PRODUTO", "Excesso de Partículas de Soja"),
        StopCode("Q5", "QUALIDADE DO PRODUTO", "Metal"),

        // SOLDA VERTICAL
        StopCode("R1-V", "SOLDA VERTICAL", "Solda Vertical Ajuste"),
        StopCode("R2-V", "SOLDA VERTICAL", "Solda Vertical Desalinhada"),
        StopCode("R3-V", "SOLDA VERTICAL", "Borracha Vertical"),
        StopCode("R5-V", "SOLDA VERTICAL", "Troca Borracha Vertical")
    )
}
