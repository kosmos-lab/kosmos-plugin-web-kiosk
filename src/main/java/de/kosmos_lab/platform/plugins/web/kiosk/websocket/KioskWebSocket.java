package de.kosmos_lab.platform.plugins.web.kiosk.websocket;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskConstants;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskController;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.Channel;
import de.kosmos_lab.web.doc.openapi.Message;
import de.kosmos_lab.web.doc.openapi.WebSocketEndpoint;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import de.kosmos_lab.web.server.WebSocketService;
import io.netty.util.internal.ConcurrentSet;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Extension
@ObjectSchema(
        componentName = "kiosk_media",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the media",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "src",
                        schema = @Schema(
                                description = "The source of the media",
                                type = SchemaType.STRING,
                                required = true

                        )
                )
        },
        examples = {
                @ExampleObject(
                        name = "cat",
                        value = "{\"src\":\"https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_960_720.jpg\",\"name\":\"cat2\"}"
                )
        }
)

@ObjectSchema(
        componentName = "kiosk_auth_success",
        properties = {
                @SchemaProperty(name = "type",
                        schema = @Schema(
                                type = SchemaType.STRING,
                                allowableValues = {KioskConstants.WS_Type_authSuccess}
                        )
                )
        }

)
@ObjectSchema(
        componentName = "kiosk_auth_failed",
        properties = {
                @SchemaProperty(name = "type",
                        schema = @Schema(
                                type = SchemaType.STRING,
                                allowableValues = {KioskConstants.WS_Type_authFailed}
                        )
                )
        }

)
@ObjectSchema(
        componentName = "kiosk_objectList",
        properties = {

                @SchemaProperty(
                        name = "page",
                        array = @ArraySchema(
                                uniqueItems = true,
                                arraySchema = @Schema(
                                        description = "List of pages",
                                        ref = "#/components/schemas/kiosk_media",
                                        required = true
                                )
                        )

                ),
                @SchemaProperty(
                        name = "image",
                        array = @ArraySchema(
                                uniqueItems = true,
                                arraySchema = @Schema(
                                        description = "List of images",
                                        ref = "#/components/schemas/kiosk_media",
                                        required = true
                                )
                        )

                ),
                @SchemaProperty(
                        name = "video",
                        array = @ArraySchema(
                                uniqueItems = true,
                                arraySchema = @Schema(
                                        description = "List of videos",
                                        ref = "#/components/schemas/kiosk_media",
                                        required = true
                                )
                        )

                ),
        },
        examples = {
                @ExampleObject(
                        name = "show image on test2",
                        value = "{\"image\":[{\"src\":\"https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_960_720.jpg\",\"name\":\"cat2\"},{\"src\":\"https://cdn.pixabay.com/photo/2014/11/30/14/11/cat-551554_960_720.jpg\",\"name\":\"cat1\"}],\"page\":[{\"src\":\"https://dfki.de\",\"name\":\"dfki.de\"}],\"video\":[{\"src\":\"https://foo.bar/video.mp4\",\"name\":\"test video\"}]}"
                )
        }
)
@Message(

        name = "kiosk_show_video",
        payload = @ObjectSchema(
                componentName = "kiosk_show_video",
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {"show-video"}, required = true)),
                        @SchemaProperty(name = "target", schema = @Schema(type = SchemaType.STRING, description = "The target to show the video on", required = true)),
                        @SchemaProperty(name = "value", schema = @Schema(
                                type = SchemaType.STRING,
                                description = "Tells the client to display a specific video",
                                required = true
                        )
                        )
                },
                examples = {
                        @ExampleObject(
                                name = "show video on test2",
                                value = "{\"type\":\"show-image\",\"target\":\"test2\",\"value\":\"https://foo.bar/image.mp4\"}"
                        )
                }
        )
)
@Message(

        name = "kiosk_show_page",
        payload = @ObjectSchema(
                componentName = "kiosk_show_page",
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {"show-page"}, required = true)),
                        @SchemaProperty(name = "target", schema = @Schema(type = SchemaType.STRING, description = "The target to show the page on", required = true)),
                        @SchemaProperty(name = "value", schema = @Schema(
                                type = SchemaType.STRING,
                                description = "Tells the client to display a specific page",
                                required = true
                        )
                        )
                },
                examples = {
                        @ExampleObject(
                                name = "show page on test2",
                                value = "{\"type\":\"show-image\",\"target\":\"test2\",\"value\":\"https://foo.bar/\"}"
                        )
                }
        )
)
@Message(

        name = "kiosk_show_image",
        payload = @ObjectSchema(
                componentName = "kiosk_show_image",
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {"show-image"}, required = true)),
                        @SchemaProperty(name = "target", schema = @Schema(type = SchemaType.STRING, description = "The target to show the image on", required = true)),
                        @SchemaProperty(name = "value", schema = @Schema(
                                type = SchemaType.STRING,
                                description = "Tells the client to display a specific image",
                                required = true
                        )
                        )
                },
                examples = {
                        @ExampleObject(
                                name = "show image on test2",
                                value = "{\"type\":\"show-image\",\"target\":\"test2\",\"value\":\"https://foo.bar/image.png\"}"
                        )
                }
        )
)
@Message(

        name = "kiosk_target_list",
        payload = @ObjectSchema(
                componentName = "kiosk_target_list",
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {KioskConstants.WS_Type_targetList}, required = true)),


                        @SchemaProperty(name = "value", array = @ArraySchema(
                                schema = @Schema(type = SchemaType.STRING)
                        )
                        )
                },
                examples = {
                        @ExampleObject(
                                name = "show image on test2",
                                value = "{\"type\":\"show-image\",\"target\":\"test2\",\"value\":\"https://foo.bar/image.png\"}"
                        )
                }
        )
)
@Message(

        name = "kiosk_object_list",
        payload = @ObjectSchema(
                componentName = "kiosk_object_list",
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {KioskConstants.WS_Type_objectList}, required = true)),
                        @SchemaProperty(name = "target", schema = @Schema(type = SchemaType.STRING, description = "The target to show the image on", required = true)),

                        @SchemaProperty(name = "value", schema = @Schema(ref = "#/components/schemas/kiosk_objectList"
                        )
                        )
                },
                examples = {
                        @ExampleObject(
                                name = "show image on test2",
                                value = "{\"type\":\"show-image\",\"target\":\"test2\",\"value\":\"https://foo.bar/image.png\"}"
                        )
                }
        )
)
@Message(

        name = "kiosk_ping",
        payload = @ObjectSchema(
                properties = {
                        @SchemaProperty(name = "type",
                                schema = @Schema(
                                        type = SchemaType.STRING,
                                        allowableValues = {KioskConstants.WS_Type_ping}
                                )
                        )
                }

        )
)
@Message(

        name = "kiosk_auth",
        payload = @ObjectSchema(
                properties = {
                        @SchemaProperty(name = "type", schema = @Schema(type = SchemaType.STRING, allowableValues = {KioskConstants.WS_Type_auth}, required = true)),
                        @SchemaProperty(name = "username", schema = @Schema(type = SchemaType.STRING, required = true)),
                        @SchemaProperty(name = "password", schema = @Schema(type = SchemaType.STRING, required = true))
                },
                examples = {
                        @ExampleObject(
                                name = "login example",
                                value = "{\"type\":\"" + KioskConstants.WS_Type_auth + "\",\"username\":\"user\",\"password\":\"secret\"}"
                        )
                }
        ),
        xResponseSchema = {@Schema(ref =
                "#/components/schemas/kiosk_auth_failed"), @Schema(ref =
                "#/components/schemas/kiosk_auth_success")
        }

)


@Message(name = "kiosk_list_targets", payload = @ObjectSchema(
        componentName = "kiosk_list_targets",
        properties = {
                @SchemaProperty(name = "type",
                        schema = @Schema(
                                type = SchemaType.STRING,
                                allowableValues = {KioskConstants.listTargets}
                        )
                )
        }

))

@Message(

        name = "kiosk_auth_failed",
        payloadSchema = @Schema(ref =
                "#/components/schemas/kiosk_auth_failed")
)
@Message(
        name = "kiosk_auth_success",
        payload = @ObjectSchema(
                componentName = "kiosk_auth_success",
                properties = {
                        @SchemaProperty(name = "type",
                                schema = @Schema(
                                        type = SchemaType.STRING,
                                        allowableValues = {KioskConstants.WS_Type_authSuccess}
                                )
                        )
                }

        )
)


@Message(name = "kiosk_list_objects",
        payload =
        @ObjectSchema(
                componentName = "kiosk_list_objects",
                properties = {
                        @SchemaProperty(name = "type",
                                schema = @Schema(
                                        type = SchemaType.STRING,
                                        allowableValues = {KioskConstants.listObjects}
                                )
                        )
                }

        )
)


@WebSocketEndpoint(


        path = "/kioskws",
        channels = @Channel(
                description = "This websocket is used by the kiosk plugin. It is used by clients and admins of the kiosk application alike to exchange messages",
                tags = {@Tag(name = "kiosk")},
                subscribeRefs = {
                        "#/components/messages/kiosk_list_objects",
                        "#/components/messages/kiosk_list_targets",
                        "#/components/messages/kiosk_ping",
                        "#/components/messages/kiosk_auth"
                },
                publishRefs = {
                        "#/components/messages/kiosk_show_image",
                        "#/components/messages/kiosk_show_page",
                        "#/components/messages/kiosk_show_video",
                        "#/components/messages/kiosk_auth_failed",
                        "#/components/messages/kiosk_auth_success",
                        "#/components/messages/kiosk_object_list",
                        "#/components/messages/kiosk_target_list",

                }
        )

)
@WebSocket
public class KioskWebSocket extends WebSocketService implements ExtensionPoint {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KioskPersistence");
    private final Pinger pinger;
    private final IController controller;
    private final KioskController kiosk;
    ConcurrentSet<Session> sessions = new ConcurrentSet<>();
    ConcurrentHashMap<Session, String> mapSessionTarget = new ConcurrentHashMap<>();
    ConcurrentHashMap<Session, IUser> mapSessionAuth = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Set<Session>> mapTargetSessions = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, JSONObject> mapTargetShow = new ConcurrentHashMap<>();

    public KioskWebSocket(KosmoSWebServer server, IController controller) {
        super(server);

        this.controller = controller;
        this.kiosk = KioskController.getInstance(controller);
        this.kiosk.setWebSocket(this);
        this.pinger = new Pinger(this);
        this.pinger.start();

    }

    public ConcurrentHashMap<String, Set<Session>> getMapTargetSessions() {
        return mapTargetSessions;
    }

    @Override
    public void addWebSocketClient(Session session) {
        sessions.add(session);


    }

    @Override
    public void delWebSocketClient(Session session) {

        String t = mapSessionTarget.remove(session);
        if (t != null) {
            Set<Session> set = mapTargetSessions.get(t);
            if (set != null) {
                set.remove(session);
            }

        }
        mapSessionAuth.remove(session);
        sessions.remove(session);
        sendTargets();

    }

    public void sendToTarget(String target, JSONObject message) {
        Set<Session> s = this.mapTargetSessions.get(target);
        String type = message.optString("type", "");
        if (type.startsWith("show-")) {
            saveShow(target, message, true);
        }
        if (s != null) {
            for (Session sess : s) {

                try {

                    sess.getRemote().sendString(message.toString());

                } catch (org.eclipse.jetty.io.EofException ex) {
                    //Nothing here
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }

    }

    public void saveShow(String target, JSONObject message, boolean save) {
        mapTargetShow.put(target, message);
        if (save) {
            kiosk.saveTargetShows(mapTargetShow);
        }
    }


    public void broadCast(String message) {
        for (Session session : this.sessions) {
            try {
                session.getRemote().sendString(message);
            } catch (org.eclipse.jetty.io.EofException ex) {
                //Nothing here
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessage(JSONObject message) {
        logger.info("sending message {}", message);
        if (message.has("target")) {
            this.sendToTarget(message.getString("target"), message);


        } else {
            broadCast(message.toString());
        }

    }

    @Override
    @OnWebSocketMessage

    public void onWebSocketMessage(Session session, String message) {
        try {
            JSONObject json = new JSONObject(message);
            if (json.has("type")) {
                String type = json.getString("type");
                if (type.equals(KioskConstants.WS_Type_setTarget)) {
                    this.setTarget(session, json.getString("value"));
                } else if (type.equals(KioskConstants.WS_Type_auth)) {
                    IUser u = null;
                    try {
                        u = this.controller.tryLogin(json.getString("username"), json.getString("password"));
                    } catch (LoginFailedException e) {
                        //throw new RuntimeException(e);
                    }
                    if (u != null) {
                        this.setAuth(session, u);
                        try {
                            session.getRemote().sendString(new JSONObject().put("type", KioskConstants.WS_Type_authSuccess).toString());
                            sendObjects(session);
                            sendTargets(session);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            session.getRemote().sendString(new JSONObject().put("type", KioskConstants.WS_Type_authFailed).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (type.equals(KioskConstants.WS_Type_objectList)) {
                    IUser u = mapSessionAuth.get(session);
                    if (u != null) {
                        sendObjects(session);
                    }


                } else if (type.equals(KioskConstants.WS_Type_targetList)) {
                    IUser u = mapSessionAuth.get(session);
                    if (u != null) {
                        sendTargets(session);
                    }


                }
            }
        } catch (JSONException ex) {
            logger.error("could not parse JSON: {}", message);
        }
    }

    private void setAuth(Session session, IUser u) {

        mapSessionAuth.put(session, u);

    }

    public void sendTargets() {
        for (Entry<Session, IUser> s : this.mapSessionAuth.entrySet()) {
            try {
                if (s.getValue() != null) {
                    if (s.getValue().getLevel() >= 1) {
                        sendTargets(s.getKey());
                    } else {
                        logger.info("session has auth {} but access is not enough!", s.getValue().toString());
                    }
                }
            } catch (Exception ex) {
                logger.error("exception", ex);
            }
        }
    }

    public void sendObjects() {
        for (Entry<Session, IUser> s : this.mapSessionAuth.entrySet()) {
            try {
                if (s.getValue() != null) {
                    if (s.getValue().getLevel() >= 1) {
                        sendObjects(s.getKey());
                    } else {
                        logger.info("session has auth {} but access is not enough!", s.getValue().toString());
                    }
                }
            } catch (Exception ex) {
                logger.error("exception", ex);
            }
        }
    }

    private void setTarget(Session session, String value) {
        String oldTarget = mapSessionTarget.get(session);
        Set<Session> set;
        if (oldTarget != null) {
            set = mapTargetSessions.get(oldTarget);
            if (set != null) {
                set.remove(session);
            }
        }
        mapSessionTarget.put(session, value);
        set = mapTargetSessions.get(value);
        if (set == null) {
            set = new ConcurrentSet<Session>();
            mapTargetSessions.put(value, set);

        }
        set.add(session);
        JSONObject message = mapTargetShow.get(value);
        if (message != null) {
            try {
                session.getRemote().sendString(message.toString());
            } catch (org.eclipse.jetty.io.EofException ex) {
                //Nothing here
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        sendTargets();


    }

    public void sendTargets(Session session) {
        try {
            session.getRemote().sendString(new JSONObject().put("type", KioskConstants.WS_Type_targetList).put("value", kiosk.getTargets()).toString());
        } catch (Exception e) {
            logger.error("Exception", e);
        }

    }

    public void sendObjects(Session session) {
        try {
            session.getRemote().sendString(new JSONObject().put("type", KioskConstants.WS_Type_objectList).put("value", kiosk.getObjects()).toString());
        } catch (Exception e) {
            logger.error("Exception", e);
        }

    }
}
