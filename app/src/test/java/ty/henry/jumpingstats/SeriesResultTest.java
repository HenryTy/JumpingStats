package ty.henry.jumpingstats;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.competitions.SeriesResult;
import ty.henry.jumpingstats.jumpers.Jumper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SeriesResultTest {

    private Result result;

    @Before
    public void setUp() {
        Competition competition = new Competition("CityName", Country.USA,
                125f, 140f, 10.8f, 13.07f, LocalDate.now());
        Jumper jumper = new Jumper("Ernest", "Makaron", Country.ITALY,
                LocalDate.of(1990, Month.JANUARY, 1), 1.8f);
        result = new Result(jumper, competition);
    }

    @Test
    public void constructor_SeriesArgumentEqualTo1_DoesntThrowException() {
        try {
            new SeriesResult(1, 129.5f, -0.22f, 100f,
                    Collections.nCopies(5, 18.0f), 0f, result);
        } catch (IllegalArgumentException ex) {
            fail("SeriesResult constructor shouldn't throw exception if series argument is equal to 1");
        }
    }

    @Test
    public void constructor_SeriesArgumentEqualTo2_DoesntThrowException() {
        try {
            new SeriesResult(2, 129.5f, -0.22f, 100f,
                    Collections.nCopies(5, 18.0f), 0f, result);
        } catch (IllegalArgumentException ex) {
            fail("SeriesResult constructor shouldn't throw exception if series argument is equal to 2");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_TooLowValueOfSeries_ThrowIllegalArgumentException() {
        new SeriesResult(0, 129.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_TooHighValueOfSeries_ThrowIllegalArgumentException() {
        new SeriesResult(3, 129.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
    }

    @Test
    public void constructor_CorrectStyleScoresList_DoesntThrowException() {
        try {
            new SeriesResult(1, 129.5f, -0.22f, 100f,
                    Collections.nCopies(5, 18.0f), 0f, result);
        } catch (IllegalArgumentException ex) {
            fail("SeriesResult constructor shouldn't throw exception if styleScores list is correct");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_IncorrectNote_ThrowIllegalArgumentException() {
        new SeriesResult(1, 129.5f, -0.22f, 100f,
                Arrays.asList(18f, 18f, 18f, 21f, 18f), 0f, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_IncorrectNumberOfNotes_ThrowIllegalArgumentException() {
        new SeriesResult(1, 129.5f, -0.22f, 100f,
                Collections.nCopies(4, 18.0f), 0f, result);
    }

    @Test
    public void pointsForDistance_DistanceAbovePointK() {
        SeriesResult seriesResult = new SeriesResult(1, 129.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
        assertEquals(68.1, seriesResult.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_DistanceBelowPointK() {
        SeriesResult seriesResult = new SeriesResult(1, 115.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
        assertEquals(42.9, seriesResult.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_DistanceEqualsPointK_60() {
        SeriesResult seriesResult = new SeriesResult(1, 125f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
        assertEquals(60, seriesResult.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_SkiFlyingHill() {
        Competition competition = new Competition("CityName", Country.USA,
                200f, 240f, 10.8f, 13.07f, LocalDate.now());
        Result flyingResult = new Result(result.getJumper(), competition);
        SeriesResult seriesResult = new SeriesResult(1, 245f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, flyingResult);
        assertEquals(174, seriesResult.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_NormalHill() {
        Competition competition = new Competition("CityName", Country.USA,
                98f, 109f, 10.8f, 13.07f, LocalDate.now());
        Result normalResult = new Result(result.getJumper(), competition);
        SeriesResult seriesResult = new SeriesResult(1, 88f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, normalResult);
        assertEquals(40, seriesResult.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForStyle_EqualMarks() {
        SeriesResult seriesResult = new SeriesResult(1, 129.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.5f), 0f, result);
        assertEquals(55.5, seriesResult.pointsForStyle(), 0.1);
    }

    @Test
    public void pointsForStyle_DifferentMarks() {
        SeriesResult seriesResult = new SeriesResult(1, 129.5f, -0.22f, 100f,
                Arrays.asList(18f, 17f, 18.5f, 17.5f, 16f), 0f, result);
        assertEquals(52.5, seriesResult.pointsForStyle(), 0.1);
    }

    @Test
    public void pointsForWind_HeadWind() {
        SeriesResult seriesResult = new SeriesResult(1, 129.5f, -0.22f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
        assertEquals(-2.4, seriesResult.pointsForWind(), 0.1);
    }

    @Test
    public void pointsForWind_TailWind() {
        SeriesResult seriesResult = new SeriesResult(1, 129.5f, 0.56f, 100f,
                Collections.nCopies(5, 18.0f), 0f, result);
        assertEquals(7.3, seriesResult.pointsForWind(), 0.1);
    }

    @Test
    public void pointsTest() {
        SeriesResult seriesResult = new SeriesResult(2, 141.5f, 0.01f, 91f,
                Arrays.asList(18f, 18.5f, 17.5f, 19f, 18.5f), 4.8f, result);
        assertEquals(149.6, seriesResult.points(), 0.1);
    }
}
