package numbers;
import java.util.List;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface ui = new UserInterface(scanner);
        ui.run();
    }
}
class UserInterface {
    private final Scanner scanner;

    public UserInterface(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Welcome to Amazing Numbers!\n\n"+
                "Supported requests:\n" +
                "- enter a natural number to know its properties;\n" +
                "- enter two natural numbers to obtain the properties of the list:\n" +
                "  * the first parameter represents a starting number;\n" +
                "  * the second parameter shows how many consecutive numbers are to be printed;\n" +
                "- two natural numbers and properties to search for;\n" +
                "- a property preceded by minus must not be present in numbers;\n" +
                "- separate the parameters with one space;\n" +
                "- enter 0 to exit.\n");

        while (true) {
            System.out.print("Enter a request: ");
            String input = scanner.nextLine();
            input = input.trim();
            System.out.println();
            if (input.equals("0")) {
                break;
            } else if (!input.isEmpty()) {
                new Request(input.toUpperCase());
            }
        }
        System.out.println("Goodbye!");
    }

}
class SignedProperty {
    private Property property;
    private boolean negative;

    public SignedProperty(Property property, boolean negative) {
        this.property = property;
        this.negative = negative;
    }

    public List<SignedProperty> getMutuallyExclusives() {
        List<SignedProperty> mutuallyExclusives = List.of(new SignedProperty(getProperty(), !isNegative()));
        if (Property.mutuallyExclusiveProperties.containsKey(this)) {
            mutuallyExclusives.add(Property.mutuallyExclusiveProperties.get(this));
        }
        return mutuallyExclusives;
    }

    public SignedProperty(Property property) {
        this(property, false);
    }

    public Property getProperty() {
        return property;
    }

    public boolean isNegative() {
        return negative;
    }

    public SignedProperty getOpposite() {
        return new SignedProperty(getProperty(), !isNegative());
    }

    @Override
    public String toString() {
        String name = getProperty().getName();
        if (isNegative()) {
            name = "-" + name;
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignedProperty pair = (SignedProperty) o;
        return isNegative() == pair.isNegative() && getProperty() == pair.getProperty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProperty(), isNegative());
    }
}
class Request {
    private final String[] parts;
    private final long start;
    private long length;
    private List<SignedProperty> requestedProperties;
    private List<String> illegalRequests;


    public Request(String input) {
        if (input.isBlank()) {
            throw new IllegalArgumentException("The string you have entered is blank.");
        }
        this.parts = input.split("\\s+");
        this.start = getNumber(this.parts[0]);
        if (this.start < 0) {
            printErrorMessage(1);
            return;
        }
        this.requestedProperties = new ArrayList<>();
        makeRequest();
    }

    private void makeRequest() {
        if (this.parts.length == 1) {
            new Number(this.start).printCard();
            return;
        }
        this.length = getNumber(this.parts[1]);
        if (this.length <= 0) {
            printErrorMessage(2);
            return;
        }
        this.illegalRequests = new ArrayList<>();
        for (int i = 2; i < this.parts.length; i++) {
            boolean negated = false;
            String name = this.parts[i];
            if (this.parts[i].charAt(0) == '-') {
                negated = true;
                name = name.substring(1);
            }
            try {
                Property property = Property.valueOf(name);
                this.requestedProperties.add(new SignedProperty(property, negated));
            } catch (IllegalArgumentException e) {
                this.illegalRequests.add(this.parts[i]);
            }
        }
        if (!this.illegalRequests.isEmpty()) {
            if (this.illegalRequests.size() == 1) {
                printErrorMessage(3);
            } else {
                printErrorMessage(4);
            }
            return;
        }
        if (areAnyPropertiesMutuallyExclusive()) {
            return;
        }
        new NumberList(this);
    }

    public List<SignedProperty> getRequestedProperties() {
        return this.requestedProperties;
    }

    public long getStart() {
        return this.start;
    }

    public long getLength() {
        return this.length;
    }

    private boolean areAnyPropertiesMutuallyExclusive() {
        for (SignedProperty signedProperty: this.requestedProperties) {
            if (this.requestedProperties.contains(signedProperty.getOpposite())) {
                printErrorMessage(5, signedProperty, signedProperty.getOpposite());
                return true;
            }
            if (Property.mutuallyExclusiveProperties.containsKey(signedProperty) &&
                    this.requestedProperties.contains(Property.mutuallyExclusiveProperties.get(signedProperty))) {
                printErrorMessage(5, signedProperty, Property.mutuallyExclusiveProperties.get(signedProperty));
                return true;
            }
        }
        return false;
    }

    private long getNumber(String part) {
        try {
            return Long.parseLong(part);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void printErrorMessage(int level, SignedProperty... pair) {
        switch (level) {
            case 1 -> {
                System.out.println(Message.FIRST.getMessage());
            }
            case 2 -> {
                System.out.println(Message.SECOND.getMessage());
            }
            case 3 -> {
                System.out.printf(Message.IS_WRONG.getMessage(), this.illegalRequests.get(0));
                System.out.println(Message.AVAILABLE.getMessage());
            }
            case 4 -> {
                System.out.printf(Message.ARE_WRONG.getMessage(), String.join(", ", this.illegalRequests));
                System.out.println(Message.AVAILABLE.getMessage());
            }
            case 5 -> {
                System.out.printf(Message.MUTUALLY.getMessage(), pair[0].toString(),
                        pair[1].toString());
                System.out.println(Message.NO_NUMBERS.getMessage());
            }
        }
        System.out.println();
    }

}
enum Property {

    EVEN("EVEN"),
    ODD("ODD"),
    BUZZ("BUZZ"),
    DUCK("DUCK"),
    PALINDROMIC("PALINDROMIC"),
    GAPFUL("GAPFUL"),
    SPY("SPY"),
    SQUARE("SQUARE"),
    SUNNY("SUNNY"),
    JUMPING("JUMPING"),
    HAPPY("HAPPY"),
    SAD("SAD");

    String property;
    public final static Map<SignedProperty, SignedProperty> mutuallyExclusiveProperties;

    static {
        mutuallyExclusiveProperties = new HashMap<>();
        mutuallyExclusiveProperties.put(new SignedProperty(EVEN), new SignedProperty(ODD));
        mutuallyExclusiveProperties.put(new SignedProperty(EVEN, true), new SignedProperty(ODD, true));
        mutuallyExclusiveProperties.put(new SignedProperty(DUCK), new SignedProperty(SPY));
        mutuallyExclusiveProperties.put(new SignedProperty(SQUARE), new SignedProperty(SUNNY));
        mutuallyExclusiveProperties.put(new SignedProperty(HAPPY), new SignedProperty(SAD));
        mutuallyExclusiveProperties.put(new SignedProperty(HAPPY, true), new SignedProperty(SAD, true));
    }

    Property(String property) {
        this.property = property;
    }

    public String getName() {
        return this.property;
    }

    public static List<Property> getAllProperties() { return List.of(values()); }
}

class NumberList {
    private Request request;
    public NumberList(Request request) {
        this.request = request;
        getNumbers().forEach(Number::printLine);
        System.out.println();
    }

    private List<Number> getNumbers() {
        List<Number> numberList = new ArrayList<>();
        if (this.request.getRequestedProperties().isEmpty()) {
            for (long i = this.request.getStart(); i < this.request.getStart() + this.request.getLength(); i++) {
                numberList.add(new Number(i));
            }
        } else {
            long i = this.request.getStart();
            while (numberList.size() < this.request.getLength()) {
                Number number = new Number(i);
                boolean correspondToRequest = true;
                for (SignedProperty pair: this.request.getRequestedProperties()) {
                    if (pair.isNegative() && number.getProperty(pair.getProperty()) ||
                            !(pair.isNegative() || number.getProperty(pair.getProperty()))) {
                        correspondToRequest = false;
                        break;
                    }
                }
                if (correspondToRequest) {
                    numberList.add(number);
                }
                i++;
            }
        }
        return numberList;
    }
}
 class Number {
    private final long number;
    private final List<Integer> digits;
    private final Map<Property, Boolean> properties;

    Number(long number) {
        this.number = number;
        this.digits = listDigits(this.number);
        this.properties = new HashMap<>();
        setProperties();
    }
    private void setProperties() {
        this.properties.put(Property.EVEN, isEven());
        this.properties.put(Property.ODD, !isEven());
        this.properties.put(Property.BUZZ, isBuzz());
        this.properties.put(Property.DUCK, isDuck());
        this.properties.put(Property.PALINDROMIC, isPalindromic());
        this.properties.put(Property.GAPFUL, isGapful());
        this.properties.put(Property.SPY, isSpy());
        this.properties.put(Property.SQUARE, isSquare(this.number));
        this.properties.put(Property.SUNNY, isSunny());
        this.properties.put(Property.JUMPING, isJumping());
        this.properties.put(Property.HAPPY, isHappy());
        this.properties.put(Property.SAD, !isHappy());
    }

    public boolean getProperty(Property property) {
        return this.properties.get(property);
    }

    private static List<Integer> listDigits(long number) {
        List<Integer> digits = new ArrayList<>();
        while (number > 0) {
            digits.add(0, (int) (number % 10));
            number /= 10;
        }
        return digits;
    }

    private static int getSumDigitsSquared(long number) {
        return listDigits(number).stream().map(x -> x * x).reduce(0, Integer::sum);
    }

    private List<Integer> getDigits() { return this.digits; }

    private int getNumberDigits() { return getDigits().size(); }

    private int getDigit(int fromLeft) { return getDigits().get(fromLeft); }

    private boolean isHappy() {
        List<Integer> visitedDigitsSquared = new ArrayList<>();
        int sumDigitsSquared = getSumDigitsSquared(this.number);
        while(!visitedDigitsSquared.contains(sumDigitsSquared)) {
            if (sumDigitsSquared == 1) {
                return true;
            }
            visitedDigitsSquared.add(sumDigitsSquared);
            sumDigitsSquared = getSumDigitsSquared(sumDigitsSquared);
        }
        return false;
    }

    private boolean isPalindromic() {
        int numDigits = getNumberDigits();
        for (int i = 0; i < numDigits / 2; i++) {
            int left = getDigit(i);
            int right = getDigit(numDigits - 1 - i);
            if (left != right) {
                return false;
            }
        }
        return true;
    }

    private boolean isJumping() {
        for (int i = 1; i < getNumberDigits(); i++) {
            int difference = Math.abs(getDigit(i) - getDigit(i - 1));
            if (difference != 1) {
                return false;
            }
        }
        return true;
    }
    private boolean isDuck() { return getDigits().contains(0); }

    private boolean isBuzz() { return this.number % 7 == 0 || this.number % 10 == 7; }

    private boolean isEven() {
        return this.number % 2 == 0;
    }

    private boolean isGapful() {
        if (getNumberDigits() < 3) {
            return false;
        }
        int gap = 10 * getDigit(0) + getDigit(getNumberDigits() - 1);
        return this.number % gap == 0;
    }

    private boolean isSpy() {
        int sumOfDigits = getDigits().stream().reduce(0, Integer::sum);
        int productOfDigits = getDigits().stream().reduce(1, (a, b) -> a * b);
        return sumOfDigits == productOfDigits;
    }

    private static boolean isSquare(long number) { return Math.sqrt(number) == Math.floor(Math.sqrt(number)); }

    private boolean isSunny() { return isSquare(this.number + 1); }

    public void printCard() {
        System.out.printf("Properties of %d\n", this.number);
        for (Map.Entry<Property, Boolean> entry: this.properties.entrySet()) {
            System.out.printf("%12s: %s\n", entry.getKey().getName().toLowerCase(), entry.getValue().toString());
        }
    }

    private List<String> getNamesPropertiesNumberHas() {
        return this.properties.entrySet().stream().filter(Map.Entry::getValue)
                .map(propertyBooleanEntry -> propertyBooleanEntry.getKey().getName().toLowerCase())
                .collect(Collectors.toList());
    }
    public void printLine() {
        System.out.printf("%16s is %s\n",
                NumberFormat.getIntegerInstance().format(this.number),
                String.join(", ", getNamesPropertiesNumberHas()));
    }
}
enum Message {

    FIRST("The first parameter should be a natural number or zero."),
    SECOND("The second parameter should be a natural number."),
    AVAILABLE(String.format("Available properties: [%s]", getAllProperties())),
    IS_WRONG("The property [%s] is wrong.\n"),
    ARE_WRONG("The properties [%s] are wrong.\n"),
    MUTUALLY("The request contains mutually exclusive properties: [%s, %s]\n"),
    NO_NUMBERS("There are no numbers with these properties.");

    String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    private static String getAllProperties() {
        List<String> names = Property.getAllProperties().stream().map(Property::getName).collect(Collectors.toList());
        return String.join(", ", names);
    }
}