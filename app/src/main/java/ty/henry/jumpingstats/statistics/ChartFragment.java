package ty.henry.jumpingstats.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.time.format.DateTimeFormatter;
import java.util.List;

import ty.henry.jumpingstats.competitions.Competition;

public abstract class ChartFragment extends Fragment {

    private StatsFragment statsFragment;
    private int entriesCount;
    private int labelCount;
    private BarLineChartBase chart;

    static int[] colors = new int[]{Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.GRAY};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!(getParentFragment() instanceof StatsFragment)) {
            throw new IllegalStateException("Parent fragment for ChartFragment must be StatsFragment");
        }
        statsFragment = (StatsFragment) getParentFragment();

        labelCount = getLabelCount();

        View fragmentView = inflater.inflate(getLayoutId(), container, false);
        chart = fragmentView.findViewById(getChartId());

        setChartProperties(fragmentView);
        setXAxisProperties();
        setYAxisProperties();

        setChartValues();

        chart.invalidate();

        SeekBar seekBar = fragmentView.findViewById(getSeekBarId());
        seekBar.setOnSeekBarChangeListener(getChartSeekBarListener());

        return fragmentView;
    }

    protected abstract int getLayoutId();

    protected abstract int getChartId();

    protected abstract int getSeekBarId();

    protected abstract int getLabelCount();

    protected abstract void setChartValues();

    protected void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }

    protected BarLineChartBase getChart() {
        return chart;
    }

    protected StatsFragment getStatsFragment() {
        return statsFragment;
    }

    protected void setChartProperties(View fragmentView) {
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
    }

    protected void setXAxisProperties() {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(labelCount, true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(labelCount - 1);
        xAxis.setTextSize(12);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisLineWidth(1);
        xAxis.setAxisLineColor(Color.BLACK);
    }

    protected void setYAxisProperties() {
        chart.getAxisRight().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextSize(12);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1);
        leftAxis.setAxisLineColor(Color.BLACK);
    }

    protected SeekBar.OnSeekBarChangeListener getChartSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxBegin = entriesCount - labelCount;
                int begin = Math.round(((float) progress*maxBegin)/100);
                setDataRange(begin);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    protected void setDataRange(int begin) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(begin);
        xAxis.setAxisMaximum(begin+labelCount-1);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    protected IAxisValueFormatter getXValuesFormatter(List<Float> xValues) {
        return (value, axis) -> {
            int ind = (int) value;
            if(ind < xValues.size()) {
                return xValues.get(ind)+"";
            }
            else {
                return "";
            }
        };
    }

    protected IAxisValueFormatter getCompetitionsValueFormatter() {
        List<Competition> competitions = statsFragment.selectedCompetitionsList;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        return (value, axis) -> {
            int ind = ((int)value)/2;
            if(ind < competitions.size())
                return formatter.format(competitions.get(ind).getDate());
            else
                return "";
        };
    }
}
