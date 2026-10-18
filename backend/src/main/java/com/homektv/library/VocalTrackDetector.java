package com.homektv.library;

import com.homektv.media.AudioStreamInfo;
import com.homektv.media.MediaProbe;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 伴奏轨判定（第 1 层：音轨元数据）。
 *
 * <p>目标：入库时确定哪条音轨是伴奏（无人声），哪条是原唱（含人声），免运行时猜轨（详设§11）。
 * 判定结果的 index 语义与 {@code song_files.vocalTrackIndex} 一致——<b>伴奏轨</b>的
 * 0-based 音频相对序号。
 *
 * <p>判定依据（可靠度从高到低）：
 * <ol>
 *   <li>disposition.karaoke 标志位 —— ffmpeg 明确标记的卡拉OK/伴奏轨；</li>
 *   <li>音轨标题标签命中伴奏/原唱词表（中「伴奏/伴唱/消音」、英 instrumental/karaoke/off vocal 等）；</li>
 *   <li>仅识别出原唱轨时，取另一条为伴奏轨（双轨场景）。</li>
 * </ol>
 * 任何一条命中即为高/中置信度；全部落空返回默认（第 2/3 层兜底或人工复核）。
 */
public final class VocalTrackDetector {

    /** 伴奏（无人声）关键词：命中即判为伴奏轨 */
    private static final Pattern ACCOMP = Pattern.compile(
            "伴奏|伴唱|消音|去人声|减字|inst(rumental)?|karaoke|off.?vocal|no.?vocal|music.?only|minus.?one|backing",
            Pattern.CASE_INSENSITIVE);

    /** 原唱（含人声）关键词：命中即判为原唱轨 */
    private static final Pattern VOCAL = Pattern.compile(
            "原唱|原版|人声|主唱|导唱|vocal|original|lead|guide",
            Pattern.CASE_INSENSITIVE);

    private VocalTrackDetector() {}

    public enum Confidence { HIGH, MEDIUM, LOW, NONE }

    /**
     * 判定结果。
     *
     * @param accompanimentIndex 伴奏轨的 0-based 音频相对序号；判不出时为 null
     * @param confidence         置信度；LOW/NONE 建议后台人工复核
     * @param reason             判定依据（日志/复核用）
     */
    public record Result(Integer accompanimentIndex, Confidence confidence, String reason) {}

    /**
     * 从探测结果判定伴奏轨。仅在 ≥2 音轨时有意义；单音轨/无明细返回 NONE。
     */
    public static Result detect(MediaProbe probe) {
        List<AudioStreamInfo> streams = probe.audioStreams();
        if (probe.audioTracks() < 2) {
            return new Result(null, Confidence.NONE, "单音轨，无原伴唱之分");
        }
        if (streams == null || streams.size() < 2) {
            // 有 ≥2 音轨但没采到明细（如旧探测路径）：无法判定，交由默认值/人工
            return new Result(null, Confidence.NONE, "缺少音轨元数据");
        }

        // 1) disposition.karaoke —— 最可靠
        for (AudioStreamInfo s : streams) {
            if (s.karaoke()) {
                return new Result(s.index(), Confidence.HIGH, "disposition.karaoke 标记 track#" + s.index());
            }
        }

        // 2) 标题标签词表
        Integer accompByTitle = null;
        Integer vocalByTitle = null;
        for (AudioStreamInfo s : streams) {
            String title = s.title();
            if (title == null) continue;
            boolean isAccomp = ACCOMP.matcher(title).find();
            boolean isVocal = VOCAL.matcher(title).find();
            // 「原唱」也含「唱」，但 ACCOMP 里的「伴唱/伴奏」更具体；同时命中时以伴奏词优先
            if (isAccomp) {
                if (accompByTitle == null) accompByTitle = s.index();
            } else if (isVocal) {
                if (vocalByTitle == null) vocalByTitle = s.index();
            }
        }
        if (accompByTitle != null) {
            return new Result(accompByTitle, Confidence.HIGH, "标题标签命中伴奏 track#" + accompByTitle);
        }
        // 仅识别出原唱轨 → 双轨场景取另一条为伴奏
        if (vocalByTitle != null && streams.size() == 2) {
            int other = vocalByTitle == 0 ? 1 : 0;
            return new Result(other, Confidence.MEDIUM,
                    "标题标签仅命中原唱 track#" + vocalByTitle + "，推断伴奏 track#" + other);
        }

        // 3) 元数据无线索：返回 LOW，index 留空，供第 2/3 层兜底或人工复核
        return new Result(null, Confidence.LOW, "音轨元数据无原伴唱线索");
    }
}
