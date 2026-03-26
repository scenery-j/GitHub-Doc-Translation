package com.gitdoc.translation.transconfig.service;

import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.entity.RepositoryBranchEntity;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.repository.BranchJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.repo.service.RepoService;
import com.gitdoc.translation.transconfig.dto.BranchConfigDTO;
import com.gitdoc.translation.transconfig.dto.TransConfigDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransConfigService {

    private final RepoService repoService;
    private final RepoJpaRepository repoJpaRepository;
    private final BranchJpaRepository branchJpaRepository;

    // ── Global config (per-repo) ────────────────────────────────────────────

    public TransConfigDTO getConfig(Long userId, Long repoId) {
        RepositoryEntity repo = repoService.getAndValidateRepo(userId, repoId);
        return toDTO(repo);
    }

    @Transactional
    public TransConfigDTO updateConfig(Long userId, Long repoId, TransConfigDTO request) {
        RepositoryEntity repo = repoService.getAndValidateRepo(userId, repoId);

        if (request.getBaseLanguage() != null) repo.setBaseLanguage(request.getBaseLanguage());
        if (request.getTargetLanguages() != null) repo.setTargetLanguages(request.getTargetLanguages());
        if (request.getAiModel() != null) repo.setAiModel(request.getAiModel());

        repoJpaRepository.save(repo);
        return toDTO(repo);
    }

    // ── Branch config (per-branch) ──────────────────────────────────────────

    public List<BranchConfigDTO> listBranches(Long userId, Long repoId) {
        repoService.getAndValidateRepo(userId, repoId);
        return branchJpaRepository.findByRepositoryId(repoId)
                .stream().map(this::toBranchDTO).collect(Collectors.toList());
    }

    public BranchConfigDTO getBranchConfig(Long userId, Long repoId, String branchName) {
        repoService.getAndValidateRepo(userId, repoId);
        RepositoryBranchEntity branch = branchJpaRepository
                .findByRepositoryIdAndBranchName(repoId, branchName)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "分支配置不存在: " + branchName));
        return toBranchDTO(branch);
    }

    @Transactional
    public BranchConfigDTO addBranch(Long userId, Long repoId, String branchName) {
        repoService.getAndValidateRepo(userId, repoId);
        if (branchName == null || branchName.isBlank()) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "分支名称不能为空");
        }
        if (branchJpaRepository.existsByRepositoryIdAndBranchName(repoId, branchName)) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "该分支已存在配置: " + branchName);
        }
        RepositoryBranchEntity branch = new RepositoryBranchEntity();
        branch.setRepositoryId(repoId);
        branch.setBranchName(branchName);
        return toBranchDTO(branchJpaRepository.save(branch));
    }

    @Transactional
    public BranchConfigDTO updateBranchConfig(Long userId, Long repoId, String branchName, BranchConfigDTO request) {
        repoService.getAndValidateRepo(userId, repoId);
        RepositoryBranchEntity branch = branchJpaRepository
                .findByRepositoryIdAndBranchName(repoId, branchName)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "分支配置不存在: " + branchName));

        if (request.getSelectedPaths() != null) branch.setSelectedPaths(request.getSelectedPaths());
        if (request.getIgnorePatterns() != null) branch.setIgnorePatterns(request.getIgnorePatterns());
        if (request.getOutputPathPattern() != null) branch.setOutputPathPattern(request.getOutputPathPattern());
        if (request.getWebhookActive() != null) branch.setWebhookActive(request.getWebhookActive());
        if (request.getAutoMergePr() != null) branch.setAutoMergePr(request.getAutoMergePr());

        return toBranchDTO(branchJpaRepository.save(branch));
    }

    @Transactional
    public void deleteBranch(Long userId, Long repoId, String branchName) {
        repoService.getAndValidateRepo(userId, repoId);
        if (branchJpaRepository.countByRepositoryId(repoId) <= 1) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "至少保留一个分支配置");
        }
        branchJpaRepository.deleteByRepositoryIdAndBranchName(repoId, branchName);
    }

    // ── Ignore content helpers (read/write as plain text) ───────────────────

    public String getIgnoreContent(Long userId, Long repoId, String branchName) {
        repoService.getAndValidateRepo(userId, repoId);
        return branchJpaRepository.findByRepositoryIdAndBranchName(repoId, branchName)
                .map(b -> {
                    List<String> patterns = b.getIgnorePatterns();
                    return (patterns == null || patterns.isEmpty()) ? "" : String.join("\n", patterns);
                })
                .orElse("");
    }

    @Transactional
    public void updateIgnoreContent(Long userId, Long repoId, String branchName, String content) {
        repoService.getAndValidateRepo(userId, repoId);
        RepositoryBranchEntity branch = branchJpaRepository
                .findByRepositoryIdAndBranchName(repoId, branchName)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "分支配置不存在: " + branchName));

        List<String> patterns = Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());
        branch.setIgnorePatterns(patterns);
        branchJpaRepository.save(branch);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private TransConfigDTO toDTO(RepositoryEntity repo) {
        TransConfigDTO dto = new TransConfigDTO();
        dto.setBaseLanguage(repo.getBaseLanguage());
        dto.setTargetLanguages(repo.getTargetLanguages());
        dto.setAiModel(repo.getAiModel());
        return dto;
    }

    private BranchConfigDTO toBranchDTO(RepositoryBranchEntity branch) {
        BranchConfigDTO dto = new BranchConfigDTO();
        dto.setBranchName(branch.getBranchName());
        dto.setSelectedPaths(branch.getSelectedPaths());
        dto.setIgnorePatterns(branch.getIgnorePatterns());
        dto.setOutputPathPattern(branch.getOutputPathPattern());
        dto.setWebhookActive(branch.getWebhookActive());
        dto.setAutoMergePr(branch.getAutoMergePr());
        return dto;
    }
}
