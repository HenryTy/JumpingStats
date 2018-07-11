package ty.henry.jumpingstats.competitions;


import java.util.Calendar;
import java.util.Objects;

import ty.henry.jumpingstats.TextImageAdapter;

public class Season implements TextImageAdapter.TextImage, Comparable<Season> {

    private static int WINTER = 0;
    private static int SUMMER = 1;

    private String name;
    private int typeOfSeason;
    private int year;

    public Season(Calendar date) {
        int month = date.get(Calendar.MONTH);
        int year = date.get(Calendar.YEAR);
        if(month < 4) {
            name = (year-1) + "/" + year + " Winter";
            this.typeOfSeason = WINTER;
            this.year = year - 1;
        }
        else if(month < 10) {
            name = year + " Summer";
            this.typeOfSeason = SUMMER;
            this.year = year;
        }
        else {
            name = year + "/" + (year+1) + " Winter";
            this.typeOfSeason = WINTER;
            this.year = year;
        }
    }

    public int getTypeOfSeason() {
        return typeOfSeason;
    }

    public int getStartingYear() {
        return year;
    }

    public String[] getText() {
        return new String[]{name};
    }

    public int getImage() {
        return -1;
    }

    public int getType() {
        return TextImageAdapter.TextImage.TYPE_HEADER;
    }

    public int getId() {
        return -1;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other==null || getClass() != other.getClass()) {
            return false;
        }
        return Objects.equals(name, other.toString());
    }

    @Override
    public String toString() {
        return name;
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
