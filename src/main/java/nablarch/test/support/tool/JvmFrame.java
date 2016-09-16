package nablarch.test.support.tool;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nablarch.test.support.tool.Builder.join;

public class
        JvmFrame {

    public static JvmFrame
    getCallerFrame() {
        return new JvmFrame(Thread.currentThread().getStackTrace(), 3);
    }

    private StackTraceElement[] frameStack;

    private int pos;

    JvmFrame(StackTraceElement[] frameStack, int pos) {
        this.frameStack = frameStack;
        this.pos = pos;
    }

    public JvmFrame
    callee() {
        return new JvmFrame(this.frameStack, pos - 1);
    }

    public JvmFrame
    caller() {
        return new JvmFrame(this.frameStack, pos + 1);
    }

    public String
    className() {
        return frameStack[pos].getClassName();
    }

    public String
    methodName() {
        return frameStack[pos].getMethodName();
    }

    public int
    lineNumber() {
        return frameStack[pos].getLineNumber();
    }

    public String
    sourceFileName() {
        return frameStack[pos].getFileName();
    }

    public List<String>
    argumentLiterals() {

        String calleeMethod = this.callee().methodName();
        List<String> src = this.sourceCode(20);
        List<String> result = new ArrayList<String>();

        Matcher methodCall = Pattern.compile(
                calleeMethod + "\\s*\\(\\s*(.*?)\\)+\\s*;", Pattern.DOTALL
        ).matcher(join(src, "\n"));

        if (!methodCall.find()) {
            return result;
        }
        String arguments = methodCall.group(1);
        Matcher eachArg = Pattern.compile(
                "( \"[^\"]*\" | [^,()] )*  ([,()]|$)",
                Pattern.COMMENTS | Pattern.DOTALL
        ).matcher(arguments);

        int parenthesesNests = 0;
        StringBuilder arg = new StringBuilder();

        while (eachArg.find()) {
            arg.append(eachArg.group());
            char separator = arguments.charAt(eachArg.end() - 1);
            switch (separator) {
                case '(':
                    parenthesesNests++;
                    break;
                case ')':
                    parenthesesNests--;
                    break;
                case ',':
                default:
                    if (parenthesesNests == 0) {
                        String str = arg.toString().replaceAll("[,]$", "")
                                .trim();
                        if (!(str.length() == 0)) {
                            result.add(str);
                        }
                        arg = new StringBuilder();
                        break;
                    }
            }
        }
        if (arg.length() > 0) {
            result.add(arg.toString().trim());
        }
        return result;
    }

    public List<String>
    sourceCode() {
        String className = this.className();
        if (!srcCache.keySet().contains(className)) {
            srcCache.put(className, readSource(className));
        }
        return srcCache.get(className);
    }

    static Map<String, List<String>>
            srcCache = new HashMap<String, List<String>>(30);

    public List<String>
    sourceCode(int offset) {
        int from = this.lineNumber();
        int to = from + offset;
        return this.sourceCode(from, to);
    }

    public List<String>
    sourceCode(int from, int to) {
        List<String> src = this.sourceCode();
        to = Math.min(to, src.size());
        return src.subList(from - 1, to - 1);
    }

    public List<String>
    followingComment() {
        List<String> sourceCode = this.sourceCode(5000);
        List<String> comment = new ArrayList<String>();

        int startColumn = -1;
        int endColumn = -1;

        for (String line : sourceCode) {
            if (startColumn < 0) {
                Matcher beginning = commentBeginningMark.matcher(line);
                if (beginning.find()) {
                    startColumn = beginning.start();
                }
                continue;
            }

            Matcher ending = commentEndingMark.matcher(line);
            if (ending.find()) {
                endColumn = ending.start();
            }

            if (endColumn >= 0) { // reached the last line
                line = line.substring(0, endColumn);
                //if (line.trim().length() > 0)
                comment.add(line);
                break;
            }
            comment.add(line);
        }

        int headColumn = (startColumn < endColumn) ? startColumn
                : endColumn;
        for (int i = 0; i < comment.size(); i++) {
            String line = comment.get(i);
            line = line.length() <= headColumn
                    ? ""
                    : line.substring(headColumn, line.length());
            comment.set(i, line);
        }
        return comment;
    }

    static final Pattern commentBeginningMark = Pattern.compile("/\\*+");

    static final Pattern commentEndingMark = Pattern.compile("\\*+/");

    static List<String>
    readSource(String fqn) {
        List<String> lines = new ArrayList<String>(500);
        String path = fqn.replace('.', '/')
                .replaceFirst("\\$[0-9a-zA-Z\\$]+$", "") + ".java";
        ClassLoader loader;
        try {
            loader = Class.forName(fqn).getClassLoader();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        InputStream src = loader.getResourceAsStream(path);
        if (src == null) {
            throw new RuntimeException(
                    "Couldn't find the source file of the class: " + path +
                            "  (A Java source file which uses `SimpleSpec` or `Hereis`"
                            +
                            " must be placed in CLASSPATH.)"
            );
        }
        Scanner reader = new Scanner(src);
        while (reader.hasNext()) {
            lines.add(reader.nextLine());
        }
        return lines;
    }
}

