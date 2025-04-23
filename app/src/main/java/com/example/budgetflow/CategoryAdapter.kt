package com.example.budgetflow  // Replace with your actual package name

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.example.budgetflow.R  // Adjust to match your actual resource package

class CategoryAdapter(
    private val categories: List<CategorySummary>,
    private val isIncome: Boolean
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.categoryIcon)
        val name: TextView = view.findViewById(R.id.categoryName)
        val percentage: TextView = view.findViewById(R.id.categoryPercentage)
        val progress: LinearProgressIndicator = view.findViewById(R.id.categoryProgress)
        val amount: TextView = view.findViewById(R.id.categoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        val iconRes = when (category.name) {
            "Food" -> R.drawable.ic_food
            "Transport" -> R.drawable.transport
            "Bills" -> R.drawable.ic_bills
            "Entertainment" -> R.drawable.ic_entertainment
            "Salary" -> R.drawable.salary
            "Freelance" -> R.drawable.ic_freelance
            else -> R.drawable.ic_other
        }

        holder.icon.setImageResource(iconRes)
        holder.name.text = category.name
        holder.percentage.text = "${category.percentage}% of ${if (isIncome) "income" else "expenses"}"
        holder.progress.progress = category.percentage
        holder.amount.text = if (isIncome) "+Rs.${category.amount}" else "-Rs.${category.amount}"
        holder.amount.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (isIncome) R.color.green else R.color.red
            )
        )
    }

    override fun getItemCount(): Int = categories.size
}
