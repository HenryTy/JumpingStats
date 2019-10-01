package ty.henry.jumpingstats;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.competitions.SeriesResult;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;

import static org.junit.Assert.*;

public class JumperTest {

    private Jumper jumper;
    private Jumper jumperBorn31_12;
    private LocalDate dateOfBirth;
    private Competition competition;

    private static final int YEAR_OF_BIRTH = 1990;

    @Before
    public void setUp() {
        dateOfBirth = LocalDate.of(YEAR_OF_BIRTH, Month.JANUARY, 1);
        jumper = new Jumper("Ernest", "Makaron", Country.ITALY, dateOfBirth, 1.8f);
        jumperBorn31_12 = new Jumper(jumper.getName(), jumper.getSurname(), jumper.getCountry(),
                LocalDate.of(YEAR_OF_BIRTH, Month.DECEMBER, 31), jumper.getHeight());
        competition = new Competition("CityName", Country.USA,
                125f, 140f, 0f, 0f, LocalDate.now());
    }

    @Test
    public void getAge_JumperHasAlreadyHadBirthday_CurrentYearMinusYearOfBirth() {
        int currentYear = LocalDate.now().getYear();
        int actualAge = jumper.getAge();
        int expectedAge = currentYear - YEAR_OF_BIRTH;
        assertEquals(expectedAge, actualAge);
    }

    @Test
    public void getAge_JumperHasNotHadBirthdayYet_CurrentYearMinusYearOfBirthMinus1() {
        LocalDate today = LocalDate.now();
        if(today.getMonth()!= Month.DECEMBER || today.getDayOfMonth()!=31) {
            int currentYear = today.getYear();
            int actualAge = jumperBorn31_12.getAge();
            int expectedAge = currentYear - YEAR_OF_BIRTH - 1;
            assertEquals(expectedAge, actualAge);
        }
    }

    @Test
    public void getResult_ResultIsSet_ShouldGetSetResult() {
        Result result = new Result(jumper, competition);
        SeriesResult seriesResult = new SeriesResult(1, 125, 0, 100,
                Collections.nCopies(5, 20.0f), 0, result);
        result.setResultForSeries(1, seriesResult);
        jumper.setResult(competition, result);
        try {
            SeriesResult receivedResult = jumper.getResult(competition).getResultForSeries(1);
            assertEquals(seriesResult.getDistance(), receivedResult.getDistance(), 0);
        } catch (NoResultForJumperException ex) {
            fail("Exception shouldn't be thrown if jumper has result set");
        }
    }

    @Test(expected = NoResultForJumperException.class)
    public void getResult_ResultIsNotSet_ShouldThrowException() throws NoResultForJumperException {
        Jumper otherJumper = new Jumper(jumper.getName(), jumper.getSurname(),
                Country.ITALY, dateOfBirth, jumper.getHeight());
        otherJumper.getResult(competition);
    }

    @Test(expected = NoResultForJumperException.class)
    public void getResult_EmptyResultIsSet_ShouldThrowException() throws NoResultForJumperException {
        Result emptyResult = new Result(jumper, competition);
        jumper.setResult(competition, emptyResult);
        jumper.getResult(competition);
    }

    @Test
    public void equals_EqualJumper_True() {
        Jumper otherJumper = new Jumper(jumper.getName(), jumper.getSurname(), Country.POLAND, dateOfBirth,
                jumper.getHeight() + 0.05f);
        assertEquals(jumper, otherJumper);
    }

    @Test
    public void equals_JumperWithDifferentDateOfBirth_False() {
        assertNotEquals(jumper, jumperBorn31_12);
    }

    @Test
    public void equals_JumperWithDifferentName_False() {
        Jumper otherJumper = new Jumper(jumper.getName() + "o", jumper.getSurname(),
                Country.ITALY, dateOfBirth, jumper.getHeight());
        assertNotEquals(jumper, otherJumper);
    }
}
