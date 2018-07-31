package ty.henry.jumpingstats.statistics;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

import static java.util.stream.Collectors.*;

public class BarChartFragment extends Fragment {

    private StatsFragment statsFragment;
    private BarChart barChart;
    private int entriesCount;

    private static final int LABEL_COUNT = 3;

    public BarChartFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!(getParentFragment() instanceof StatsFragment)) {
            throw new IllegalStateException("Parent fragment for LineChartFragment must be StatsFragment");
        }
        statsFragment = (StatsFragment) getParentFragment();

        View fragmentView = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        barChart = fragmentView.findViewById(R.id.barChart);
        barChart.setHighlightPerDragEnabled(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.setDescription(null);

        ((TextView) fragmentView.findViewById(R.id.xTitle)).setText(statsFragment.xAxisTitle);
        ((TextView) fragmentView.findViewById(R.id.yTitle)).setText(statsFragment.yAxisTitle);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(LABEL_COUNT+1, true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(LABEL_COUNT);
        xAxis.setTextSize(12);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisLineWidth(1);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setCenterAxisLabels(true);

        barChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextSize(12);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1);
        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setSpaceTop(15f);

        ArrayList<Competition> competitions = statsFragment.selectedCompetitionsList;
        ArrayList<Jumper> jumpers = statsFragment.selectedJumpersList;

        if(statsFragment.xValueGetter == null) {
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            entriesCount = Math.max(2*competitions.size(), LABEL_COUNT);
            xAxis.setValueFormatter((value, axis) -> {
                int ind = ((int)value)/2;
                if(ind < competitions.size())
                    return competitions.get(ind).getShortDate();
                else
                    return "";
            });
            for(int ii = 0; ii < jumpers.size(); ii++) {
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                for(int i = 0; i < competitions.size(); i++) {
                    for(int series = 1; series < 3; series++) {
                        float x = 2 * i + series - 1;
                        if(jumpers.size() == 1) {
                            x += 0.5;
                        }
                        try {
                            float y = statsFragment.yValueGetter.getValue(jumpers.get(ii), competitions.get(i), series);
                            barEntries.add(new BarEntry(x, y));
                        }
                        catch (Exception ex) {
                            barEntries.add(new BarEntry(x, Float.NaN));
                        }
                    }
                }
                if(barEntries.size() > 0) {
                    BarDataSet dataSet = new BarDataSet(barEntries, jumpers.get(ii).getText()[0]);
                    dataSet.setColor(LineChartFragment.colors[ii]);
                    dataSet.setDrawValues(false);
                    dataSets.add(dataSet);
                }
            }
            BarData barData = new BarData(dataSets);
            float groupSpace = 0.1f;
            float barSpace = 0f;
            float barWidth = (1-groupSpace)/jumpers.size();
            barData.setBarWidth(barWidth);
            barChart.setData(barData);
            if(jumpers.size() > 1 && competitions.size() > 0) {
                barChart.groupBars(0, groupSpace, barSpace);
            }

            Legend legend = barChart.getLegend();
            legend.setTextSize(14);
            legend.setDrawInside(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setWordWrapEnabled(true);
        }
        else {
            float barWidth = 0.9f;
            StatsFragment.XValueGetter xValueGetter = statsFragment.xValueGetter;
            Map<Float, List<Jumper>> xValueToJumpers = jumpers.stream()
                    .collect(groupingBy(xValueGetter::getValue));
            ArrayList<Float> xValues = new ArrayList<>(xValueToJumpers.keySet());
            Collections.sort(xValues);
            entriesCount = Math.max(xValues.size(), LABEL_COUNT);
            xAxis.setValueFormatter(((value, axis) -> {
                int ind = (int) value;
                if(ind < xValues.size()) {
                    return xValues.get(ind)+"";
                }
                else {
                    return "";
                }
            }));
            ArrayList<BarEntry> barEntries = new ArrayList<>();
            for(int i=0; i<xValues.size(); i++) {
                DoubleSummaryStatistics summaryStatistics = xValueToJumpers.get(xValues.get(i))
                        .stream().flatMap(this::getResultsForJumper).collect(summarizingDouble(r -> r));
                if(summaryStatistics.getCount() > 0) {
                    barEntries.add(new BarEntry(i + 0.5f, (float) summaryStatistics.getAverage()));
                }
            }
            if(barEntries.size() > 0) {
                BarDataSet dataSet = new BarDataSet(barEntries, null);
                dataSet.setColor(Color.RED);
                dataSet.setDrawValues(false);
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(barWidth);
                barChart.setData(barData);
            }
            barChart.getLegend().setEnabled(false);
        }

        barChart.setFitBars(true);
        barChart.invalidate();

        SeekBar seekBar = fragmentView.findViewById(R.id.barChartSeekBar);
        seekBar.setOnSeekBarChangeListener(LineChartFragment.getChartSeekBarListener(barChart,
                entriesCount+1, LABEL_COUNT+1));

        return fragmentView;
    }

    private Stream<Double> getResultsForJumper(Jumper jumper) {
        ArrayList<Double> results = new ArrayList<>();
        for(Competition comp : statsFragment.selectedCompetitionsList) {
            for(int i=1; i<3; i++) {
                try {
                    results.add((double)statsFragment.yValueGetter.getValue(jumper, comp, i));
                }
                catch (Exception ex) {

                }
            }
        }
        return results.stream();
    }

}
