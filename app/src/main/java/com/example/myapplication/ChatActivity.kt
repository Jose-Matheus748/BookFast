package com.example.bookfast.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookfast.R
import com.example.bookfast.adapter.ChatAdapter
import com.example.bookfast.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var recycler: RecyclerView
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        val editText = findViewById<EditText>(R.id.editTextMessage)
        val btnSend  = findViewById<ImageButton>(R.id.btnSend)
        recycler     = findViewById(R.id.recyclerViewChat)

        configurarRecyclerView()
        adicionarMensagemBot("Olá! Sou o assistente do BookFast. Como posso te ajudar hoje?")

        btnSend.setOnClickListener {
            val texto = editText.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener

            adicionarMensagemUsuario(texto)
            editText.setText("")
            responderBot(texto)
        }
    }

    // ── Configuração do RecyclerView ────────────────────────────────────────

    private fun configurarRecyclerView() {
        adapter = ChatAdapter(messages)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true   // lista começa pela última mensagem

        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
    }

    // ── Inserção de mensagens ───────────────────────────────────────────────

    private fun adicionarMensagemBot(texto: String) {
        adapter.addMessage(Message(texto, isBot = true, time = horaAtual()))
        scrollParaBaixo()
    }

    private fun adicionarMensagemUsuario(texto: String) {
        adapter.addMessage(Message(texto, isBot = false, time = horaAtual()))
        scrollParaBaixo()
    }

    // ── Lógica de resposta do bot ───────────────────────────────────────────
    // Substitua este bloco pela integração com sua API real quando necessário

    private fun responderBot(pergunta: String) {
        val resposta = when {
            pergunta.contains("renovar", ignoreCase = true) ->
                "Me informe o código do empréstimo ou o título do livro para verificar a disponibilidade de renovação."

            pergunta.contains("multa", ignoreCase = true) ||
                    pergunta.contains("vencimento", ignoreCase = true) ->
                "Você possui R\$ 3,00 em multas pendentes. Deseja mais detalhes?"

            pergunta.contains("reservar", ignoreCase = true) ||
                    pergunta.contains("reserva", ignoreCase = true) ->
                "Para reservar um livro, acesse a aba Reservas e selecione o título desejado."

            pergunta.contains("fila", ignoreCase = true) ->
                "Você está na posição 1 de 3 na fila de espera. Previsão: 20/01/27."

            pergunta.contains("olá", ignoreCase = true) ||
                    pergunta.contains("oi", ignoreCase = true) ->
                "Olá! Em que posso ajudar você hoje?"

            else ->
                "Não entendi sua mensagem. Pode tentar novamente? Posso ajudar com renovações, reservas, multas e fila de espera."
        }

        // Delay de 800ms para simular o bot "digitando"
        Handler(Looper.getMainLooper()).postDelayed({
            adicionarMensagemBot(resposta)
        }, 800)
    }

    // ── Utilitários ─────────────────────────────────────────────────────────

    private fun scrollParaBaixo() {
        recycler.scrollToPosition(messages.size - 1)
    }

    private fun horaAtual(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}