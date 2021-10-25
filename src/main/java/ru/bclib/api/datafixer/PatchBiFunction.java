package ru.bclib.api.datafixer;

@FunctionalInterface
public interface PatchBiFunction<U, V, R> {
	R apply(U t, V v, MigrationProfile profile) throws PatchDidiFailException;
}