package ty.henry.jumpingstats.competitions;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.MainActivity;
import ty.henry.jumpingstats.TextImageAdapter;

public class Competition implements TextImageAdapter.TextImage, Comparable<Competition> {
    private int id = -1;
    private String city;
    private Country country;
    private float pointK;
    private float hillSize;
    private float headWindPoints;
    private float tailWindPoints;
    private Calendar date;
    private Season season;

    public Competition(String city, Country country, float pointK, float hillSize,
                       float headWindPoints, float tailWindPoints, Calendar date) {
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

    public Calendar getDate() {
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
                hillSize==otherComp.getHillSize() && compareDates(date, otherComp.getDate())==0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, hillSize, date.get(Calendar.YEAR),
                date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }

    public String[] getText() {
        String[] result = new String[2];
        result[0] = String.format("%s (K %.0f)", city, pointK);
        result[1] = DBHelper.calendarToString(date);
        return result;
    }

    public int getImage() {
        return country.getFlagId();
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_ITEM;
    }

    public static int compareDates(Calendar date1, Calendar date2) {
        int res = date1.get(Calendar.YEAR) - date2.get(Calendar.YEAR);
        if(res==0) {
            res = date1.get(Calendar.MONTH) - date2.get(Calendar.MONTH);
            if(res==0) {
                res = date1.get(Calendar.DAY_OF_MONTH) - date2.get(Calendar.DAY_OF_MONTH);
            }
        }
        return res;
    }

    public int compareTo(Competition other) {
        return compareDates(other.getDate(), this.getDate());
    }

    public String getShortDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        return dateFormat.format(date.getTime());
    }

}
