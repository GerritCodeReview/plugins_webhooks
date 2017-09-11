workspace(name = "webhooks")
load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "b7514d03a7798905ff1513295b46620e57b8f386",
)

#Snapshot Plugin API
#load(
#    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
#    "gerrit_api_maven_local",
#)

# Load snapshot Plugin API
#gerrit_api_maven_local()

# Release Plugin API
load(
   "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
   "gerrit_api",
)

# Load release Plugin API
gerrit_api()

load("//:external_plugin_deps.bzl", "external_plugin_deps")

external_plugin_deps()
