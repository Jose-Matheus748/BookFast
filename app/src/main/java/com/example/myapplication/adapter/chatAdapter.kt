package com.example.bookfast.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookfast.R
import com.example.bookfast.model.Message

/**
 * Adapter do RecyclerView do chat.
 * Decide automaticamente qual bolha exibir (bot ou usuário)
 * baseado no campo isBot de cada Message.
 */
class ChatAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BOT  = 0
        private const val TYPE_USER = 1
    }

    // Retorna o tipo de view conforme quem enviou a mensagem
    override fun getItemViewType(position: Int): Int =
        if (messages[position].isBot) TYPE_BOT else TYPE_USER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_BOT) {
            BotViewHolder(inflater.inflate(R.layout.item_message_bot, parent, false))
        } else {
            UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is BotViewHolder  -> holder.bind(msg)
            is UserViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Adiciona uma nova mensagem ao fim da lista
     * e notifica o RecyclerView para renderizá-la.
     */
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ── ViewHolder da bolha do BOT ──────────────────────────────────────────
    inner class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText: TextView = view.findViewById(R.id.tvBotMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvBotTime)

        fun bind(msg: Message) {
            tvText.text = msg.text
            tvTime.text = msg.time
        }
    }

    // ── ViewHolder da bolha do USUÁRIO ──────────────────────────────────────
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText: TextView = view.findViewById(R.id.tvUserMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvUserTime)

        fun bind(msg: Message) {
            tvText.text = msg.text
            tvTime.text = msg.time
        }
    }
}
