package ty.henry.jumpingstats;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.TextImageAdapter.TextImage;
import ty.henry.jumpingstats.competitions.AddEditCompetitionFragment;
import ty.henry.jumpingstats.competitions.AddEditResultsFragment;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.CompetitionDetailsFragment;
import ty.henry.jumpingstats.competitions.CompetitionsFragment;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.AddEditJumperFragment;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.jumpers.JumperDetailsFragment;
import ty.henry.jumpingstats.jumpers.JumpersFragment;
import ty.henry.jumpingstats.statistics.StatsFragment;

public class MainActivity extends AppCompatActivity implements JumpersFragment.JumpersFragmentListener,
        CompetitionsFragment.CompetitionsFragmentListener, StatsFragment.StatsFragmentListener {

    public static final String VISIBLE_FRAGMENT = "visible_fragment";

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private ArrayList<Jumper> jumpers;
    private ArrayList<TextImage> seasonsAndCompetitions;
    private TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDataFromDB();

        prepareDrawer();
        StatsFragment statsFragment = new StatsFragment();
        statsFragment.setListener(this);
        openFragment(statsFragment, false);
        getSupportActionBar().setTitle(drawerOptions[0]);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(drawerToggle.onOptionsItemSelected(menuItem)) {
            return true;
        }
        return false;
    }

    private void getDataFromDB() {
        DBHelper dbHelper = new DBHelper(this);
        jumpers = dbHelper.getAllJumpers();
        seasonToCompetitions = dbHelper.getSeasonToCompetitionsMap();
        competitionsMapToList();
    }

    public void openFragment(Fragment fragment, boolean backStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame, fragment, VISIBLE_FRAGMENT);
        if(backStack)
            ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public ArrayList<Jumper> getJumpersList() {
        return jumpers;
    }

    public void onJumperAdded(Jumper jumper) {
        jumpers.add(jumper);
    }

    public void onJumperDeleted(Jumper jumper) {
        jumpers.remove(jumper);
    }

    public void onJumpersDeleted(Set<Jumper> delJumps) {
        jumpers.removeAll(delJumps);
    }

    public void onJumperUpdated(Jumper jumper) {
        for(int i=0; i<jumpers.size(); i++) {
            if(jumpers.get(i).getId()==jumper.getId()) {
                jumpers.set(i, jumper);
                break;
            }
        }
    }

    public ArrayList<TextImage> getCompetitionsList() {
        return seasonsAndCompetitions;
    }

    public TreeMap<Season, TreeSet<Competition>> getSeasonToCompetitionsMap() {
        return seasonToCompetitions;
    }

    public void onCompetitionAdded(Competition competition) {
        TreeSet<Competition> set = seasonToCompetitions.get(competition.getSeason());
        if(set==null) {
            set = new TreeSet<>();
            set.add(competition);
            seasonToCompetitions.put(competition.getSeason(), set);
        }
        else {
            set.add(competition);
        }
        competitionsMapToList();
    }

    public void onCompetitionDeleted(Competition competition) {
        TreeSet<Competition> set = seasonToCompetitions.get(competition.getSeason());
        set.remove(competition);
        if(set.isEmpty()) {
            seasonToCompetitions.remove(competition.getSeason());
        }
        competitionsMapToList();
        for(Jumper j : jumpers) {
            j.removeResultsFromCompetition(competition);
        }
    }

    public void onCompetitionsDeleted(Set<Competition> competitions) {
        for(Competition competition : competitions) {
            TreeSet<Competition> set = seasonToCompetitions.get(competition.getSeason());
            set.remove(competition);
            if(set.isEmpty()) {
                seasonToCompetitions.remove(competition.getSeason());
            }
            for(Jumper j : jumpers) {
                j.removeResultsFromCompetition(competition);
            }
        }
        competitionsMapToList();
    }

    public void onCompetitionUpdated(Competition competition) {
        outerloop:
        for(Season season : seasonToCompetitions.keySet()) {
            TreeSet<Competition> set = seasonToCompetitions.get(season);
            for(Competition c : set) {
                if(c.getId()==competition.getId()) {
                    set.remove(c);
                    if(set.isEmpty()) {
                        seasonToCompetitions.remove(c.getSeason());
                    }
                    for(Jumper j : jumpers) {
                        j.onCompetitionUpdated(c, competition);
                    }
                    break outerloop;
                }
            }
        }
        onCompetitionAdded(competition);
    }

    private void prepareDrawer() {
        drawerOptions = getResources().getStringArray(R.array.drawer_options);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerList = findViewById(R.id.drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, drawerOptions));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment;
                switch (position) {
                    case 1:
                        fragment = new CompetitionsFragment();
                        ((CompetitionsFragment) fragment).setListener(MainActivity.this);
                        break;
                    case 2:
                        fragment = new JumpersFragment();
                        ((JumpersFragment) fragment).setListener(MainActivity.this);
                        break;
                    default:
                        fragment = new StatsFragment();
                        ((StatsFragment) fragment).setListener(MainActivity.this);
                }
                openFragment(fragment, true);
                drawerLayout.closeDrawer(drawerList);
            }
        });
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(VISIBLE_FRAGMENT);
                int position = 0;
                if(fragment instanceof AddEditCompetitionFragment ||
                        fragment instanceof CompetitionsFragment || fragment instanceof CompetitionDetailsFragment ||
                        fragment instanceof AddEditResultsFragment) {
                    position = 1;
                }
                else if(fragment instanceof JumpersFragment ||
                        fragment instanceof AddEditJumperFragment || fragment instanceof JumperDetailsFragment) {
                    position = 2;
                }
                getSupportActionBar().setTitle(drawerOptions[position]);
                drawerList.setItemChecked(position, true);
            }
        });
    }

    private void competitionsMapToList() {
        if(seasonsAndCompetitions==null) {
            seasonsAndCompetitions = new ArrayList<>();
        }
        else {
            seasonsAndCompetitions.clear();
        }
        for(Season season : seasonToCompetitions.keySet()) {
            seasonsAndCompetitions.add(season);
            seasonsAndCompetitions.addAll(seasonToCompetitions.get(season));
        }
    }
}
