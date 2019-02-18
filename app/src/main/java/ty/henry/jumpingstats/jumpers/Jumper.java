package ty.henry.jumpingstats.jumpers;


import android.content.Context;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.TextImageAdapter;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;

public class Jumper implements TextImageAdapter.TextImage {
    private int id = -1;
    private String name;
    private String surname;
    private Country country;
    private LocalDate dateOfBirth;
    private float height;

    private HashMap<Competition, Result> compResMap;

    public Jumper(String name, String surname, Country country, LocalDate dateOfBirth, float height) {
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public  float getHeight() {
        return height;
    }

    public int getAge() {
        return (int) dateOfBirth.until(LocalDate.now(), ChronoUnit.YEARS);
    }

    public Result getResult(Competition competition) throws NoResultForJumperException {
        Result result = compResMap.get(competition);
        if(result == null) {
            throw new NoResultForJumperException();
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
                Objects.equals(dateOfBirth, otherJumper.getDateOfBirth());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, dateOfBirth);
    }

    @Override
    public String toString() {
        return name + " " + surname;
    }

    public String[] getText(Context context) {
        return new String[]{name + " " + surname};
    }

    public int getImage() {
        return country.getFlagId();
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_ITEM;
    }
}
