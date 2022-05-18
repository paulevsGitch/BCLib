package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.datafixer.MigrationProfile;
import org.betterx.bclib.api.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchFunction<T, R> {
    R apply(T t, MigrationProfile profile) throws PatchDidiFailException;
}
