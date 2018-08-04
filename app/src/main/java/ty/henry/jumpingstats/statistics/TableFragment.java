package ty.henry.jumpingstats.statistics;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.Barrier;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class TableFragment extends Fragment {

    private static final int TEXT_SIZE = 20;
    private static final int MARGIN = 8;

    private static final int FIRST = 0;
    private static final int MIDDLE = 1;
    private static final int LAST = 2;
    private static final int FIRST_AND_LAST = 3;

    private List<Jumper> selectedJumpersList;
    private List<Competition> selectedCompetitionsList;
    private StatsFragment.StatsFragmentListener statsFragmentListener;
    private boolean groupByK;
    private TableItem[] tableItems;

    public TableFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(!(context instanceof StatsFragment.StatsFragmentListener)) {
            throw new IllegalStateException("TableFragment's activity must implement StatsFragmentListener");
        }
        statsFragmentListener = (StatsFragment.StatsFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDataFromSharedPreferences();
        View fragmentView = inflater.inflate(R.layout.fragment_table, container, false);

        String[] titles = getResources().getStringArray(R.array.table_values);
        StatsFragment.YValueGetter[] functions = new StatsFragment.YValueGetter[]{
                (jumper, competition, series) -> jumper.getResults(competition)[series-1].getDistance(),
                (jumper, competition, series) -> Math.abs(jumper.getResults(competition)[series-1].getWind()),
                (jumper, competition, series) -> jumper.getResults(competition)[series-1].getSpeed(),
                (jumper, competition, series) -> {
                    float[] marks = jumper.getResults(competition)[series-1].getStyleScores();
                    return (float) IntStream.range(0, marks.length)
                            .mapToDouble(i -> marks[i]).average().getAsDouble();
                },
                (jumper, competition, series) -> {
                    Result[] results = jumper.getResults(competition);
                    float diff = results[1].points() - results[0].points();
                    return Math.abs(diff);
                }
        };
        tableItems = new TableItem[titles.length];
        for(int i=0; i<titles.length; i++) {
            tableItems[i] = new TableItem();
            tableItems[i].title = titles[i];
            tableItems[i].function = functions[i];
        }

        ConstraintLayout constraintLayout = fragmentView.findViewById(R.id.constraintLayout);
        if(groupByK && selectedCompetitionsList.size() > 0) {
            Map<Float, List<Competition>> pointKToComps = selectedCompetitionsList
                    .stream()
                    .collect(Collectors.groupingBy(Competition::getPointK));
            ArrayList<Float> pointsK = new ArrayList<>(pointKToComps.keySet());
            Collections.sort(pointsK);
            int firstLastGroup = pointsK.size()==1 ? FIRST_AND_LAST : FIRST;
            int prevId = addItemsGroup(constraintLayout, pointsK.get(0),
                    constraintLayout.getId(), firstLastGroup, pointKToComps.get(pointsK.get(0)));
            firstLastGroup = MIDDLE;
            for(int i=1; i<pointsK.size(); i++) {
                if(i == pointsK.size() - 1) firstLastGroup = LAST;
                prevId = addItemsGroup(constraintLayout, pointsK.get(i),
                        prevId, firstLastGroup, pointKToComps.get(pointsK.get(i)));
            }
        }
        else {
            addItemsGroup(constraintLayout, 0, constraintLayout.getId(), FIRST_AND_LAST, selectedCompetitionsList);
        }

        return fragmentView;
    }

    private void getDataFromSharedPreferences() {
        selectedJumpersList = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> selectedJumpers = preferences.getStringSet(TableDataFragment.JUMPERS_PREF_KEY,
                Collections.emptySet());
        for(Jumper jump : statsFragmentListener.getJumpersList()) {
            if(selectedJumpers.contains(jump.getId()+"")) {
                selectedJumpersList.add(jump);
            }
        }

        selectedCompetitionsList = new ArrayList<>();
        TreeMap<Season, TreeSet<Competition>> seasonToCompetitions = statsFragmentListener.getSeasonToCompetitionsMap();
        for(Season season : seasonToCompetitions.keySet()) {
            Set<String> selectedCompetitions = preferences
                    .getStringSet(TableDataFragment.SEASON_PREF_KEY(season),
                            Collections.emptySet());
            for(Competition comp : seasonToCompetitions.get(season)) {
                if(selectedCompetitions.contains(comp.getId()+"")) {
                    selectedCompetitionsList.add(comp);
                }
            }
        }

        groupByK = preferences.getBoolean(TableDataFragment.GROUP_BY_K_PREF_KEY, false);
    }

    private int addItemsGroup(ConstraintLayout constraintLayout, float pointK,
                              int lastViewId, int firstLastGroup, List<Competition> competitions) {
        int i = 0;
        if(pointK != 0) {
            TextView pointKTextView = new TextView(getActivity());
            pointKTextView.setText(String.format("K %.0f", pointK));
            setTextViewProperties(pointKTextView, TEXT_SIZE+5);
            constraintLayout.addView(pointKTextView);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(pointKTextView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, MARGIN);
            constraintSet.connect(pointKTextView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, MARGIN);
            if(firstLastGroup == FIRST || firstLastGroup == FIRST_AND_LAST) {
                constraintSet.connect(pointKTextView.getId(), ConstraintSet.TOP,
                        lastViewId, ConstraintSet.TOP, MARGIN);
            }
            else {
                constraintSet.connect(pointKTextView.getId(), ConstraintSet.TOP,
                        lastViewId, ConstraintSet.BOTTOM, MARGIN);
            }
            constraintSet.applyTo(constraintLayout);

            lastViewId = pointKTextView.getId();
        }
        else {
            lastViewId = addTableItem(constraintLayout, tableItems[0],
                    constraintLayout.getId(), FIRST, competitions);
            i = 1;
        }
        int firstLast = MIDDLE;
        for( ; i < tableItems.length; i++) {
            if(i == tableItems.length-1 && (firstLastGroup == LAST || firstLastGroup == FIRST_AND_LAST)) {
                firstLast = LAST;
            }
            lastViewId = addTableItem(constraintLayout, tableItems[i], lastViewId, firstLast, competitions);
        }
        return lastViewId;
    }

    private int addTableItem(ConstraintLayout constraintLayout, TableItem tableItem,
                             int lastViewId, int firstLast, List<Competition> competitions) {
        TextView titleTextView = new TextView(getActivity());
        setTextViewProperties(titleTextView, TEXT_SIZE);
        titleTextView.setText(tableItem.title);
        constraintLayout.addView(titleTextView);

        TextView[] valueNameTextViews = new TextView[3];
        TextView[] valueTextViews = new TextView[3];
        String[] values = tableItem.getValues(selectedJumpersList, competitions);
        String[] valuesNames = new String[]{getString(R.string.max), getString(R.string.min), getString(R.string.avg)};

        for(int i=0; i<3; i++) {
            valueNameTextViews[i] = new TextView(getActivity());
            valueTextViews[i] = new TextView(getActivity());
            valueNameTextViews[i].setText(valuesNames[i]);
            valueTextViews[i].setText(values[i]);
            setTextViewProperties(valueNameTextViews[i], TEXT_SIZE);
            setTextViewProperties(valueTextViews[i], TEXT_SIZE);

            constraintLayout.addView(valueNameTextViews[i]);
            constraintLayout.addView(valueTextViews[i]);
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(titleTextView.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, MARGIN);
        constraintSet.connect(titleTextView.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, MARGIN);
        if(firstLast == FIRST) {
            constraintSet.connect(titleTextView.getId(), ConstraintSet.TOP, lastViewId, ConstraintSet.TOP, MARGIN);
        }
        else {
            constraintSet.connect(titleTextView.getId(), ConstraintSet.TOP, lastViewId, ConstraintSet.BOTTOM, MARGIN);
        }

        int prevId = titleTextView.getId();
        for(int i=0; i<3; i++) {
            int id1 = valueNameTextViews[i].getId();
            int id2 = valueTextViews[i].getId();

            constraintSet.connect(id1, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, MARGIN);
            constraintSet.connect(id2, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, MARGIN);
            constraintSet.connect(id1, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, MARGIN);
            constraintSet.connect(id2, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, MARGIN);

            constraintSet.connect(id1, ConstraintSet.RIGHT, id2, ConstraintSet.LEFT, MARGIN);
            constraintSet.connect(id2, ConstraintSet.LEFT, id1, ConstraintSet.RIGHT);
            constraintSet.setHorizontalWeight(id1, 1);
            constraintSet.setHorizontalWeight(id2, 4);

            Barrier barrier = new Barrier(getActivity());
            int barrierId = View.generateViewId();
            barrier.setId(barrierId);
            barrier.setType(Barrier.BOTTOM);
            barrier.setReferencedIds(new int[]{id1, id2});
            constraintLayout.addView(barrier);

            prevId = barrierId;

            if(i==2 && firstLast==LAST) {
                constraintSet.connect(id1, ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM, MARGIN);
                constraintSet.connect(id2, ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM, MARGIN);
            }
        }

        constraintSet.applyTo(constraintLayout);

        return prevId;
    }

    private void setTextViewProperties(TextView textView, int textSize) {
        textView.setTextSize(textSize);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        textView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams
                (ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private static class TableItem {
        String title;
        StatsFragment.YValueGetter function;

        String[] getValues(List<Jumper> jumpers, List<Competition> competitions) {
            String[] values = new String[3];
            getJumperAndResultStream(jumpers, competitions)
                    .max(JumperAndResult::compare)
                    .ifPresent(r -> values[0] = r.toString());
            if(values[0] != null) {
                getJumperAndResultStream(jumpers, competitions)
                        .min(JumperAndResult::compare)
                        .ifPresent(r -> values[1] = r.toString());
                double avg = getJumperAndResultStream(jumpers, competitions)
                        .collect(Collectors.averagingDouble(JumperAndResult::getResult));
                values[2] = String.format("%.2f", avg);
            }
            else {
                for(int i=0; i<3; i++) values[i] = "-";
            }
            return values;
        }

        private Stream<JumperAndResult> getJumperAndResultStream(List<Jumper> jumpers, List<Competition> competitions){
            return jumpers.stream().flatMap(j -> getResultsForJumper(j, competitions));
        }

        private Stream<JumperAndResult> getResultsForJumper(Jumper jumper, List<Competition> competitions) {
            ArrayList<JumperAndResult> results = new ArrayList<>();
            for(Competition comp : competitions) {
                for(int i=1; i<3; i++) {
                    try {
                        double res = (double)function.getValue(jumper, comp, i);
                        results.add(new JumperAndResult(jumper, res));
                    }
                    catch (Exception ex) {

                    }
                }
            }
            return results.stream();
        }
    }

    private static class JumperAndResult {

        Jumper jumper;
        double result;

        JumperAndResult(Jumper jumper, double result) {
            this.jumper = jumper;
            this.result = result;
        }

        double getResult() {
            return result;
        }

        static int compare(JumperAndResult r1, JumperAndResult r2) {
            if(r1.result < r2.result) return -1;
            if(r1.result > r2.result) return 1;
            return 0;
        }

        public String toString() {
            return String.format("%.2f (%s)", result, jumper.getText()[0]);
        }
    }

}
