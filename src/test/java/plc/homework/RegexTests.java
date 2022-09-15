package plc.homework;

import com.sun.org.apache.xpath.internal.Arg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. A framework of the test structure 
 * is provided, you will fill in the remaining pieces.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 * Name: Rhyan Lugo Crespo
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),
                Arguments.of("20 Characters", "thisisatestfortwenty", true),
                Arguments.of("19 Characters", "thisisatestfortwen!", false),
                Arguments.of("11 Characters", "_!^$@$$@#(*", false),
                Arguments.of("5 Characters", "Rhyan", false),
                Arguments.of("25 Characters", "thisisatestfortwnetyRHYAN", false),
                Arguments.of("16 Characters", "thisisaTESTfortw", true),
                Arguments.of("1 Character", ";", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Empty Bracket", "[]", true),
                Arguments.of("Only 1 Number", "[5]", true),
                Arguments.of("Only zero", "[0]", false),
                Arguments.of("Mixed Space", "[4,7, 83]", true),
                Arguments.of("White Space", "[1, 21, 33]", true),
                Arguments.of("Missing 1 Comma", "[1, 2 3]", false),
                Arguments.of("Letters", "[a,b,c]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        //throw new UnsupportedOperationException() //TODO
        return Stream.of(
                Arguments.of("One number", "1", true),
                Arguments.of("Negative One number", "-1", true),
                Arguments.of("Positive One number", "+1", true),
                Arguments.of("Decimal", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".1", false),
                Arguments.of("Leading Zeroes", "00001", true),
                Arguments.of("Leading Zeroes Decimal", "00001.56", true),
                Arguments.of("Trailing Zeroes Decimal", "1000.560000", true),
                Arguments.of("Leading/Trailing Zeroes Decimal", "000005000.560000", true),
                Arguments.of("Negative Trailing Zeroes Decimal", "-1000.560000", true),
                Arguments.of("Positive Trailing Zeroes Decimal", "+1000.560000", true),
                Arguments.of("Positive/Negative One number", "+-1", false),
                Arguments.of("Negative Large Number", "-1234567891011121314151617181920", true),
                Arguments.of("Alphanumeric", "abc123", false),
                Arguments.of("Alphanumeric Positive", "+abc123", false),
                Arguments.of("Alphanumeric Negative", "-abc123", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Hello World", "\"Hello, World!\"", true),
                Arguments.of("Escape", "\"1\\t2\"", true),
                Arguments.of("Escape 2", "\"1\\b5\"", true),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                Arguments.of("No Quotes", "", false),
                Arguments.of("One Quotes", "\"", false),
                Arguments.of("Three Quotes", "\" \" \"", true),
                Arguments.of("Four Quotes", "\" \"\" \"", true),
                Arguments.of("Quotes with white space", "\"  \"", true),
                Arguments.of("Alphanumeric", "\"123abc\"", true),
                Arguments.of("Alphanumeric Negative", "\"-123abc\"", true),
                Arguments.of("Alphanumeric Negative Escape", "\"-123abc\\n\"", true),
                Arguments.of("Alphanumeric Invalid Escape", "\"156jg\\h", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
