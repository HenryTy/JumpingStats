package ty.henry.jumpingstats;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.competitions.Result;
import ty.henry.jumpingstats.jumpers.Jumper;

import static java.util.Calendar.*;
import static org.junit.Assert.*;

public class JumperTest {

    private Jumper jumper;
    private Calendar dateOfBirth;
    private Competition competition;
    private Result result1, result2;

    @Before
    public void setUp() {
        dateOfBirth = getInstance();
        dateOfBirth.set(1990, 0, 1);
        jumper = new Jumper("Ernest", "Makaron", Country.ITALY, dateOfBirth, 1.8f);
        competition = new Competition("CityName", Country.USA,
                125f, 140f, 0f, 0f, getInstance());
        result1 = new Result(jumper, competition, 1, 125f, 0f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
        result2 = new Result(jumper, competition, 2, 130f, 0f, 100f,
                new float[]{18f, 18f, 18f, 18f, 18f}, 0f);
    }

    @Test
    public void getAge_JumperHasAlreadyHadBirthday_CurrentYearMinusYearOfBirth() {
        int currentYear = getInstance().get(YEAR);
        int actualAge = jumper.getAge();
        int expectedAge = currentYear - 1990;
        assertEquals(expectedAge, actualAge);
    }

    @Test
    public void getAge_JumperHasNotHadBirthdayYet_CurrentYearMinusYearOfBirthMinus1() {
        Calendar today = getInstance();
        if(today.get(MONTH)!=11 || today.get(DAY_OF_MONTH)!=31) {
            dateOfBirth.set(1990, 11, 31);
            int currentYear = today.get(YEAR);
            int actualAge = jumper.getAge();
            int expectedAge = currentYear - 1990 - 1;
            assertEquals(expectedAge, actualAge);
        }
    }

    @Test
    public void getResults_ResultsAreSet_ShouldGetSetResults() {
        jumper.setResult(competition, result2);
        jumper.setResult(competition, result1);
        try {
            Result[] jumpRes = jumper.getResults(competition);
            assertEquals(result1.getDistance(), jumpRes[0].getDistance(), 0);
            assertEquals(result2.getDistance(), jumpRes[1].getDistance(), 0);
        }
        catch (Exception ex) {
            fail("Exception shouldn't be thrown if jumper has result set");
        }
    }

    @Test
    public void getResults_OneResultWasRemoved_ShouldGetOneResultAndNull() {
        jumper.setResult(competition, result2);
        jumper.setResult(competition, result1);
        jumper.removeResult(competition, 1);
        try {
            Result[] jumpRes = jumper.getResults(competition);
            assertNull(jumpRes[0]);
            assertEquals(result2.getDistance(), jumpRes[1].getDistance(), 0);
        }
        catch (Exception ex) {
            fail("Exception shouldn't be thrown if jumper has result set");
        }
    }

    @Test(expected = Exception.class)
    public void getResults_OneResultWasSetAndRemoved_ShouldThrowException() throws Exception {
        jumper.setResult(competition, result2);
        jumper.removeResult(competition, 2);
        jumper.getResults(competition);
    }

    @Test(expected = Exception.class)
    public void getResults_ResultsFromCompetitionWereRemoved_ShouldThrowException() throws Exception {
        jumper.setResult(competition, result2);
        jumper.setResult(competition, result1);
        jumper.removeResultsFromCompetition(competition);
        jumper.getResults(competition);
    }

    @Test(expected = Exception.class)
    public void onCompetitionUpdated_GettingResultsFromOldCompetition_ShouldThrowException() throws Exception {
        jumper.setResult(competition, result2);
        Competition newComp = new Competition("OtherCity", Country.CANADA,
                125f, 140f, 0f, 0f, getInstance());
        jumper.onCompetitionUpdated(competition, newComp);
        jumper.getResults(competition);
    }

    @Test
    public void onCompetitionUpdated_GettingResultsFromNewCompetition_ShouldGetResultsFromOldCompetition() {
        jumper.setResult(competition, result2);
        Competition newComp = new Competition("OtherCity", Country.CANADA,
                125f, 140f, 0f, 0f, getInstance());
        jumper.onCompetitionUpdated(competition, newComp);
        try {
            Result[] jumpRes = jumper.getResults(newComp);
            assertEquals(result2.getDistance(), jumpRes[1].getDistance(), 0);
        } catch (Exception ex) {
            fail("Getting results from updated competition shouldn't throw exception.");
        }
    }

    @Test
    public void onCompetitionUpdated_GettingCompetitionOfResultFromOldCompetition_NewCompetition() {
        jumper.setResult(competition, result2);
        Competition newComp = new Competition("OtherCity", Country.CANADA,
                125f, 140f, 0f, 0f, getInstance());
        jumper.onCompetitionUpdated(competition, newComp);
        assertEquals(newComp, result2.getCompetition());
    }

    @Test
    public void getPointsFromComp_BothResultsAreSet_SumOfPointsFromResults() throws Exception {
        jumper.setResult(competition, result1);
        jumper.setResult(competition, result2);
        float expectedPoints = result1.points() + result2.points();
        float actualPoints = jumper.getPointsFromComp(competition);
        assertEquals(expectedPoints, actualPoints, 0.1);
    }

    @Test
    public void getPointsFromComp_OneResultIsSet_PointsFromResult() throws Exception {
        jumper.setResult(competition, result1);
        float expectedPoints = result1.points();
        float actualPoints = jumper.getPointsFromComp(competition);
        assertEquals(expectedPoints, actualPoints, 0.1);
    }

    @Test
    public void setCompResMap_MapFromOldJumperWasSetToNewJumper_OwnerOfOldJumpersResultsShouldBeNewJumper() {
        jumper.setResult(competition, result1);
        jumper.setResult(competition, result2);
        Jumper newJumper = new Jumper("Ernesto", "Makarono", Country.ITALY, dateOfBirth, 1.8f);
        newJumper.setCompResMap(jumper.getCompResMap());
        assertEquals(newJumper, result1.getJumper());
        assertEquals(newJumper, result2.getJumper());
    }

    @Test
    public void equals_EqualJumper_True() {
        Jumper otherJumper = new Jumper("Ernest", "Makaron", Country.POLAND, dateOfBirth, 1.85f);
        assertEquals(jumper, otherJumper);
    }

    @Test
    public void equals_JumperWithDifferentDateOfBirth_False() {
        Calendar dateOfBirth2 = getInstance();
        dateOfBirth2.set(1990, 0, 2);
        Jumper otherJumper = new Jumper("Ernest", "Makaron", Country.POLAND, dateOfBirth2, 1.85f);
        assertNotEquals(jumper, otherJumper);
    }

    @Test
    public void equals_JumperWithDifferentName_False() {
        Jumper otherJumper = new Jumper("Ernesto", "Makaron", Country.POLAND, dateOfBirth, 1.85f);
        assertNotEquals(jumper, otherJumper);
    }
}
