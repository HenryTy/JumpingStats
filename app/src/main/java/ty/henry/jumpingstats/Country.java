package ty.henry.jumpingstats;

public enum Country {

    AUSTRIA(R.drawable.flag_austria, R.string.aus),
    BULGARIA(R.drawable.flag_bulgaria, R.string.bul),
    CANADA(R.drawable.flag_canada, R.string.can),
    CZECH_REPUBLIC(R.drawable.flag_czech_republic, R.string.czech),
    ESTONIA(R.drawable.flag_estonia, R.string.est),
    FINLAND(R.drawable.flag_finland, R.string.fin),
    FRANCE(R.drawable.flag_france, R.string.fr),
    GERMANY(R.drawable.flag_germany, R.string.ger),
    ITALY(R.drawable.flag_italy, R.string.it),
    JAPAN(R.drawable.flag_japan, R.string.jap),
    KAZAKHSTAN(R.drawable.flag_kazakhstan, R.string.kaz),
    NORWAY(R.drawable.flag_norway, R.string.nor),
    POLAND(R.drawable.flag_poland, R.string.pol),
    RUSSIA(R.drawable.flag_russia, R.string.rus),
    SLOVENIA(R.drawable.flag_slovenia, R.string.slo),
    SOUTH_KOREA(R.drawable.flag_south_korea, R.string.kor),
    SWITZERLAND(R.drawable.flag_switzerland, R.string.switz),
    USA(R.drawable.flag_usa, R.string.usa);

    private int flagId;
    private int nameId;

    Country(int flagId, int nameId) {
        this.flagId = flagId;
        this.nameId = nameId;
    }

    public int getFlagId() {
        return flagId;
    }

    public int getNameId() {
        return nameId;
    }
}
