package ty.henry.jumpingstats.statistics;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public class LineChartFragment extends ChartFragment {

    private static final int LABEL_COUNT = 6;

    public LineChartFragment() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_line_chart;
    }

    @Override
    protected int getChartId() {
        return R.id.lineChart;
    }

    @Override
    protected int getSeekBarId() {
        return R.id.lineChartSeekBar;
    }

    @Override
    protected int getLabelCount() {
        return LABEL_COUNT;
    }

    @Override
    protected void setChartProperties(View fragmentView) {
        super.setChartProperties(fragmentView);
        getChart().getDescription().setText(getStatsFragment().xAxisTitle);
        getChart().getDescription().setTextSize(14);
        ((TextView) fragmentView.findViewById(R.id.yTitle)).setText(getStatsFragment().yAxisTitle);
    }

    @Override
    protected void setChartValues() {
        StatsFragment statsFragment = getStatsFragment();

        ArrayList<Competition> competitions = statsFragment.selectedCompetitionsList;
        ArrayList<Jumper> jumpers = statsFragment.selectedJumpersList;
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        LegendEntry[] legendEntries;

        if(statsFragment.xValueGetter == null) {
            legendEntries = new LegendEntry[jumpers.size()];
            setEntriesCount(Math.max(2*competitions.size(), LABEL_COUNT));
            getChart().getXAxis().setValueFormatter(getCompetitionsValueFormatter());
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
            Map<Float, List<Jumper>> xValueToJumpers = getXValueToJumpersMap();
            ArrayList<Float> xValues = getXValuesList(xValueToJumpers);
            setEntriesCount(Math.max(xValues.size(), LABEL_COUNT));
            getChart().getXAxis().setValueFormatter(getXValuesFormatter(xValues));
            ArrayList<Entry> entries = new ArrayList<>();
            for(int i=0; i<xValues.size(); i++) {
                DoubleSummaryStatistics summaryStatistics = getSummaryStatistics(xValueToJumpers.get(xValues.get(i)));
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
        ((LineChart)getChart()).setData(lineData);

        Legend legend = getChart().getLegend();
        legend.setCustom(legendEntries);
        legend.setTextSize(14);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setWordWrapEnabled(true);
    }

    private LineDataSet createDataSet(ArrayList<Entry> entries, int color) {
        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        return dataSet;
    }

}
