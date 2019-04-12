package ty.henry.jumpingstats;

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

import ty.henry.jumpingstats.competitions.AddEditCompetitionFragment;
import ty.henry.jumpingstats.competitions.AddEditResultsFragment;
import ty.henry.jumpingstats.competitions.CompetitionDetailsFragment;
import ty.henry.jumpingstats.competitions.CompetitionsFragment;
import ty.henry.jumpingstats.jumpers.AddEditJumperFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareDrawer();

        StatsFragment statsFragment = new StatsFragment();
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

    public void openFragment(Fragment fragment, boolean backStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame, fragment, VISIBLE_FRAGMENT);
        if(backStack)
            ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
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
                        break;
                    case 2:
                        fragment = new JumpersFragment();
                        break;
                    default:
                        fragment = new StatsFragment();
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
}
