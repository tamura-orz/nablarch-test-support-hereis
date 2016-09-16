package nablarch.test.support.tool;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * JavaのMS932 CharsetDecoderを使用して、特定の文字集合の全文字を含む文字列を取得するユーティリティ。
 * 
 * 
 * @author Koichi Asano
 *
 */
public final class JapaneseCharsetUtil {

    /**
     * 使用するエンコーディング名。
     */
    private static final String MS932_NAME = "MS932";

    /**
     * UNICODE変換に失敗した文字。
     */
    private static final int UNICODE_REPLACEMENT_CHARACTER = 0xFFFD;
    /**
     * ASCIIの開始位置。
     */
    private static final int ASCII_CHAR_START = 0x20;
    /**
     * ASCIIの終了位置。
     */
    private static final int ASCII_CHAR_END = 0x7E;
    /**
     * 半角カナの開始位置。
     */
    private static final int HANKANA_CHAR_START = 0xA0;
    /**
     * 半角カナの終了位置。
     */
    private static final int HANKANA_CHAR_END = 0xDF;
    /**
     * JIS第1～2区の記号文字の開始位置。
     */
    private static final int JIS_SYMBOL_START = 0x8140;
    /**
     * JIS第1～2区の記号文字の終了位置。
     */
    private static final int JIS_SYMBOL_END = 0x81FC;
    
    /**
     * JIS第13区のNEC 特殊文字の開始位置。
     */
    private static final int NEC_SYMBOL_START = 0x8740;
    
    /**
     * JIS第13区のNEC 特殊文字の終了位置。
     */
    private static final int NEC_SYMBOL_END = 0x879E;
    
    /**
     * 全角数字の開始位置。
     */
    private static final int ZENKAKU_NUM_START = 0x824F;
    /**
     * 全角数字の終了位置。
     */
    private static final int ZENKAKU_NUM_END = 0x8259;
    /**
     * 全角英字の開始位置。
     */
    private static final int ZENKAKU_ALPHA_START = 0x825A;
    /**
     * 全角英字の終了位置。
     */
    private static final int ZENKAKU_ALPHA_END = 0x829A;
    /**
     * 全角ひらがなの開始位置。
     */
    private static final int ZENKAKU_HIRAGANA_START = 0x829F;
    /**
     * 全角ひらがなの終了位置。
     */
    private static final int ZENKAKU_HIRAGANA_END = 0x82F2;
    /**
     * 全角カタカナの開始位置。
     */
    private static final int ZENKAKU_KATAKANA_START = 0x833F;
    /**
     * 全角カタカナの終了位置。
     */
    private static final int ZENKAKU_KATAKANA_END = 0x8396;
    /**
     * 全角ギリシャ文字の開始位置。
     */
    private static final int ZENKAKU_GREEK_START = 0x839E;
    /**
     * 全角ギリシャ文字の終了位置。
     */
    private static final int ZENKAKU_GREEK_END = 0x83DE;
    /**
     * 全角ロシア文字の開始位置。
     */
    private static final int ZENKAKU_RUSSIAN_START = 0x843F;
    /**
     * 全角ロシア文字の終了位置。
     */
    private static final int ZENKAKU_RUSSIAN_END = 0x8491;
    /**
     * 全角ロシア文字の開始位置。
     */
    private static final int ZENKAKU_KEISEN_START = 0x849E;
    /**
     * 全角ロシア文字の終了位置。
     */
    private static final int ZENKAKU_KEISEN_END = 0x84BE;
    /**
     * NEC選定IBM拡張文字の開始位置。
     */
    private static final int NEC_EXTENDED_START = 0XED40;
    /**
     * NEC選定IBM拡張文字の終了位置。
     */
    private static final int NEC_EXTENDED_END = 0XEEFC;
    /**
     * IBM拡張文字の開始位置。
     */
    private static final int IBM_EXTENDED_START = 0xFA40;
    /**
     * IBM拡張文字の終了位置。
     */
    private static final int IBM_EXTENDED_END = 0xFC4B;
    
    
    /**
     * 第1水準に含まれる漢字。
     */
    private static final int[][] LEVEL1_ZENKAKU_KANJI_RANGES = new int[][] {

        // 16区
        {0x889E, 0x88FD},
        // 17区,18区
        {0x893F, 0x89FD},
        // 19区, 20区
        {0x8A3F, 0x8AFD},
        // 21区, 22区
        {0x8B3F, 0x8BFD},
        // 23区, 24区
        {0x8C3F, 0x8CFD},
        // 25区, 26区
        {0x8D3F, 0x8DFD},
        // 27区, 28区
        {0x8E3F, 0x8EFD},
        // 29区, 30区
        {0x8F3F, 0x8FFD},
        // 31区, 32区
        {0x903F, 0x90FD},
        // 33区, 34区
        {0x913F, 0x91FD},
        // 35区, 36区
        {0x923F, 0x92FD},
        // 37区, 38区
        {0x933F, 0x93FD},
        // 39区, 40区
        {0x943F, 0x94FD},
        // 41区, 42区
        {0x953F, 0x95FD},
        // 43区, 44区
        {0x963F, 0x96FD},
        // 45区, 46区
        {0x973F, 0x97FD},
        // 47区(47区の後半は第3水準)
        {0x983F, 0x987C},
    };

    /**
     * 第2水準に含まれる漢字。
     */
    private static final int[][] LEVEL2_ZENKAKU_KANJI_RANGES = new int[][] {
        // 48区
        {0x989E, 0x98FD},
        // 49区, 50区
        {0x993F, 0x99FD},
        // 51区, 52区
        {0x9A3F, 0x9AFD},
        // 53区, 54区
        {0x9B3F, 0x9BFD},
        // 55区, 56区
        {0x9C3F, 0x9CFD},
        // 57区, 58区
        {0x9D3F, 0x9DFD},
        // 59区, 60区
        {0x9E3F, 0x9EFD},
        // 61区, 62区
        {0x9F3F, 0x9FFD},
        // 63区, 64区
        {0xE03F, 0xE0FD},
        // 65区, 66区
        {0xE13F, 0xE1FD},
        // 67区, 68区
        {0xE23F, 0xE2FD},
        // 69区, 70区
        {0xE33F, 0xE3FD},
        // 71区, 72区
        {0xE43F, 0xE4FD},
        // 73区, 74区
        {0xE53F, 0xE5FD},
        // 75区, 76区
        {0xE63F, 0xE6FD},
        // 77区, 78区
        {0xE73F, 0xE7FD},
        // 79区, 80区
        {0xE83F, 0xE8FD},
        // 81区, 82区
        {0xE93F, 0xE9FD},
        // 83区, 84区(84区は途中まで)
        {0xEA3F, 0xEAA6},
    };

    /**
     * 隠蔽コンストラクタ。
     */
    private JapaneseCharsetUtil() {
    }
    
    /**
     * 全てのAscii文字を取得する。
     * 
     * @return 全てのAscii文字を羅列した文字列
     */
    public static String getAsciiChars() {
        return getShiftJisChars(ASCII_CHAR_START, ASCII_CHAR_END);
    }

    /**
     * 全ての半角カナ文字を取得する。
     * 
     * @return 全ての半角カナ文字を羅列した文字列
     */
    public static String getHankakuKanaChars() {
        return getShiftJisChars(HANKANA_CHAR_START, HANKANA_CHAR_END);
    }

    /**
     * JIS第1～2区の記号文字に含まれる全ての全角記号を取得する。
     * 
     * @return JIS第1～2区の記号文字に含まれる全ての全角記号を羅列した文字列
     */
    public static String getJisSymbolChars() {
        return getShiftJisChars(JIS_SYMBOL_START, JIS_SYMBOL_END);
    }

    /**
     * JIS第13区のNEC 特殊文字を取得する。
     * @return JIS第13区のNEC 特殊文字に含まれる全ての全角記号を羅列した文字列
     */
    public static String getNecSymbolChars() {
        return getShiftJisChars(NEC_SYMBOL_START, NEC_SYMBOL_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角数字を取得する。
     * 
     * @return JIS第1水準に含まれる全ての全角数字を羅列した文字列
     */
    public static String getZenkakuNumChars() {
        return getShiftJisChars(ZENKAKU_NUM_START, ZENKAKU_NUM_END);
    }

    /**
     * JIS第1水準に含まれる全てのアルファベットを取得する。
     * 
     * @return JIS第1水準に含まれる全てのアルファベットを羅列した文字列
     */
    public static String getZenkakuAlphaChars() {
        return getShiftJisChars(ZENKAKU_ALPHA_START, ZENKAKU_ALPHA_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角ひらがなを取得する。
     * 
     * @return JIS第1水準に含まれる全ての全角ひらがなを羅列した文字列
     */
    public static String getZenkakuHiraganaChars() {
        return getShiftJisChars(ZENKAKU_HIRAGANA_START, ZENKAKU_HIRAGANA_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角カタカナを取得する。
     * 
     * @return JIS第1水準に含まれる全ての全角カタカナを羅列した文字列
     */
    public static String getZenkakuKatakanaChars() {
        return getShiftJisChars(ZENKAKU_KATAKANA_START, ZENKAKU_KATAKANA_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角ギリシャ文字を取得する。
     * 
     * @return JIS第1水準に含まれる全ての全角ギリシャ文字を羅列した文字列
     */
    public static String getZenkakuGreekChars() {
        return getShiftJisChars(ZENKAKU_GREEK_START, ZENKAKU_GREEK_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角ロシア文字を取得する。
     * @return JIS第1水準に含まれる全ての全角ロシア文字を羅列した文字列
     */
    public static String getZenkakuRussianChars() {
        return getShiftJisChars(ZENKAKU_RUSSIAN_START, ZENKAKU_RUSSIAN_END);
    }

    /**
     * JIS第1水準に含まれる全ての全角罫線を取得する。
     * @return JIS第1水準に含まれる全ての全角罫線を羅列した文字列
     */
    public static String getZenkakuKeisenChars() {
        return getShiftJisChars(ZENKAKU_KEISEN_START, ZENKAKU_KEISEN_END);
    }

    /**
     * 全てのNEC選定IBM拡張文字を取得する。
     * @return 全てのNEC選定IBM拡張文字を羅列した文字列
     */
    public static String getNecExtendedChars() {
        return getShiftJisChars(NEC_EXTENDED_START, NEC_EXTENDED_END);
    }

    /**
     * 全てのIBM拡張文字を取得する。
     * @return 全てのIBM拡張文字を羅列した文字列
     */
    public static String getIbmExtendedChars() {
        return getShiftJisChars(IBM_EXTENDED_START, IBM_EXTENDED_END);
    }

    /**
     * JIS第1水準に含まれる全ての漢字を取得する。
     * @return JIS第1水準に含まれる全ての漢字を羅列した文字列
     */
    public static String getLevel1Kanji() {
        StringBuilder sb = new StringBuilder();
        for (int[] startEnd : LEVEL1_ZENKAKU_KANJI_RANGES) {
            sb.append(getShiftJisChars(startEnd[0], startEnd[1]));
        }
        return sb.toString();
    }

    /**
     * JIS第2水準に含まれる全ての漢字を取得する。
     * @return JIS第2水準に含まれる全ての漢字を羅列した文字列
     */
    public static String getLevel2Kanji() {
        StringBuilder sb = new StringBuilder();
        for (int[] startEnd : LEVEL2_ZENKAKU_KANJI_RANGES) {
            sb.append(getShiftJisChars(startEnd[0], startEnd[1]));
        }
        return sb.toString();
    }

    /**
     * 引数で指定した範囲の文字列を取得する。
     * @param start Shift JISの文字範囲の開始位置
     * @param end Shift JISの文字範囲の終了位置
     * 
     * @return MS932文字エンコーディング指定した範囲で有効な文字
     */
    private static String getShiftJisChars(int start, int end) {

        StringBuilder sb = new StringBuilder(end - start + 1);
        for (int i = start; i <= end; i++) {
            try {
                char c = new String(getBytes(i) , MS932_NAME).charAt(0);
                if (c != UNICODE_REPLACEMENT_CHARACTER) {
                    sb.append(c);
                }
            } catch (UnsupportedEncodingException e) {
                // 通常例外は発生しない
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * int型の値を2バイトのバイト配列に変換する。
     * 
     * @param value 変換する値
     * @return 変換後のバイト配列
     */
    private static byte[] getBytes(int value) {
        if ((value >= 0x20 && value <= 0x7F)
                || (value >= 0xA0 && value <= 0xDF)) {
            return new byte[] {(byte) value};
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int high = (byte) ((value >>> 8) & 0xFF);
            int low = (byte) ((0xFF & value));
            baos.write(high);
            baos.write(low);
            return baos.toByteArray();
        }
    }

}
