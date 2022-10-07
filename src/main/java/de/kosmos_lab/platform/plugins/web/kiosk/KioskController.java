package de.kosmos_lab.platform.plugins.web.kiosk;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.client.HTTPClient;
import de.kosmos_lab.platform.plugins.web.kiosk.websocket.KioskWebSocket;
import de.kosmos_lab.utils.FileUtils;
import de.kosmos_lab.utils.KosmosFileUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KioskController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KioskPersistence");

    private static KioskController instance;

    private final IController controller;
    private final File file;
    private final String[] types = {"video", "image", "page","drawing"};
    private File showfile;
    private ConcurrentHashMap<String, Set<JSONObject>> sources = new ConcurrentHashMap();

    private KioskWebSocket websocket;
    private KioskObjects objects;
    private JSONObject config;

    public KioskController(IController controller) {
        this.controller = controller;
        this.objects = new KioskObjects();
        File dir = controller.getFile("config/kiosk");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.error("could not create kiosk directory");
                controller.stop();

            }
        }
        this.file = controller.getFile("config/kiosk/targets.json");
        reload();
        this.save();

    }

    public static synchronized KioskController getInstance(IController controller) {
        if (instance == null) {
            instance = new KioskController(controller);
        }
        return instance;
    }


    public void reload() {
        this.config = null;
        if (file.exists()) {
            try {
                config = new JSONObject(FileUtils.readFile(file));
                this.objects.deleteAllFromSource("local");
                for (String type : types) {
                    try {
                        if (config.has(type)) {
                            JSONArray list = config.getJSONArray(type);
                            for (int i = 0; i < list.length(); i++) {
                                try {
                                    JSONObject entry = list.getJSONObject(i);
                                    this.objects.addEntry(KioskType.valueOf(type.toUpperCase()), entry.getString("name"), entry.getString("src"), "local");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                reloadRemoteSources();
            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (config == null) {
            config = new JSONObject();
        }


        for (String type : types) {
            if (!config.has(type)) {
                config.put(type, new JSONArray());
            }
        }
        if ( websocket != null ) {
            websocket.sendTargets();
            websocket.sendObjects();
        }
        //this.save();
    }

    private void delAllFromSource(String source) {
        Set<JSONObject> slist = sources.get(source);
        if (slist != null) {
            for (JSONObject json : slist) {
                for (String type : types) {
                    try {
                        JSONArray myList = new JSONArray();
                        if (json.has(type)) {
                            JSONArray arr = json.getJSONArray(type);

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void reloadRemoteSources() {
        JSONArray remotesSources = this.config.optJSONArray("remote");
        if (remotesSources == null) {
            return;
        }
        for (int i = 0; i < remotesSources.length(); i++) {
            try {
                String rs = remotesSources.getString(i);
                logger.info("Loading remote source {}", rs);
                HTTPClient c = new HTTPClient();
                this.objects.deleteAllFromSource(rs);
                JSONObject json = c.fetchJSONObject(rs, HttpMethod.GET);
                if (json != null) {
                    for (String type : types) {
                        try {
                            if (json.has(type)) {

                                JSONArray list = json.getJSONArray(type);
                                logger.info("{} {} on remote source {}", list.length(), type, rs);
                                for (int j = 0; j < list.length(); j++) {
                                    try {
                                        JSONObject entry = list.getJSONObject(j);
                                        objects.addEntry(KioskType.valueOf(type.toUpperCase()), entry.getString("name"), entry.getString("src"), rs);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    private void save() {
        KosmosFileUtils.writeToFile(file, config.toString());
    }

    public JSONObject getObjects() {
        return objects.toJSON();
    }


    public void sendMessage(JSONObject message) {
        if (websocket != null) {
            this.websocket.sendMessage(message);
        }

    }

    public void setWebSocket(KioskWebSocket kioskWebSocket) {
        this.websocket = kioskWebSocket;

        this.showfile = controller.getFile("config/kiosk/targetShow.json");
        if (showfile.exists()) {
            try {
                JSONObject json = new JSONObject(FileUtils.readFile(showfile));
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    this.websocket.saveShow(key, json.getJSONObject(key), false);

                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * get all valid targets
     *
     * @return
     */
    public JSONArray getTargets() {
        JSONArray arr = new JSONArray();
        if (websocket != null) {
            for (Entry<String, Set<Session>> entry : this.websocket.getMapTargetSessions().entrySet()) {
                if (entry.getValue().size() > 0) {
                    arr.put(entry.getKey());
                }
            }

        }
        return arr;


    }


    /**
     * saves the given targetShow to the disk
     *
     * @param mapTargetShow
     */
    public void saveTargetShows(ConcurrentHashMap<String, JSONObject> mapTargetShow) {

        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, JSONObject> entry : mapTargetShow.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }
        KosmosFileUtils.writeToFile(this.showfile, jsonObject.toString());


    }
}
