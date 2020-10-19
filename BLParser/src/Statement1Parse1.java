import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Jackson Jiang
 * @author Jimmy Yuan
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        Condition result;
        if (c.equals("next-is-empty")) {
            result = Condition.NEXT_IS_EMPTY;
        } else if (c.equals("next-is-not-empty")) {
            result = Condition.NEXT_IS_NOT_EMPTY;
        } else if (c.equals("next-is-enemy")) {
            result = Condition.NEXT_IS_ENEMY;
        } else if (c.equals("next-is-not-enemy")) {
            result = Condition.NEXT_IS_NOT_ENEMY;
        } else if (c.equals("next-is-friend")) {
            result = Condition.NEXT_IS_FRIEND;
        } else if (c.equals("next-is-not-friend")) {
            result = Condition.NEXT_IS_NOT_FRIEND;
        } else if (c.equals("next-is-wall")) {
            result = Condition.NEXT_IS_WALL;
        } else if (c.equals("next-is-not-wall")) {
            result = Condition.NEXT_IS_NOT_WALL;
        } else if (c.equals("random")) {
            result = Condition.RANDOM;
        } else { // c.equals("true")
            result = Condition.TRUE;
        }
        return result;
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        tokens.dequeue(); //Dequeue if.

        //Check before parsing
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: CONDITION expected, found: " + tokens.front());

        Condition conditionIf = parseCondition(tokens.dequeue());

        Reporter.assertElseFatalError(tokens.front().equals("THEN"),
                "Error: THEN expected, found: " + tokens.front());

        tokens.dequeue(); //Dequeue thenToken.

        Statement statementIf = s.newInstance();
        statementIf.parseBlock(tokens);

        Reporter.assertElseFatalError(
                tokens.front().equals("ELSE") || tokens.front().equals("END"),
                "Error: ElSE or END expected, found " + tokens.front());

        //If it is an if - else statement
        if (tokens.front().equals("ELSE")) {
            tokens.dequeue(); //Dequeue elseToken.
            Statement statementElse = s.newInstance();
            statementElse.parseBlock(tokens);
            s.assembleIfElse(conditionIf, statementIf, statementElse);
            Reporter.assertElseFatalError(tokens.front().equals("END"),
                    "Error: END expected, found: " + tokens.front());
        } else {
            //if it is an if statement
            s.assembleIf(conditionIf, statementIf);
        }
        tokens.dequeue(); //Dequeue endToken.

        Reporter.assertElseFatalError(tokens.front().equals("IF"),
                "Error: IF expected, found " + tokens.front());

        tokens.dequeue(); //Dequeue endIfToken.
    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        tokens.dequeue(); //Dequeues WHILE token.

        //Checks for CONDITION before parsing
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "\"Error: CONDITION expected, found: " + tokens.front());

        Condition conditionWhile = parseCondition(tokens.dequeue());

        Reporter.assertElseFatalError(tokens.front().equals("DO"),
                "\"Error: DO expected, found: " + tokens.front());

        tokens.dequeue(); //Dequeues DO token.

        //Parses and assembles WHILE
        Statement statementWhile = s.newInstance();
        statementWhile.parseBlock(tokens);
        s.assembleWhile(conditionWhile, statementWhile);

        Reporter.assertElseFatalError(tokens.front().equals("END"),
                "Error: END expected, found: " + tokens.front());

        tokens.dequeue(); //Dequeues END.
        Reporter.assertElseFatalError(tokens.front().equals("WHILE"),
                "Error: WHILE expected, found: " + tokens.front());

        tokens.dequeue(); //Dequeues WHILE token.

    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";

        //Dequeues identifier
        String identifier = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(identifier),
                "Error: Valid identifier expected, found: " + tokens.front());

        s.assembleCall(identifier);
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Reporter.assertElseFatalError(
                tokens.front().equals("IF") || tokens.front().equals("WHILE")
                        || Tokenizer.isIdentifier(tokens.front()),
                "Error: IF, WHILE, or, identifier expected, found: "
                        + tokens.front());

        //Parses IF, WHILE, or CALL
        switch (tokens.front()) {
            case "IF":
                parseIf(tokens, this);
                break;
            case "WHILE":
                parseWhile(tokens, this);
                break;
            default:
                parseCall(tokens, this);
                break;
        }
    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        //New Statement
        Statement state = this.newInstance();
        //Starting position
        int pos = 0;
        //Keep going if the front of token equals IF, WHILE, or a identifier.
        while (tokens.front().equals("IF") || tokens.front().equals("WHILE")
                || Tokenizer.isIdentifier(tokens.front())) {
            //Parses and adds statement to BLOCK
            state.parse(tokens);
            this.addToBlock(pos, state);
            pos++;
        }
    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
