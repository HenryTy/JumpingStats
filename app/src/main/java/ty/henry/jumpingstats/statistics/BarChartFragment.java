package ty.henry.jumpingstats.statistics;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public class BarChartFragment extends ChartFragment {

    private static final int LABEL_COUNT = 3;

    public BarChartFragment() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bar_chart;
    }

    @Override
    protected int getChartId() {
        return R.id.barChart;
    }

    @Override
    protected int getSeekBarId() {
        return R.id.barChartSeekBar;
    }

    @Override
    protected int getLabelCount() {
        return LABEL_COUNT + 1;
    }

    @Override
    protected void setChartProperties(View fragmentView) {
        super.setChartProperties(fragmentView);
        BarChart barChart = (BarChart) getChart();
        barChart.setDescription(null);
        barChart.setFitBars(true);
        ((TextView) fragmentView.findViewById(R.id.xTitle)).setText(getStatsFragment().xAxisTitle);
        ((TextView) fragmentView.findViewById(R.id.yTitle)).setText(getStatsFragment().yAxisTitle);
    }

    @Override
    protected void setXAxisProperties() {
        super.setXAxisProperties();
        getChart().getXAxis().setCenterAxisLabels(true);
    }

    @Override
    protected void setYAxisProperties() {
        super.setYAxisProperties();
        getChart().getAxisLeft().setSpaceTop(15f);
    }

    @Override
    protected void setChartValues() {
        StatsFragment statsFragment = getStatsFragment();

        ArrayList<Competition> competitions = statsFragment.selectedCompetitionsList;
        ArrayList<Jumper> jumpers = statsFragment.selectedJumpersList;
        BarChart barChart = (BarChart) getChart();

        if (statsFragment.xValueGetter == null) {
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            setEntriesCount(Math.max(2 * competitions.size(), LABEL_COUNT) + 1);
            getChart().getXAxis().setValueFormatter(getCompetitionsValueFormatter());
            for (int ii = 0; ii < jumpers.size(); ii++) {
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                for (int i = 0; i < competitions.size(); i++) {
                    for (int series = 1; series < 3; series++) {
                        float x = 2 * i + series - 1;
                        if (jumpers.size() == 1) {
                            x += 0.5;
                        }
                        try {
                            float y = statsFragment.yValueGetter.getValue(jumpers.get(ii), competitions.get(i), series);
                            barEntries.add(new BarEntry(x, y));
                        } catch (Exception ex) {
                            barEntries.add(new BarEntry(x, Float.NaN));
                        }
                    }
                }
                if (barEntries.size() > 0) {
                    BarDataSet dataSet = new BarDataSet(barEntries, jumpers.get(ii).getText()[0]);
                    dataSet.setColor(colors[ii]);
                    dataSet.setDrawValues(false);
                    dataSets.add(dataSet);
                }
            }
            BarData barData = new BarData(dataSets);
            float groupSpace = 0.1f;
            float barSpace = 0f;
            float barWidth = (1 - groupSpace) / jumpers.size();
            barData.setBarWidth(barWidth);
            barChart.setData(barData);
            if (jumpers.size() > 1 && competitions.size() > 0) {
                barChart.groupBars(0, groupSpace, barSpace);
            }

            Legend legend = barChart.getLegend();
            legend.setTextSize(14);
            legend.setDrawInside(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setWordWrapEnabled(true);
        } else {
            float barWidth = 0.9f;
            Map<Float, List<Jumper>> xValueToJumpers = getXValueToJumpersMap();
            ArrayList<Float> xValues = getXValuesList(xValueToJumpers);
            setEntriesCount(Math.max(xValues.size(), LABEL_COUNT) + 1);
            getChart().getXAxis().setValueFormatter(getXValuesFormatter(xValues));
            ArrayList<BarEntry> barEntries = new ArrayList<>();
            for (int i = 0; i < xValues.size(); i++) {
                DoubleSummaryStatistics summaryStatistics = getSummaryStatistics(xValueToJumpers.get(xValues.get(i)));
                if (summaryStatistics.getCount() > 0) {
                    barEntries.add(new BarEntry(i + 0.5f, (float) summaryStatistics.getAverage()));
                }
            }
            if (barEntries.size() > 0) {
                BarDataSet dataSet = new BarDataSet(barEntries, null);
                dataSet.setColor(Color.RED);
                dataSet.setDrawValues(false);
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(barWidth);
                barChart.setData(barData);
            }
            barChart.getLegend().setEnabled(false);
        }
    }

}
