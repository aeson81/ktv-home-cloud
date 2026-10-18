package com.homektv.repo;

import com.homektv.domain.PlayerState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * 播放器状态数据访问接口，操作 player_state 数据表。
 *
 * Player state data access interface operating on the player_state table.
 */
public interface PlayerStateRepository extends JpaRepository<PlayerState, Short> {

    /**
     * 获取唯一的播放状态行（id=1）。
     *
     * Retrieve the singleton player state row (id=1).
     */
    default PlayerState getSingleton() {
        return findById(PlayerState.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("player_state 单行未初始化"));
    }

    /**
     * 使用悲观写锁按 ID 查询播放器状态行，用于并发安全的更新操作。
     *
     * Find a player state row by ID with pessimistic write lock for concurrency-safe updates.
     *
     * @param id 播放器状态行 ID / the player state row ID
     * @return 包含 PlayerState 的 Optional / an Optional containing the PlayerState
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from PlayerState state where state.id = :id")
    Optional<PlayerState> findByIdForUpdate(Short id);

    /**
     * 使用悲观写锁获取唯一的播放状态行（id=1），用于并发安全的更新操作。
     *
     * Retrieve the singleton player state row (id=1) with pessimistic write lock
     * for concurrency-safe updates.
     */
    default PlayerState getSingletonForUpdate() {
        return findByIdForUpdate(PlayerState.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("player_state 单行未初始化"));
    }
}
