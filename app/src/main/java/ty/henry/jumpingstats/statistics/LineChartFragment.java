package ty.henry.jumpingstats.statistics;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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


public class LineChartFragment extends Fragment {

    private StatsFragment statsFragment;
    private LineChart lineChart;
    private int entriesCount;
    static int[] colors = new int[]{Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.GRAY};

    private static final int LABEL_COUNT = 6;

    public LineChartFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!(getParentFragment() instanceof StatsFragment)) {
            throw new IllegalStateException("Parent fragment for LineChartFragment must be StatsFragment");
        }
        statsFragment = (StatsFragment) getParentFragment();

        View fragmentView = inflater.inflate(R.layout.fragment_line_chart, container, false);
        lineChart = fragmentView.findViewById(R.id.lineChart);
        lineChart.setHighlightPerDragEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.getDescription().setText(statsFragment.xAxisTitle);
        lineChart.getDescription().setTextSize(14);

        ((TextView) fragmentView.findViewById(R.id.yTitle)).setText(statsFragment.yAxisTitle);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(LABEL_COUNT, true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(LABEL_COUNT - 1);
        xAxis.setTextSize(12);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisLineWidth(1);
        xAxis.setAxisLineColor(Color.BLACK);

        lineChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextSize(12);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1);
        leftAxis.setAxisLineColor(Color.BLACK);

        ArrayList<Competition> competitions = statsFragment.selectedCompetitionsList;
        ArrayList<Jumper> jumpers = statsFragment.selectedJumpersList;
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        LegendEntry[] legendEntries;

        if(statsFragment.xValueGetter == null) {
            legendEntries = new LegendEntry[jumpers.size()];
            entriesCount = Math.max(2*competitions.size(), LABEL_COUNT);
            xAxis.setValueFormatter((value, axis) -> {
                    int ind = ((int)value)/2;
                    if(ind < competitions.size())
                        return competitions.get(ind).getShortDate();
                    else
                        return "";
            });
            for(int ii = 0; ii < jumpers.size(); ii++) {
                ArrayList<Entry> entries = new ArrayList<>();
                for(int i = 0; i < competitions.size(); i++) {
                    for(int series = 1; series < 3; series++) {
                        int x = 2*i+series-1;
                        try {
                            float y = statsFragment.yValueGetter.getValue(jumpers.get(ii), competitions.get(i), series);
                            entries.add(new Entry(x, y));
                        } catch (Exception ex) {
                            if(entries.size() > 0) {
                                LineDataSet dataSet = createDataSet(entries, colors[ii]);
                                dataSets.add(dataSet);
                                entries = new ArrayList<>();
                            }
                        }
                    }
                }
                if(entries.size() > 0) {
                    LineDataSet dataSet = createDataSet(entries, colors[ii]);
                    dataSets.add(dataSet);
                }
                legendEntries[ii] = new LegendEntry(jumpers.get(ii).getText()[0], Legend.LegendForm.SQUARE,
                        Float.NaN, Float.NaN, null, colors[ii]);
            }
        }
        else {
            legendEntries = new LegendEntry[0];
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
            ArrayList<Entry> entries = new ArrayList<>();
            for(int i=0; i<xValues.size(); i++) {
                DoubleSummaryStatistics summaryStatistics = xValueToJumpers.get(xValues.get(i))
                        .stream().flatMap(this::getResultsForJumper).collect(summarizingDouble(r -> r));
                if(summaryStatistics.getCount() > 0) {
                    entries.add(new Entry(i, (float) summaryStatistics.getAverage()));
                }
                else if(entries.size() > 0) {
                    LineDataSet dataSet = createDataSet(entries, Color.RED);
                    dataSets.add(dataSet);
                    entries = new ArrayList<>();
                }
            }
            if(entries.size() > 0) {
                LineDataSet dataSet = createDataSet(entries, Color.RED);
                dataSets.add(dataSet);
            }
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        Legend legend = lineChart.getLegend();
        legend.setCustom(legendEntries);
        legend.setTextSize(14);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setWordWrapEnabled(true);

        lineChart.invalidate();

        SeekBar seekBar = fragmentView.findViewById(R.id.lineChartSeekBar);
        seekBar.setOnSeekBarChangeListener(getChartSeekBarListener(lineChart, entriesCount, LABEL_COUNT));

        return fragmentView;
    }

    private LineDataSet createDataSet(ArrayList<Entry> entries, int color) {
        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        return dataSet;
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

    static SeekBar.OnSeekBarChangeListener getChartSeekBarListener(Chart chart, int entriesCount, int labelCount) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxBegin = entriesCount - labelCount;
                int begin = Math.round(((float) progress*maxBegin)/100);
                setDataRange(chart, begin, labelCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    static void setDataRange(Chart chart, int begin, int count) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(begin);
        xAxis.setAxisMaximum(begin+count-1);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

}
