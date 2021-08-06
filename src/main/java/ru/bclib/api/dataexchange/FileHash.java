package ru.bclib.api.dataexchange;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import ru.bclib.BCLib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Calculates a hash based on the contents of a File.
 * <p>
 * A File-Hash contains the md5-sum of the File, as well as its size and byte-values from defined positions
 * <p>
 * You can compare instances using {@link #equals(Object)} to determine if two files are
 * identical.
 */
public class FileHash {
    /**
     * The md5-hash of the file
     */
    @NotNull
    public final byte[] md5;

    /**
     * The size (in bytes) of the input.
     */
    public final int size;

    /**
     * a value that is directly calculated from defined byte positions.
     */
    public final int value;

    FileHash(byte[] md5, int size, int value) {
        Objects.nonNull(md5);
        this.md5 = md5;
        this.size = size;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%08x", size)
            + "-"
            + String.format("%08x", value)
            + "-"
            + getMd5String();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileHash that = (FileHash) o;
        return size == that.size && value == that.value && Arrays.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size, value);
        result = 31 * result + Arrays.hashCode(md5);
        return result;
    }

    /**
     * Convert the md5-hash to a human readable string
     * @return The converted String
     */
    public String getMd5String(){
        return toHexString(md5);
    }
    /**
     * Serializes the Object to a buffer
     * @param buf The buffer to write to
     */
    public void writeString(FriendlyByteBuf buf) {
        buf.writeInt(size);
        buf.writeInt(value);
        buf.writeByteArray(md5);
    }

    /**
     *Deserialize a Buffer to a new {@link FileHash}-Object
     * @param buf Thea buffer to read from
     * @return The received String
     */
    public static FileHash readString(FriendlyByteBuf buf){
        final int size = buf.readInt();
        final int value = buf.readInt();
        final byte[] md5 = buf.readByteArray();

        return new FileHash(md5, size, value);
    }

    /**
     * Converts a byte-array to a hex-string representation
     * @param bytes The source array
     * @return The resulting string, or an empty String if the input was {@code null}
     */
    public static String toHexString(byte[] bytes) {
        if (bytes==null) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Create a new {@link FileHash}.
     * @param file The input file
     * @return A new Instance. You can compare instances using {@link #equals(Object)} to determine if two files are
     * identical.
     */
    public static FileHash createFromBinary(File file){
        if (!file.exists()) return null;
        final Path path = file.toPath();

        int size = 0;
        byte[] md5 = new byte[0];
        int value = 0;

        try {
            byte[] data = Files.readAllBytes(path);

            size = data.length;

            value = size>0 ? (data[size/3] | (data [size/2]<<8) | (data [size/5]<<16)) : -1;
            if (size>20) value |= data[20]<<24;

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            md5 = md.digest();

            return new FileHash(md5, size, value);
        } catch (IOException e) {
            BCLib.LOGGER.error("Failed to read file: " + file);
            return null;
        } catch (NoSuchAlgorithmException e) {
            BCLib.LOGGER.error("Unable to build hash for file: " + file);
        }
    }
}
