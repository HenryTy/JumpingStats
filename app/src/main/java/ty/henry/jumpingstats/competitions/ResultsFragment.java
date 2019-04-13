package ty.henry.jumpingstats.competitions;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.ResultsAdapter;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;


public class ResultsFragment extends Fragment {

    private CompetitionDetailsFragment parent;
    private RecyclerView recyclerView;
    private ResultsAdapter resultsAdapter;

    public ResultsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_results, container, false);
        recyclerView = fragmentView.findViewById(R.id.resultsRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        if(resultsAdapter != null) {
            recyclerView.setAdapter(resultsAdapter);
        }
        return fragmentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!(getParentFragment() instanceof CompetitionDetailsFragment)) {
            throw new IllegalStateException("Parent fragment for ResultsFragment must be CompetitionDetailsFragment");
        }
        parent = (CompetitionDetailsFragment) getParentFragment();
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getJumpers().observe(this, this::updateRecyclerView);
    }

    private void updateRecyclerView(List<Jumper> jumpers) {
        DBHelper dbHelper = new DBHelper(getActivity());
        dbHelper.fillJumpersWithResults(jumpers,
                Collections.singletonList(parent.competition));
        List<Jumper> sortedJumpers = new ArrayList<>(jumpers);
        sortedJumpers.sort(this::compareJumpers);
        resultsAdapter = new ResultsAdapter(parent.competition, sortedJumpers, getActivity());
        resultsAdapter.setListener(position -> {
            AddEditResultsFragment fragment = AddEditResultsFragment
                    .newInstance(parent.competition.getId(), sortedJumpers.get(position).getId());
            parent.listener.openFragment(fragment, true);
        });
        recyclerView.setAdapter(resultsAdapter);
    }

    private int compareJumpers(Jumper jumper1, Jumper jumper2) {
        float points1, points2;
        try {
            points1 = jumper1.getResult(parent.competition).points();
        } catch (NoResultForJumperException ex) {
            points1 = 0;
        }
        try {
            points2 = jumper2.getResult(parent.competition).points();
        } catch (NoResultForJumperException ex) {
            points2 = 0;
        }
        if(points2 > points1) {
            return 1;
        }
        else if(points2 == points1) {
            return 0;
        }
        else {
            return -1;
        }
    }

}
