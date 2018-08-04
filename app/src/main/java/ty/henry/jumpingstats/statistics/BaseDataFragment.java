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

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.jumpers.Jumper;

public abstract class BaseDataFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected abstract String getJumpersPrefKey();

    protected abstract String getSeasonPrefKey(Season season);

    protected abstract void initPreferenceScreen(String rootKey);

    private ArrayList<Jumper> jumpers;
    private TreeMap<Season, TreeSet<Competition>> seasonToCompetitions;

    public void setJumpersAndCompetitions(ArrayList<Jumper> jumpers,
                                          TreeMap<Season, TreeSet<Competition>> seasonToCompetitions) {
        this.jumpers = jumpers;
        this.seasonToCompetitions = seasonToCompetitions;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        initPreferenceScreen(rootKey);

        Context activityContext = getActivity();
        TypedValue themeTypedValue = new TypedValue();
        activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);

        PreferenceCategory jumpersCategory = new PreferenceCategory(contextThemeWrapper);
        jumpersCategory.setTitle(R.string.jumpers_options_title);
        MultiSelectListPreference jumpersPreference = new MultiSelectListPreference(contextThemeWrapper);
        jumpersPreference.setKey(getJumpersPrefKey());
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
            competitionsPreference.setKey(getSeasonPrefKey(season));
            competitionsPreference.setTitle(season.toString());
            competitionsPreference.setDialogTitle(R.string.competition_options_title);

            CharSequence[] compNames = seasonToCompetitions.get(season).stream()
                    .map(comp -> String.format("%s K-%.0f (%s)", comp.getCity(), comp.getPointK(), comp.getText()[1]))
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

    protected void updatePrefSummary(Preference pref) {
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
