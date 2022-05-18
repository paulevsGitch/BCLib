package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.datafixer.MigrationProfile;
import org.betterx.bclib.api.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchBiFunction<U, V, R> {
    R apply(U t, V v, MigrationProfile profile) throws PatchDidiFailException;
}