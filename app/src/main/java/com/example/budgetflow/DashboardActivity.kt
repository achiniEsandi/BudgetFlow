package com.example.budgetflow

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetflow.PieChartView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val pieChart = findViewById<PieChartView>(R.id.pieChartCategory)

        // Data for the PieChart
        val entries = listOf(
            PieChartView.PieSlice(40f, Color.RED),
            PieChartView.PieSlice(30f, Color.GREEN),
            PieChartView.PieSlice(20f, Color.BLUE),
            PieChartView.PieSlice(10f, Color.YELLOW)
        )

        pieChart.setData(entries)
    }
}
