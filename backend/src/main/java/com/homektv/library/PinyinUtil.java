package com.homektv.library;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 中文转拼音工具（P1.5）。
 * 生成全拼（如 周杰伦→zhoujielun）与首字母（→zjl），供搜索使用。
 * 非中文字符（英文/数字）原样保留小写，多音字取第一个读音。
 */
public final class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    private PinyinUtil() {}

    /** 全拼：周杰伦 → zhoujielun；Jay 周 → jayzhou */
    public static String fullPinyin(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            String py = pinyinOfChar(c);
            if (py != null) {
                sb.append(py);
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
            // 其它符号忽略
        }
        return sb.toString();
    }

    /** 首字母：周杰伦 → zjl；G.E.M. → gem */
    public static String initials(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            String py = pinyinOfChar(c);
            if (py != null && !py.isEmpty()) {
                sb.append(py.charAt(0));
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /** 返回单个汉字的拼音（无声调小写）；非汉字返回 null */
    private static String pinyinOfChar(char c) {
        if (c < 0x4E00 || c > 0x9FFF) return null; // 非 CJK 基本区
        try {
            String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
            if (arr != null && arr.length > 0) {
                return arr[0]; // 多音字取第一个读音
            }
        } catch (BadHanyuPinyinOutputFormatCombination ignored) {
            // 不会发生（格式固定）
        }
        return null;
    }
}
