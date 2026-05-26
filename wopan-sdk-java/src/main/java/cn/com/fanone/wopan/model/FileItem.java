package cn.com.fanone.wopan.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class FileItem {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String id;
    private final String fid;
    private final boolean folder;
    private final long size;
    private final Instant modified;
    private final String name;
    private final String thumbnail;
    private final int type;
    private final String fileType;

    public FileItem(String id, String fid, boolean folder, long size, String modifiedRaw, String name, String thumbnail, int type, String fileType) {
        this.id = id;
        this.fid = fid;
        this.folder = folder;
        this.size = size;
        this.modified = parse(modifiedRaw);
        this.name = name;
        this.thumbnail = thumbnail;
        this.type = type;
        this.fileType = fileType;
    }

    private Instant parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Instant.EPOCH;
        }
        LocalDateTime dateTime = LocalDateTime.parse(raw, FORMATTER);
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    public String getId() {
        return id;
    }

    public String getFid() {
        return fid;
    }

    public boolean isFolder() {
        return folder;
    }

    public long getSize() {
        return size;
    }

    public Instant getModified() {
        return modified;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public int getType() {
        return type;
    }

    public String getFileType() {
        return fileType;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "id='" + id + '\'' +
                ", fid='" + fid + '\'' +
                ", folder=" + folder +
                ", size=" + size +
                ", modified=" + modified +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileItem other)) return false;
        return Objects.equals(this.id, other.id) && Objects.equals(this.fid, other.fid);
    }
}
