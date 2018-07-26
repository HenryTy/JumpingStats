package ty.henry.jumpingstats.statistics;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class StatsFragment extends Fragment {

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
        return fragmentView;
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
