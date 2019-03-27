package ty.henry.jumpingstats;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.competitions.SeriesResult;
import ty.henry.jumpingstats.jumpers.Jumper;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<List<Jumper>> jumpers;
    private MutableLiveData<TreeMap<Season, TreeSet<Competition>>> seasonToCompetitions;

    public MainViewModel(Application application) {
        super(application);

        jumpers = new MutableLiveData<>();
        seasonToCompetitions = new MutableLiveData<>();

        loadJumpers();
        loadCompetitions();
    }

    public LiveData<List<Jumper>> getJumpers() {
        return jumpers;
    }

    public LiveData<TreeMap<Season, TreeSet<Competition>>> getSeasonToCompetitions() {
        return seasonToCompetitions;
    }

    public Jumper getJumperById(int id) {
        List<Jumper> jumperList = jumpers.getValue();
        if(jumperList != null) {
            for(Jumper j : jumperList) {
                if(j.getId() == id) {
                    return j;
                }
            }
        }
        return null;
    }

    public Competition getCompetitionById(int id) {
        TreeMap<Season, TreeSet<Competition>> seasonCompsMap = seasonToCompetitions.getValue();
        if(seasonCompsMap != null) {
            for(Season s : seasonCompsMap.keySet()) {
                for(Competition c : seasonCompsMap.get(s)) {
                    if(c.getId() == id) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    public void addJumper(Jumper jumper) {
        UpdateTask<Jumper> updateTask = new UpdateTask<>(DBHelper::add, jumper, R.string.saved,
                true, false);
        updateTask.execute();
    }

    public void updateJumper(Jumper jumper) {
        UpdateTask<Jumper> updateTask = new UpdateTask<>(DBHelper::update, jumper, R.string.saved,
                true, false);
        updateTask.execute();
    }

    public void deleteJumper(int jumpersId) {
        UpdateTask<Integer> updateTask = new UpdateTask<>(DBHelper::deleteJumper, jumpersId, R.string.deleted,
                true, false);
        updateTask.execute();
    }

    public void deleteJumpers(Set<Jumper> jumpers) {
        UpdateTask<Set<Jumper>> updateTask = new UpdateTask<>(DBHelper::deleteJumpers, jumpers, R.string.deleted,
                true, false);
        updateTask.execute();
    }

    public void addCompetition(Competition competition) {
        UpdateTask<Competition> updateTask = new UpdateTask<>(DBHelper::add, competition, R.string.saved,
                true, true);
        updateTask.execute();
    }

    public void updateCompetition(Competition competition) {
        UpdateTask<Competition> updateTask = new UpdateTask<>(DBHelper::update, competition, R.string.saved,
                true, true);
        updateTask.execute();
    }

    public void deleteCompetition(int compId) {
        UpdateTask<Integer> updateTask = new UpdateTask<>(DBHelper::deleteCompetition, compId, R.string.deleted,
                true, true);
        updateTask.execute();
    }

    public void deleteCompetitions(Set<Competition> competitions) {
        UpdateTask<Set<Competition>> updateTask = new UpdateTask<>(DBHelper::deleteCompetitions, competitions, R.string.deleted,
                true, true);
        updateTask.execute();
    }

    public void addResult(SeriesResult result) {
        UpdateTask<SeriesResult> updateTask = new UpdateTask<>(DBHelper::add, result, R.string.saved,
                false, false);
        updateTask.execute();
    }

    public void updateResult(SeriesResult result) {
        UpdateTask<SeriesResult> updateTask = new UpdateTask<>(DBHelper::update, result, R.string.saved,
                false, false);
        updateTask.execute();
    }

    public void deleteResult(SeriesResult result) {
        UpdateTask<SeriesResult> updateTask = new UpdateTask<>(DBHelper::deleteResult, result, R.string.deleted,
                false, false);
        updateTask.execute();
    }

    private void loadJumpers() {
        new AsyncTask<Void, Void, List<Jumper>>() {
            @Override
            protected List<Jumper> doInBackground(Void... params) {
                DBHelper dbHelper = new DBHelper(getApplication());
                return dbHelper.getAllJumpers();
            }

            @Override
            protected void onPostExecute(List<Jumper> jumpers) {
                MainViewModel.this.jumpers.setValue(jumpers);
            }
        }.execute();
    }

    private void loadCompetitions() {
        new AsyncTask<Void, Void, TreeMap<Season, TreeSet<Competition>>>() {
            @Override
            protected TreeMap<Season, TreeSet<Competition>> doInBackground(Void... params) {
                DBHelper dbHelper = new DBHelper(getApplication());
                return dbHelper.getSeasonToCompetitionsMap();
            }

            @Override
            protected void onPostExecute(TreeMap<Season, TreeSet<Competition>> seasonTreeSetTreeMap) {
                MainViewModel.this.seasonToCompetitions.setValue(seasonTreeSetTreeMap);
            }
        }.execute();
    }

    private class UpdateTask<T> extends AsyncTask<Void, Void, Void> {

        private BiConsumer<DBHelper, T> backgroundFunction;
        private T dataToUpdate;
        private Integer messageId;
        private boolean refreshJumpers;
        private boolean refreshCompetitions;

        public UpdateTask(BiConsumer<DBHelper, T> backgroundFunction, T dataToUpdate, Integer messageId,
                          boolean refreshJumpers, boolean refreshCompetitions) {
            this.backgroundFunction = backgroundFunction;
            this.dataToUpdate = dataToUpdate;
            this.messageId = messageId;
            this.refreshJumpers = refreshJumpers;
            this.refreshCompetitions = refreshCompetitions;
        }

        @Override
        protected Void doInBackground(Void... params) {
            DBHelper dbHelper = new DBHelper(getApplication());
            backgroundFunction.accept(dbHelper, dataToUpdate);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(messageId != null) {
                Toast.makeText(getApplication(), messageId, Toast.LENGTH_SHORT).show();
            }
            if(refreshJumpers) loadJumpers();
            if(refreshCompetitions) loadCompetitions();
        }
    }
}
