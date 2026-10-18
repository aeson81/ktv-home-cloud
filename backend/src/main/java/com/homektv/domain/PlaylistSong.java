package com.homektv.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_songs")
@IdClass(PlaylistSongId.class)
/**
 * 歌单-歌曲关联实体，表示歌单与歌曲之间的多对多关联关系。
 *
 * Playlist-song join entity, representing the many-to-many relationship between playlists and songs.
 */
public class PlaylistSong {
    @Id
    @Column(name = "playlist_id")
    private Long playlistId; // 歌单ID / Playlist ID

    @Id
    @Column(name = "song_id")
    private Long songId; // 歌曲ID / Song ID

    @Column(name = "sort_order", nullable = false)
    private int sortOrder; // 排序顺序 / Sort order

    @Column(nullable = false)
    private boolean manual; // 是否手动添加 / Whether manually added

    // ---- getters / setters ----

    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isManual() { return manual; }
    public void setManual(boolean manual) { this.manual = manual; }
}
