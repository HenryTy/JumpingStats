package ty.henry.jumpingstats.competitions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.ResultsAdapter;
import ty.henry.jumpingstats.jumpers.Jumper;


public class ResultsFragment extends Fragment {

    private CompetitionDetailsFragment parent;


    public ResultsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_results, container, false);
        if(!(getParentFragment() instanceof CompetitionDetailsFragment)) {
            throw new IllegalStateException("Parent fragment for ResultsFragment must be CompetitionDetailsFragment");
        }
        parent = (CompetitionDetailsFragment) getParentFragment();

        ArrayList<Jumper> jumpers = new ArrayList<>(parent.listener.getJumpersList());
        jumpers.sort(this::compareJumpers);

        RecyclerView recyclerView = fragmentView.findViewById(R.id.resultsRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        ResultsAdapter resultsAdapter = new ResultsAdapter(parent.competition, jumpers);
        resultsAdapter.setListener(position -> {
            AddEditResultsFragment fragment = new AddEditResultsFragment();
            fragment.setData(parent.competition, jumpers.get(position));
            parent.listener.openFragment(fragment, true);
        });
        recyclerView.setAdapter(resultsAdapter);
        return fragmentView;
    }

    private int compareJumpers(Jumper jumper1, Jumper jumper2) {
        float points1, points2;
        try {
            points1 = jumper1.getPointsFromComp(parent.competition);
        } catch (Exception ex) {
            points1 = 0;
        }
        try {
            points2 = jumper2.getPointsFromComp(parent.competition);
        } catch (Exception ex) {
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
