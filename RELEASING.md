# Releasing `com.swfte:swfte-sdk`

**Nothing has ever been published from this repository.** Maven Central has zero
artifacts under `com.swfte`, and the previous configuration could not have
produced any: it deployed to `s01.oss.sonatype.org`, a staging host Sonatype has
retired, which now answers 404.

This document is the list of things that must be true before a release can work.
Four of them require a person; none can be done from CI.

---

## What has to exist first (one-time)

### 1. The `com.swfte` namespace, verified on the Central Portal

Maven Central will not accept a coordinate you have not proved you own. Register
at <https://central.sonatype.com>, add the `com.swfte` namespace, and complete
the DNS challenge — Sonatype issues a token to publish as a `TXT` record on
`swfte.com`. Verification is usually minutes once the record propagates.

This is the longest-lead item. Start it before the rest.

### 2. A Central Portal user token

Portal → your account → **Generate User Token**. It returns a username and a
password; neither is your login. Store them as repository secrets:

| Secret | Value |
|---|---|
| `CENTRAL_TOKEN_USERNAME` | the token username |
| `CENTRAL_TOKEN_PASSWORD` | the token password |

### 3. A GPG signing key

Central rejects unsigned bundles. Generate a key, publish the **public** half to
a keyserver (`keys.openpgp.org`), and store the private half:

```bash
gpg --full-generate-key                      # RSA 4096, no expiry is fine
gpg --list-secret-keys --keyid-format=long   # note the key id
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
gpg --armor --export-secret-keys <KEY_ID>    # the value for GPG_PRIVATE_KEY
```

| Secret | Value |
|---|---|
| `GPG_PRIVATE_KEY` | the full ASCII-armoured private key, `-----BEGIN` line included |
| `GPG_PASSPHRASE` | the passphrase for that key |

### 4. The `maven-publish-prod` environment

Settings → Environments → create `maven-publish-prod`. Add required reviewers if
the plan allows it; the workflow's typed-version confirmation stands either way.

---

## Cutting a release

1. Bump `<version>` in `pom.xml` and add a `CHANGELOG.md` entry. Merge that.
2. Optionally tag `v<version>` and push. **This builds and verifies. It does not
   publish** — that is deliberate, and is the opposite of what the old workflow
   did.
3. Actions → **Release** → *Run workflow*:
   - `publish`: ✅
   - `confirm_version`: type the version exactly, e.g. `1.1.1`
4. The run uploads a **validated but unpublished** bundle to the Portal.
5. Go to <https://central.sonatype.com/publishing/deployments>, look at it, and
   press **Publish**.

Step 5 is not automated on purpose. A Maven Central coordinate cannot be yanked,
deleted, or replaced — not after 72 hours, not ever. `autoPublish` is `false` in
the pom's `release` profile so the irreversible step is taken by a person who
has just looked at what they are releasing.

---

## What the pipeline checks before it lets you publish

- the tag matches `pom.xml` (a tag that disagrees fails rather than surprising you)
- the version is not already on Maven Central
- `mvn verify` passes
- **the compiled jar's default `baseUrl` is `https://api.swfte.com/agents/v2/gateway`**

That last one is there because that exact default shipped wrong: it omitted
`/agents`, so every caller who took the default got a bare nginx 403. It is
asserted against the built artefact rather than the source, because what ships
is the artefact.
