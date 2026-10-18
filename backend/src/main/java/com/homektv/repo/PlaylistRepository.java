package com.homektv.repo;

import com.homektv.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

/**
 * Playlist 仓库接口，负责歌单实体（playlists 表）的数据库操作。
 *
 * Playlist repository interface for database operations on playlist entities (playlists table).
 */
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByName(String name);
    List<Playlist> findAllByOrderByUpdatedAtDesc();

    /**
     * 使用 PostgreSQL 事务级咨询锁锁定歌单名称，防止并发生成同名歌单。
     *
     * Acquires a PostgreSQL transaction-level advisory lock on the playlist name
     * to prevent concurrent generation of duplicate playlist names.
     *
     * @param name 歌单名称 / the playlist name to lock
     */
    @Query(value = "SELECT pg_advisory_xact_lock(hashtextextended(:name, 0))", nativeQuery = true)
    void lockGeneratedName(String name);
}
