package com.gitdoc.translation.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {

    private final GitHubTokenManager tokenManager;
    private final ObjectMapper objectMapper;

    private static final String API_BASE = "https://api.github.com";
    private static final String API_VERSION_HEADER = "2022-11-28";

    private RestClient authClient(Long installationId) {
        String token = tokenManager.getInstallationToken(installationId);
        return RestClient.builder()
                .baseUrl(API_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader("X-GitHub-Api-Version", API_VERSION_HEADER)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
    }

    /**
     * Get repositories accessible to an installation
     */
    public JsonNode getInstallationRepositories(Long installationId) {
        return authClient(installationId).get()
                .uri("/installation/repositories?per_page=100")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get all accessible repositories for user via user token
     */
    public JsonNode getUserInstallations(String userAccessToken) {
        return RestClient.builder().build()
                .get()
                .uri(API_BASE + "/user/installations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAccessToken)
                .header("X-GitHub-Api-Version", API_VERSION_HEADER)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get repository file tree (all .md files recursively)
     */
    public JsonNode getRepoTree(String fullName, String branch, Long installationId) {
        return authClient(installationId).get()
                .uri(repoBase(fullName) + "/git/trees/{branch}?recursive=1", branch)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get file content from a specific branch (returns decoded text)
     */
    public String getFileContent(String fullName, String path, Long installationId, String branch) {
        String uri = repoBase(fullName) + "/contents/" + path
                + (branch != null && !branch.isBlank() ? "?ref=" + branch : "");
        JsonNode response = authClient(installationId).get()
                .uri(uri)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("content")) {
            throw new RuntimeException("File not found: " + path);
        }
        String base64Content = response.get("content").asText().replace("\n", "");
        return new String(Base64.getDecoder().decode(base64Content));
    }

    /**
     * List branches of a repository
     */
    public JsonNode listBranches(String fullName, Long installationId) {
        return authClient(installationId).get()
                .uri(repoBase(fullName) + "/branches?per_page=100")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get default branch latest commit SHA
     */
    public String getLatestCommitSha(String fullName, String branch, Long installationId) {
        JsonNode ref = authClient(installationId).get()
                .uri(repoBase(fullName) + "/git/ref/heads/{branch}", branch)
                .retrieve()
                .body(JsonNode.class);
        return ref.get("object").get("sha").asText();
    }

    /**
     * Get tree SHA for a commit
     */
    public String getCommitTreeSha(String fullName, String commitSha, Long installationId) {
        JsonNode commit = authClient(installationId).get()
                .uri(repoBase(fullName) + "/git/commits/{sha}", commitSha)
                .retrieve()
                .body(JsonNode.class);
        return commit.get("tree").get("sha").asText();
    }

    /**
     * Create a new git tree with translated files  在临时分支的基础树上添加需要变更的文件
     */
    public String createTree(String fullName, String baseTreeSha,
                             Map<String, String> files, Long installationId) {
        ArrayNode treeItems = objectMapper.createArrayNode();
        files.forEach((path, content) -> {
            ObjectNode item = objectMapper.createObjectNode();
            item.put("path", path);
            item.put("mode", "100644");
            item.put("type", "blob");
            item.put("content", content);
            treeItems.add(item);
        });

        ObjectNode body = objectMapper.createObjectNode();
        body.put("base_tree", baseTreeSha);
        body.set("tree", treeItems);

        JsonNode response = authClient(installationId).post()
                .uri(repoBase(fullName) + "/git/trees")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.get("sha").asText();
    }

    /**
     * Create a commit
     */
    public String createCommit(String fullName, String message,
                               String treeSha, String parentSha, Long installationId) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("message", message);
        body.put("tree", treeSha);
        ArrayNode parents = objectMapper.createArrayNode();
        parents.add(parentSha);
        body.set("parents", parents);

        JsonNode response = authClient(installationId).post()
                .uri(repoBase(fullName) + "/git/commits")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.get("sha").asText();
    }

    /**
     * Create a branch
     */
    public void createBranch(String fullName, String branchName,
                             String commitSha, Long installationId) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("ref", "refs/heads/" + branchName);
        body.put("sha", commitSha);

        authClient(installationId).post()
                .uri(repoBase(fullName) + "/git/refs")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Update branch ref to new commit
     */
    public void updateBranchRef(String fullName, String branchName,
                                String newCommitSha, Long installationId) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("sha", newCommitSha);
        body.put("force", false);

        authClient(installationId).patch()
                .uri(repoBase(fullName) + "/git/refs/heads/{branch}", branchName)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Create a pull request
     */
    public JsonNode createPullRequest(String fullName, String title, String body,
                                      String head, String base, Long installationId) {
        ObjectNode prBody = objectMapper.createObjectNode();
        prBody.put("title", title);
        prBody.put("body", body);
        prBody.put("head", head);
        prBody.put("base", base);

        return authClient(installationId).post()
                .uri(repoBase(fullName) + "/pulls")
                .body(prBody)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Auto-merge a pull request using squash strategy.
     *
     * @throws org.springframework.web.client.HttpClientErrorException 405 – merge not allowed (branch protection / pending reviews)
     *                                                                 409 – merge conflict
     */
    public void mergePullRequest(String fullName, int prNumber, String commitTitle, Long installationId) {
        authClient(installationId).put()
                .uri(repoBase(fullName) + "/pulls/{prNumber}/merge", prNumber)
                .body(Map.of(
                        "merge_method", "squash",
                        "commit_title", commitTitle
                ))
                .retrieve()
                .toBodilessEntity();
    }


    /**
     * Delete a branch by its reference.
     *
     * @param fullName       Repository full name (owner/repo)
     * @param branchName     Branch name to delete (e.g., "feature-branch")
     * @param installationId GitHub App installation ID
     */
    public void deleteBranch(String fullName, String branchName, Long installationId) {
        authClient(installationId).delete()
                .uri(repoBase(fullName) + "/git/refs/heads/{branch}", branchName)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Get a single pull request by number.
     * Returns the full PR object; the {@code state} field is {@code "open"} or {@code "closed"}.
     */
    public JsonNode getPullRequest(String fullName, int prNumber, Long installationId) {
        return authClient(installationId).get()
                .uri(repoBase(fullName) + "/pulls/{prNumber}", prNumber)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Get open pull requests for translation branch
     */
    public JsonNode getPullRequests(String fullName, String state, Long installationId) {
        return authClient(installationId).get()
                .uri(repoBase(fullName) + "/pulls?state={state}&per_page=20", state)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Compare commits to get changed files (for incremental sync)
     */
    public JsonNode compareCommits(String fullName, String base, String head, Long installationId) {
        return authClient(installationId).get()
                .uri(repoBase(fullName) + "/compare/{base}...{head}", base, head)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Returns the base path for repo-scoped API calls.
     * Builds the path via string concat instead of URI template variables
     * to prevent Spring from percent-encoding the '/' in fullName
     * (e.g. "owner/repo" must not become "owner%2Frepo").
     */
    private String repoBase(String fullName) {
        return "/repos/" + fullName;
    }
}
