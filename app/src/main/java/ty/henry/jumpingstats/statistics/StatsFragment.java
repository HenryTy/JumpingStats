package ty.henry.jumpingstats.statistics;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class StatsFragment extends Fragment {

    interface YValueGetter {
        float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException;
    }

    public static final int MAX_JUMPERS = 5;

    private StatsFragmentListener statsFragmentListener;
    List<Jumper> allJumpers;
    List<Jumper> selectedJumpersList;
    TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;
    List<Competition> selectedCompetitionsList;
    YOption yOption;
    XOption xOption;

    public interface StatsFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
    }

    public StatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_stats, container, false);
        ViewPager viewPager = fragmentView.findViewById(R.id.statsViewPager);
        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        allJumpers = mainViewModel.getJumpers().getValue();
        seasonToCompetitions = mainViewModel.getSeasonToCompetitions().getValue();
        getDataFromSharedPreferences();

        DBHelper dbHelper = new DBHelper(getActivity());
        dbHelper.fillJumpersWithResults(selectedJumpersList, selectedCompetitionsList);
        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            statsFragmentListener = (StatsFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement StatsFragmentListener");
        }
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

        int xOptionNr = preferences.getInt(ChartsDataFragment.X_AXIS_PREF_KEY, 0);
        xOption = XOption.values()[xOptionNr];

        int yOptionNr = preferences.getInt(ChartsDataFragment.Y_AXIS_PREF_KEY, 0);
        yOption = YOption.values()[yOptionNr];
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
                statsFragmentListener.openFragment(chartsDataFragment, true);
                return true;
            case R.id.tableData:
                TableDataFragment tableDataFragment = new TableDataFragment();
                statsFragmentListener.openFragment(tableDataFragment, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
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
