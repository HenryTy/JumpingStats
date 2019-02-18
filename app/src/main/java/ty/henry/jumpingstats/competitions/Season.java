package ty.henry.jumpingstats.competitions;


import android.content.Context;

import java.time.LocalDate;
import java.util.Objects;

import ty.henry.jumpingstats.TextImageAdapter;

import static ty.henry.jumpingstats.competitions.SeasonType.SUMMER;
import static ty.henry.jumpingstats.competitions.SeasonType.WINTER;

public class Season implements TextImageAdapter.TextImage, Comparable<Season> {

    private String yearText;
    private SeasonType typeOfSeason;
    private int year;

    public Season(LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();
        if(month < 5) {
            yearText = (year-1) + "/" + year;
            this.typeOfSeason = WINTER;
            this.year = year - 1;
        }
        else if(month < 11) {
            yearText = year + "";
            this.typeOfSeason = SUMMER;
            this.year = year;
        }
        else {
            yearText = year + "/" + (year+1);
            this.typeOfSeason = WINTER;
            this.year = year;
        }
    }

    public SeasonType getTypeOfSeason() {
        return typeOfSeason;
    }

    public int getStartingYear() {
        return year;
    }

    public String[] getText(Context context) {
        String name = yearText + " " + context.getString(typeOfSeason.getNameId());
        return new String[]{name};
    }

    public int getImage() {
        return -1;
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_HEADER;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeOfSeason, year);
    }

    @Override
    public boolean equals(Object other) {
        if(other==null || getClass() != other.getClass()) {
            return false;
        }
        Season otherSeason = (Season) other;
        return Objects.equals(typeOfSeason, otherSeason.getTypeOfSeason())
                && year == otherSeason.getStartingYear();
    }

    public int compareTo(Season other) {
        int res = other.getStartingYear() - this.getStartingYear();
        if(res==0){
            if(this.getTypeOfSeason()!=other.getTypeOfSeason()) {
                res = other.getTypeOfSeason()==WINTER ? 1 : -1;
            }
        }
        return res;
    }
}
