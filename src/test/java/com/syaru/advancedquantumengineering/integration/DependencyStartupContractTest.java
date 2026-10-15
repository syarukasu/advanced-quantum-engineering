package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class DependencyStartupContractTest {
    @Test
    void packagedMetadataAcceptsTheActualAdvancedAeReleaseSuffix() throws Exception {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/neoforge.mods.toml")) {
            var metadata = new TomlParser().parse(
                    new String(input.readAllBytes(), StandardCharsets.UTF_8));
            List<UnmodifiableConfig> mixins = metadata.get("mixins");
            assertTrue(mixins.stream().anyMatch(mixin ->
                    "advanced_quantum_engineering.mixins.json".equals(mixin.get("config"))));
            List<UnmodifiableConfig> dependencies =
                    metadata.get("dependencies.advanced_quantum_engineering");
            String range = dependencies.stream()
                    .filter(dependency -> "advanced_ae".equals(dependency.get("modId")))
                    .map(dependency -> dependency.<String>get("versionRange"))
                    .findFirst().orElseThrow();
            var supported = VersionRange.createFromVersionSpec(range);
            assertTrue(supported.containsVersion(new DefaultArtifactVersion("1.6.11-1.21.1")));
            assertFalse(supported.containsVersion(new DefaultArtifactVersion("1.6.12")));
        }
    }

    @Test
    void startupRetainsAbiChecksWithoutRepeatingExactVersionStringChecks() throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(
                "com/syaru/advancedquantumengineering/integration/AQEDiagnostics.class")) {
            var calls = new java.util.HashSet<String>();
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String method,
                                String methodDescriptor, boolean isInterface) {
                            calls.add(method);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            assertTrue(calls.contains("checkAdvancedAeApi"));
            assertTrue(calls.contains("checkRequiredMixinTargets"));
            assertFalse(calls.contains("checkExactVersion"));
            assertFalse(calls.contains("checkExactDependencyVersions"));
        }
    }
}
