package com.gitdoc.translation.entity.repository;

import com.gitdoc.translation.entity.RepositoryBranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchJpaRepository extends JpaRepository<RepositoryBranchEntity, Long> {

    List<RepositoryBranchEntity> findByRepositoryId(Long repositoryId);

    Optional<RepositoryBranchEntity> findByRepositoryIdAndBranchName(Long repositoryId, String branchName);

    boolean existsByRepositoryIdAndBranchName(Long repositoryId, String branchName);

    long countByRepositoryId(Long repositoryId);

    void deleteByRepositoryIdAndBranchName(Long repositoryId, String branchName);
}
