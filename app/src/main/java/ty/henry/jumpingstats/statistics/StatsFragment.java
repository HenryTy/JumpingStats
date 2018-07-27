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
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class StatsFragment extends Fragment {

    public static final int MAX_JUMPERS = 5;

    private StatsFragmentListener statsFragmentListener;
    ArrayList<Jumper> allJumpers;
    boolean[] isJumperSelected;
    TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;
    TreeMap<Season, ArrayList<Boolean>> isCompetitionSelected;

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
        isJumperSelected = new boolean[allJumpers.size()];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> selectedJumpers = preferences.getStringSet(ChartsDataFragment.JUMPERS_PREF_KEY,
                Collections.emptySet());
        for(int i=0; i<allJumpers.size(); i++) {
            if(selectedJumpers.contains(allJumpers.get(i).getId()+"")) {
                isJumperSelected[i] = true;
            }
        }

        isCompetitionSelected = new TreeMap<>();
        for(Season season : seasonToCompetitions.keySet()) {
            ArrayList<Boolean> isSelected = new ArrayList<>();
            Set<String> selectedCompetitions = preferences
                    .getStringSet(ChartsDataFragment.SEASON_PREF_KEY(season),
                            Collections.emptySet());
            for(Competition comp : seasonToCompetitions.get(season)) {
                if(selectedCompetitions.contains(comp.getId()+"")) {
                    isSelected.add(true);
                }
                else {
                    isSelected.add(false);
                }
            }
            isCompetitionSelected.put(season, isSelected);
        }
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
            return new LineChartFragment();
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

}
