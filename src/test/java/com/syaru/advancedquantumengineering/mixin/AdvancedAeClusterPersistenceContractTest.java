package com.syaru.advancedquantumengineering.mixin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AdvancedAeClusterPersistenceContractTest {
    private static final String TARGET_CLASS =
            "net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster";
    /** Minecraft 1.21.1では保存・復元の双方にRegistry Providerが必要。 */
    private static final String PERSISTENCE_DESCRIPTOR =
            "(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V";

    @Test
    void productionClusterUsesRegistryAwarePersistence() throws Exception {
        String resourceName = TARGET_CLASS.replace('.', '/') + ".class";
        InputStream classBytes =
                AdvancedAeClusterPersistenceContractTest.class
                        .getClassLoader()
                        .getResourceAsStream(resourceName);
        assertNotNull(classBytes, "AdvancedAE crafting cluster is missing");

        Set<String> methods = new HashSet<>();
        try (InputStream input = classBytes) {
            new ClassReader(input)
                    .accept(
                            new ClassVisitor(Opcodes.ASM9) {
                                @Override
                                public MethodVisitor visitMethod(
                                        int access,
                                        String name,
                                        String descriptor,
                                        String signature,
                                        String[] exceptions) {
                                    methods.add(name + descriptor);
                                    return null;
                                }
                            },
                            ClassReader.SKIP_CODE
                                    | ClassReader.SKIP_DEBUG
                                    | ClassReader.SKIP_FRAMES);
        }

        assertTrue(methods.contains("writeToNBT" + PERSISTENCE_DESCRIPTOR));
        assertTrue(methods.contains("readFromNBT" + PERSISTENCE_DESCRIPTOR));
    }
}
