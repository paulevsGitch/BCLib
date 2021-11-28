package ru.bclib.interfaces;

import ru.bclib.api.datafixer.MigrationProfile;
import ru.bclib.api.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchFunction<T, R> {
	R apply(T t, MigrationProfile profile) throws PatchDidiFailException;
}
