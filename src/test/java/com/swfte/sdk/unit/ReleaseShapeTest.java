package com.swfte.sdk.unit;

import com.swfte.sdk.SdkVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Keeps the pom, README, CHANGELOG, User-Agent and release workflow telling the same story. */
class ReleaseShapeTest {

    private static String read(String rel) throws IOException {
        Path p = Paths.get(rel);
        assertTrue(Files.exists(p), rel + " must exist (tests run from the module root)");
        return Files.readString(p);
    }

    private static String pomVersion() throws IOException {
        // first <version> after the artifactId is the project version
        Matcher m = Pattern.compile("<artifactId>swfte-sdk</artifactId>\\s*<version>([^<]+)</version>")
            .matcher(read("pom.xml"));
        assertTrue(m.find());
        return m.group(1);
    }

    @Test
    void pomVersionIs120() throws IOException {
        assertEquals("1.2.0", pomVersion());
    }

    @Test
    void sdkVersionConstantEqualsPomVersion() throws IOException {
        assertEquals(pomVersion(), SdkVersion.VERSION);
    }

    @Test
    void readmeInstallSnippetsUseThePomVersion() throws IOException {
        String readme = read("README.md");
        String v = pomVersion();
        assertTrue(readme.contains("<version>" + v + "</version>"), "Maven snippet");
        assertTrue(readme.contains("com.swfte:swfte-sdk:" + v + "'"), "Gradle snippet");
        assertFalse(readme.contains("pypi.org/project/swfte/"), "unregistered PyPI name");
        Matcher m = Pattern.compile("com\\.swfte:swfte-sdk:(\\d+\\.\\d+\\.\\d+)").matcher(readme);
        while (m.find()) {
            assertEquals(v, m.group(1));
        }
    }

    @Test
    void changelogHasADatedSectionForThePomVersion() throws IOException {
        assertTrue(Pattern.compile("(?m)^## \\[" + Pattern.quote(pomVersion()) + "\\] - \\d{4}-\\d{2}-\\d{2}$")
            .matcher(read("CHANGELOG.md")).find());
    }

    @Test
    void jacksonIsAtLeast218Patch10() throws IOException {
        Matcher m = Pattern.compile("<jackson.version>(\\d+)\\.(\\d+)\\.(\\d+)</jackson.version>").matcher(read("pom.xml"));
        assertTrue(m.find());
        int major = Integer.parseInt(m.group(1)), minor = Integer.parseInt(m.group(2)), patch = Integer.parseInt(m.group(3));
        assertTrue(major > 2 || minor > 18 || (minor == 18 && patch >= 10), "jackson " + m.group(0));
    }

    @Test
    void everyWorkflowActionIsPinnedToAFullShaWithATagComment() throws IOException {
        for (String f : new String[] {".github/workflows/release.yml", ".github/workflows/ci.yml"}) {
            Matcher m = Pattern.compile("(?m)^\\s*(?:-\\s+)?uses:\\s*(\\S+)(.*)$").matcher(read(f));
            int n = 0;
            while (m.find()) {
                n++;
                assertTrue(m.group(1).matches("[\\w./-]+@[0-9a-f]{40}"), f + ": " + m.group(1));
                assertTrue(m.group(2).matches("\\s*# v\\d.*"), f + ": " + m.group(1) + m.group(2));
            }
            assertTrue(n > 0, f);
        }
    }

    @Test
    void ciHasExplicitReadOnlyContentsPermission() throws IOException {
        String ci = read(".github/workflows/ci.yml");
        assertTrue(Pattern.compile("(?m)^permissions:\\n  contents: read\\n").matcher(ci).find());
        assertFalse(Pattern.compile("\\b(?:contents|id-token|actions|packages): write\\b").matcher(ci).find());
    }

    @Test
    void githubReleaseRunsInItsOwnJobAwayFromTheSigningKey() throws IOException {
        String wf = read(".github/workflows/release.yml");
        int publish = wf.indexOf("\n  publish:");
        int release = wf.indexOf("\n  github-release:");
        assertTrue(publish > 0 && release > publish);
        String publishJob = wf.substring(publish, release);
        String releaseJob = wf.substring(release);
        assertFalse(publishJob.contains("softprops"), "release action must not share the signing job");
        assertFalse(publishJob.contains("contents: write"));
        assertTrue(publishJob.contains("GPG_PRIVATE_KEY"));
        assertFalse(releaseJob.contains("secrets."), "release job must not see secrets");
        assertTrue(releaseJob.contains("target_commitish: ${{ github.sha }}"));
        assertTrue(publishJob.contains("github.ref == 'refs/heads/main'"));
    }

    @Test
    void alreadyPublishedCheckAsksRepo1AndFailsClosed() throws IOException {
        String wf = read(".github/workflows/release.yml");
        assertTrue(wf.contains("repo1.maven.org/maven2/com/swfte/swfte-sdk/"));
        assertFalse(wf.contains("search.maven.org"));
    }

    @Test
    void runtimeLegalResourceContainsTheExactRootLicense() throws Exception {
        // Generic class-loader resource lookup can resolve a dependency's LICENSE.
        java.nio.file.Path legal = Paths.get(SdkVersion.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).resolve("META-INF/LICENSE");
        assertTrue(Files.isRegularFile(legal), "the SDK's own compiled output must carry its license resource");
        assertArrayEquals(Files.readAllBytes(Paths.get("LICENSE")), Files.readAllBytes(legal));
        assertEquals(read("LICENSE"), read("src/main/legal/LICENSE"));
    }

    @Test
    void ciDoesNotUploadAJaCoCoReportWithoutAGenerator() throws IOException {
        String ci = read(".github/workflows/ci.yml");
        assertFalse(ci.contains("codecov"));
        assertFalse(ci.contains("jacoco.xml"));
        assertTrue(ci.contains("mvn verify -B"));
        assertTrue(ci.contains("java-version: [11, 17, 21]"));
    }
}
