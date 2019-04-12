package ty.henry.jumpingstats.competitions;

import android.content.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.TextImageAdapter;

public class Competition implements TextImageAdapter.TextImage, Comparable<Competition>, DBHelper.Identifiable {
    private int id = -1;
    private String city;
    private Country country;
    private float pointK;
    private float hillSize;
    private float headWindPoints;
    private float tailWindPoints;
    private LocalDate date;
    private Season season;

    public Competition(String city, Country country, float pointK, float hillSize,
                       float headWindPoints, float tailWindPoints, LocalDate date) {
        this.city = city;
        this.country = country;
        this.pointK = pointK;
        this.hillSize = hillSize;
        this.headWindPoints = headWindPoints;
        this.tailWindPoints = tailWindPoints;
        this.date = date;
        this.season = new Season(date);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public Country getCountry() {
        return country;
    }

    public float getPointK() {
        return pointK;
    }

    public float getHillSize() {
        return hillSize;
    }

    public float getHeadWindPoints() {
        return headWindPoints;
    }

    public float getTailWindPoints() {
        return tailWindPoints;
    }

    public LocalDate getDate() {
        return date;
    }

    public Season getSeason() {
        return season;
    }

    @Override
    public boolean equals(Object other) {
        if(other==null || getClass()!=other.getClass()) {
            return false;
        }
        Competition otherComp = (Competition) other;
        return Objects.equals(city, otherComp.getCity()) &&
                hillSize==otherComp.getHillSize() && Objects.equals(date, otherComp.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, hillSize, date);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", city, DateTimeFormatter.ISO_LOCAL_DATE.format(date));
    }

    public String[] getText(Context context) {
        String[] result = new String[2];
        result[0] = String.format("%s (K %.0f)", city, pointK);
        result[1] = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        return result;
    }

    public int getImage() {
        return country.getFlagId();
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_ITEM;
    }

    public int compareTo(Competition other) {
        return date.compareTo(other.getDate());
    }
}
