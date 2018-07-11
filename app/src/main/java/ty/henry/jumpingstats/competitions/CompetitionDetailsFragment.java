package ty.henry.jumpingstats.competitions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ty.henry.jumpingstats.R;


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
