package plc.homework;

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
                // Examples
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),

                // Matching
                Arguments.of("Numeric", "123456@gmail.com", true),
                Arguments.of("Hyphen", "test-test@gmail.com", true),
                Arguments.of("Dot", "test.test@gmail.com", true),
                Arguments.of("Underscore", "test_test@gmail.com", true),
                Arguments.of("Hyphen Dot", "domain-test.dot@gmail.com", true),
                Arguments.of("Hyphen Underscore", "domain_test-hyphen@gmail.com", true),
                Arguments.of("All Three", "._-@gmail.com", true),
                Arguments.of("All Three Alphanumeric", "t.e_s-t.@1-2-3gmail.com", true),
                Arguments.of(".edu", "ufdomain@ufl.edu", true),
                Arguments.of("Extra Domain Characters", "ufdomain@--abc.edu", true),

                // Non-Matching
                Arguments.of("Escape", "test\\domain@gmail.com", false),
                Arguments.of(".edus", "ufdomain@ufl.edus", false),
                Arguments.of("Invalid Extra Domain Characters", "ufdomain@+-abc.edu", false),
                Arguments.of("Whitespace", "d o m a i n@gmail.com", false),
                Arguments.of(".a", "test@gmail.a", false),
                Arguments.of(".@#$", "test123-.@gmail.@#$", false),
                Arguments.of(".@#$ 2", "test123-.@gmail.com@#$", false),
                Arguments.of("Valid Invalid Characters", ".@_#-$1a2b3@-gmail.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                // Examples
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),

                // Matching
                Arguments.of("20 Characters", "thisisatestfortwenty", true),
                Arguments.of("16 Characters", "thisisaTESTfortw", true),
                Arguments.of("Non-Alphabetical", "!@#$%^&*()", true),
                Arguments.of("10 Character - White Space", "          ", true),
                Arguments.of("Alphanumeric - 14 Characters", "ThIs12345T3S18", true),
                Arguments.of("Alphabetical/Non-Alphabetical - 18 Characters", "TeSt!@#$RHyaN)(*&^", true),

                // Non-Matching
                Arguments.of("9 Character - White Space", "         ", false),
                Arguments.of("19 Characters", "thisisatestfortwen!", false),
                Arguments.of("11 Characters - Non-Alphabetical", "_!^$@$$@#(*", false),
                Arguments.of("5 Characters", "Rhyan", false),
                Arguments.of("25 Characters", "thisisatestfortwnetyRHYAN", false),
                Arguments.of("Semi-Colon", ";", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                // Examples
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),

                // Matching
                Arguments.of("Empty Bracket", "[]", true),
                Arguments.of("Single Digit", "[5]", true),
                Arguments.of("White Space", "[1, 21, 33]", true),
                Arguments.of("Mixed White Space", "[4,7, 83]", true),
                Arguments.of("Large Numbers", "[124572456,124624627256256,2432726234521317,1345176571345,2762347213]", true),
                Arguments.of("Large Numbers Mixed White Space", "[124572456, 124624627256256, 2432726234521317,1345176571345, 2762347213]", true),
                Arguments.of("Zero After First Digit", "[100,202,303]", true),

                // Non-Matching
                Arguments.of("Single Zero", "[0]", false),
                Arguments.of("Missing Single Comma", "[1, 2 3]", false),
                Arguments.of("Alphabetical", "[a,b,c]", false),
                Arguments.of("Trailing Comma", "[1, 2, 3,]", false),
                Arguments.of("Double White Space", "[1,2,  3]", false),
                Arguments.of("Empty Bracket White Space", "[ ]", false),
                Arguments.of("Alphanumeric", "[1,a,2,b,3,c]", false),
                Arguments.of("Missing Bracket", "[1, 2, 3", false),
                Arguments.of("Empty", "", false),
                Arguments.of("Large Numbers Double/Triple White Space", "[124572456,   124624627256256,2432726234521317,  1345176571345,2762347213]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(
                // Examples
                Arguments.of("Single Digit Integer", "1", true),
                Arguments.of("Multiple Digit Decimal", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),

                // Matching
                Arguments.of("Negative One Integer", "-1", true),
                Arguments.of("Positive One Integer", "+1", true),
                Arguments.of("Leading Zeroes", "00001", true),
                Arguments.of("Leading Zeroes Decimal", "00001.56", true),
                Arguments.of("Trailing Zeroes Decimal", "1000.560000", true),
                Arguments.of("Leading/Trailing Zeroes Decimal", "000005000.560000", true),
                Arguments.of("Negative Trailing Zeroes Decimal", "-1000.560000", true),
                Arguments.of("Positive Trailing Zeroes Decimal", "+1000.560000", true),
                Arguments.of("Negative Large Number", "-1234567891011121314151617181920", true),

                // Non-Matching
                Arguments.of("Positive/Negative Single Digit", "+-1", false),
                Arguments.of("Positive Trailing Decimal", "+1.", false),
                Arguments.of("Negative Trailing Decimal", "-1.", false),
                Arguments.of("Alphabetic", "asdf", false),
                Arguments.of("Alphanumeric", "abc123", false),
                Arguments.of("Alphanumeric Positive", "+abc123", false),
                Arguments.of("Alphanumeric Negative", "-abc123", false),
                Arguments.of("Empty", "", false),
                Arguments.of("Empty White Space", " ", false),
                Arguments.of("Positive Only", "+", false),
                Arguments.of("Negative Only", "-", false)

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
                // Examples
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Hello World", "\"Hello, World!\"", true),
                Arguments.of("Escape", "\"1\\t2\"", true),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),

                // Matching
                Arguments.of("Escape 2", "\"1\\b5\"", true),
                Arguments.of("Three Quotes", "\" \" \"", true),
                Arguments.of("Four Quotes", "\" \"\" \"", true),
                Arguments.of("Quotes White Space", "\"  \"", true),
                Arguments.of("Alphanumeric", "\"123abc\"", true),
                Arguments.of("Alphanumeric Negative", "\"-123abc\"", true),
                Arguments.of("Alphanumeric Negative Escape", "\"-123abc\\n\"", true),
                Arguments.of("Quote Mark", "\"\\'\"", true),
                Arguments.of("Valid Slash", "\"\\\\\"", true),
                Arguments.of("Non-Alphanumeric", "\"!@#$%^+_-\"", true),

                // Non-Matching
                Arguments.of("No Quotes", "", false),
                Arguments.of("Single Quotes", "\"", false),
                Arguments.of("Alphanumeric Invalid Escape", "\"156jg\\h", false),
                Arguments.of("Invalid Escape", "\"\\p\"", false),
                Arguments.of("Numeric Invalid Escape", "\"\\2\"", false),
                Arguments.of("Single Slash", "\"\\\"", false),
                Arguments.of("Triple Slash", "\"\\\\\\\"", false),
                Arguments.of("Valid/Invalid Escape", "\"\\r\\t\\a\\v", false)
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
