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

    private HashMap<Competition, Result> compResMap;

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

    public Result getResult(Competition competition) throws Exception {
        Result result = compResMap.get(competition);
        if(result==null) {
            String message = "No results for " + this.surname + " in competition in " + competition.getCity();
            throw new Exception(message);
        }
        return result;
    }

    public void setResult(Competition competition, Result result) {
        compResMap.put(competition, result);
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
