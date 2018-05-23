@PLUGIN@ - /config/ REST API
============================

This page describes the REST endpoints that are added by the @PLUGIN@ plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

<a id="webhooks-endpoints"> Webhooks Endpoints
----------------------------------------------

### <a id="list-webhooks"> List webhooks
_GET /config/server/@PLUGIN@~projects/[\{project-name\}](../../../Documentation/rest-api-projects.html#project-name)/remotes/_

List existing webhooks for a project.

#### Request

```
  GET /config/server/@PLUGIN@~projects/myProject/remotes/
```

As result a map is returned that maps remote name to [RemoteInfo](#remote-info) entity.

#### Response

```
  HTTP/1.1 200 OK
  Content-Disposition: attachment
  Content-Type: application/json; charset=UTF-8

  )]}'
  {
    "foo": {
      "url" : "https://foo.org/gerrit-events",
      "maxTries" = 3,
      "sslVerify": true,
    }
    "bar": {
      "url" : "https://bar.org/",
      "events" : ["patchset-created", "ref-updated"],
    }
  }
```

### <a id="get-webhook"> Get webhook
_GET /config/server/@PLUGIN@~projects/[\{project-name\}](../../../Documentation/rest-api-projects.html#project-name)/remotes/[\{remote-name\}]_

Get information about one webhook.

### Request

```
  GET /config/server/@PLUGIN@~projects/myProject/remotes/foo
```

As result a [RemoteInfo](#remote-info) entity is returned.

### Response

```
  HTTP/1.1 200 OK
  Content-Disposition: attachment
  Content-Type: application/json; charset=UTF-8

  )]}'
  {
    "url" : "https://foo.org/gerrit-events",
    "maxTries" = 3,
    "sslVerify": true,
  }
```

### <a id="remote-info"> RemoteInfo
The `RemoteInfo` contains information about a remote section in a `webhooks.config` file.

* _url_ : See [url](config.md#url)
* _events_ : List of events for which to invoke webhook. See [event](config.md#event)
* _connectionTimeout_ : See [connectionTimeout](config.md#connectionTimeout)
* _socketTimeout_ : See [socketTimeout](config.md#socketTimeout)
* _maxTries_ : See [maxTries](config.md#maxTries)
* _retryInterval_ : See [retryInterval](config.md#retryInterval)
* _sslVerify_ : See [sslVerify](config.md#sslVerify)
