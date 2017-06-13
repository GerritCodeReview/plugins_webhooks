load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "webhooks",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: webhooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.webhooks.Module",
        "Implementation-Title: webhooks plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/webhooks",
        "Implementation-Vendor: Gerrit Code Review",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "webhooks_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/**/*"]),
    tags = [
        "webhooks",
        "local",
    ],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":webhooks__plugin_test_deps",
        ":webhooks__plugin",
    ],
)

java_library(
    name = "webhooks__plugin_test_deps",
    visibility = ["//visibility:public"],
    exports = [
        "@byte-buddy//jar",
        "@mockito//jar",
        "@objenesis//jar",
    ],
)
