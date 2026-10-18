package com.homektv.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * 歌单歌曲关联复合主键。
 *
 * Composite primary key for playlist-song association.
 */
public class PlaylistSongId implements Serializable {
    // 歌单ID
    // English: Playlist ID
    private Long playlistId;
    // 歌曲ID
    // English: Song ID
    private Long songId;

    /**
     * 无参构造方法。
     *
     * No-arg constructor.
     */
    public PlaylistSongId() {}
    /**
     * 全参构造方法。
     *
     * All-args constructor.
     */
    public PlaylistSongId(Long playlistId, Long songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }
    /**
     * 基于 playlistId 和 songId 判断相等性。
     *
     * Equality based on playlistId and songId.
     */
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof PlaylistSongId other)) return false;
        return Objects.equals(playlistId, other.playlistId) && Objects.equals(songId, other.songId);
    }
    /**
     * 基于 playlistId 和 songId 生成哈希码。
     *
     * Hash code based on playlistId and songId.
     */
    public int hashCode() { return Objects.hash(playlistId, songId); }
}
