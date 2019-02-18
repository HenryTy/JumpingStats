package ty.henry.jumpingstats.statistics;

import java.util.stream.IntStream;

import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public enum YOption implements StatsFragment.YValueGetter {
    DISTANCE(R.string.y_axis_distance) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getDistance();
        }
    },
    DISTANCE_FROM_POINT_K(R.string.y_axis_dist_from_k) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getDistance() -
                    competition.getPointK();
        }
    },
    POINTS_FOR_DISTANCE(R.string.y_axis_points_for_dist) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).pointsForDistance();
        }
    },
    WIND(R.string.y_axis_wind_speed) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getWind();
        }
    },
    POINTS_FOR_WIND(R.string.y_axis_wind_points) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).pointsForWind();
        }
    },
    SPEED(R.string.y_axis_speed) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).getSpeed();
        }
    },
    MARK_FOR_STYLE(R.string.y_axis_style) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            float[] marks = jumper.getResult(competition).getResultForSeries(series).getStyleScores();
            return (float) IntStream.range(0, marks.length)
                    .mapToDouble(i -> marks[i]).average().getAsDouble();
        }
    },
    POINTS(R.string.y_axis_points) {
        @Override
        public float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException {
            return jumper.getResult(competition).getResultForSeries(series).points();
        }
    };

    private int titleId;

    YOption(int titleId) {
        this.titleId = titleId;
    }

    public int getTitleId() {
        return titleId;
    }

    public abstract float getValue(Jumper jumper, Competition competition, int series) throws NoResultForJumperException;
}
