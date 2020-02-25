package cfAnalyze;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class cfAnalyze {
    
    private JTable jtable1;
    private JTable jtable2;
    private JTable jtable3;
    private String text;

    static ArrayList<SymbolTable> tablelist = new ArrayList<SymbolTable>();
    static Symbol symbol = new Symbol();
    static SymbolTable sym = new SymbolTable();

    public cfAnalyze(String text, JTable jtable1, JTable jtable2, JTable jtable3)
    {
        this.text = text;
        this.jtable1 = jtable1;
        this.jtable2 = jtable2;
        this.jtable3 = jtable3;
    }
    
    // 主程序
    public  void analyse(){
        String[] texts = text.split("\n");        
        String result = "C:\\Users\\HP\\Desktop\\编译系统\\compiler\\src\\cfAnalyze\\result.txt";

        Stack<String> stack1 = new Stack<String>(); // put in {,if } pop
        Stack<String> stack2 = new Stack<String>(); // put in (,if ) pop
        Stack<String> stack3 = new Stack<String>(); // put in [,if ] pop

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(result));

            String line = "";
            int count = 0;// 测试代码行计数
            boolean ifNote = false;// 注释
            int begin = 0, end = 0;
            String note = "";

            for(int m = 0; m < texts.length; m++) {
                count++;            
//                output.write(String.format("Line " + count + ":\n"));
                line=texts[m];
/** 处理空行******************************************************************************/
                // ^是行开始，\\s*代表任意个空格，$代表行结束
                if (line.matches("^\\s*$")) {
                    continue;
                }
                
/** 处理注释******************************************************************************/
                if (!ifNote) {
                    begin = line.indexOf("/*");// 返回/*在字符串中首次出现的位置
                    ifNote = false;
                    end = line.lastIndexOf("*/");
                    if (begin > -1) {
                        if (end > -1 && end != begin) {// 在同一行出现
                            note = line.substring(begin, end + 2);
                            line = line.substring(0, begin) + line.substring(end + 2, line.length());
                            System.out.println("注释是：" + note);
                            output.write(String.format("注释是：" + note) + System.getProperty("line.separator"));
                            note = "";
                            ifNote = false;
                        } else {// 不在同一行
                            ifNote = true;
                            note = line.substring(begin, line.length());
                            line = line.substring(0, begin);

                        }
                    }
                } else if (ifNote) {
                    end = line.indexOf("*/");
                    if (end > -1) {// 如果下一行注释封闭
                        ifNote = false;
                        note += line.substring(0, end + 2);
                        line = line.substring(end + 2, line.length());
                        System.out.println("注释是：" + note);
                        output.write(String.format("注释是：" + note) + System.getProperty("line.separator"));
                        note = "";
                    } else {// 多行注释
                        note += line;
                        if(m==texts.length-1) {
                            DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                            tableModel2.addRow(new Object[] {m+1, "注释未封闭"});
                            jtable2.invalidate();  
                        }
                        continue;
                    }                    
                }

                char[] strLine = line.toCharArray();// 将字符串转化为字符串数组
                

                for (int i = 0; i < strLine.length; i++) {
                    String tmpToken = "";
                    char ch = strLine[i];

/** 处理关键字or标识符******************************************************************************/
                    if (isAlpha(ch)) {
                        while ((isAlpha(ch) || isDigit(ch)) && ch != '\0') {// 字符串的结束符号
                            tmpToken += ch;
                            i++;
                            if (i == strLine.length) {
                                break;
                            }
                            ch = strLine[i];
                        } // 得到完整的字符串
                        
                        int pos = Symbol.isKeyWord(tmpToken);
                        

                        if (pos >= 0 && pos != 20 && pos != 21) { // 是关键字
                            DefaultTableModel tableModel = (DefaultTableModel) jtable1.getModel();
                            tableModel.addRow(new Object[] {tmpToken,  pos,"---", count});
                            jtable1.invalidate();
                            
                            output.write(String.format(tmpToken + "   <" + pos + ",--->")
                                    + System.getProperty("line.separator"));

                        } else if (pos == 20 || pos == 21) {// 是true或false
                            if (pos == 20) {
                                DefaultTableModel tableModel = (DefaultTableModel) jtable1.getModel();
                                tableModel.addRow(new Object[] {tmpToken, pos, "1", count});
                                jtable1.invalidate();
                                
                                output.write(String.format(tmpToken + "   <" + pos + ",1>")
                                        + System.getProperty("line.separator"));
                                
                            } else {
                                DefaultTableModel tableModel = (DefaultTableModel) jtable1.getModel();
                                tableModel.addRow(new Object[] {tmpToken,  pos, "0",count});
                                jtable1.invalidate();
                                
                                output.write(String.format(tmpToken + "   <" + pos + ",0")
                                        + System.getProperty("line.separator"));
                                
                            }
                        }
                        else {// 标识符
                            //如果符号表为空或符号表中不包含当前token，则加入
                            if(tablelist.isEmpty() || !isExist(tmpToken)){
                                SymbolTable new_sym = new SymbolTable(tablelist.size(), 1, tmpToken);
                                tablelist.add(new_sym);
                                
                                DefaultTableModel tableModel3 = (DefaultTableModel) jtable3.getModel();
                                tableModel3.addRow(new Object[] {tmpToken, tablelist.size() - 1});
                                jtable3.invalidate();                             
                            }
                            output.write(String.format(tmpToken + "    <1," + tmpToken + ">")
                                    + System.getProperty("line.separator"));
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, "1",tmpToken, count});
                            jtable1.invalidate();

                        }
                        i--;
                        tmpToken = "";

                    } else if (isDigit(ch)) {

/** 处理数字 **************************************************************/
                        //初始化进入1状态
                        int state = 1;
                        //声明计数变量
                        int k;
                        Boolean isfloat = false;  
                        while ( (ch != '\0') && (isDigit(ch) || ch == '.' || ch == 'e' || ch == '-'))
                        {
                            if (ch == '.' || ch == 'e')  
                              isfloat = true;
                              
                            for (k = 0; k <= 6; k++) 
                            {  
                                char tmpstr[] = digitDFA[state].toCharArray();  
                                if (ch != '#' && 1 == in_digitDFA(ch, tmpstr[k])) 
                                {  
                                    tmpToken += ch;  
                                    state = k;  
                                    break;  
                                }  
                            }
                            if (k > 6) break;
                            //遍历符号先前移动
                            i++;
                            if(i>=strLine.length) break;  
                            ch = strLine[i]; 
                        }
                        Boolean haveMistake = false;  
                        
                        if (state == 2 || state == 4 || state == 5) 
                        {  
                            haveMistake = true;  
                        } 
                        
                        else//1,3,6  
                        {  
                            if (((symbol.isOper(ch)==0) || ch == '.') && !isDigit(ch))  
                                haveMistake = true;  
                        }  
                        
                        //错误处理 
                        if (haveMistake)  
                        {  
                            //一直到“可分割”的字符结束  
                            while (ch != '\0' && ch != ',' && ch != ';' && ch != ' ')
                            {  
                                tmpToken += ch;  
                                i++;
                                if(i >= strLine.length) break;  
                                ch = strLine[i];  
                            }  
                            DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                            tableModel2.addRow(new Object[] {count, tmpToken + " 确认无符号常数输入正确"});
                            jtable2.invalidate();
                        }
                        else 
                        {  
                            if (isfloat) 
                            {   
                                output.write(String.format(tmpToken + "    <3," + tmpToken + ">")
                                        + System.getProperty("line.separator"));
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken,  "3", tmpToken,count});
                                jtable1.invalidate();    
                            } 
                            else
                            {  
                                output.write(String.format(tmpToken + "    <2," + tmpToken + ">")
                                        + System.getProperty("line.separator"));
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken, "2",tmpToken, count});
                                jtable1.invalidate();   
                            }  
                        }
                        i--;
                        tmpToken = "";                       
                        
                    } else if (ch == '\'') {
/** 处理单引号内的字符常量 **************************************************************/
                        boolean RightChar = false;// 是否为完整字符
                        for (int x = i + 1; x < strLine.length; x++) {
                            ch = strLine[x];
                            if (ch == '\'') {
                                RightChar = true;
                                break;
                            }
                            tmpToken += ch;
                            i = x + 1;
                        }
                        if (RightChar) {
                            if (tmpToken.length() > 1 || tmpToken.length() == 0) {
                                
                                DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                                tableModel2.addRow(new Object[] {count, tmpToken + " 字符长度只能是1!"});
                                jtable2.invalidate();  
                            } else {
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken,  "4",tmpToken, i});
                                jtable1.invalidate();  
                                
                                // valuelist.add(new value_table(valuelist.size(),tmpToken,"char"));
                                 output.write(String.format(tmpToken+"    <4,"+tmpToken+">")+System.getProperty("line.separator"));
                                
                            }
                        } else {
                           
                            DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                            tableModel2.addRow(new Object[] {count, tmpToken + " 字符符号不封闭"});
                            jtable2.invalidate();  
                            // output.write(String.format("字符符号不封闭->"+tmpToken)+System.getProperty("line.separator"));
                        }

                    } else if (ch == '"') {
/** 处理双引号内的字符串 **************************************************************/
                        boolean RightChars = false;
                        for (int x = i + 1; x < strLine.length; x++) {
                            ch = strLine[x];
                            if (ch == '"') {
                                RightChars = true;
                                break;
                            }
                            tmpToken += ch;
                            i = x + 1;
                        }
                        if (RightChars) {
                            // valuelist.add(new value_table(valuelist.size(),tmpToken));
                            output.write(String.format("\"" + tmpToken + "\"" + "     <5,"+tmpToken+">")
                                    + System.getProperty("line.separator"));
                         
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken,"5",tmpToken, i});
                            jtable1.invalidate();  
                        } else {

                            DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                            tableModel2.addRow(new Object[] {i, tmpToken + " 字符串常量引号未封闭"});
                            jtable2.invalidate();  
                            
                        }
                        tmpToken = "";
                    } else if (ch == '/') {
/** 处理/or//or/* **************************************************************/
                        tmpToken += ch;
                        i++;
                        if (i == strLine.length) {
                            break;
                        }
                        ch = strLine[i];
                        if (ch != '/' && ch != '*') {// 如果是除号
                            if (ch == '=') {// 是/=
                                tmpToken += ch;

                                output.write(String.format(tmpToken + "      <" + symbol.isOper(tmpToken) + ",--->")
                                        + System.getProperty("line.separator"));
                                
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken,symbol.isOper(tmpToken), "---",count});
                                jtable1.invalidate();  
                                
                            } else {// 是/
                                i--;

                                output.write(String.format(tmpToken + "     <" + symbol.isOper('/') + ",--->")
                                        + System.getProperty("line.separator"));
                                
                                
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken, symbol.isOper(tmpToken), "---",count});
                                jtable1.invalidate();  
                            }

                        } else {// // or /*
                                
                            Boolean haveMistake = false;
                            int State = 0;
                            if (ch == '/') {
                              //单行注释读取所有字符
                                i++;
                                System.out.println(line.substring(i - 2) + "   是注释");                                
                                i = strLine.length;
                            }else if (ch == '*') {
                                tmpToken += ch;  
                                int state = 2;  

                                while (state != 4) 
                                {  
                                    i++;
                                    if(i>=strLine.length) break;  
                                    ch = strLine[i];
                                    
                                    if (ch == '\0') {  
                                        haveMistake = true;  
                                        break;  
                                    }  
                                    for (int k = 2; k <= 4; k++) {  
                                        char tmpstr[] = noteDFA[state].toCharArray();  
                                        if (in_noteDFA(ch, tmpstr[k], state)) {  
                                            tmpToken += ch;  
                                            state = k;  
                                            break;  
                                        }  
                                    }  
                                }
                                State = state;
                            }
                            if(haveMistake || State != 4)
                            {
                                DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                                tableModel2.addRow(new Object[] {m+1, "注释未封闭"});
                                jtable2.invalidate();  
                                --i;
                            }
                        }
                        tmpToken = "";
                    } else if (symbol.isOper(ch) > 0||symbol.isSeparator(ch)>0) {
/** 处理 运算符 和 界符 **************************************************************/
                        if ((ch == '+' || ch == '-' || ch == '*' || ch == '=' || ch == '<'
                                || ch == '>' || ch == '!' || ch == '?') && (strLine[i + 1] == '=')) {
                            // 后面可以有一个=
                            tmpToken += ch;
                            i++;
                            if (i >= strLine.length) {
                                break;
                            }
                            ch = strLine[i];
                            tmpToken += ch;
                            output.write(String.format(tmpToken + "         <" + symbol.isOper(tmpToken) + ",--->")
                                    + System.getProperty("line.separator"));
                            
                           
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, symbol.isOper(tmpToken),"---", count});
                            jtable1.invalidate();  
                            
                        } else if ((ch == '+' || ch == '-' || ch == '&' || ch == '|') && strLine[i + 1] == ch) {
                            // 可以连续两个运算符一样
                            tmpToken += ch;
                            i++;
                            if (i >= strLine.length) {
                                break;
                            }
                            ch = strLine[i];
                            tmpToken += ch;

                            output.write(String.format(tmpToken + "         <" + symbol.isOper(tmpToken) + ",--->")
                                    + System.getProperty("line.separator"));
                            
                           
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, symbol.isOper(tmpToken), "---",count});
                            jtable1.invalidate();  
                        } else if (ch == '{') {
                            tmpToken += ch;
                            stack1.push(String.valueOf(ch));// 压入栈1
                           
                            output.write(String.format(tmpToken + "         <" + symbol.isSeparator(ch) + ",--->")
                                    + System.getProperty("line.separator"));
                           
                            
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, symbol.isSeparator(ch), "---",count});
                            jtable1.invalidate();  
                            
                        } else if (ch == '}') {
                            tmpToken += ch;
                            if (!stack1.empty()) {
                                stack1.pop();// 把{弹出栈
                               
                                output.write(String.format(tmpToken + "        <" + symbol.isSeparator(ch) + ",--->")
                                        + System.getProperty("line.separator"));
                                
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken, symbol.isSeparator(ch), "---",count});
                                jtable1.invalidate();  
                            } else if (stack1.empty()) {
                                
                                DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                                tableModel2.addRow(new Object[] {count, tmpToken + " 这个符号或之前的这个没有起始！"});
                                jtable2.invalidate();  
                            }

                        } else if (ch == '(') {
                            tmpToken += ch;
                            stack2.push(String.valueOf(ch));
                            
                            output.write(String.format(tmpToken + "         <" + symbol.isSeparator(ch) + ",--->")
                                    + System.getProperty("line.separator"));
                            
                            
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, symbol.isSeparator(ch), "---",count});
                            jtable1.invalidate();  
                            
                        } else if (ch == ')') {
                            tmpToken += ch;
                            if (!stack2.empty()) {
                                stack2.pop();
                                
                                output.write(String.format(tmpToken + "         <" + symbol.isSeparator(ch) + ",--->")
                                        + System.getProperty("line.separator"));
                                
                               
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken, symbol.isSeparator(ch),"---", count});
                                jtable1.invalidate();  
                            } else if (stack2.empty()) {
                                
                                
                                DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                                tableModel2.addRow(new Object[] {count, tmpToken + " 这个符号或之前的这个没有起始！"});
                                jtable2.invalidate();  

                            }

                        } else if (ch == '[') {
                            tmpToken += ch;
                            stack3.push(String.valueOf(ch));
                            
                            output.write(String.format(tmpToken + "         <" + symbol.isSeparator(ch) + ",--->")
                                    + System.getProperty("line.separator"));
                            
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken,symbol.isSeparator(ch),"---", count});
                            jtable1.invalidate();  
                        } else if (ch == ']') {
                            tmpToken += ch;
                            if (!stack3.empty()) {
                                stack3.pop();
                                
                                output.write(String.format(tmpToken + "         <" + symbol.isSeparator(ch) + ",--->")
                                        + System.getProperty("line.separator"));
                                
                                DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                                tableModel1.addRow(new Object[] {tmpToken,  symbol.isSeparator(ch),"---", count});
                                jtable1.invalidate();  
                            } else if (stack3.empty()) {

                                DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                                tableModel2.addRow(new Object[] {count, tmpToken + " 这个符号或之前的这个没有起始！"});
                                jtable2.invalidate();  
                            }
                        } else if (ch == ',') {
                            tmpToken += ch;
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken,  symbol.isSeparator(ch),"---", count});
                            jtable1.invalidate();  
                            
                        }else if (ch == ';') {
                            tmpToken += ch;
                            //TODO 缺少
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken, symbol.isSeparator(ch),"---", count});
                            jtable1.invalidate();  
                        }
                        else {
                            tmpToken += ch;
                           
                            output.write(String.format(tmpToken + "         <" + symbol.isOper(ch) + ",--->")
                                    + System.getProperty("line.separator"));
                         
                            DefaultTableModel tableModel1 = (DefaultTableModel) jtable1.getModel();
                            tableModel1.addRow(new Object[] {tmpToken,  symbol.isOper(ch), "---",count});
                            jtable1.invalidate();  
                        }

                    } else {
                        if(ch != ' ' && ch != '\t' && ch != '\0' && ch != '\n' && ch != '\r')  
                        {  
                            DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                            tableModel2.addRow(new Object[] {m+1, "存在不合法字符"});
                            jtable2.invalidate();
                            System.out.println(ch);
                        }  
                    }
                }
                output.write(System.getProperty("line.separator"));
            }
            
/** 处理异常******************************************************************************/
            if (!stack1.empty()) {
                while (!stack1.empty()) {
                    String top = stack1.pop();
                    DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                    tableModel2.addRow(new Object[] {stack1.size(), top + " 这个符号没有结束符"});
                    jtable2.invalidate();  
                }
            }
            if (!stack2.empty()) {
                while (!stack2.empty()) {
                    String top = stack2.pop();
                    DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                    tableModel2.addRow(new Object[] {stack2.size(), top + " 这个符号没有结束符"});
                    jtable2.invalidate();  
                }
            }
            if (!stack3.empty()) {
                while (!stack3.empty()) {
                    String top = stack3.pop();
                    DefaultTableModel tableModel2 = (DefaultTableModel) jtable2.getModel();
                    tableModel2.addRow(new Object[] {stack3.size(), top + " 这个符号没有结束符"});
                    jtable2.invalidate();  
                }
            }            

            output.flush();

            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 识别数字的DFA
    public static String digitDFA[] = { 
            "#d#####", 
            "#d.#e##", 
            "###d###", 
            "###de##",  
            "#####-d", 
            "######d", 
            "######d" };
    
        //判断输入符号是否符合状态机
        public static int in_digitDFA(char ch, char test){  
            if (test == 'd') {  
                if (isDigit(ch))  
                    return 1;  
                else  
                    return 0;  
            }  
            else
            {
                if (ch == test)
                    return 1;
                else
                    return 0;
            }
        }
        public static Boolean isAlpha(char ch) { // 字符
            return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_');
        }

        public static Boolean isDigit(char ch) { // 数字
            return (ch >= '0' && ch <= '9');
        }
        public static boolean isExist(String str) {
            for (int i = 0; i < tablelist.size(); i++) {
                if (tablelist.get(i).sym_name.equals(str)) {
                    return true;
                }
            }
            return false;
        }
        //多行注释DFA
        public static String noteDFA[] = { 
            "#####", 
            "##*##", 
            "##c*#", 
            "##c*/", 
            "#####" };
        
        public static Boolean in_noteDFA(char ch, char nD, int s) {  
            if (s == 2) {  
                if (nD == 'c') 
                {  
                    if (ch != '*') return true;  
                    else return false;  
                }  
            }  
            if (s == 3) {  
                if (nD == 'c') {  
                    if (ch != '*' && ch != '/') return true;  
                    else return false;  
                }  
            }  
            return (ch == nD) ? true : false;  
        }
}
