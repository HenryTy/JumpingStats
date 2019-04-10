package ty.henry.jumpingstats.statistics;


import android.content.SharedPreferences;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Season;

public class ChartsDataFragment extends BaseDataFragment {

    public static final String X_AXIS_PREF_KEY = "pref_X_axis";
    public static final String Y_AXIS_PREF_KEY = "pref_Y_axis";
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
        if(xPreference.getValue().equals("0")) {
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

    @Override
    protected void addPreferences() {
        PreferenceCategory dataCategory = new PreferenceCategory(getContextThemeWrapper());
        dataCategory.setTitle(R.string.axes_options_title);
        getPreferenceScreen().addPreference(dataCategory);

        addXOptionPreference(dataCategory);

        addYOptionPreference(dataCategory);

        super.addPreferences();
    }

    private void addXOptionPreference(PreferenceCategory category) {
        ListPreference xPreference = new ListPreference(getContextThemeWrapper());
        xPreference.setKey(X_AXIS_PREF_KEY);
        xPreference.setTitle(R.string.x_axis_options_title);
        xPreference.setDialogTitle(R.string.x_axis_options_title);

        CharSequence[] xTitles = new CharSequence[XOption.values().length];
        CharSequence[] xNumbers = new CharSequence[XOption.values().length];
        for(int i = 0; i < xTitles.length; i++) {
            xTitles[i] = getString(XOption.values()[i].getTitleId());
            xNumbers[i] = i+"";
        }

        xPreference.setEntries(xTitles);
        xPreference.setEntryValues(xNumbers);
        xPreference.setDefaultValue("0");

        category.addPreference(xPreference);
    }

    private void addYOptionPreference(PreferenceCategory category) {
        ListPreference yPreference = new ListPreference(getContextThemeWrapper());
        yPreference.setKey(Y_AXIS_PREF_KEY);
        yPreference.setTitle(R.string.y_axis_options_title);
        yPreference.setDialogTitle(R.string.y_axis_options_title);

        CharSequence[] yTitles = new CharSequence[YOption.values().length];
        CharSequence[] yNumbers = new CharSequence[YOption.values().length];
        for(int i = 0; i < yTitles.length; i++) {
            yTitles[i] = getString(YOption.values()[i].getTitleId());
            yNumbers[i] = i+"";
        }

        yPreference.setEntries(yTitles);
        yPreference.setEntryValues(yNumbers);
        yPreference.setDefaultValue("0");

        category.addPreference(yPreference);
    }
}
