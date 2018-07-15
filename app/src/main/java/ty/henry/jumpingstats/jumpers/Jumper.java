package ty.henry.jumpingstats.jumpers;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.MainActivity;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.TextImageAdapter;

public class Jumper implements TextImageAdapter.TextImage {
    private int id = -1;
    private String name;
    private String surname;
    private Country country;
    private Calendar dateOfBirth;
    private float height;

    private HashMap<Competition, Result[]> compResMap;

    public Jumper(String name, String surname, Country country, Calendar dateOfBirth, float height) {
        this.name = name;
        this.surname = surname;
        this.country = country;
        this.dateOfBirth = dateOfBirth;
        this.height = height;
        compResMap = new HashMap<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCompResMap(HashMap<Competition, Result[]> compResMap) {
        this.compResMap = compResMap;
        for(Result[] res : compResMap.values()) {
            if(res[0] != null) res[0].setJumper(this);
            if(res[1] != null) res[1].setJumper(this);
        }
    }

    public int getId() {
        return id;
    }

    public HashMap<Competition, Result[]> getCompResMap() {
        return compResMap;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Country getCountry() {
        return country;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    public  float getHeight() {
        return height;
    }

    public int getAge() {
        Calendar today = Calendar.getInstance();
        int diff = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
        if(dateOfBirth.get(Calendar.MONTH) > today.get(Calendar.MONTH) ||
                (dateOfBirth.get(Calendar.MONTH)==today.get(Calendar.MONTH) &&
                        dateOfBirth.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH))) {
            diff--;
        }
        return diff;
    }

    public Result[] getResults(Competition competition) throws Exception {
        Result[] results = compResMap.get(competition);
        if(results==null) {
            String message = "No results for " + this.surname + " in competition in " + competition.getCity();
            throw new Exception(message);
        }
        return results;
    }

    public void setResult(Competition competition, Result result) {
        Result[] resultArr = compResMap.get(competition);
        if(resultArr == null) {
            resultArr = new Result[2];
            compResMap.put(competition, resultArr);
        }
        resultArr[result.getSeries()-1] = result;
    }

    public void removeResult(Competition competition, int series) {
        Result[] resultArr = compResMap.get(competition);
        resultArr[series-1] = null;
        if(resultArr[series%2] == null) {
            compResMap.remove(competition);
        }
    }

    public void removeResultsFromCompetition(Competition competition) {
        compResMap.remove(competition);
    }

    public void onCompetitionUpdated(Competition oldComp, Competition updatedComp) {
        Result[] results = compResMap.remove(oldComp);
        if(results != null) {
            compResMap.put(updatedComp, results);
            if(results[0] != null) results[0].setCompetition(updatedComp);
            if(results[1] != null) results[1].setCompetition(updatedComp);
        }
    }

    public float getPointsFromComp(Competition competition) throws Exception {
        float points = 0;
        Result[] results = getResults(competition);
        if(results[0] != null) {
            points += results[0].points();
        }
        if(results[1] != null) {
            points += results[1].points();
        }
        return points;
    }

    @Override
    public boolean equals(Object other) {
        if(other==null || getClass() != other.getClass()) {
            return false;
        }
        Jumper otherJumper = (Jumper) other;
        return Objects.equals(name, otherJumper.getName()) && Objects.equals(surname, otherJumper.getSurname()) &&
                Competition.compareDates(dateOfBirth, otherJumper.getDateOfBirth())==0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, dateOfBirth.get(Calendar.YEAR),
                dateOfBirth.get(Calendar.MONTH), dateOfBirth.get(Calendar.DAY_OF_MONTH));
    }

    public String[] getText() {
        return new String[]{name + " " + surname};
    }

    public int getImage() {
        return country.getFlagId();
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_ITEM;
    }
}
