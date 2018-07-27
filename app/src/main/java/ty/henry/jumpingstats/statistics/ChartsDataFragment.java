package ty.henry.jumpingstats.statistics;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public class ChartsDataFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String X_AXIS_PREF_KEY = "pref_x_axis";
    public static final String Y_AXIS_PREF_KEY = "pref_y_axis";
    public static final String JUMPERS_PREF_KEY = "pref_jumpers";

    public static String SEASON_PREF_KEY(Season season) {
        return "pref_season_" + season.toString().replace(' ', '_');
    }

    private ArrayList<Jumper> jumpers;
    private TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;

    public ChartsDataFragment() {

    }

    public void setJumpersAndCompetitions(ArrayList<Jumper> jumpers,
                                          TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        this.jumpers = jumpers;
        this.seasonToCompetitions = seasonToCompetitions;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.charts_preferences, rootKey);

        Context activityContext = getActivity();
        TypedValue themeTypedValue = new TypedValue();
        activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);

        PreferenceCategory jumpersCategory = new PreferenceCategory(contextThemeWrapper);
        jumpersCategory.setTitle(R.string.jumpers_options_title);
        MultiSelectListPreference jumpersPreference = new MultiSelectListPreference(contextThemeWrapper);
        jumpersPreference.setKey(JUMPERS_PREF_KEY);
        jumpersPreference.setTitle(R.string.jumpers_options_title);
        jumpersPreference.setDialogTitle(R.string.jumpers_options_title);

        CharSequence[] jumpersName = jumpers.stream().map(j -> j.getText()[0]).toArray(CharSequence[]::new);
        CharSequence[] jumpersId = jumpers.stream().map(j -> j.getId()+"").toArray(CharSequence[]::new);
        jumpersPreference.setEntries(jumpersName);
        jumpersPreference.setEntryValues(jumpersId);

        getPreferenceScreen().addPreference(jumpersCategory);
        jumpersCategory.addPreference(jumpersPreference);

        PreferenceCategory competitionsCategory = new PreferenceCategory(contextThemeWrapper);
        competitionsCategory.setTitle(R.string.competition_options_title);
        getPreferenceScreen().addPreference(competitionsCategory);

        for(Season season : seasonToCompetitions.keySet()) {
            MultiSelectListPreference competitionsPreference = new MultiSelectListPreference(contextThemeWrapper);
            competitionsPreference.setKey(SEASON_PREF_KEY(season));
            competitionsPreference.setTitle(season.toString());
            competitionsPreference.setDialogTitle(R.string.competition_options_title);

            CharSequence[] compNames = seasonToCompetitions.get(season).stream()
                    .map(comp -> String.format("%s (%s)", comp.getCity(), comp.getText()[1]))
                    .toArray(CharSequence[]::new);
            CharSequence[] compIds = seasonToCompetitions.get(season).stream()
                    .map(comp -> comp.getId()+"")
                    .toArray(CharSequence[]::new);
            competitionsPreference.setEntries(compNames);
            competitionsPreference.setEntryValues(compIds);

            competitionsCategory.addPreference(competitionsPreference);

            initSummary(getPreferenceScreen());
        }
    }

    private void initSummary(Preference pref) {
        if(pref instanceof PreferenceGroup) {
            PreferenceGroup preferenceGroup = (PreferenceGroup) pref;
            for(int i=0; i<preferenceGroup.getPreferenceCount(); i++) {
                initSummary(preferenceGroup.getPreference(i));
            }
        }
        else {
            updatePrefSummary(pref);
        }
    }

    private void updatePrefSummary(Preference pref) {
        if(pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            pref.setSummary(listPreference.getEntry());
        }
        else if(pref instanceof MultiSelectListPreference) {
            int selected = ((MultiSelectListPreference) pref).getValues().size();
            String summary = getString(R.string.multi_select_pref_summary, selected);
            pref.setSummary(summary);
        }
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

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

}
