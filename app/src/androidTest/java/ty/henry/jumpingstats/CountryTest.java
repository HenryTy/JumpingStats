package ty.henry.jumpingstats;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class CountryTest {

    @Test
    public void getCountryNameTest() {
        Context context = InstrumentationRegistry.getTargetContext();
        for(Country country : Country.values()) {
            String actualName = country.getCountryName(context)
                    .toUpperCase().replace(' ', '_');
            String expectedName = country.name();
            String message = "Name of country " + expectedName + " is incorrect";
            assertEquals(message, expectedName, actualName);
        }
    }
}
