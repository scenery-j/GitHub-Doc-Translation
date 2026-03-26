package com.gitdoc.translation.entity.repository;

import com.gitdoc.translation.entity.TranslationFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileJpaRepository extends JpaRepository<TranslationFileEntity, Long> {

    List<TranslationFileEntity> findByTaskId(Long taskId);

    List<TranslationFileEntity> findByTaskIdAndStatus(Long taskId, String status);

    List<TranslationFileEntity> findByRepositoryId(Long repositoryId);

    Optional<TranslationFileEntity> findFirstByRepositoryIdAndSourcePathAndTargetLanguageAndStatusOrderByCreatedAtDesc(
            Long repositoryId, String sourcePath, String targetLanguage, String status);

    long countByRepositoryIdAndStatus(Long repositoryId, String status);

    List<TranslationFileEntity> findByRepositoryIdAndSourcePathAndStatus(
            Long repositoryId, String sourcePath, String status);
}
