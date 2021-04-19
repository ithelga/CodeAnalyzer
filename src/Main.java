import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;


public class Main {

    private static String C_ANY = ">";
    private static String C_LEN_MORE_ZERO = ">0";
    private static String C_SPACE = "<SPACE>";
    private static String C_SEMICOLON = ";";

    private static String[] V_MAIN = {"public", "static", "void", "main", "(", "String", "[", "]", C_LEN_MORE_ZERO, ")"};

    private static String[] M_PRINT = {"System", ".", "out", ".", "print", "(", C_ANY, ")", C_SEMICOLON};
    private static String[] M_PRINTLN = {"System", ".", "out", ".", "println", "(", C_ANY, ")", C_SEMICOLON};
    private static String[] M_NANOTIME = {"System", ".", "nanoTime", "(", ")", C_SEMICOLON};
    private static String[] M_TIMEMILLIS = {"System", ".", "currentTimeMillis", "(", ")", C_SEMICOLON};

    private static String[] SYMBOLS = new String[]{"(", ")", "{", "}", "[", "]", ".", ";"};
    private static String[] BRACKETS = new String[]{"(", ")", "{", "}", "[", "]"};

    public static void main(String[] args) throws Exception {

        System.out.println("\nГрищенкова А.А. Давыдов М.С. Тетенева О.А. ПИ19-1в\n");

        // Чтение файла в StringBuilder
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\otete\\IdeaProjects\\CodeAnalyzer\\src\\java.txt"));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = br.readLine()) != null) builder.append(line);

        System.out.println("# Данные из файла.\n" + builder);

        // Меняем пробелы которые в кавычках на <SPACE>, чтобы при split игнорировать их
        boolean inText = false;
        for (int i = 0; i < builder.length(); i++) {
            if (builder.charAt(i) == '"') inText = !inText;
            if (inText && builder.charAt(i) == ' ') builder.replace(i, i + 1, C_SPACE);
        }

        // Преобразуем StringBuilder в String
        String codeStr = builder.toString();
        // Меняем каждый символ из массива SYMBOLS на тот же символ и пробел с каждой стороны,
        // чтобы гарантировать
        for (String s : SYMBOLS) codeStr = codeStr.replaceAll("\\" + s + "{1}", " " + s + " ");
        // Убираем все не одинарные пробелы, результатом чего можем гарантировать наличие ровно одного пробела
        // с каждой стороны рядом с символом для дальнейшего split
        codeStr = codeStr.replaceAll(" {2,}", " ");

        // Делаем split строки и получаем массив
        String[] codeArr = codeStr.split(" ");
        // Преобразуем обратно <SPACE> в пробелы в элементах массива
        for (int i = 0; i < codeArr.length; i++) codeArr[i] = codeArr[i].replaceAll("\\" + C_SPACE + "{1}", " ");

        System.out.println("# Преобразованный и упакованный в массив код.\n" + Arrays.toString(codeArr));

        boolean bracketsIsCorrect = checkBrackets(codeArr);
        boolean methodsIsCorrect = checkMethods(codeArr, M_PRINTLN, M_PRINT, M_NANOTIME, M_TIMEMILLIS);
        boolean mainIsCorrect = checkMain(codeArr);

        System.out.println("# Скобки расставлены " + (bracketsIsCorrect ? "":"НЕ") + "верно.");
        System.out.println("# Методы написаны " + (methodsIsCorrect ? "":"НЕ") + "правильно.");
        System.out.println("# Точка входа " + (mainIsCorrect ? "":"НЕ ") + "найдена.");

        if (bracketsIsCorrect && mainIsCorrect && methodsIsCorrect) {
            System.out.println("# Компиляция прошла успешно.");
            runProgram(codeArr, M_PRINTLN, M_PRINT, M_NANOTIME, M_TIMEMILLIS);
        }
        else System.out.println("# Ошибка компиляции, выполнение невозможно.");
    }

    private static boolean checkBrackets(String[] codeArr) {
        int[] bracketsCount = new int[BRACKETS.length / 2];
        for (String code : codeArr) {
            for (int i = 0; i < BRACKETS.length; i += 2) {
                if (code.equals(BRACKETS[i])) bracketsCount[i / 2]++;
                if (code.equals(BRACKETS[i + 1])) {
                    bracketsCount[i / 2]--;
                    if (bracketsCount[i / 2] < 0) {
                        System.out.println("* * * Ошибка! Скобка " + BRACKETS[i] + " закрыта до открытия! * * *");
                        return false;
                    }
                }
            }
        }
        for(int count : bracketsCount) {
            if (count != 0) {
                System.out.println("* * * Ошибка! Не все скобки закрыты! * * *");
                return false;
            }
        }
        return true;
    }

    private static boolean checkMain(String[] codeArr) {
        int i = 0;
        for (String code : codeArr) {
            String methodItem = V_MAIN[i];
            if (i == 0 && methodItem.equals(code)) i++;
            else if (i > 0) {
                if (methodItem.equals(C_ANY)) i++;
                else if (methodItem.equals(C_LEN_MORE_ZERO) && code.length() > 0) i++;
                else if (methodItem.equals(code)) i++;
                else i = 0;
            }
            if (i == V_MAIN.length) return true;
        }
        System.out.println("* * * Ошибка! Точка входа не обнаружена! * * *");
        return false;
    }

    private static boolean checkMethods(String[] codeArr, String[]... methods) {
        int[] m = new int[methods.length];
        for (String code : codeArr) {
            for (int i = 0; i < methods.length; i++) {
                String methodItem = methods[i][m[i]];
                if (m[i] == 0 && methodItem.equals(code)) m[i]++;
                else if (m[i] > 0) {
                    if (methodItem.equals(C_ANY)) m[i]++;
                    else if (methodItem.equals(C_LEN_MORE_ZERO) && code.length() > 0) m[i]++;
                    else if (methodItem.equals(code)) m[i]++;
                    else if (methodItem.equals(C_SEMICOLON)) {
                        System.out.println("* * * Ошибка! Отсутствует точка с запятой! * * *");
                        return false;
                    }
                    else m[i] = 0;
                }
                if (m[i] == methods[i].length) m[i] = 0;
            }
        }
        return true;
    }

    private static void runProgram(String[] codeArr, String[]... methods) {
        System.out.println("# Запуск программы.\n");

        int[] m = new int[methods.length];
        String[] args = new String[methods.length];

        for (String code : codeArr) {
            for (int i = 0; i < methods.length; i++) {
                String methodItem = methods[i][m[i]];

                if (m[i] == 0 && methodItem.equals(code)) m[i]++;
                else if (m[i] > 0) {
                    if (methodItem.equals(C_ANY)) {
                        args[i] = code;
                        if (args[i].startsWith("\"") && args[i].endsWith("\""))
                            args[i] = args[i].substring(1, args[i].length() - 1);
                        m[i]++;
                    }
                    else if (methodItem.equals(C_LEN_MORE_ZERO) && code.length() > 0) m[i]++;
                    else if (methodItem.equals(code)) m[i]++;
                    else m[i] = 0;
                }

                if (m[i] == methods[i].length) {
                    if (methods[i] == M_PRINT) System.out.print(args[i]);
                    else if (methods[i] == M_PRINTLN) System.out.println(args[i]);
                    else if (methods[i] == M_NANOTIME) System.out.println("Debug <System.nanoTime()>: " + System.nanoTime());
                    else if (methods[i] == M_TIMEMILLIS) System.out.println("Debug <System.currentTimeMillis()>: " + System.currentTimeMillis());
                    m[i] = 0;
                }
            }
        }
    }
}