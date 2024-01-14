import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Main {

    static enum TokenType {
        NUMBER,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        EOF
    };
    static TokenType[] OPERATOR_TOKENS = {
            TokenType.PLUS,
            TokenType.MINUS,
            TokenType.STAR,
            TokenType.SLASH
    };

    // HashSet?
    static String OPERATOR_CHARS = "+-*/";
    static String ROMAN_CHARS = "IVX";

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Input: ");
        String s = br.readLine();
        System.out.print("Output: ");
        System.out.print(Calc(s));
    }
    public static String Calc(String input) throws Exception {

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.getTokenized();
        if(tokens.size() < 2) {
            throw new Exception("Строка не является математической операцией");
        }

        int firstNum = Integer.parseInt(tokens.get(0).Value);
        String operator = tokens.get(1).Value;
        int secondNum = Integer.parseInt(tokens.get(2).Value);

        int answer = switch (operator) {
            case "+" -> firstNum + secondNum;
            case "-" -> firstNum - secondNum;
            case "*" -> firstNum * secondNum;
            case "/" -> firstNum / secondNum;
            default -> throw new Exception("Неизвестный оператор");
        };
        if(lexer.isRoman){
            if (answer < 1) {
                throw new Exception("В римской системе нет отрицательных чисел");
            }
            return toRoman(answer);
        }
        return answer + "";
    }

    // https://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java
    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }
    static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }
    static class Lexer {
        String input;
        int pos = 0;
        boolean isRoman;
        int length;
        List<Token> tokens;
        Lexer( String Input) {
            input = Input;
            length = Input.length();
            tokens = new ArrayList<>();

            // не придумал и не нашел способа лучше :<
            try {
                Integer.parseInt(Character.toString(Input.charAt(0)));
                isRoman = false;
            }
            catch (Exception E){
                isRoman = true;
            }
        }
        List<Token> getTokenized() throws Exception {
            while(pos < length) {
                char curr = take(0);

                if (OPERATOR_CHARS.indexOf(curr) != -1) {
                    placeToken(TokenType.NUMBER, Character.toString(curr));
                }
                else if(Character.isDigit(curr)) {
                    if(isRoman) {
                        throw new Exception("Используются одновременно разные системы счисления");
                    }
                    parseArabic(curr);
                }
                else if(ROMAN_CHARS.indexOf(curr) != -1) {
                    if(!isRoman) {
                        throw new Exception("Используются одновременно разные системы счисления");
                    }
                    parseRoman(curr);
                }

                if(tokens.size() > 3){
                    throw new Exception("Формат математической операции не удовлетворяет заданию - два операнда и один оператор (+, -, /, *)");
                }
                next();
            }
            return tokens;
        }
        char next() {
            pos++;
            return take(0);
        }
        char take(int relPos) {
            int lpos = pos + relPos;

            if (lpos >= length) {
                return '\0';
            }
            return input.charAt(lpos);
        }
        void placeToken(TokenType type, String value) {
            tokens.add(new Token(type, value));
        }
        void parseArabic(char curr) throws Exception {
            StringBuilder number = new StringBuilder();

            while(Character.isDigit(curr)) {
                number.append(curr);
                curr = next();
                if(curr == '.'){
                    throw new Exception("Калькулятор умеет работать только с целыми числами");
                }
            }
            String snum = number.toString();
            int inum = Integer.parseInt(snum);

            if(inum > 10 || inum < 1) {
                throw new Exception("На входе обноружено число не от 1 до 10 включительно");
            }

            placeToken(TokenType.NUMBER, snum);
        }
        void parseRoman(char curr) throws Exception {
            StringBuilder number = new StringBuilder();

            while(ROMAN_CHARS.indexOf(curr) != -1) {
                number.append(curr);
                curr = next();
            }
            String snum = number.toString();
            int inum = romanToInt(snum);

            if(inum > 10 || inum < 1) {
                throw new Exception("На входе обноружено число не от I до X включительно");
            }

            placeToken(TokenType.NUMBER, Integer.toString(inum));
        }
        static int romanToIntSingle(char letter) throws Exception {
            return switch (letter) {
                case 'X' -> 10;
                case 'V' -> 5;
                case 'I' -> 1;
                default -> throw new Exception("Неизвестный операнд");
            };
        }
        static int romanToInt(String roman) throws Exception {
            // https://rosettacode.org/wiki/Roman_numerals/Decode
            int result = 0;
            String uRoman = roman.toUpperCase();
            for (int i = 0; i < uRoman.length() - 1; i++) {
                if (romanToIntSingle(uRoman.charAt(i)) < romanToIntSingle(uRoman.charAt(i + 1))) {
                    result -= romanToIntSingle(uRoman.charAt(i));
                } else {
                    result += romanToIntSingle(uRoman.charAt(i));
                }
            }
            result += romanToIntSingle(uRoman.charAt(uRoman.length() - 1));
            return result;
        }
    }
    static class Token {
        public TokenType Type = TokenType.EOF;
        public String Value = "";
        public Token(TokenType type, String value) {
            Type = type;
            Value = value;
        }

        public String toString () {
            return "TOKEN: " + Type + " VALUE: " + Value;
        }
    }

}