package com.gitdoc.translation.entity.repository;

import com.gitdoc.translation.entity.RepositoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoJpaRepository extends JpaRepository<RepositoryEntity, Long> {

    Page<RepositoryEntity> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM RepositoryEntity r WHERE r.userId = :userId " +
            "AND (:search IS NULL OR LOWER(r.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:status IS NULL OR :status = 'all' OR r.status = :status)")
    Page<RepositoryEntity> findByUserIdWithFilter(Long userId, String search, String status, Pageable pageable);

    Optional<RepositoryEntity> findByUserIdAndGithubRepoId(Long userId, Long githubRepoId);

    Optional<RepositoryEntity> findByFullName(String fullName);

    boolean existsByUserIdAndGithubRepoId(Long userId, Long githubRepoId);

    /**
     * 查找所有属于某个 installation 的仓库（卸载时批量清理用）
     */
    List<RepositoryEntity> findAllByInstallationId(Long installationId);
}
