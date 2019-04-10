package ty.henry.jumpingstats.statistics;

import android.content.SharedPreferences;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Season;

public class TableDataFragment extends BaseDataFragment {

    public static final String JUMPERS_PREF_KEY = "pref_jumpers_table";

    public static String SEASON_PREF_KEY(Season season) {
        return "pref_season_" + season.toString().replace(' ', '_') + "_table";
    }

    public static final String GROUP_BY_K_PREF_KEY = "pref_group_k";

    protected String getJumpersPrefKey() {
        return JUMPERS_PREF_KEY;
    }

    protected String getSeasonPrefKey(Season season) {
        return SEASON_PREF_KEY(season);
    }

    protected void initPreferenceScreen(String rootKey) {
        setPreferencesFromResource(R.xml.table_preferences, rootKey);
    }

    public TableDataFragment() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }
}
