load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin", "gerrit_plugin_tests")

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

gerrit_plugin_tests(
    name = "webhooks_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["webhooks"],
    deps = [
        ":webhooks__plugin",
    ],
)
