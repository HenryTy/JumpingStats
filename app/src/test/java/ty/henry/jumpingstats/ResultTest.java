package ty.henry.jumpingstats;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.competitions.SeriesResult;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;

import static org.junit.Assert.*;

public class ResultTest {

    private Result result;
    private Result emptyResult;
    private SeriesResult seriesResult1;
    private SeriesResult seriesResult2;

    @Before
    public void setUp() {
        Competition competition = new Competition("CityName", Country.USA,
                85f, 90f, 7.0f, 8.48f, LocalDate.now());
        Jumper jumper = new Jumper("Ernest", "Makaron", Country.ITALY,
                LocalDate.of(1990, Month.JANUARY, 1), 1.8f);
        result = new Result(jumper, competition);
        seriesResult1 = new SeriesResult(1, 92, -0.6f, 82.7f,
                Arrays.asList(19f, 19f, 18.5f, 18.5f, 19f), 0, result);
        seriesResult2 = new SeriesResult(2, 86, 0.37f, 83f,
                Arrays.asList(18f, 17.5f, 17.5f, 18f, 17.5f), 0, result);
        emptyResult = new Result(jumper, competition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getResultForSeries_IncorrectSeries_ThrowIllegalArgumentException() {
        try {
            result.getResultForSeries(0);
        } catch (NoResultForJumperException ex) {
            fail("NoResultForJumperException shouldn't be thrown if series argument is incorrect");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setResultForSeries_IncorrectSeries_ThrowIllegalArgumentException() {
        result.setResultForSeries(3, seriesResult1);
    }

    @Test(expected = NoResultForJumperException.class)
    public void getResultForSeries_ResultIsNotSet_ThrowNoResultForJumperException()
            throws NoResultForJumperException {
        emptyResult.getResultForSeries(1);
    }

    @Test
    public void getResultForSeries_ResultIsSet_ShouldGetSetResult() {
        result.setResultForSeries(2, seriesResult2);
        try {
            SeriesResult receivedResult = result.getResultForSeries(2);
            assertEquals(seriesResult2.getDistance(), receivedResult.getDistance(), 0);
        } catch (NoResultForJumperException ex) {
            fail("Exception shouldn't be thrown if result has series result set");
        }
    }

    @Test
    public void points_EmptyResult_0() {
        assertEquals(0, emptyResult.points(), 0.1);
    }

    @Test
    public void points_OneSeriesResultSet_PointsFromSeries() {
        result.setResultForSeries(1, seriesResult1);
        result.setResultForSeries(2, null);
        assertEquals(126.3, result.points(), 0.1);
    }

    @Test
    public void points_TwoSeriesResultsSet_SumOfPointsFromSeries() {
        result.setResultForSeries(1, seriesResult1);
        result.setResultForSeries(2, seriesResult2);
        assertEquals(244.4, result.points(), 0.1);
    }

    @Test
    public void absPointsDifference_EmptyResult_0() {
        assertEquals(0, emptyResult.absPointsDifference(), 0.1);
    }

    @Test
    public void absPointsDifference_OneSeriesResultSet_PointsFromSeries() {
        result.setResultForSeries(2, seriesResult2);
        result.setResultForSeries(1, null);
        assertEquals(118.1, result.absPointsDifference(), 0.1);
    }

    @Test
    public void absPointsDifference_MorePointsInSecondSeries() {
        result.setResultForSeries(2, seriesResult1);
        result.setResultForSeries(1, seriesResult2);
        assertEquals(8.2, result.absPointsDifference(), 0.1);
    }

    @Test
    public void absPointsDifference_MorePointsInFirstSeries() {
        result.setResultForSeries(1, seriesResult1);
        result.setResultForSeries(2, seriesResult2);
        assertEquals(8.2, result.absPointsDifference(), 0.1);
    }
}
