package ty.henry.jumpingstats.statistics;


import android.arch.lifecycle.ViewModelProviders;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
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
    private boolean groupByK;
    private TableItem[] tableItems;

    public TableFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDataFromSharedPreferences();
        View fragmentView = inflater.inflate(R.layout.fragment_table, container, false);

        tableItems = TableItem.values();

        ConstraintLayout constraintLayout = fragmentView.findViewById(R.id.constraintLayout);
        if(groupByK && selectedCompetitionsList.size() > 0) {
            Classifier<Competition, Float> compClassifier = new Classifier<>(selectedCompetitionsList,
                    Competition::getPointK);
            List<Float> pointsK = compClassifier.getValues();
            Collections.sort(pointsK);
            int firstLastGroup = pointsK.size()==1 ? FIRST_AND_LAST : FIRST;
            int prevId = addItemsGroup(constraintLayout, pointsK.get(0),
                    constraintLayout.getId(), firstLastGroup, compClassifier.getGroup(pointsK.get(0)));
            firstLastGroup = MIDDLE;
            for(int i=1; i<pointsK.size(); i++) {
                if(i == pointsK.size() - 1) firstLastGroup = LAST;
                prevId = addItemsGroup(constraintLayout, pointsK.get(i),
                        prevId, firstLastGroup, compClassifier.getGroup(pointsK.get(i)));
            }
        }
        else {
            addItemsGroup(constraintLayout, 0, constraintLayout.getId(), FIRST_AND_LAST, selectedCompetitionsList);
        }

        return fragmentView;
    }

    private void getDataFromSharedPreferences() {
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        List<Jumper> allJumpers = mainViewModel.getJumpers().getValue();

        selectedJumpersList = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> selectedJumpers = preferences.getStringSet(TableDataFragment.JUMPERS_PREF_KEY,
                Collections.emptySet());
        for(Jumper jump : allJumpers) {
            if(selectedJumpers.contains(jump.getId()+"")) {
                selectedJumpersList.add(jump);
            }
        }

        selectedCompetitionsList = new ArrayList<>();
        TreeMap<Season, TreeSet<Competition>> seasonToCompetitions = mainViewModel.getSeasonToCompetitions().getValue();
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
        titleTextView.setText(tableItem.getTitleId());
        constraintLayout.addView(titleTextView);

        GroupResult groupResult = new GroupResult(selectedJumpersList, competitions, tableItem);
        String[] values;
        try {
            String maxString = groupResult.getMax().toString();
            String minString = groupResult.getMin().toString();
            String avgString = String.format("%.2f", groupResult.getAvg());
            values = new String[]{maxString, minString, avgString};
        } catch (NoResultForJumperException ex) {
            values = new String[]{"-", "-", "-"};
        }

        TextView[] valueNameTextViews = new TextView[3];
        TextView[] valueTextViews = new TextView[3];
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
}
