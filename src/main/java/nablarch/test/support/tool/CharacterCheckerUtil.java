package nablarch.test.support.tool;

import java.util.BitSet;


/**
 * 文字種チェック時に使用するユーティリティクラス。
 *
 * @author Koichi Asano 
 *
 */
public final class CharacterCheckerUtil {

    /**
     * 隠蔽コンストラクタ。
     */
    private CharacterCheckerUtil() {
    }
    /**
     * 有効文字チェックに使用する文字の集合を作成する。
     * 
     * @param validCharStrings 有効な全ての文字
     * @return 有効文字チェックに使用する文字の集合
     */
    public static BitSet createCharSet(String... validCharStrings) {
        BitSet bs = new BitSet();
        CharacterCheckerUtil.storeCharSet(bs, validCharStrings);
        return bs;
    }

    /**
     * 有効文字チェックに使用する文字の集合を{@link BitSet}に設定する。
     *
     * @param stored 文字集合を保持する{@link BitSet}
     * @param validCharStrings 有効な全ての文字
     */
    public static void storeCharSet(BitSet stored, String... validCharStrings) {
        for (String validCharString : validCharStrings) {
            for (char c : validCharString.toCharArray()) {
                stored.set(c);
            }
        }
    }

    /**
     * 文字列が有効な文字集合に全て含まれているかチェックする。
     * 
     * @param validChars 有効な文字の集合
     * @param value チェック対象の文字列
     * @return チェック対象の文字列が全て文字集合に含まれる場合{@code true}
     */
    public static boolean checkValidCharOnly(BitSet validChars, String value) {
        
        for (char c : value.toCharArray()) {
            if (!validChars.get(c)) {
                return false;
            }
        }
        return true;
    }
}
