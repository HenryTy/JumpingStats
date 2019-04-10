package ty.henry.jumpingstats.statistics;

import java.util.List;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public enum TableItem implements StatsFragment.YValueGetter {
    DISTANCE(R.string.table_distance) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getDistance();
        }
    },
    WIND(R.string.table_wind) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return Math.abs(jumper.getResult(competition).getResultForSeries(series).getWind());
        }
    },
    SPEED(R.string.table_speed) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getSpeed();
        }
    },
    MARK_FOR_STYLE(R.string.table_style) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            List<Float> marks = jumper.getResult(competition).getResultForSeries(series).getStyleScores();
            return (float) marks.stream().mapToDouble(Float::floatValue).average().getAsDouble();
        }
    },
    POINTS_DIFF(R.string.table_points_diff) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).absPointsDifference();
        }
    };

    private int titleId;

    TableItem(int titleId) {
        this.titleId = titleId;
    }

    public int getTitleId() {
        return titleId;
    }

    public abstract float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException;
}
