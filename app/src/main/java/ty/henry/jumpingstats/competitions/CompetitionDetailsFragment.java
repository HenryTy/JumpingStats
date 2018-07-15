package ty.henry.jumpingstats.competitions;


import android.os.AsyncTask;
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
import android.widget.TextView;

import ty.henry.jumpingstats.ConfirmFragment;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.AddEditJumperFragment;
import ty.henry.jumpingstats.jumpers.JumperDetailsFragment;


public class CompetitionDetailsFragment extends Fragment {

    Competition competition;
    CompetitionsFragment.CompetitionsFragmentListener listener;

    public CompetitionDetailsFragment() {

    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public void setListener(CompetitionsFragment.CompetitionsFragmentListener listener) {
        this.listener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_competition_details, container, false);
        TextView compNameTextView = fragmentView.findViewById(R.id.compNameTextView);
        TextView compDateTextView = fragmentView.findViewById(R.id.compDateTextView);
        String[] compTitle = competition.getText();
        compNameTextView.setText(compTitle[0]);
        compDateTextView.setText(compTitle[1]);

        ViewPager viewPager = fragmentView.findViewById(R.id.compViewPager);
        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_delete_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                ConfirmFragment confirmFragment = new ConfirmFragment();
                confirmFragment.setMessage(getString(R.string.delete_competition_message));
                confirmFragment.setListener(() -> {
                    DeleteTask deleteTask = new DeleteTask();
                    deleteTask.execute(competition);
                });
                confirmFragment.show(getChildFragmentManager(), "dialog");
                return true;
            case R.id.edit:
                AddEditCompetitionFragment fragment = new AddEditCompetitionFragment();
                fragment.setListener(listener);
                fragment.editCompetition(competition, this);
                listener.openFragment(fragment, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DeleteTask extends AsyncTask<Competition, Void, Competition> {

        @Override
        protected Competition doInBackground(Competition... comp) {
            DBHelper dbHelper = new DBHelper(getActivity());
            dbHelper.deleteCompetition(comp[0].getId());
            return comp[0];
        }

        @Override
        protected void onPostExecute(Competition comp) {
            listener.onCompetitionDeleted(comp);
            if(getActivity()!=null) {
                getActivity().onBackPressed();
            }
        }
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return new ResultsFragment();
            }
            else {
                return new CompetitionDataFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
