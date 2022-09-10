package opstopus.deploptopus.github

enum class GitHubHeaders(val headerText: String) {
    EVENT_TYPE("X-GitHub-Event"),
    HUB_SIGNATURE_SHA_256("X-Hub-Signature-256");
}
