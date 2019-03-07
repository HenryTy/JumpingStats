package ty.henry.jumpingstats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.competitions.Season;
import ty.henry.jumpingstats.competitions.SeriesResult;
import ty.henry.jumpingstats.jumpers.Jumper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ski_data";
    private static final int DB_VERSION = 1;

    public interface Identifiable {
        int getId();
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(JumpersColumns.createTable);
        db.execSQL(CompetitionColumns.createTable);
        db.execSQL(ResultsColumns.createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int add(Jumper jumper) {
        ContentValues values = JumpersColumns.values(jumper);
        return insert(JumpersColumns.TABLE_NAME, values);
    }

    public int add(Competition competition) {
        ContentValues values = CompetitionColumns.values(competition);
        return insert(CompetitionColumns.TABLE_NAME, values);
    }

    public void add(SeriesResult result) {
        ContentValues values = ResultsColumns.values(result);
        insert(ResultsColumns.TABLE_NAME, values);
    }

    public void update(Jumper jumper) {
        if(jumper.getId() < 0) {
            throw new IllegalArgumentException("Jumper must have non-negative id set");
        }
        ContentValues values = JumpersColumns.values(jumper);
        update(JumpersColumns.TABLE_NAME, values, jumper.getId());
    }

    public void update(Competition competition) {
        if(competition.getId() < 0) {
            throw new IllegalArgumentException("Competition must have non-negative id set");
        }
        ContentValues values = CompetitionColumns.values(competition);
        update(CompetitionColumns.TABLE_NAME, values, competition.getId());
    }

    public void update(SeriesResult result) {
        int jumperId = result.getJumper().getId();
        int competitionId = result.getCompetition().getId();
        int series = result.getSeries();
        ContentValues values = ResultsColumns.values(result);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(ResultsColumns.TABLE_NAME, values,
                ResultsColumns._JUMPER + "=? AND " + ResultsColumns._COMPETITION + "=? AND " + ResultsColumns._SERIES + "=?",
                new String[]{Integer.toString(jumperId), Integer.toString(competitionId), Integer.toString(series)});
        db.close();
    }

    public void deleteJumper(int id) {
        delete(JumpersColumns.TABLE_NAME, id);
    }

    public void deleteJumpers(Set<Jumper> jumpers) {
        deleteAll(JumpersColumns.TABLE_NAME, jumpers);
    }

    public void deleteCompetition(int id) {
        delete(CompetitionColumns.TABLE_NAME, id);
    }

    public void deleteCompetitions(Set<Competition> competitions) {
        deleteAll(CompetitionColumns.TABLE_NAME, competitions);
    }

    public void deleteResult(SeriesResult result) {
        int jumperId = result.getJumper().getId();
        int competitionId = result.getCompetition().getId();
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ResultsColumns.TABLE_NAME,
                ResultsColumns._JUMPER + "=? AND " + ResultsColumns._COMPETITION + "=? AND " + ResultsColumns._SERIES + "=?",
                new String[]{Integer.toString(jumperId), Integer.toString(competitionId), Integer.toString(result.getSeries())});
        db.close();
    }

    private int insert(String table, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = (int) db.insert(table, null, values);
        db.close();
        return id;
    }

    private void update(String table, ContentValues values, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(table, values, BaseColumns._ID + "=?",
                new String[]{Integer.toString(id)});
        db.close();
    }

    private void delete(String table, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, BaseColumns._ID + "=?", new String[]{Integer.toString(id)});
        String resColumn;
        if(table.equals(JumpersColumns.TABLE_NAME)) {
            resColumn = ResultsColumns._JUMPER;
        } else {
            resColumn = ResultsColumns._COMPETITION;
        }
        db.delete(ResultsColumns.TABLE_NAME, resColumn + "=?", new String[]{id+""});
        db.close();
    }

    private void deleteAll(String table, Set<? extends Identifiable> toDelete) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder where = new StringBuilder(BaseColumns._ID + " IN (");
        for(Identifiable item : toDelete) {
            where.append(item.getId()).append(", ");
        }
        where.replace(where.length()-2, where.length(), ")");
        db.delete(table, where.toString(), null);

        String resColumn;
        if(table.equals(JumpersColumns.TABLE_NAME)) {
            resColumn = ResultsColumns._JUMPER;
        } else {
            resColumn = ResultsColumns._COMPETITION;
        }
        where.replace(0, BaseColumns._ID.length(), resColumn);
        db.delete(ResultsColumns.TABLE_NAME, where.toString(), null);
        db.close();
    }

    public ArrayList<Jumper> getAllJumpers() {
        ArrayList<Jumper> jumpers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(JumpersColumns.TABLE_NAME, null, null,
                null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                jumpers.add(createJumper(cursor));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return jumpers;
    }

    public TreeMap<Season, TreeSet<Competition>> getSeasonToCompetitionsMap() {
        TreeMap<Season, TreeSet<Competition>> seasonToCompetitions = new TreeMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CompetitionColumns.TABLE_NAME, null, null,
                null, null, null, CompetitionColumns._DATE + " DESC");
        Season currentSeason = null;
        TreeSet<Competition> currentSet = null;
        if(cursor.moveToFirst()) {
            do {
                Competition competition = createCompetition(cursor);
                Season season = competition.getSeason();
                if(season.equals(currentSeason)) {
                    currentSet.add(competition);
                }
                else {
                    currentSet = new TreeSet<>();
                    currentSeason = season;
                    currentSet.add(competition);
                    seasonToCompetitions.put(currentSeason, currentSet);
                }
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return seasonToCompetitions;
    }

    public void fillJumpersWithResults(List<Jumper> jumpers, List<Competition> competitions) {
        SQLiteDatabase db = this.getReadableDatabase();
        for(Jumper j : jumpers) {
            for(Competition c : competitions) {
                Result result = getResult(j, c, db);
                if(result != null) {
                    j.setResult(c, result);
                }
            }
        }
        db.close();
    }

    private Jumper createJumper(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(JumpersColumns._NAME));
        String surname = cursor.getString(cursor.getColumnIndex(JumpersColumns._SURNAME));
        String country = cursor.getString(cursor.getColumnIndex(JumpersColumns._COUNTRY));
        String dateString = cursor.getString(cursor.getColumnIndex(JumpersColumns._DATE_OF_BIRTH));
        float height = cursor.getFloat(cursor.getColumnIndex(JumpersColumns._HEIGHT));
        int id = cursor.getInt(cursor.getColumnIndex(JumpersColumns._ID));
        Jumper jumper = new Jumper(name, surname, Country.valueOf(country), stringToDate(dateString), height);
        jumper.setId(id);
        return jumper;
    }

    private Competition createCompetition(Cursor cursor) {
        String city = cursor.getString(cursor.getColumnIndex(CompetitionColumns._CITY));
        String country = cursor.getString(cursor.getColumnIndex(CompetitionColumns._COUNTRY));
        float pointK = cursor.getFloat(cursor.getColumnIndex(CompetitionColumns._POINT_K));
        float hillSize = cursor.getFloat(cursor.getColumnIndex(CompetitionColumns._HILL_SIZE));
        float headWindPoints = cursor.getFloat(cursor.getColumnIndex(CompetitionColumns._HEAD_WIND_POINTS));
        float tailWindPoints = cursor.getFloat(cursor.getColumnIndex(CompetitionColumns._TAIL_WIND_POINTS));
        String dateString = cursor.getString(cursor.getColumnIndex(CompetitionColumns._DATE));
        int id = cursor.getInt(cursor.getColumnIndex(CompetitionColumns._ID));
        Competition competition = new Competition(city, Country.valueOf(country), pointK, hillSize,
                    headWindPoints, tailWindPoints, stringToDate(dateString));
        competition.setId(id);
        return competition;
    }

    private SeriesResult createSeriesResult(Cursor cursor, Result result) {
        int series = cursor.getInt(cursor.getColumnIndex(ResultsColumns._SERIES));
        float distance = cursor.getFloat(cursor.getColumnIndex(ResultsColumns._DISTANCE));
        float wind = cursor.getFloat(cursor.getColumnIndex(ResultsColumns._WIND));
        float speed = cursor.getFloat(cursor.getColumnIndex(ResultsColumns._SPEED));
        float gateCompensation = cursor.getFloat(cursor.getColumnIndex(ResultsColumns._GATE_COMPENSATION));
        List<Float> marks = new ArrayList<>();
        for(int i=0; i<5; i++) {
            marks.add(cursor.getFloat(cursor.getColumnIndex(ResultsColumns._JUDGES[i])));
        }
        return new SeriesResult(series, distance, wind, speed, marks, gateCompensation, result);
    }

    private Result getResult(Jumper jumper, Competition competition, SQLiteDatabase db) {
        Result result = new Result(jumper, competition);
        String selection = ResultsColumns._JUMPER + "=? AND " + ResultsColumns._COMPETITION + "=?";
        Cursor cursor = db.query(ResultsColumns.TABLE_NAME, null, selection,
                new String[]{jumper.getId()+"", competition.getId()+""}, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                SeriesResult seriesResult = createSeriesResult(cursor, result);
                result.setResultForSeries(seriesResult.getSeries(), seriesResult);
            } while(cursor.moveToNext());
        }
        else {
            result = null;
        }
        cursor.close();
        return result;
    }

    private static String dateToString(LocalDate date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    private static LocalDate stringToDate(String date) {
        return LocalDate.parse(date);
    }

    public static class JumpersColumns implements BaseColumns {
        public static String TABLE_NAME = "jumpers";
        public static String _NAME = "name";
        public static String _SURNAME = "surname";
        public static String _COUNTRY = "country";
        public static String _DATE_OF_BIRTH = "date_of_birth";
        public static String _HEIGHT = "height";
        static String createTable = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                " %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s REAL)", TABLE_NAME, _ID, _NAME, _SURNAME, _COUNTRY,
                _DATE_OF_BIRTH, _HEIGHT);
        static ContentValues values(Jumper jumper) {
            ContentValues values = new ContentValues();
            values.put(_NAME, jumper.getName());
            values.put(_SURNAME, jumper.getSurname());
            values.put(_COUNTRY, jumper.getCountry().name());
            values.put(_DATE_OF_BIRTH, dateToString(jumper.getDateOfBirth()));
            values.put(_HEIGHT, jumper.getHeight());
            return values;
        }
    }

    public static class CompetitionColumns implements BaseColumns {
        public static String TABLE_NAME = "competition";
        public static String _CITY = "city";
        public static String _COUNTRY = "country";
        public static String _POINT_K = "point_K";
        public static String _HILL_SIZE = "hill_size";
        public static String _HEAD_WIND_POINTS = "head_wind_points";
        public static String _TAIL_WIND_POINTS = "tail_wind_points";
        public static String _DATE = "date";
        static String createTable = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " %s TEXT, %s TEXT, %s REAL, %s REAL, %s REAL, %s REAL, %s TEXT)", TABLE_NAME, _ID, _CITY, _COUNTRY,
                _POINT_K, _HILL_SIZE, _HEAD_WIND_POINTS, _TAIL_WIND_POINTS, _DATE);
        static ContentValues values(Competition competition) {
            ContentValues values = new ContentValues();
            values.put(_CITY, competition.getCity());
            values.put(_COUNTRY, competition.getCountry().name());
            values.put(_POINT_K, competition.getPointK());
            values.put(_HILL_SIZE, competition.getHillSize());
            values.put(_HEAD_WIND_POINTS, competition.getHeadWindPoints());
            values.put(_TAIL_WIND_POINTS, competition.getTailWindPoints());
            values.put(_DATE, dateToString(competition.getDate()));
            return values;
        }
    }

    public static class ResultsColumns {
        public static String TABLE_NAME = "results";
        public static String _JUMPER = "jumper";
        public static String _COMPETITION = "competition";
        public static String _SERIES = "series";
        public static String _DISTANCE = "distance";
        public static String _WIND = "wind";
        public static String _SPEED = "speed";
        public static String[] _JUDGES = new String[5];
        public static String _GATE_COMPENSATION = "gate_compensation";
        static {
            for(int i=1; i<6; i++) {
                _JUDGES[i-1] = "judge" + i;
            }
        }
        static String createTable = String.format("CREATE TABLE %s(" +
                "%s INTEGER, %s INTEGER, %s INTEGER, %s REAL, %s REAL, %s REAL, %s REAL, %s REAL, %s REAL, %s REAL, %s REAL, %s REAL)",
                TABLE_NAME, _JUMPER, _COMPETITION, _SERIES, _DISTANCE, _WIND, _SPEED, _JUDGES[0], _JUDGES[1], _JUDGES[2], _JUDGES[3],
                _JUDGES[4], _GATE_COMPENSATION);
        static ContentValues values(SeriesResult result) {
            ContentValues values = new ContentValues();
            values.put(_JUMPER, result.getJumper().getId());
            values.put(_COMPETITION, result.getCompetition().getId());
            values.put(_SERIES, result.getSeries());
            values.put(_DISTANCE, result.getDistance());
            values.put(_WIND, result.getWind());
            values.put(_SPEED, result.getSpeed());
            List<Float> styleScores = result.getStyleScores();
            for(int i=0; i<5; i++) {
                values.put(_JUDGES[i], styleScores.get(i));
            }
            values.put(_GATE_COMPENSATION, result.pointsForGate());
            return values;
        }
    }
}
