load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "webhooks",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: webhooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.webhooks.PluginModule",
        "Implementation-Title: webhooks plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/webhooks",
        "Implementation-Vendor: Gerrit Code Review",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "webhooks_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["webhooks"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":webhooks__plugin",
    ],
)
