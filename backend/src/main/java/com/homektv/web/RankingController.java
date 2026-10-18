package com.homektv.web;

import com.homektv.domain.Song;
import com.homektv.repo.SongRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排行榜：热歌榜（播放量）、新歌榜（入库时间）。
 */
@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final SongRepository songs;

    public RankingController(SongRepository songs) {
        this.songs = songs;
    }

    @GetMapping("/hot")
    public Map<String, Object> hot(@RequestParam(defaultValue = "50") int limit) {
        List<Song> list = songs.findTop50ByOrderByPlayCountDesc();
        return Map.of("type", "hot", "songs", list.stream().limit(limit).map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/new")
    public Map<String, Object> fresh(@RequestParam(defaultValue = "50") int limit) {
        List<Song> list = songs.findTop50ByOrderByCreatedAtDesc();
        return Map.of("type", "new", "songs", list.stream().limit(limit).map(this::toDto).collect(Collectors.toList()));
    }

    private Map<String, Object> toDto(Song s) {
        return Map.of(
                "id", s.getId(),
                "title", s.getTitle() == null ? "" : s.getTitle(),
                "artist", s.getArtist() == null ? "" : s.getArtist(),
                "playCount", s.getPlayCount()
        );
    }
}
