@PLUGIN@ Configuration
=========================

The @PLUGIN@ plugin's per project configuration is stored in the
`webhooks.config` file in project's `refs/meta/config` branch.
The configuration is inheritable.

Global @PLUGIN@ plugin configuration is stored in the `gerrit.config` file.

File 'webhooks.config'
----------------------

remote.NAME.url
: Address of the remote server to post events to.

remote.NAME.event
: Type of the event which will be posted to the remote url. Multiple event
  types can be specified, listing event types which should be posted.
  When no event type is configured, all events will be posted.

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
    Defaults to 1.