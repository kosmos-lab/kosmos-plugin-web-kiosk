package de.kosmos_lab.platform.plugins.web.kiosk;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class KioskObjects {
    private Map<KioskType, HashSet<KioskEntry>> map;
    private ReentrantLock lock = new ReentrantLock();
    private JSONObject cache = null;

    public KioskObjects() {
        map = new ConcurrentHashMap<KioskType, HashSet<KioskEntry>>();
    }

    private JSONArray getJSON(KioskType type) {

        JSONArray arr = new JSONArray();

        HashSet<KioskEntry> set = map.get(type);
        if (set != null) {
            for (KioskEntry entry : set) {
                arr.put(entry.toJSON());
            }
        }

        return arr;
    }

    public JSONObject toJSON() {
        if (cache != null) {
            return cache;
        }
        JSONObject json = new JSONObject();
        lock.lock();
        try {
            json.put("image", getJSON(KioskType.IMAGE));
            json.put("video", getJSON(KioskType.VIDEO));
            json.put("page", getJSON(KioskType.PAGE));
            json.put("drawing", getJSON(KioskType.DRAWING));
            cache = json;
        } finally {
            lock.unlock();
        }
        return json;
    }

    public void deleteAllFromSource(String source) {
        lock.lock();
        try {
            for (HashSet<KioskEntry> set : map.values()) {
                HashSet<KioskEntry> toRemove = new HashSet<>();
                Iterator<KioskEntry> it = set.iterator();
                while (it.hasNext()) {
                    KioskEntry entry = it.next();
                    if (entry.getSource().equals(source)) {
                        it.remove();
                    }
                }
                /*for (KioskEntry entry : toRemove) {
                    set.remove(entry);
                }*/

            }

        } finally {
            lock.unlock();
            cache = null;
        }
    }

    public void addEntry(KioskType type, KioskEntry entry) {
        lock.lock();
        try {
            HashSet<KioskEntry> set = map.get(type);
            if (set == null) {
                set = new HashSet<>();
                map.put(type, set);
            }
            set.add(entry);

        } finally {
            lock.unlock();
            cache = null;
        }

    }

    public void addEntry(KioskType type, String name, String url, String source) {
        addEntry(type, new KioskEntry(name, url, source));

    }


}
