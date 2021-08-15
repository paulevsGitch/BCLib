package ru.bclib.api.dataexchange.handler;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class AutoSyncID {
	static class WithContentOverride extends AutoSyncID {
		final FileContentWrapper contentWrapper;
		final File localFile;
		
		WithContentOverride(String modID, String uniqueID, FileContentWrapper contentWrapper, File localFile) {
			super(modID, uniqueID);
			this.contentWrapper = contentWrapper;
			this.localFile = localFile;
		}
	}
	/**
	 * A Unique ID for the referenced File.
	 * <p>
	 * Files with the same {@link #modID} need to have a unique IDs. Normally the filename from FileHash(String, File, byte[], int, int)
	 * is used to generated that ID, but you can directly specify one using FileHash(String, String, byte[], int, int).
	 */
	@NotNull
	public final String uniqueID;
	
	/**
	 * The ID of the Mod that is registering the File
	 */
	@NotNull
	public final String modID;
	
	public AutoSyncID(String modID, String uniqueID) {
		Objects.nonNull(modID);
		Objects.nonNull(uniqueID);
		
		this.modID = modID;
		this.uniqueID = uniqueID;
	}
	
	@Override
	public String toString() {
		return modID + "." + uniqueID;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AutoSyncID)) return false;
		AutoSyncID that = (AutoSyncID) o;
		return uniqueID.equals(that.uniqueID) && modID.equals(that.modID);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(uniqueID, modID);
	}
}
