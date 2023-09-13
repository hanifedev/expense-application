package com.kiliccambaz.expenseapp.ui.admin.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private lateinit var reportsViewModel: ReportsViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        reportsViewModel =
            ViewModelProvider(this)[ReportsViewModel::class.java]

        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        reportsViewModel.expenseData.observe(viewLifecycleOwner) { userExpensesMap ->
            createBarChart(userExpensesMap)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createBarChart(userExpensesMap: Map<String, Float>) {
        val entries = mutableListOf<PieEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((userEmail, totalExpense) in userExpensesMap) {
            entries.add(PieEntry(index, totalExpense))
            labels.add(userEmail)
            index++
        }

        val dataSet = PieDataSet(entries, getString(R.string.userExpenses))
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.asList()

        val data = PieData(dataSet)
        val pieChart = binding.pieChart
        pieChart.data = data

        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false) // Etiketleri gösterme
        pieChart.isRotationEnabled = true // Pasta grafiğinin dönebilir olması
        pieChart.legend.isEnabled = true // Açıklama etkinleştirme

        pieChart.invalidate()
    }
}