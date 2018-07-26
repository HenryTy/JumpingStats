package ty.henry.jumpingstats.statistics;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import ty.henry.jumpingstats.R;

public class ChartsDataFragment extends PreferenceFragmentCompat {

    public ChartsDataFragment() {

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.charts_preferences, rootKey);
    }

}
