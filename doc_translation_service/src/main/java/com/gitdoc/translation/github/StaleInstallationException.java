package com.gitdoc.translation.github;

/**
 * Thrown when a stored installation_id is no longer valid on GitHub
 * (e.g. user uninstalled and reinstalled the App, changing the installation ID).
 */
public class StaleInstallationException extends RuntimeException {

    private final Long installationId;

    public StaleInstallationException(Long installationId) {
        super("GitHub App installation not found or revoked: " + installationId);
        this.installationId = installationId;
    }

    public Long getInstallationId() {
        return installationId;
    }
}
