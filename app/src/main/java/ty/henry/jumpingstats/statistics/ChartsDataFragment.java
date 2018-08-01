package ty.henry.jumpingstats.statistics;


import android.content.SharedPreferences;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.ListPreference;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Season;

public class ChartsDataFragment extends BaseDataFragment {

    public static final String X_AXIS_PREF_KEY = "pref_x_axis";
    public static final String Y_AXIS_PREF_KEY = "pref_y_axis";
    public static final String JUMPERS_PREF_KEY = "pref_jumpers";

    public static String SEASON_PREF_KEY(Season season) {
        return "pref_season_" + season.toString().replace(' ', '_');
    }

    protected String getJumpersPrefKey() {
        return JUMPERS_PREF_KEY;
    }

    protected String getSeasonPrefKey(Season season) {
        return SEASON_PREF_KEY(season);
    }

    protected void initPreferenceScreen(String rootKey) {
        setPreferencesFromResource(R.xml.charts_preferences, rootKey);
    }

    public ChartsDataFragment() {

    }

    private void checkIfTooManyJumpersSelected() {
        ListPreference xPreference = (ListPreference) findPreference(X_AXIS_PREF_KEY);
        if(xPreference.getEntry().equals(getString(R.string.x_axis_default))) {
            MultiSelectListPreference jumpersPreference = (MultiSelectListPreference) findPreference(JUMPERS_PREF_KEY);
            Set<String> selectedJumpers = jumpersPreference.getValues();
            if (selectedJumpers.size() > StatsFragment.MAX_JUMPERS) {
                Set<String> lessJumpers = new HashSet<>();
                int i = 0;
                for (String s : selectedJumpers) {
                    if (i == StatsFragment.MAX_JUMPERS) break;
                    lessJumpers.add(s);
                    i++;
                }
                jumpersPreference.setValues(lessJumpers);
                Toast.makeText(getActivity(), getString(R.string.too_many_jumpers_selected,
                        StatsFragment.MAX_JUMPERS), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(X_AXIS_PREF_KEY) || key.equals(JUMPERS_PREF_KEY)) {
            checkIfTooManyJumpersSelected();
        }
        updatePrefSummary(findPreference(key));
    }

}
