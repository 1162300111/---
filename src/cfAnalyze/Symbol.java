package cfAnalyze;

public class Symbol {
    public String symbol_name;//符号名称
    public int tag;//种别码

    public Symbol() {
    }

    public Symbol(String symbol_name, int tag) {
        this.symbol_name = symbol_name;
        this.tag = tag;
    }

    public String getSymbol_name() {
        return symbol_name;
    }

    public void setSymbol_name(String symbol_name) {
        this.symbol_name = symbol_name;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public static Symbol[] newSymbolRecord() {
        Symbol[] record = new Symbol[48];
        // 参照ppt，从6开始，1-5是标识符和常数
        //16，特别注意true和false  15 16
        String keyWords[] = { "int", "float", "char", "boolean", "string", "void", "while", "return",
                "do", "println", "if", "else", "main","record", "true", "false"};
        //23
        String operator[]= {"+", "-", "*","/", "!","&", "|","=", "<", ">",">=", "<=", "!=","==","+="
               ,"-=","*=","/=","?=","++","--","&&","||"};
        //9
        String separator[]= {"(", ")", "[","]", "{", "}", ";", ",","."};
        for (int a = 0; a < 16; a++) {
            record[a] = new Symbol(keyWords[a], a + 6);
        }
        for (int a = 16,i=0; a < (16+23); a++,i++) {	
            record[a] = new Symbol(operator[i], a + 6);
        }
        for (int a = 39,i=0; a < 48; a++,i++) {
            record[a] = new Symbol(separator[i], a + 6);
        }
        return record;
    }

    /*
     * 是否是关键字
     */
    public static int isKeyWord(String str) {
        Symbol[] record = newSymbolRecord();
        for (int i = 0; i < 16; i++) {
            if (str.equals(record[i].symbol_name)) {
                return record[i].tag;
            }
        }
        return -1;
    }

    /*
     * 是运算符,参数是char
     */
    public int isOper(char ch) {
        String str = String.valueOf(ch);
        Symbol[] record = newSymbolRecord();
        for (int i = 16; i < 39; i++) {
            if (str.equals(record[i].symbol_name)) {
                return record[i].tag;
            }
        }
        return -1;
    }
    
    /*
     * 是双运算符,参数是string
     */
    public int isOper(String ch) {
        String str = String.valueOf(ch);
        Symbol[] record = newSymbolRecord();
        for (int i = 16; i < 39; i++) {
            if (str.equals(record[i].symbol_name)) {
                return record[i].tag;
            }
        }
        return -1;
    }

    /*
     * 是分界符
     */
    public int isSeparator(char ch) {
        String str = String.valueOf(ch);
        Symbol[] record = newSymbolRecord();
        for (int i = 39; i < 48; i++) {
            if (str.equals(record[i].symbol_name)) {
                return record[i].tag;
            }
        }
        return -1;
    }

    public static void main(String agrs[]) {
        System.out.println(isKeyWord("true"));
    }

}
