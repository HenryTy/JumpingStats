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

    public int getId() {
        return id;
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

    public void setResult(Competition competition, Result result, int series) {
        if(series>2 || series<1) {
            throw new IllegalArgumentException("Series argument must be 1 or 2");
        }
        Result[] resultArr = compResMap.get(competition);
        if(resultArr == null) {
            resultArr = new Result[2];
            compResMap.put(competition, resultArr);
        }
        resultArr[series-1] = result;
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
