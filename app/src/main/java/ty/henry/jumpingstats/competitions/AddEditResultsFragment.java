package ty.henry.jumpingstats.competitions;


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

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.Jumper;


public class AddEditResultsFragment extends Fragment {

    Competition competition;
    Jumper jumper;


    public AddEditResultsFragment() {

    }

    public void setData(Competition competition, Jumper jumper) {
        this.competition = competition;
        this.jumper = jumper;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_add_edit_results, container, false);
        TextView compNameTextView = fragmentView.findViewById(R.id.compNameTextView);
        TextView compDateTextView = fragmentView.findViewById(R.id.compDateTextView);
        TextView jumpNameTextView = fragmentView.findViewById(R.id.jumpNameTextView);
        String[] compTitle = competition.getText();
        compNameTextView.setText(compTitle[0]);
        compDateTextView.setText(compTitle[1]);
        jumpNameTextView.setText(jumper.getText()[0]);

        Button[] seriesButtons = new Button[2];
        seriesButtons[0] = fragmentView.findViewById(R.id.button1Series);
        seriesButtons[1] = fragmentView.findViewById(R.id.button2Series);

        ViewPager viewPager = fragmentView.findViewById(R.id.resultsViewPager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        for(int i=0; i<2; i++) {
            int position = i;
            seriesButtons[i].setOnClickListener(view -> {
                viewPager.setCurrentItem(position);
            });
        }
        return fragmentView;
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            EditResultFragment fragment = new EditResultFragment();
            fragment.setSeries(position+1);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
