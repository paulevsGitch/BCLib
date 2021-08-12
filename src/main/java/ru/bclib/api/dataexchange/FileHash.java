package ru.bclib.api.dataexchange;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.handler.DataExchange;

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
public class FileHash extends DataExchange.AutoSyncID {
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



    FileHash(String modID, File file, byte[] md5, int size, int value) {
        this(modID, file.getName(), md5, size, value);
    }

    FileHash(String modID, String uniqueID, byte[] md5, int size, int value) {
        super(modID, uniqueID);
        Objects.nonNull(md5);

        this.md5 = md5;
        this.size = size;
        this.value = value;
    }

    private static int ERR_DOES_NOT_EXIST = -10;
    private static int ERR_IO_ERROR = -20;
    static FileHash createForEmpty(String modID, String uniqueID, int errCode){
        return new FileHash(modID, uniqueID, new byte[0], 0, errCode);
    }

    final static DataExchange.NeedTransferPredicate NEED_TRANSFER = (clientHash, serverHash, content)-> !clientHash.equals(serverHash);

    public boolean noFile() {
        return md5.length == 0;
    }

    @Override
    public String toString() {
        return super.toString()+": "+String.format("%08x", size)
            + "-"
            + String.format("%08x", value)
            + "-"
            + getMd5String();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileHash fileHash = (FileHash) o;
        return size == fileHash.size && value == fileHash.value && Arrays.equals(md5, fileHash.md5) && uniqueID.equals(fileHash.uniqueID) && modID.equals(fileHash.modID);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size, value, uniqueID, modID);
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
    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(size);
        buf.writeInt(value);
        buf.writeByteArray(md5);
        DataHandler.writeString(buf, modID);
        DataHandler.writeString(buf, uniqueID);
    }

    /**
     *Deserialize a Buffer to a new {@link FileHash}-Object
     * @param buf Thea buffer to read from
     * @return The received String
     */
    public static FileHash deserialize(FriendlyByteBuf buf){
        final int size = buf.readInt();
        final int value = buf.readInt();
        final byte[] md5 = buf.readByteArray();
        final String modID = DataHandler.readString(buf);
        final String uniqueID = DataHandler.readString(buf);

        return new FileHash(modID, uniqueID, md5, size, value);
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
     * <p>
     * Will call {@link #create(String, File, String)} using the name of the File as {@code uniqueID}.
     * @param modID ID of the calling Mod
     * @param file The input file
     *
     * @return A new Instance. You can compare instances using {@link #equals(Object)} to determine if two files are
     * identical. Will return {@code null} when an error occurs or the File does not exist
     */
    public static FileHash create(String modID, File file){
        return create(modID, file, file.getName());
    }

    /**
     * Create a new {@link FileHash}.
     * @param modID ID of the calling Mod
     * @param file The input file
     * @param uniqueID The unique ID that is used for this File (see {@link FileHash#uniqueID} for Details.
     * @return A new Instance. You can compare instances using {@link #equals(Object)} to determine if two files are
     * identical. Will return {@code null} when an error occurs or the File does not exist
     */
    public static FileHash create(String modID, File file, String uniqueID){
        if (!file.exists()) return createForEmpty(modID, uniqueID, ERR_DOES_NOT_EXIST);
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

            return new FileHash(modID, uniqueID, md5, size, value);
        } catch (IOException e) {
            BCLib.LOGGER.error("Failed to read file: " + file);
            return null;
        } catch (NoSuchAlgorithmException e) {
            BCLib.LOGGER.error("Unable to build hash for file: " + file);
        }

        return createForEmpty(modID, uniqueID,  ERR_IO_ERROR);
    }
}
