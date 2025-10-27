package com.application.sisacadepcc.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Content {

    private String name;
    private String type;
    private String url;
    private Long sizeBytes;

    protected Content() {}

    public Content(String name, String type, String url, Long sizeBytes) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.sizeBytes = sizeBytes;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public Long getSizeBytes() { return sizeBytes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content content)) return false;
        return Objects.equals(name, content.name) &&
                Objects.equals(type, content.type) &&
                Objects.equals(url, content.url) &&
                Objects.equals(sizeBytes, content.sizeBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, url, sizeBytes);
    }

    public boolean isPdf() {
        return type != null && type.equalsIgnoreCase("application/pdf");
    }

    public boolean isImage() {
        return type != null && type.startsWith("image/");
    }

    public boolean isVideo() {
        return type != null && type.startsWith("video/");
    }
}