package com.kiliccambaz.expenseapp.ui.admin.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
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

        reportsViewModel.userExpenseData.observe(viewLifecycleOwner) { userExpensesMap ->
            createBarChart(userExpensesMap)
        }

        reportsViewModel.expenseTypeData.observe(viewLifecycleOwner) { expenseTypeMap ->
            createPieChart(expenseTypeMap)
        }

        reportsViewModel.dailyExpense.observe(viewLifecycleOwner) { dailyExpense ->
            createLineChart(dailyExpense)
        }

        return root
    }

    private fun createLineChart(dailyExpenses: Map<String, Float>?) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        var index = 0f
        if (dailyExpenses != null) {
            for ((date, totalAmount) in dailyExpenses) {
                entries.add(Entry(index, totalAmount))
                labels.add(date)
                index++
            }
        }

        val dataSet = LineDataSet(entries, getString(R.string.daily_expenses))
        dataSet.setDrawValues(false)

        val data = LineData(dataSet)

        val lineChart = binding.lineChart
        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT

        val xAxis: XAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createPieChart(expenseTypeMap: Map<String, Float>) {
        val entries = mutableListOf<PieEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((expenseType, totalExpense) in expenseTypeMap) {
            entries.add(PieEntry(index, totalExpense))
            labels.add(expenseType)
            index++
        }

        val dataSet = PieDataSet(entries, getString(R.string.expense_type))
        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f


        val colors = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors


        val data = PieData(dataSet)
        val pieChart = binding.pieChart
        pieChart.data = data

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f


        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)

        pieChart.rotationAngle = 0F
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        val l: Legend = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f


        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        pieChart.invalidate()
    }

    private fun createBarChart(userExpensesMap: Map<String, Float>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((userEmail, totalExpense) in userExpensesMap) {
            entries.add(BarEntry(index, totalExpense))
            labels.add(userEmail)
            index++
        }


        val dataSet = BarDataSet(entries, getString(R.string.userExpenses))
        dataSet.setDrawIcons(false)

        val colors = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
        val data = BarData(dataSet)
        data.barWidth = 0.3f
        val barChart = binding.barChart
        barChart.data = data
        barChart.setDrawBarShadow(false)

        barChart.setDrawValueAboveBar(true)

        barChart.description.isEnabled = false

        barChart.setMaxVisibleValueCount(60)


        barChart.setPinchZoom(false)

        barChart.setDrawGridBackground(false)

        val xl: XAxis = barChart.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.granularity = 1f
        xl.valueFormatter = IndexAxisValueFormatter(labels)
        xl.labelRotationAngle = -45f
        xl.textSize = 6f

        val yl: YAxis = barChart.axisLeft
        yl.setDrawAxisLine(true)
        yl.setDrawGridLines(true)
        yl.axisMinimum = 0f


        val yr: YAxis = barChart.axisRight
        yr.setDrawAxisLine(true)
        yr.setDrawGridLines(false)
        yr.axisMinimum = 0f


        barChart.setFitBars(true)
        barChart.animateY(2500)

        val l: Legend = barChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.formSize = 8f
        l.xEntrySpace = 4f

        barChart.invalidate()
    }
}