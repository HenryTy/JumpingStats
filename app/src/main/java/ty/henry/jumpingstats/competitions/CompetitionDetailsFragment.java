package ty.henry.jumpingstats.competitions;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;


public class CompetitionDetailsFragment extends Fragment {

    public static final String ID_ARG = "id";

    Competition competition;
    CompetitionsFragment.CompetitionsFragmentListener listener;

    public CompetitionDetailsFragment() {

    }

    public static CompetitionDetailsFragment newInstance(int compId) {
        Bundle args = new Bundle();
        args.putInt(ID_ARG, compId);

        CompetitionDetailsFragment fragment = new CompetitionDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_competition_details, container, false);

        int compId = getArguments() != null ? getArguments().getInt(ID_ARG, -1) : -1;

        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        competition = mainViewModel.getCompetitionById(compId);

        if(competition != null) {
            TextView compNameTextView = fragmentView.findViewById(R.id.compNameTextView);
            TextView compDateTextView = fragmentView.findViewById(R.id.compDateTextView);
            String[] compTitle = competition.getText(getActivity());
            compNameTextView.setText(compTitle[0]);
            compDateTextView.setText(compTitle[1]);

            ViewPager viewPager = fragmentView.findViewById(R.id.compViewPager);
            viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        }
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CompetitionsFragment.CompetitionsFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement CompetitionsFragmentListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_delete_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if(competition != null) {
                    ConfirmFragment confirmFragment = new ConfirmFragment();
                    confirmFragment.setMessage(getString(R.string.delete_competition_message));
                    confirmFragment.setListener(() -> {
                        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
                        mainViewModel.deleteCompetition(competition.getId());
                        getActivity().onBackPressed();
                    });
                    confirmFragment.show(getChildFragmentManager(), "dialog");
                }
                return true;
            case R.id.edit:
                if(competition != null) {
                    AddEditCompetitionFragment fragment = AddEditCompetitionFragment.newInstance(competition.getId());
                    listener.openFragment(fragment, true);
                }
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
