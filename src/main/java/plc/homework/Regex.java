package plc.homework;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regexes as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._\\-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            EVEN_STRINGS = Pattern.compile("(?=.{10,20}$)(..)*"), //?=.{10,20}: Upcoming string must be in range. (..)*: each . represents a char so there would be a multiple of 2 chars.
            INTEGER_LIST = Pattern.compile("\\[([1-9]+(,\\s*[1-9]+)*)*\\]"),
            NUMBER = Pattern.compile("[+-]?([0-9]\\d*)(\\.\\d+)*"),
            STRING = Pattern.compile("\"([^\\\\]|\\\\([bnrt'\"\\\\]))*\"");
}
