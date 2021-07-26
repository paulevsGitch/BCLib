package ru.bclib.api.datafixer;

@FunctionalInterface
public interface PatchFunction<T, R> {
	R apply(T t) throws PatchDidiFailException;
}
