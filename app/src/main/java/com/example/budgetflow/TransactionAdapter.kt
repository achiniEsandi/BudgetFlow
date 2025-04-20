package com.example.budgetflow

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val context: Context,
    private val transactions: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeText: TextView = view.findViewById(R.id.transactionType)
        val amountText: TextView = view.findViewById(R.id.transactionAmount)
        val categoryText: TextView = view.findViewById(R.id.transactionCategory)
        val dateText: TextView = view.findViewById(R.id.transactionDate)
        val notesText: TextView = view.findViewById(R.id.transactionNotes)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.typeText.text = transaction.type
        holder.amountText.text = "₹%.2f".format(transaction.amount)
        holder.categoryText.text = transaction.category
        holder.dateText.text = transaction.date
        holder.notesText.text = transaction.notes

        holder.editButton.setOnClickListener {
            val intent = Intent(context, AddTransaction::class.java)
            intent.putExtra("transaction_id", transaction.id)
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Delete Transaction")
                setMessage("Are you sure you want to delete this transaction?")
                setPositiveButton("Yes") { _, _ ->
                    TransactionManager.deleteTransaction(context, transaction.id)
                    transactions.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, transactions.size)
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("No", null)
                create()
                show()
            }
        }
    }

    override fun getItemCount(): Int = transactions.size
}
