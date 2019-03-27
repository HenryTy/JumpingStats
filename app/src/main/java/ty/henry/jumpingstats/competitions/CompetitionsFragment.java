package ty.henry.jumpingstats.competitions;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.TextImageAdapter;
import ty.henry.jumpingstats.TextImageAdapter.TextImage;


public class CompetitionsFragment extends Fragment {

    private CompetitionsFragmentListener listener;
    private TextImageAdapter<TextImage> textImageAdapter;
    private SimpleAdapter spinnerAdapter;
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private Spinner seasonsSpinner;
    private List<TextImage> seasonsAndCompetitions;
    private List<Season> seasonsList;

    public interface CompetitionsFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
    }

    public CompetitionsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_competitions, container, false);
        recyclerView = fragmentView.findViewById(R.id.competitionsRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        addButton = fragmentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEditCompetitionFragment fragment = new AddEditCompetitionFragment();
                listener.openFragment(fragment, true);
            }
        });

        seasonsSpinner = fragmentView.findViewById(R.id.seasonsSpinner);

        ImageButton seasonButton = fragmentView.findViewById(R.id.seasonButton);
        seasonButton.setOnClickListener(view -> {
            Season selectedSeason = seasonsList.get(seasonsSpinner.getSelectedItemPosition());
            int i = 0;
            while(!selectedSeason.equals(seasonsAndCompetitions.get(i))) i++;
            layoutManager.scrollToPositionWithOffset(i, 0);
        });
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getSeasonToCompetitions().observe(this, this::updateUI);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CompetitionsFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement CompetitionsFragmentListener");
        }
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
                                MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
                                mainViewModel.deleteCompetitions(toDelete);
                            }
                            mode.finish();
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            textImageAdapter.setMultiselection(false);
                            addButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI(TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        createTextImageAdapter(seasonToCompetitions);
        recyclerView.setAdapter(textImageAdapter);
        createSeasonsSpinnerAdapter(seasonToCompetitions);
        seasonsSpinner.setAdapter(spinnerAdapter);
    }

    private List<TextImage> competitionsMapToList(TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        ArrayList<TextImage> seasonsAndCompetitions = new ArrayList<>();
        for(Season season : seasonToCompetitions.keySet()) {
            seasonsAndCompetitions.add(season);
            seasonsAndCompetitions.addAll(seasonToCompetitions.get(season));
        }
        return seasonsAndCompetitions;
    }

    private void createTextImageAdapter(TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        seasonsAndCompetitions = competitionsMapToList(seasonToCompetitions);
        textImageAdapter = new TextImageAdapter<>(seasonsAndCompetitions, getActivity());
        textImageAdapter.setListener(position -> {
            Competition selectedComp = (Competition) seasonsAndCompetitions.get(position);
            CompetitionDetailsFragment detailsFragment = CompetitionDetailsFragment.newInstance(selectedComp.getId());
            listener.openFragment(detailsFragment, true);
        });
    }

    private void createSeasonsSpinnerAdapter(TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        String name = "name";
        ArrayList<HashMap<String, Object>> spinnerData = new ArrayList<>();
        seasonsList = new ArrayList<>(seasonToCompetitions.keySet());
        for(Season s : seasonsList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(name, s.getText(getActivity())[0]);
            spinnerData.add(map);
        }
        spinnerAdapter = new SimpleAdapter(getActivity(), spinnerData, android.R.layout.simple_list_item_1,
                new String[]{name}, new int[]{android.R.id.text1});
    }
}
