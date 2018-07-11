package ty.henry.jumpingstats.competitions;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.TextImageAdapter;
import ty.henry.jumpingstats.TextImageAdapter.TextImage;


public class CompetitionsFragment extends Fragment {

    private CompetitionsFragmentListener listener;
    private TextImageAdapter<TextImage> textImageAdapter;
    private ArrayAdapter<Season> spinnerAdapter;
    private FloatingActionButton addButton;


    public interface CompetitionsFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
        ArrayList<TextImageAdapter.TextImage> getCompetitionsList();
        TreeMap<Season, TreeSet<Competition>> getSeasonToCompetitionsMap();
        void onCompetitionAdded(Competition competition);
        void onCompetitionDeleted(Competition competition);
        void onCompetitionsDeleted(Set<Competition> competitions);
        void onCompetitionUpdated(Competition competition);
    }

    public void setListener(CompetitionsFragmentListener listener) {
        this.listener = listener;
    }

    public CompetitionsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_competitions, container, false);
        RecyclerView recyclerView = fragmentView.findViewById(R.id.competitionsRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        textImageAdapter = new TextImageAdapter<>(listener.getCompetitionsList());
        recyclerView.setAdapter(textImageAdapter);

        addButton = fragmentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEditCompetitionFragment fragment = new AddEditCompetitionFragment();
                fragment.setListener(listener);
                listener.openFragment(fragment, true);
            }
        });

        Spinner seasonsSpinner = fragmentView.findViewById(R.id.seasonsSpinner);
        spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        spinnerAdapter.addAll(listener.getSeasonToCompetitionsMap().keySet());
        seasonsSpinner.setAdapter(spinnerAdapter);

        ImageButton seasonButton = fragmentView.findViewById(R.id.seasonButton);
        seasonButton.setOnClickListener(view -> {
            Season selectedSeason = (Season) seasonsSpinner.getSelectedItem();
            ArrayList<TextImage> competitionsAndSeasons = listener.getCompetitionsList();
            int i = 0;
            while(!selectedSeason.equals(competitionsAndSeasons.get(i))) i++;
            layoutManager.scrollToPositionWithOffset(i, 0);
        });
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
        menu.findItem(R.id.edit).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if(getActivity() != null) {
                    ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            textImageAdapter.setMultiselection(true);
                            menu.add(getString(R.string.delete));
                            addButton.setVisibility(View.GONE);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            Set<Competition> toDelete = textImageAdapter.getSelectedItems()
                                    .stream().map(t -> ((Competition) t)).collect(Collectors.toSet());
                            if(toDelete.size() > 0) {
                                DeleteTask deleteTask = new DeleteTask(toDelete);
                                deleteTask.execute();
                                listener.onCompetitionsDeleted(toDelete);
                            }
                            mode.finish();
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            textImageAdapter.setMultiselection(false);
                            textImageAdapter.notifyDataSetChanged();
                            spinnerAdapter.clear();
                            spinnerAdapter.addAll(listener.getSeasonToCompetitionsMap().keySet());
                            spinnerAdapter.notifyDataSetChanged();
                            addButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DeleteTask extends AsyncTask<Void, Void, Boolean> {

        private Set<Competition> toDelete;

        DeleteTask(Set<Competition> competitions) {
            toDelete = competitions;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DBHelper dbHelper = new DBHelper(getActivity());
            dbHelper.deleteCompetitions(toDelete);
            return true;
        }

    }

}
