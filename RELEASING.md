# Releasing `com.swfte:swfte-sdk`

Current version **1.2.0** (single source: `<version>` in `pom.xml`). Nothing has
been published yet: Maven Central has no artifacts under `com.swfte`.

A Maven Central coordinate can never be yanked, deleted or replaced, so the last
step (pressing Publish in the Portal) is deliberately manual.

## Owner setup (one time; only a person can do these)

1. **Namespace.** <https://central.sonatype.com>, Namespaces, add `com.swfte`,
   publish the `TXT` record Sonatype shows on `swfte.com`, press Verify. This
   proves domain ownership for the groupId and is the longest-lead item. Nothing
   can be tested end to end before it shows Verified.
2. **Portal user token.** Account, Generate User Token. It returns a username and
   password (neither is your login).
3. **GPG signing key (keep the primary key offline).**
   - Create a certify-only primary key on a hardware token or encrypted offline
     storage, plus a signing **subkey** that expires in 1-2 years.
   - Publish the public key to `keys.openpgp.org` (confirm the email there) and
     `keyserver.ubuntu.com`; Central validates signatures against them.
   - Export only the subkey: `gpg --armor --export-secret-subkeys <SUBKEY_ID>!`
   - Keep the revocation certificate offline.
4. **Environment `maven-publish-prod`** (Settings, Environments):
   - Deployment branches: `main` only. Required reviewers: at least two people who
     are not the person dispatching; enable "prevent self-review".
   - Add the four **environment secrets** here (not repository secrets, so no
     other workflow or branch can read them):

     | Secret | Value |
     |---|---|
     | `CENTRAL_TOKEN_USERNAME` | Portal token username |
     | `CENTRAL_TOKEN_PASSWORD` | Portal token password |
     | `GPG_PRIVATE_KEY` | armored signing subkey export (`-----BEGIN` line included) |
     | `GPG_PASSPHRASE` | its passphrase |
5. **Branch protection on `main`** (pull request required, no force pushes).

## Publish procedure

1. Merge the release commit to `main`. `pom.xml` `<version>`, the README install
   snippets and a dated `CHANGELOG.md` heading must agree (`ReleaseShapeTest`
   enforces this).
2. Confirm the version is free and the namespace verified:
   `curl -s -o /dev/null -w '%{http_code}' https://repo1.maven.org/maven2/com/swfte/swfte-sdk/<version>/swfte-sdk-<version>.pom`
   must print `404`.
3. Optional: `git tag v<version> && git push origin v<version>`. A tag push only
   builds and verifies (never publishes); the tag must equal the pom version.
4. Actions, **Release**, *Run workflow* on `main`: tick `publish` and type the exact
   version into `confirm_version`. A dispatch from any other branch does not reach
   the publish job.
5. An approver approves the paused `publish` job. It runs
   `mvn -B -P release deploy -DskipTests`: signs the jar, sources, javadoc and pom
   and uploads a bundle that is **validated but not published**
   (`autoPublish=false`). The separate `github-release` job then creates the
   GitHub Release for the built commit.
6. Central Portal, Deployments: inspect the bundle (jar, sources, javadoc, pom,
   `.asc`, checksums), then press **Publish**. Irreversible.
7. After the sync (typically 10-30 minutes) verify from an empty local repository:
   `mvn dependency:get -Dartifact=com.swfte:swfte-sdk:<version> -Dtransitive=true`
   and `gpg --verify swfte-sdk-<version>.jar.asc` against the keyserver key.

## What the pipeline checks before upload

- the tag matches `pom.xml`
- the version is not on Maven Central (asks `repo1.maven.org`, fails closed)
- `mvn verify` passes
- the compiled jar's default `baseUrl` is `https://api.swfte.com/agents/v2/gateway`
