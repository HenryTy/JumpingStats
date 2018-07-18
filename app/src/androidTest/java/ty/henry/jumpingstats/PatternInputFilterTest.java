package ty.henry.jumpingstats;

import android.support.test.filters.SmallTest;
import android.text.SpannedString;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

@SmallTest
public class PatternInputFilterTest {

    private PatternInputFilter patternInputFilter;
    private SpannedString dest;

    @Before
    public void setUp() {
        Pattern pattern = Pattern.compile("[A-Z][a-z]+[0-9]+");
        patternInputFilter = new PatternInputFilter(pattern);
        dest = new SpannedString("Ht1");
    }

    @Test
    public void filter_CorrectInputAddedToTheEndOfDest_Null() {
        String source = "5679";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 4,
                dest, dest.length(), dest.length());
        assertNull(filterResult);
    }

    @Test
    public void filter_InCorrectInputAddedToTheEndOfDest_EmptyString() {
        String source = "a5679";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 4,
                dest, dest.length(), dest.length());
        assertEquals(0, filterResult.length());
    }

    @Test
    public void filter_CorrectInputInsertedInTheMiddleOfDest_Null() {
        String source = "y3";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 2,
                dest, 2, 2);
        assertNull(filterResult);
    }

    @Test
    public void filter_InCorrectInputInsertedInTheMiddleOfDest_EmptyString() {
        String source = "3y";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 2,
                dest, 2, 2);
        assertEquals(0, filterResult.length());
    }

    @Test
    public void filter_DestReplacedWithCorrectInput_Null() {
        String source = "Adam5";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 5,
                dest, 0, dest.length());
        assertNull(filterResult);
    }

    @Test
    public void filter_DestReplacedWithInCorrectInput_EmptyString() {
        String source = "ADam5";
        CharSequence filterResult = patternInputFilter.filter(source, 0, 5,
                dest, 0, dest.length());
        assertEquals(0, filterResult.length());
    }
}
