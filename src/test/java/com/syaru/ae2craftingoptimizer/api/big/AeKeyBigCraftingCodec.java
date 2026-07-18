package com.syaru.ae2craftingoptimizer.api.big;

import com.syaru.ae2craftingoptimizer.engine.BigCraftingKeyCodec;

/** Test-only optional API fixture. */
public final class AeKeyBigCraftingCodec implements BigCraftingKeyCodec<Object> {
    public static final AeKeyBigCraftingCodec INSTANCE = new AeKeyBigCraftingCodec();

    private AeKeyBigCraftingCodec() {
    }
}
