package ty.henry.jumpingstats.competitions;

import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;

public class Result {

    private Jumper jumper;
    private Competition competition;
    private SeriesResult[] seriesResults;

    public Result(Jumper jumper, Competition competition) {
        this.jumper = jumper;
        this.competition = competition;
        this.seriesResults = new SeriesResult[2];
    }

    public void setJumper(Jumper jumper) {
        this.jumper = jumper;
    }

    public Jumper getJumper() {
        return jumper;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Competition getCompetition() {
        return competition;
    }

    public SeriesResult getResultForSeries(int series) throws NoResultForJumperException {
        checkSeriesArgument(series);
        SeriesResult seriesResult = seriesResults[series - 1];
        if(seriesResult == null) {
            throw new NoResultForJumperException();
        }
        return seriesResult;
    }

    public void setResultForSeries(int series, SeriesResult seriesResult) {
        checkSeriesArgument(series);
        seriesResults[series - 1] = seriesResult;

    }

    public float points() {
        float points = 0;
        for(int i = 0; i < 2; i++) {
            if(seriesResults[i] != null) {
                points += seriesResults[i].points();
            }
        }
        return points;
    }

    public float absPointsDifference() {
        float points1 = seriesResults[0] == null ? 0 : seriesResults[0].points();
        float points2 = seriesResults[1] == null ? 0 : seriesResults[1].points();
        return Math.abs(points1 - points2);
    }

    public static void checkSeriesArgument(int series) {
        if(series>2 || series<1) {
            throw new IllegalArgumentException("Series argument must be 1 or 2");
        }
    }
}
