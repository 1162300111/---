package cfAnalyze;

public class Test {
    public static void main(String[] args) {
        String aString = "ab cd ef";
        char[] aa = aString.toCharArray();
        for (int i = 0; i < aa.length; i++) {
            if (aa[i] != '\0') {
                System.out.println(aa[i]);
            }
        }

    }

}
