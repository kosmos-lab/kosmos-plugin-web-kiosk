package de.kosmos_lab.platform.plugins.web.kiosk;

import org.json.JSONObject;

import javax.annotation.Nonnull;

public class KioskEntry {
    private final String name;
    private final String url;
    private final String source;

    public KioskEntry(@Nonnull String name, @Nonnull String url, @Nonnull String source) {
        this.name = name;
        this.url = url;
        this.source = source;
    }

    public @Nonnull String getName() {
        return name;
    }

    public @Nonnull String getUrl() {
        return url;
    }

    public @Nonnull String getSource() {
        return source;
    }

    public boolean equals(KioskEntry obj) {
        return name.equals(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(obj);
    }

    public JSONObject toJSON() {
        return new JSONObject().put("name",name).put("src",url);
    }
}
