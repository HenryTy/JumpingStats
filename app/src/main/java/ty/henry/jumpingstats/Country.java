package ty.henry.jumpingstats;

import android.content.Context;

public enum Country {

    AUSTRIA(R.drawable.flag_austria),
    BULGARIA(R.drawable.flag_bulgaria),
    CANADA(R.drawable.flag_canada),
    CZECH_REPUBLIC(R.drawable.flag_czech_republic),
    ESTONIA(R.drawable.flag_estonia),
    FINLAND(R.drawable.flag_finland),
    FRANCE(R.drawable.flag_france),
    GERMANY(R.drawable.flag_germany),
    ITALY(R.drawable.flag_italy),
    JAPAN(R.drawable.flag_japan),
    KAZAKHSTAN(R.drawable.flag_kazakhstan),
    NORWAY(R.drawable.flag_norway),
    POLAND(R.drawable.flag_poland),
    RUSSIA(R.drawable.flag_russia),
    SLOVENIA(R.drawable.flag_slovenia),
    SOUTH_KOREA(R.drawable.flag_south_korea),
    SWITZERLAND(R.drawable.flag_switzerland),
    USA(R.drawable.flag_usa);

    private int flagId;

    Country(int flagId) {
        this.flagId = flagId;
    }

    public int getFlagId() {
        return flagId;
    }

    public String getCountryName(Context context) {
        return context.getResources().getStringArray(R.array.countries)[this.ordinal()];
    }
}
