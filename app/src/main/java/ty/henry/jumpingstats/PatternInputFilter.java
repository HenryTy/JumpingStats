package ty.henry.jumpingstats;


import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternInputFilter implements InputFilter {

    public static final Pattern DISTANCE_PATTERN = Pattern.compile("[0-9]{0,3}\\.?[05]?");
    public static final Pattern DISTANCE_END_PATTERN = Pattern.compile("[0-9]{2,3}(\\.[05])?");

    private Pattern pattern;

    public PatternInputFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence text = TextUtils.concat(dest.subSequence(0, dstart),
                source.subSequence(start, end), dest.subSequence(dend, dest.length()));
        Matcher matcher = pattern.matcher(text);
        if(!matcher.matches()) {
            return "";
        }
        return null;
    }
}
