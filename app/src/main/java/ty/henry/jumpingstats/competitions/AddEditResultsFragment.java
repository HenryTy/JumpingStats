package ty.henry.jumpingstats.competitions;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;

import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;


public class AddEditResultsFragment extends Fragment {

    public static final String JUMPER_ID_ARG = "j_id";
    public static final String COMP_ID_ARG = "c_id";

    Result result;
    private Jumper jumper;
    private Competition competition;

    public AddEditResultsFragment() {

    }

    public static AddEditResultsFragment newInstance(int compId, int jumperId) {
        Bundle args = new Bundle();
        args.putInt(COMP_ID_ARG, compId);
        args.putInt(JUMPER_ID_ARG, jumperId);

        AddEditResultsFragment fragment = new AddEditResultsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void updateResult(SeriesResult seriesResult) {
        if(result == null) {
            result = new Result(jumper, competition);
            jumper.setResult(competition, result);
        }
        result.setResultForSeries(seriesResult.getSeries(), seriesResult);
    }

    public void deleteResult(int series) {
        Result.checkSeriesArgument(series);
        if(result != null) {
            result.setResultForSeries(series, null);
            if(result.isEmpty()) {
                jumper.setResult(competition, null);
                result = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_add_edit_results, container, false);

        int jumperId = getArguments() != null ? getArguments().getInt(JUMPER_ID_ARG, -1) : -1;
        int compId = getArguments() != null ? getArguments().getInt(COMP_ID_ARG, -1) : -1;
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        jumper = mainViewModel.getJumperById(jumperId);
        competition = mainViewModel.getCompetitionById(compId);

        if(jumper != null && competition != null) {
            try {
                result = jumper.getResult(competition);
            } catch (NoResultForJumperException ex) {
                DBHelper dbHelper = new DBHelper(getActivity());
                dbHelper.fillJumpersWithResults(Collections.singletonList(jumper),
                        Collections.singletonList(competition));
                try {
                    result = jumper.getResult(competition);
                } catch (NoResultForJumperException ex2) {

                }
            }

            TextView compNameTextView = fragmentView.findViewById(R.id.compNameTextView);
            TextView compDateTextView = fragmentView.findViewById(R.id.compDateTextView);
            TextView jumpNameTextView = fragmentView.findViewById(R.id.jumpNameTextView);
            String[] compTitle = competition.getText(getActivity());
            compNameTextView.setText(compTitle[0]);
            compDateTextView.setText(compTitle[1]);
            jumpNameTextView.setText(jumper.getText(getActivity())[0]);

            Button[] seriesButtons = new Button[2];
            seriesButtons[0] = fragmentView.findViewById(R.id.button1Series);
            seriesButtons[1] = fragmentView.findViewById(R.id.button2Series);

            ViewPager viewPager = fragmentView.findViewById(R.id.resultsViewPager);
            MyPagerAdapter pagerAdapter = new MyPagerAdapter(getChildFragmentManager());
            viewPager.setAdapter(pagerAdapter);

            for (int i = 0; i < 2; i++) {
                int position = i;
                seriesButtons[i].setOnClickListener(view -> {
                    viewPager.setCurrentItem(position);
                });
            }
        }
        return fragmentView;
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return EditResultFragment.newInstance(position+1);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
