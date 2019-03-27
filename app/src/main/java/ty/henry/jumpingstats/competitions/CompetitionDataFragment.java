package ty.henry.jumpingstats.competitions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ty.henry.jumpingstats.R;


public class CompetitionDataFragment extends Fragment {

    CompetitionDetailsFragment parent;

    public CompetitionDataFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_competition_data, container, false);
        if(!(getParentFragment() instanceof CompetitionDetailsFragment)) {
            throw new IllegalStateException("Parent fragment for CompetitionDataFragment must be CompetitionDetailsFragment");
        }
        parent = (CompetitionDetailsFragment) getParentFragment();

        TextView countryTextView = fragmentView.findViewById(R.id.countryTextView);
        TextView hillSizeTextView = fragmentView.findViewById(R.id.hillSizeTextView);
        TextView headWindTextView = fragmentView.findViewById(R.id.headWindTextView);
        TextView tailWindTextView = fragmentView.findViewById(R.id.tailWindTextView);

        String countryText = getString(R.string.country) + ": " + getString(parent.competition.getCountry().getNameId());
        String hillSizeText = getString(R.string.hillSize) + ": " + parent.competition.getHillSize();
        String headWindText = getString(R.string.headWindPoints) + ": " + parent.competition.getHeadWindPoints();
        String tailWindText = getString(R.string.tailWindPoints) + ": " + parent.competition.getTailWindPoints();

        countryTextView.setText(countryText);
        hillSizeTextView.setText(hillSizeText);
        headWindTextView.setText(headWindText);
        tailWindTextView.setText(tailWindText);
        return fragmentView;
    }

}
