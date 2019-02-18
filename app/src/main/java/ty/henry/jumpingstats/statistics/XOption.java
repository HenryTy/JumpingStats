package ty.henry.jumpingstats.statistics;

import java.util.function.Function;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.Jumper;

public enum XOption implements Function<Jumper, Float> {
    COMPETITION(R.string.x_axis_competition) {
        @Override
        public Float apply(Jumper jumper) {
            return null;
        }
    },
    JUMPERS_AGE(R.string.x_axis_age) {
        @Override
        public Float apply(Jumper jumper) {
            return (float) jumper.getAge();
        }
    },
    JUMPERS_HEIGHT(R.string.x_axis_height) {
        @Override
        public Float apply(Jumper jumper) {
            return jumper.getHeight();
        }
    };

    private int titleId;

    XOption(int titleId) {
        this.titleId = titleId;
    }

    public int getTitleId() {
        return titleId;
    }

    public abstract Float apply(Jumper jumper);
}
