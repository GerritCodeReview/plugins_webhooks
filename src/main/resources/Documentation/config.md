@PLUGIN@ Configuration
=========================

Global @PLUGIN@ plugin configuration is stored in the `gerrit.config` file.
An example global @PLUGIN@ configuration section:

```
[plugin "@PLUGIN@"]
  connectionTimeout = 3000
  socketTimeout = 2500
  maxTries = 300
  retryInterval = 2000
  threadPoolSize = 3
```

The @PLUGIN@ plugin's per project configuration is stored in the
`@PLUGIN@.config` file in project's `refs/meta/config` branch.
For example, to propagate all events to `https://foo.org/gerrit-events`
and propagate only `patchset-created` and `ref-updated` events to
`https://bar.org/`:

```
[remote "foo"]
  url = https://foo.org/gerrit-events
  maxTries = 3
  sslVerify = true

[remote "bar"]
  url = https://bar.org/
  event = patchset-created
  event = ref-updated
```

The configuration is inheritable. Connection parameters
`connectionTimeout`, `socketTimeout`, `maxTries`, `retryInterval` and `sslVerify`
can be fine-tuned at remote level.

File 'gerrit.config'
--------------------

@PLUGIN@.connectionTimeout
:   Maximum interval of time in milliseconds the plugin waits for a connection
    to the target instance. When not specified, the default value is set to 5000ms.

@PLUGIN@.socketTimeout
:   Maximum interval of time in milliseconds the plugin waits for a response from the
    target instance once the connection has been established. When not specified,
    the default value is set to 5000ms.

@PLUGIN@.maxTries
:   Maximum number of times the plugin should attempt when posting an event to
    the target url. Setting this value to 0 will disable retries. When not
    specified, the default value is 5. After this number of failed tries, an
    error is logged.

@PLUGIN@.retryInterval
:   The interval of time in milliseconds between the subsequent auto-retries.
    When not specified, the default value is set to 1000ms.

@PLUGIN@.threadPoolSize
:   Maximum number of threads used to send events to the target instance.
    Defaults to 2.

@PLUGIN@.sslVerify
:   When 'true' SSL certificate verification of all webhooks *is* performed
    when payload is delivered.
    Default value is 'false'.

@PLUGIN@.allowedEvent
:   Type of the event which is allowed to be posted to the remote url.
    Multiple event types can be provided. If not specified, then all event
    types are allowed.

@PLUGIN@.allowedUrlPattern
:    A regex that remote URLs in the plugin config must match. If multiple
     patterns are provided, the remote URL must match at least one of them.
     If not specified, then all remote URLs are allowed.

@PLUGIN@.maxAllowedConnectionTimeout
:   Maximum allowed value for the connection timeout. If a value greater than
    this is configured in the @PLUGIN@.config, this value will be chosen
    instead.

@PLUGIN@.maxAllowedSocketTimeout
:   Maximum allowed value for the socket timeout. If a value greater than
    this is configured in the @PLUGIN@.config, this value will be chosen
    instead.

@PLUGIN@.maxAllowedTries
:   Maximum allowed value for the retries. If a value greater than this is
    configured in the @PLUGIN@.config, this value will be chosen instead.
    If this is set to 0, retries are disabled.

@PLUGIN@.maxAllowedRetryInterval
:   Maximum allowed value for the retry interval. If a value greater than
    this is configured in the @PLUGIN@.config, this value will be chosen
    instead.


File '@PLUGIN@.config'
----------------------

<a id="url"> remote.NAME.url
: Address of the remote server to post events to.

<a id="event"> remote.NAME.event
: Type of the event which will be posted to the remote url. If this event is
  not an allowed one in the global configuration, then it won't be posted.
  Multiple event types can be specified, listing event types which should be
  posted. If no event type is configured, only the allowed events from the
  global configuration will be posted.

<a id="connectionTimeout"> remote.NAME.connectionTimeout
: Maximum interval of time in milliseconds the plugin waits for a connection
  to the target instance. When not specified, the default value is derrived
  from global configuration.

<a id="socketTimeout">remote.NAME.socketTimeout
: Maximum interval of time in milliseconds the plugin waits for a response from the
  target instance once the connection has been established. When not specified,
  the default value is derrived from global configuration.

<a id="maxTries">remote.NAME.maxTries
: Maximum number of times the plugin should attempt when posting an event to
  the target url. Setting this value to 0 will disable retries. When not
  specified, the default value is derrived from global configuration.

<a id="retryInterval">remote.NAME.retryInterval
: The interval of time in milliseconds between the subsequent auto-retries.
  When not specified, the default value is derrived from global configuration.

<a id="sslVerify">remote.NAME.sslVerify
: When 'true' SSL certificate verification of remote url *is* performed
  when payload is delivered, the default value is derived from global configuration.
