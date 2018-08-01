package ty.henry.jumpingstats.statistics;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class StatsFragment extends Fragment {

    interface YValueGetter {
        float getValue(Jumper jumper, Competition competition, int series) throws Exception;
    }

    interface XValueGetter {
        float getValue(Jumper jumper);
    }

    public static final int MAX_JUMPERS = 5;

    private StatsFragmentListener statsFragmentListener;
    ArrayList<Jumper> allJumpers;
    ArrayList<Jumper> selectedJumpersList;
    TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;
    ArrayList<Competition> selectedCompetitionsList;
    YValueGetter yValueGetter;
    XValueGetter xValueGetter;
    String yAxisTitle;
    String xAxisTitle;

    public interface StatsFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
        ArrayList<Jumper> getJumpersList();
        TreeMap<Season, TreeSet<Competition>> getSeasonToCompetitionsMap();
    }

    public StatsFragment() {

    }

    public void setListener(StatsFragmentListener listener) {
        this.statsFragmentListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stats, container, false);
        ViewPager viewPager = fragmentView.findViewById(R.id.statsViewPager);
        viewPager.setAdapter(new MyPageAdapter(getChildFragmentManager()));

        allJumpers = statsFragmentListener.getJumpersList();
        seasonToCompetitions = statsFragmentListener.getSeasonToCompetitionsMap();
        getDataFromSharedPreferences();
        return fragmentView;
    }


    private void getDataFromSharedPreferences() {
        selectedJumpersList = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> selectedJumpers = preferences.getStringSet(ChartsDataFragment.JUMPERS_PREF_KEY,
                Collections.emptySet());
        for(int i=0; i<allJumpers.size(); i++) {
            if(selectedJumpers.contains(allJumpers.get(i).getId()+"")) {
                selectedJumpersList.add(allJumpers.get(i));
            }
        }

        selectedCompetitionsList = new ArrayList<>();
        for(Season season : seasonToCompetitions.keySet()) {
            Set<String> selectedCompetitions = preferences
                    .getStringSet(ChartsDataFragment.SEASON_PREF_KEY(season),
                            Collections.emptySet());
            for(Competition comp : seasonToCompetitions.get(season)) {
                if(selectedCompetitions.contains(comp.getId()+"")) {
                    selectedCompetitionsList.add(comp);
                }
            }
        }
        Collections.reverse(selectedCompetitionsList);

        String xOption = preferences.getString(ChartsDataFragment.X_AXIS_PREF_KEY, getString(R.string.x_axis_default));
        String[] xAxisOptions = getResources().getStringArray(R.array.x_axis_options);
        if(xOption.equals(xAxisOptions[1])) {
            xValueGetter = Jumper::getAge;
        }
        else if(xOption.equals(xAxisOptions[2])) {
            xValueGetter = Jumper::getHeight;
        }
        else {
            xValueGetter = null;
        }

        String yOption = preferences.getString(ChartsDataFragment.Y_AXIS_PREF_KEY, getString(R.string.y_axis_default));
        String[] yAxisOptions = getResources().getStringArray(R.array.y_axis_options);
        if(yOption.equals(yAxisOptions[0])) {
            yValueGetter = ((jumper, competition, series) -> jumper.getResults(competition)[series-1].getDistance());
        }
        else if(yOption.equals(yAxisOptions[1])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].getDistance() - competition.getPointK());
        }
        else if(yOption.equals(yAxisOptions[2])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].pointsForDistance());
        }
        else if(yOption.equals(yAxisOptions[3])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].getWind());
        }
        else if(yOption.equals(yAxisOptions[4])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].pointsForWind());
        }
        else if(yOption.equals(yAxisOptions[5])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].getSpeed());
        }
        else if(yOption.equals(yAxisOptions[6])) {
            yValueGetter = ((jumper, competition, series) -> {
                float[] marks = jumper.getResults(competition)[series-1].getStyleScores();
                return (float) IntStream.range(0, marks.length)
                        .mapToDouble(i -> marks[i]).average().getAsDouble();
            });
        }
        else if(yOption.equals(yAxisOptions[7])) {
            yValueGetter = ((jumper, competition, series) ->
                    jumper.getResults(competition)[series-1].points());
        }

        xAxisTitle = xOption;
        yAxisTitle = yOption;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_statistics, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chartsData:
                ChartsDataFragment chartsDataFragment = new ChartsDataFragment();
                chartsDataFragment.setJumpersAndCompetitions(allJumpers, seasonToCompetitions);
                statsFragmentListener.openFragment(chartsDataFragment, true);
                return true;
            case R.id.tableData:
                TableDataFragment tableDataFragment = new TableDataFragment();
                tableDataFragment.setJumpersAndCompetitions(allJumpers, seasonToCompetitions);
                statsFragmentListener.openFragment(tableDataFragment, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class MyPageAdapter extends FragmentPagerAdapter {

        MyPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new BarChartFragment();
                case 2:
                    return new TableFragment();
                default:
                    return new LineChartFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
