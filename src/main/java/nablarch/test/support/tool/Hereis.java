package nablarch.test.support.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class
Hereis
{
    public static Process
    shell (Map<String, String> env, Object... embedParams)
    {
        String script = __string(JvmFrame.getCallerFrame(), 1, embedParams);
        return __shell(script, env);
    }
    
    public static Process
    __shell (String script, Map<String, String> env)
    {
        script = script.replaceAll("[\\r\\n]|\\r\\n", " ");
        File scriptFile = null;
        OutputStream ostream = null;
        try {
            scriptFile = File.createTempFile("nablarch_tool_Hereis_shell", ".bat");
            scriptFile.deleteOnExit();
            ostream = new FileOutputStream(scriptFile);
            ostream.write(script.getBytes());
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {
                ostream.close();
            } catch (IOException e) {
            }
        }

System.out.println(scriptFile.getAbsolutePath());
        String[] cmd = (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)
                     ? new String[] {"sh", scriptFile.getAbsolutePath()}
                     : new String[] {scriptFile.getAbsolutePath()};

        ProcessBuilder process = new ProcessBuilder(cmd);
        if (env != null) {
            process.environment().putAll(env);
        }
        process.environment().put(
            "CLASSPATH", System.getProperty("java.class.path")
        );
        process.redirectErrorStream(true);
        try {
            Process sh = process.start();
            return sh;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * このメソッドの呼び出しの直後に記述されたブロックコメントの内容を
     * 引数に指定されたパスにファイルとして保存し、そのファイルオブジェクトを返す。
     * <pre>
     * ファイルの内容はデフォルトエンコーディングで出力される。
     * </pre>
     * @param path ファイルの保存先となるパス
     * @param embedParams 埋め込みパラメータ
     * @return ファイルオブジェクト
     */
    public static File
    file (String path, Object... embedParams)
    {
        String contents = __string(JvmFrame.getCallerFrame(), 1, embedParams);
        return __file(path, null, contents, embedParams);
    }

    public static File
    fileWithEncoding (String path, String encoding, Object... embedParams)
    {
        String contents = __string(JvmFrame.getCallerFrame(), 2, embedParams);
        return __file(path, encoding, contents, embedParams);
    }

    static File
    __file (String path, String encoding, String contents, Object... embedParams)
    {
        assert path!=null && !(path.length() == 0);
        
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Charset charset = (encoding == null || encoding.length() == 0)
                        ? Charset.defaultCharset()
                        : Charset.forName(encoding);

        FileChannel out = null;
        
        try {
            out = new FileOutputStream(file, false).getChannel();
            out.write(charset.encode(contents));
        }
        catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            try { out.close(); } catch(Exception ex) {/* Nothing to do. */}
        }
        return file;
    }

    public static List<Map<String, Object>>
    table (Object... embedParams)
    {
        return __table(JvmFrame.getCallerFrame(), embedParams);
    }

    public static List<Map<String, Object>>
    __table (JvmFrame callerFrame, Object... embedParams)
    {
        String literal = __string(callerFrame, 1, embedParams);
        return new Table(literal).rows();
    }

    public static <K,V> Map<K,V>
    map (Class<K> keyType, Class<V> valType, Object... embedParams)
    {
        assert keyType != null;
        assert valType != null;

        String   literal = __string(JvmFrame.getCallerFrame(), 2, embedParams);
        String[] entries = literal.split("\n"); 

        Map<K,V> result = new HashMap<K,V>();

        for (String entry : entries) {
            Matcher m = entryTokenizer.matcher(entry);
            m.find();
            K key = Builder.valueOf(keyType, m.group(1).trim());
            V val = Builder.valueOf(valType, m.group(2).trim());
            result.put(key, val);
        }
        return result;
    }
    private static Pattern entryTokenizer = Pattern.compile (
        "([^:]*):(.*)"  //TODO too loose!!! must be fixed.
    );

    public static <T> List<T>
    list (Class<T> elementType, final Object... embedParams)
    {
        assert elementType != null;

        String   literal  = __string(JvmFrame.getCallerFrame(), 1, embedParams);
        String[] elements = literal.split("\n");

        List<T> result = new ArrayList<T>();
        for (String element : elements) {
            result.add(Builder.valueOf(elementType, element));
        }
        return result;
    }

    public static Pattern
    regex (Object... embedParams)
    {
        String pattern = __string(JvmFrame.getCallerFrame(), 0, embedParams);
        return Pattern.compile( pattern, Pattern.COMMENTS );
    }

    public static String
    string (Object... embedParams) 
    {
        return __string(JvmFrame.getCallerFrame(), 0, embedParams);
    }

    public static String
    __string (JvmFrame callerFrame, int embedParamsOffset, Object... embedParams) 
    {
        String string = Builder.join(callerFrame.followingComment());
        if (embedParams.length == 0) {
            return string;
        }
        List<String> args = callerFrame.argumentLiterals();
        for (int i=0; i < embedParams.length; i++) {
            String arg = args.get(i+embedParamsOffset);
            if (!arg.matches(javaIdentifier)) {
                throw new IllegalArgumentException(
                    "Embed parameter's name must be an Identifier. : " + arg
                );
            }
            Pattern placeHolder = Pattern.compile (
                "\\$"+arg+"(\\."+javaIdentifier+")*" + 
                "|"+
                "\\$\\{\\s*"+arg+"(\\."+javaIdentifier+")*\\s*\\}"
            );
            Matcher m = placeHolder.matcher(string);
            StringBuffer buffer = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(buffer, "");
                String property = (m.group(1) != null) ? m.group(1)
                                : (m.group(2) != null) ? m.group(2)
                                : null;
                buffer.append(evaluate(embedParams[i], property));
            }
            m.appendTail(buffer);
            string = buffer.toString();
        }
        return string;
    }
    final static String javaIdentifier = "[_a-zA-Z][_a-zA-Z0-9]*";

    static Object
    evaluate (Object ref, String property)
    {
        if (ref == null) return "";
        if (property == null) return ref;
        
        String[] nestedProp = property.replaceAll("^\\.", "").split("\\.");
        for (String prop : nestedProp) {
            ref = propertyOf(ref, prop);
        }
        return (ref instanceof List) ? Builder.join((List)ref, ", ")
                                     : ref.toString();
    }

    static Object
    propertyOf(Object ref, String prop)
    {
        if (ref instanceof Map) {
            Map map = (Map) ref;
            ref = map.get(prop);
        }
        return ref;
    }
}

