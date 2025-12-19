load("@com_googlesource_gerrit_bazlets//tools:junit.bzl", "junit_tests")
load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin")

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
    deps = [
        ":webhooks__plugin",
        "//java/com/google/gerrit/acceptance:lib",
        "//plugins:plugin-lib",
    ],
)
