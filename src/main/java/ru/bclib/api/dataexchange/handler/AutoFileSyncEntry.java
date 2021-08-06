package ru.bclib.api.dataexchange.handler;

import net.minecraft.network.FriendlyByteBuf;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.FileHash;
import ru.bclib.util.Pair;
import ru.bclib.util.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AutoFileSyncEntry extends DataExchange.AutoSyncID {
    public final DataExchange.NeedTransferPredicate needTransfer;
    public final File fileName;
    public final boolean requestContent;
    private FileHash hash;

    AutoFileSyncEntry(String modID, File fileName, boolean requestContent, DataExchange.NeedTransferPredicate needTransfer) {
        this(modID, fileName.getName(), fileName, requestContent, needTransfer);
    }

    AutoFileSyncEntry(String modID, String uniqueID, File fileName, boolean requestContent, DataExchange.NeedTransferPredicate needTransfer) {
        super(modID, uniqueID);
        this.needTransfer = needTransfer;
        this.fileName = fileName;
        this.requestContent = requestContent;
    }

    public FileHash getFileHash() {
        if (hash == null)
        {
            hash = FileHash.create(modID, fileName, uniqueID);
        }
        return hash;
    }

    public byte[] getContent() {
        if (!fileName.exists()) return new byte[0];
        final Path path = fileName.toPath();

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {

        }
        return new byte[0];
    }

    public int serializeContent(FriendlyByteBuf buf) {
        DataHandler.writeString(buf, modID);
        DataHandler.writeString(buf, uniqueID);
        return serializeFileContent(buf);
    }

    public static Triple<AutoFileSyncEntry, byte[], DataExchange.AutoSyncID> deserializeContent(FriendlyByteBuf buf) {
        final String modID = DataHandler.readString(buf);
        final String uniqueID = DataHandler.readString(buf);
        byte[] data = deserializeFileContent(buf);

        AutoFileSyncEntry entry = AutoFileSyncEntry.findMatching(modID, uniqueID);
        return new Triple<>(entry, data, new DataExchange.AutoSyncID(modID, uniqueID));
    }


    public void serialize(FriendlyByteBuf buf) {
        getFileHash().serialize(buf);
        buf.writeBoolean(requestContent);

        if (requestContent) {
            serializeFileContent(buf);
        }
    }

    public static DataExchange.AutoSyncTriple deserializeAndMatch(FriendlyByteBuf buf) {
        Pair<FileHash, byte[]> e = deserialize(buf);
        AutoFileSyncEntry match = findMatching(e.first);
        return new DataExchange.AutoSyncTriple(e.first, e.second, match);
    }

    public static Pair<FileHash, byte[]> deserialize(FriendlyByteBuf buf) {
        FileHash hash = FileHash.deserialize(buf);
        boolean withContent = buf.readBoolean();
        byte[] data = null;
        if (withContent) {
            data = deserializeFileContent(buf);
        }

        return new Pair(hash, data);
    }

    private int serializeFileContent(FriendlyByteBuf buf) {
        byte[] content = getContent();
        buf.writeInt(content.length);
        buf.writeByteArray(content);
        return content.length;
    }

    private static byte[] deserializeFileContent(FriendlyByteBuf buf) {
        byte[] data;
        int size = buf.readInt();
        data = buf.readByteArray(size);
        return data;
    }

    public static AutoFileSyncEntry findMatching(FileHash hash) {
        return findMatching(hash.modID, hash.uniqueID);
    }

    public static AutoFileSyncEntry findMatching(DataExchange.AutoSyncID aid) {
        return findMatching(aid.modID, aid.uniqueID);
    }

    public static AutoFileSyncEntry findMatching(String modID, String uniqueID) {
        return DataExchange
                .getInstance()
                .autoSyncFiles
                .stream()
                .filter(asf -> asf.modID.equals(modID) && asf.uniqueID.equals(uniqueID))
                .findFirst()
                .orElse(null);
    }
}
