package nablarch.test.support.tool;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * テーブル形式で記述されたデータのパーサ及び、その結果を格納するための
 * データオブジェクト。
 * @author Iwauo Tajima
 */
public class Table {
    /**
     * テーブル中の列の構造に関する情報を格納するクラス。
     * @author Iwauo Tajima
     */
    public static class Column {
        /** この列の開始位置 */
        private int startPos;
        /** この列の終了位置 */
        private int endPos;
        /** この列の名称 */
        private String name;

        /**
         * この列の名称を返す。
         * @return この列の名称
         */
        public String getName() {
            return name;
        }

        /**{@inheritDoc}
         * この列の名称を出力する。
         */
        public String toString() {
            return name;
        }
    }

    /**
     * このテーブル上の全ての行を返す。
     * @return このテーブル上の全ての行
     */
    public List<Map<String, Object>> rows() {
        return this.rows;
    }
    /** このテーブル上に定義されている全ての列。 */
    private List<Column> cols = new ArrayList<Column>();

    /**
     * このテーブル上の全ての列の構成情報を返す。
     * @return このテーブル上の全ての列の構成情報
     */
    public List<Column> cols() {
        return this.cols;
    }
    /** このテーブルに含まれている全ての行。 */
    private List<Map<String, Object>>
            rows = new ArrayList<Map<String, Object>>();

    /**
     * テーブルの文字列表現をパースしてインスタンスを生成する。
     * @param s テーブルの文字列表現
     */
    public Table(String s) {
        this(new Scanner(s));
    }

    /**
     * テーブルの文字列表現をパースしてインスタンスを生成する。
     * @param s スキャナ
     */
    private Table(Scanner s) {
        String line = null;
        String header = null;
        while (s.hasNextLine()) {
            line = s.nextLine();
            if (line.length() == 0) {
                continue;
            }
            if (header == null) {
                header = line;
                continue;
            }
            defineStructure(header, line);
            break;
        }
        // parses table rows
        while (s.hasNextLine()) {
            line = s.nextLine();
            this.addRow(line);
        }
    }

    /**
     * テーブルの列構造を決定する。
     * @param header    テーブルヘッダ文字列
     * @param separator ヘッダ部とボディ部の区切り文字列
     */
    private void defineStructure(String header, String separator) {
        Scanner s = new Scanner(separator);
        while (s.hasNext(BAR)) {
            s.next();
            Column column   = new Column();
            column.startPos = s.match().start();
            this.cols.add(column);
        }
        for (int i = 0; i < this.cols.size(); i++) {
            Column col = this.cols.get(i);
            col.endPos = (i == this.cols.size() - 1)
                    ? -1
                    : cols.get(i + 1).startPos - 1;
            col.name = (col.endPos == -1)
                    ? header.substring(col.startPos).trim()
                    : header.substring(col.startPos, col.endPos).trim();
        }
    }
    /** テーブルヘッダ部とボディ部の境界文字列  */
    private static final Pattern BAR = Pattern.compile("=+|-+");

    /**
     * 行データ文字列をこのテーブルに追加する。
     * @param line 行データ文字列
     */
    public void addRow(String line) {
        Map<String, Object> row = new HashMap<String, Object>();
        for (Column col : this.cols) {
            String val = (col.endPos == -1)
                    ? substring(line, col.startPos, line.length()).trim()
                    : substring(line, col.startPos, col.endPos).trim();
            row.put(col.name, Builder.valueOf(val));
        }
        this.rows.add(row);
    }

    /**
     * 部分文字列を抽出する。
     * ただし、全角文字列は半角文字2文字分とみなす。
     * @param str   抽出対象文字列
     * @param start 開始位置
     * @param end   終了位置
     * @return      抽出文字列
     */
    static String substring(String str, int start, int end) {
        char[] chars = str.toCharArray();
        StringBuilder buff = new StringBuilder(str.length());
        for (int i = 0, head = 0; head < end; i++, head++) {
            char c = chars[head];
            if (start <= i && (i < end || end == str.length())) {
                buff.append(c);
            }
            if (CHARSET_ZENKAKU.get(c)) {
                i++;
            }
        }
        return buff.toString();
    }

    /** 全角文字集合 */
    static final BitSet CHARSET_ZENKAKU = CharacterCheckerUtil.createCharSet(
            JapaneseCharsetUtil.getZenkakuAlphaChars()
            , JapaneseCharsetUtil.getZenkakuNumChars()
            , JapaneseCharsetUtil.getZenkakuGreekChars()
            , JapaneseCharsetUtil.getZenkakuRussianChars()
            , JapaneseCharsetUtil.getZenkakuHiraganaChars()
            , JapaneseCharsetUtil.getZenkakuKatakanaChars()
            , JapaneseCharsetUtil.getZenkakuKeisenChars()
            , JapaneseCharsetUtil.getLevel1Kanji()
            , JapaneseCharsetUtil.getLevel2Kanji()
    );
}
