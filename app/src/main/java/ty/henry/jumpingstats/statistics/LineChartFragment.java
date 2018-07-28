package ty.henry.jumpingstats.statistics;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;


public class LineChartFragment extends Fragment {

    private StatsFragment statsFragment;
    private LineChart lineChart;
    private int entriesCount;
    private int[] colors = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.DKGRAY};

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
        lineChart.setDescription(null);

        ((TextView) fragmentView.findViewById(R.id.xTitle)).setText(statsFragment.xAxisTitle);
        ((TextView) fragmentView.findViewById(R.id.yTitle)).setText(statsFragment.yAxisTitle);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(LABEL_COUNT, true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(LABEL_COUNT - 1);
        xAxis.setTextSize(12);

        lineChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextSize(12);

        ArrayList<Competition> competitions = statsFragment.selectedCompetitionsList;
        ArrayList<Jumper> jumpers = statsFragment.selectedJumpersList;
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        if(statsFragment.xValueGetter == null) {
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
                            //entries.add(new Entry(x, 130));
                            LineDataSet dataSet = new LineDataSet(entries, jumpers.get(ii).getText()[0]);
                            dataSet.setColor(colors[ii]);
                            dataSet.setDrawValues(false);
                            dataSets.add(dataSet);
                            entries = new ArrayList<>();
                        }
                    }
                }
                LineDataSet dataSet = new LineDataSet(entries, jumpers.get(ii).getText()[0]);
                dataSet.setColor(colors[ii]);
                dataSet.setDrawValues(false);
                dataSets.add(dataSet);
            }
        }
        else {

        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(14);
        legend.setDrawInside(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        lineChart.invalidate();

        SeekBar seekBar = fragmentView.findViewById(R.id.lineChartSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxBegin = entriesCount - LABEL_COUNT;
                int begin = progress*maxBegin/100;
                setDataRange(begin);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return fragmentView;
    }

    private void setDataRange(int begin) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(begin);
        xAxis.setAxisMaximum(begin+LABEL_COUNT-1);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

}
