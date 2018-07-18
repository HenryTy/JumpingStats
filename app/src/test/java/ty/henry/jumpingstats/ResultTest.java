package ty.henry.jumpingstats;

import org.junit.Before;
import org.junit.Test;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.jumpers.Jumper;

import static java.util.Calendar.getInstance;
import static org.junit.Assert.*;

public class ResultTest {

    private Jumper jumper;
    private Competition competition;

    @Before
    public void setUp() {
        competition = new Competition("CityName", Country.USA,
                125f, 140f, 10.8f, 13.07f, getInstance());
        jumper = new Jumper("Ernest", "Makaron", Country.ITALY, getInstance(), 1.8f);
    }

    @Test
    public void constructor_SeriesArgumentEqualTo1_DoesntThrowException() {
        try {
            new Result(jumper, competition, 1, 129.5f, -0.22f, 100f,
                    new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        } catch (IllegalArgumentException ex) {
            fail("Result constructor shouldn't throw exception if series argument is equal to 1");
        }
    }

    @Test
    public void constructor_SeriesArgumentEqualTo2_DoesntThrowException() {
        try {
            new Result(jumper, competition, 2, 129.5f, -0.22f, 100f,
                    new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        } catch (IllegalArgumentException ex) {
            fail("Result constructor shouldn't throw exception if series argument is equal to 2");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_TooLowValueOfSeries_ThrowIllegalArgumentException() {
        new Result(jumper, competition, 0, 129.5f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_TooHighValueOfSeries_ThrowIllegalArgumentException() {
        new Result(jumper, competition, 3, 129.5f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
    }

    @Test
    public void pointsForDistance_DistanceAbovePointK() {
        Result result = new Result(jumper, competition, 1, 129.5f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(68.1, result.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_DistanceBelowPointK() {
        Result result = new Result(jumper, competition, 1, 115.5f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(42.9, result.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_DistanceEqualsPointK_60() {
        Result result = new Result(jumper, competition, 1, 125f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(60, result.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_SkiFlyingHill() {
        competition = new Competition("CityName", Country.USA,
                200f, 240f, 10.8f, 13.07f, getInstance());
        Result result = new Result(jumper, competition, 1, 245f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(174, result.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForDistance_NormalHill() {
        competition = new Competition("CityName", Country.USA,
                98f, 109f, 10.8f, 13.07f, getInstance());
        Result result = new Result(jumper, competition, 1, 88f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(40, result.pointsForDistance(), 0.1);
    }

    @Test
    public void pointsForStyle_EqualMarks() {
        Result result = new Result(jumper, competition, 1, 129.5f, -0.22f, 100f,
                new float[]{18.5f, 18.5f, 18.5f, 18.5f, 18.5f}, 0f);
        assertEquals(55.5, result.pointsForStyle(), 0.1);
    }

    @Test
    public void pointsForStyle_DifferentMarks() {
        Result result = new Result(jumper, competition, 1, 129.5f, -0.22f, 100f,
                new float[]{18f, 17f, 18.5f, 17.5f, 16f}, 0f);
        assertEquals(52.5, result.pointsForStyle(), 0.1);
    }

    @Test
    public void pointsForWind_HeadWind() {
        Result result = new Result(jumper, competition, 1, 129.5f, -0.22f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(-2.4, result.pointsForWind(), 0.1);
    }

    @Test
    public void pointsForWind_TailWind() {
        Result result = new Result(jumper, competition, 1, 129.5f, 0.56f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        assertEquals(7.3, result.pointsForWind(), 0.1);
    }

    @Test
    public void pointsTest() {
        Result result = new Result(jumper, competition, 2, 141.5f, 0.01f, 91f,
                new float[]{18f, 18.5f, 17.5f, 19f, 18.5f}, 4.8f);
        assertEquals(149.6, result.points(), 0.1);
    }
}
