package com.kiliccambaz.expenseapp.ui.admin.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
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
import com.github.mikephil.charting.formatter.LargeValueFormatter
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

        reportsViewModel.monthExpense.observe(viewLifecycleOwner) { monthlyExpense ->
            showMonthlyExpensesBarChart(monthlyExpense)
        }

        return root
    }

    private fun showMonthlyExpensesBarChart(monthlyExpensesMap: Map<String, Map<String, Float>>) {
        val barEntriesList = mutableListOf<BarEntry>()
        val xAxisLabels = mutableListOf<String>()

        // Tüm ayların listesini oluşturun
        val allMonths = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")

        // Kullanıcıları temsil eden bir liste alın
        val distinctUsernames = monthlyExpensesMap.keys.toList()

        // Kullanıcıları farklı renklere otomatik olarak atayın
        val userColors = distinctUsernames.mapIndexed { index, username ->
            username to ColorTemplate.VORDIPLOM_COLORS[index % ColorTemplate.VORDIPLOM_COLORS.size]
        }.toMap()

        // Her bir ay için
        for (month in allMonths) {
            // Her bir kullanıcı için
            for (username in distinctUsernames) {
                val monthlyExpenses = monthlyExpensesMap[username] ?: emptyMap()
                val amount = monthlyExpenses[month] ?: 0f
                barEntriesList.add(BarEntry(barEntriesList.size.toFloat(), amount))
            }
            xAxisLabels.add(month)
        }

        val dataSet = BarDataSet(barEntriesList, getString(R.string.user_monthly_expenses))

        // Her bir bar için kullanıcı rengini ayarlayın
        dataSet.colors = barEntriesList.mapIndexed { index, _ ->
            userColors[distinctUsernames[index % distinctUsernames.size]] ?: Color.BLACK
        }

        val barData = BarData(dataSet)
        val barChart = binding.barChartMonthly
        val xAxis = barChart.xAxis
// Eksen etiketlerini aylara dönüştürün
        xAxis.valueFormatter = IndexAxisValueFormatter(allMonths.map { getMonthName(it) })
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f

        // Sol eksende büyük değerler için özelleştirmeler
        val leftAxis = barChart.axisLeft
        leftAxis.valueFormatter = LargeValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f

        barChart.axisRight.isEnabled = false

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()

        val legend = barChart.legend
        legend.isEnabled = true
        legend.setDrawInside(true)
        legend.xEntrySpace = 50f
        legend.yEntrySpace = 30f
        legend.textSize = 12f

        // Açıklama metni için kullanıcı adlarını ve renkleri ayarlayın
        val legendEntries = mutableListOf<LegendEntry>()
        for (username in distinctUsernames) {
            val color = userColors[username] ?: Color.BLACK
            val entry = LegendEntry()
            entry.label = username
            entry.formColor = color
            entry.formLineWidth = 4f
            legendEntries.add(entry)
        }
        legend.setCustom(legendEntries)
    }

    private fun getMonthName(monthNumber: String): String {
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val index = monthNumber.toIntOrNull()?.let { it - 1 } ?: 0
        return monthNames.getOrNull(index) ?: ""
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
        pieChart.setDrawEntryLabels(true)
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