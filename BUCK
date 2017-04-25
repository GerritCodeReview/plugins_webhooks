include_defs('//bucklets/gerrit_plugin.bucklet')
include_defs('//bucklets/java_sources.bucklet')
include_defs('//bucklets/maven_jar.bucklet')

SOURCES = glob(['src/main/java/**/*.java'])
RESOURCES = glob(['src/main/resources/**/*'])

TEST_DEPS = GERRIT_PLUGIN_API + GERRIT_TESTS + [
  ':webhooks__plugin',
]

gerrit_plugin(
  name = 'webhooks',
  srcs = SOURCES,
  resources = RESOURCES,
  manifest_entries = [
    'Gerrit-PluginName: webhooks',
    'Gerrit-ApiType: plugin',
    'Gerrit-Module: com.googlesource.gerrit.plugins.webhooks.Module',
    'Implementation-Title: webhooks plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/webhooks',
    'Implementation-Vendor: Gerrit Code Review',
  ],
  provided_deps = GERRIT_TESTS,
)

java_sources(
  name = 'webhooks-sources',
  srcs = SOURCES + RESOURCES,
)

java_library(
  name = 'classpath',
  deps = TEST_DEPS,
)

java_test(
  name = 'webhooks_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['webhooks'],
  deps = TEST_DEPS,
)
