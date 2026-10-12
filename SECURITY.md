# Security Policy

## Supported Versions

This will be determined later, after an 1.0.0 release.

## Reporting a Vulnerability

If you find a security issue, please open a Github issue.

## REST API of the ORM

The optional modules `janitor-orm-rest` and `janitor-orm-rest-servlet` expose entities of a DAO collection over HTTP. Nothing is exposed unless an entity opts in with a path, and nothing can be written unless it also opts in to the write operations. The API itself has no authentication: every request is decided by the `RestPolicy` and `RestUserResolver` that the host supplies. Without a restrictive policy, everybody who can reach the servlet can read all exposed entities, and write those that offer writing; sub-resources and links below an entity follow the same rules (see `RestPolicy.permitsLink`). Callers that are not permitted get 401 if nobody is authenticated and 403 otherwise; the API description (`/openapi.json`) is public unless the policy says otherwise (`permitsApiDescription`), and describes authentication only if the host declares it. See `janitor-orm-rest/README.md`.
