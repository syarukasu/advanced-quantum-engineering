package com.syaru.advancedquantumengineering.mixin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AdvancedAeBytecodeContractTest {
    private static final String CLUSTER =
            "net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster";
    private static final String BLOCK_ENTITY =
            "net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity";

    @Test
    void oneTwentySourceDependencyMatchesTheManifest() throws IOException {
        String manifest = Files.readString(Path.of("docs/contracts/1.20.1.json"));
        assertTrue(manifest.contains("\"minecraft\": \"1.20.1\""));
        assertTrue(manifest.contains("\"advanced_ae\": \"1.3.5\""));
        assertTrue(manifest.contains("\"ae2\": \"15.4.10\""));
        assertTrue(manifest.contains("\"match_count\": 1"));

        Contract cluster = inspect(CLUSTER);
        assertEquals("(Lnet/pedroksl/advanced_ae/common/entities/AdvCraftingBlockEntity;)V",
                cluster.methods.get("addBlockEntity"));
        assertEquals("()V", cluster.methods.get("recalculateRemainingStorage"));
        assertEquals("()I", cluster.methods.get("getCoProcessors"));
        assertEquals("()V", cluster.methods.get("destroy"));
        assertEquals("()V", cluster.methods.get("breakCluster"));
        assertEquals("()Lnet/minecraft/world/level/Level;", cluster.methods.get("getLevel"));
        assertEquals("(Lnet/minecraft/nbt/CompoundTag;)V",
                cluster.methods.get("writeToNBT"));
        assertEquals("(Lnet/minecraft/nbt/CompoundTag;)V",
                cluster.methods.get("readFromNBT"));
        assertEquals("I", cluster.fields.get("accelerator"));
        assertEquals("I", cluster.fields.get("acceleratorMultiplier"));
        assertEquals("J", cluster.fields.get("storage"));
        assertEquals("J", cluster.fields.get("storageMultiplier"));
        assertEquals("J", cluster.fields.get("remainingStorage"));
        assertEquals("Ljava/util/HashMap;", cluster.fields.get("activeCpus"));
        assertEquals("Ljava/util/List;", cluster.fields.get("blockEntities"));
        assertEquals(1, cluster.singleUnitThreadGuardMatches);

        Contract blockEntity = inspect(BLOCK_ENTITY);
        assertEquals("()J", blockEntity.methods.get("getStorageBytes"));
        assertEquals("()I", blockEntity.methods.get("getStorageMultiplier"));
        assertEquals("()I", blockEntity.methods.get("getAcceleratorThreads"));
        assertEquals("()I", blockEntity.methods.get("getAccelerationMultiplier"));
        assertEquals("()Z", blockEntity.methods.get("isFormed"));
        assertEquals("()V", blockEntity.methods.get("breakCluster"));

        Contract tooltip = inspect("appeng.core.localization.Tooltips");
        assertEquals("(J)Lappeng/core/localization/Tooltips$Amount;",
                tooltip.methods.get("getByteAmount"));
    }

    @Test
    void currentContractDocumentsTheForgeSourceAssumptions() throws IOException {
        String implementation = Files.readString(Path.of("docs/IMPLEMENTATION.md"));
        String testing = Files.readString(Path.of("docs/TESTING.md"));
        String research = Files.readString(Path.of("docs/RESEARCH.md"));
        assertTrue(implementation.contains("15.4.10"));
        assertTrue(implementation.contains("1.3.5"));
        assertTrue(testing.contains("15.4.10"));
        assertTrue(testing.contains("1.3.5"));
        assertTrue(research.contains("15.4.10"));
        assertTrue(research.contains("1.3.5"));
    }

    private static Contract inspect(String className) {
        String resourceName = className.replace('.', '/') + ".class";
        try (var input = AdvancedAeBytecodeContractTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(input, "Dependency class is missing: " + className);
            Contract contract = new Contract();
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public org.objectweb.asm.FieldVisitor visitField(
                        int access, String name, String descriptor, String signature, Object value) {
                    contract.fields.put(name, descriptor);
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(
                        int access, String name, String descriptor, String signature, String[] exceptions) {
                    contract.methods.put(name, descriptor);
                    if ("addBlockEntity".equals(name)) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitIntInsn(int opcode, int operand) {
                                if (operand == 16) {
                                    contract.singleUnitThreadGuardMatches++;
                                }
                            }

                            @Override
                            public void visitLdcInsn(Object value) {
                                if (Integer.valueOf(16).equals(value)) {
                                    contract.singleUnitThreadGuardMatches++;
                                }
                            }
                        };
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return contract;
        } catch (IOException failure) {
            throw new IllegalStateException("Could not inspect dependency class " + className, failure);
        }
    }

    private static final class Contract {
        private final Map<String, String> methods = new HashMap<>();
        private final Map<String, String> fields = new HashMap<>();
        private int singleUnitThreadGuardMatches;
    }
}
