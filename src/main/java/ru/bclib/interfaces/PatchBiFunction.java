package ru.bclib.interfaces;

import ru.bclib.api.datafixer.MigrationProfile;
import ru.bclib.api.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchBiFunction<U, V, R> {
	R apply(U t, V v, MigrationProfile profile) throws PatchDidiFailException;
}