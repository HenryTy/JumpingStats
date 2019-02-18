package ty.henry.jumpingstats.competitions;

import ty.henry.jumpingstats.R;

public enum SeasonType {
    WINTER(R.string.winter),
    SUMMER(R.string.summer);

    private int nameId;

    SeasonType(int nameId) {
        this.nameId = nameId;
    }

    public int getNameId() {
        return nameId;
    }
}
