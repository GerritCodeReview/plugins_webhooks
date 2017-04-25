Build
=====

This plugin is built with Buck.

Two build modes are supported: Standalone and in Gerrit tree. Standalone
build mode is recommended, as this mode doesn't require local Gerrit
tree to exist.

Build standalone
----------------

Clone bucklets library:

```
  git clone https://gerrit.googlesource.com/bucklets

```
and link it to @PLUGIN@ directory:

```
  cd @PLUGIN@ && ln -s ../bucklets .
```

Add link to the .buckversion file:

```
  cd @PLUGIN@ && ln -s bucklets/buckversion .buckversion
```

Add link to the .watchmanconfig file:

```
  cd @PLUGIN@ && ln -s bucklets/watchmanconfig .watchmanconfig
```

To build the plugin, issue the following command:

```
  buck build plugin
```

The output is created in:

```
  buck-out/gen/@PLUGIN@.jar
```

This project can be imported into the Eclipse IDE:

```
  ./bucklets/tools/eclipse.py
```

To execute the tests run:

```
  buck test
```

To build plugin sources run:

```
  buck build src
```

The output is created in:

```
  buck-out/gen/@PLUGIN@-sources.jar
```

Build in Gerrit tree
--------------------

Clone or link this plugin to the plugins directory of Gerrit's source
tree, and issue the command:

```
  buck build plugins/@PLUGIN@
```

The output is created in:

```
  buck-out/gen/plugins/@PLUGIN@/@PLUGIN@.jar
```

This project can be imported into the Eclipse IDE:

```
  ./tools/eclipse/project.py
```

To execute the tests run:

```
  buck test --include @PLUGIN@
```

How to build the Gerrit Plugin API is described in the [Gerrit
documentation](../../../Documentation/dev-buck.html#_extension_and_plugin_api_jar_files).

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
