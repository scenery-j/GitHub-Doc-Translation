package com.gitdoc.translation.entity.repository;

import com.gitdoc.translation.entity.OperationLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogJpaRepository extends JpaRepository<OperationLogEntity, Long> {

    Page<OperationLogEntity> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId, Pageable pageable);
}
