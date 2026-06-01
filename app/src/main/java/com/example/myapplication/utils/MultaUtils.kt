package com.example.myapplication.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Objeto para cálculo de multas: usuario fica com o livro da data em que foi reservado + 10 dias
// cada dia de atraso, = multa de R$ 3,00.

object MultaUtils {

    // Quantidade de dias que o usuario pode ficar com o livro antes de vencer.
    private const val DIAS_DE_EMPRESTIMO = 10L

    // Valor cobrado para cada dia de atraso depois da data de vencimento.
    private const val MULTA_POR_DIA = 3

    // Cria a data de vencimento somando 10 dias a partir da data informada.
    fun gerarDataVencimento(dataBase: Date = Date()): Timestamp {
        val dezDiasEmMillis = TimeUnit.DAYS.toMillis(DIAS_DE_EMPRESTIMO)
        val dataVencimento = Date(dataBase.time + dezDiasEmMillis)

        return Timestamp(dataVencimento)
    }

    // Calcula quantos dias de atraso existem entre a data de vencimento e hoje.
    fun calcularDiasDeAtraso(dataVencimento: Timestamp?, dataAtual: Date = Date()): Long {
        if (dataVencimento == null) return 0

        val diferencaEmMillis = dataAtual.time - dataVencimento.toDate().time

        if (diferencaEmMillis <= 0) return 0

        return TimeUnit.MILLISECONDS.toDays(diferencaEmMillis)
    }

    // Calcula o valor da multa: dias de atraso multiplicado por R$ 3,00.
    fun calcularValorMulta(dataVencimento: Timestamp?, dataAtual: Date = Date()): Int {
        val diasDeAtraso = calcularDiasDeAtraso(dataVencimento, dataAtual)

        return (diasDeAtraso * MULTA_POR_DIA).toInt()
    }

    // Formata a data para aparecer na tela no formato brasileiro.
    fun formatarData(timestamp: Timestamp?): String {
        if (timestamp == null) return "em breve"

        val formatador = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        return formatador.format(timestamp.toDate())
    }

    // Formata o valor da multa para aparecer como dinheiro na tela.
    fun formatarValorMulta(valor: Int): String {
        return "R$ $valor,00"
    }
}