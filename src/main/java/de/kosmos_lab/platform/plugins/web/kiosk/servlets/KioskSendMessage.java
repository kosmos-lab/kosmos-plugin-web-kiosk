package de.kosmos_lab.platform.plugins.web.kiosk.servlets;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension

@ApiEndpoint(
        path = "/kiosk/sendMessage",
        userLevel = 1
)
public class KioskSendMessage extends KosmoSAuthedServlet implements ExtensionPoint {
    private final KioskController kiosk;

    public KioskSendMessage(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
        this.kiosk = KioskController.getInstance(controller);
    }

    @Operation(

            tags = {"kiosk"},

            description = "sends the given message to the given target",
            summary = "send message",
            requestBody = @RequestBody(

                    required = true,
                    content = {

                            @Content(
                                    examples = {
                                            @ExampleObject(
                                                    name = "show image on test1",
                                                    value = "{\"type\":\"show-image\",\"target\":\"test1\",\"value\":\"https://foo.bar/test.png\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "show video on test3",
                                                    value = "{\"type\":\"show-video\",\"target\":\"test3\",\"value\":\"https://foo.bar/test.mp4\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "pause video on test3",
                                                    value = "{\"type\":\"video-pause\",\"target\":\"test3\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "play video on test3",
                                                    value = "{\"type\":\"video-play\",\"target\":\"test3\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "seek video +10s on test3",
                                                    value = "{\"type\":\"video-seek\",\"target\":\"test3\",\"value\":\"10\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "seek video -30s on test3",
                                                    value = "{\"type\":\"video-seek\",\"target\":\"test3\",\"value\":\"-30\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "show page on kiosk1",
                                                    value = "{\"type\":\"show-page\",\"target\":\"kiosk1\",\"value\":\"https://foo.bar/\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "show page on kiosk1",
                                                    value = "{\"type\":\"show-page\",\"target\":\"kiosk1\",\"value\":\"https://foo.bar/\"}"
                                            ),

                                    },
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "target",
                                                    schema = @Schema(
                                                            description = "the name of the target the message should be send to",
                                                            type = SchemaType.STRING,
                                                            required = true

                                                    )

                                            ),
                                            @SchemaProperty(
                                                    name = "value",
                                                    schema = @Schema(
                                                            description = "the value that accompanies the given type",
                                                            type = SchemaType.STRING,
                                                            required = false

                                                    )

                                            ),
                                            @SchemaProperty(
                                                    name = "type",
                                                    schema = @Schema(
                                                            description = "the type of the message",
                                                            type = SchemaType.STRING,
                                                            allowableValues = {"show-image", "show-page", "show-video", "reload", "video-play", "video-pause", "video-seek"},
                                                            required = true

                                                    )

                                            ),

                                    }

                            )
                    }),

            responses = {
                    @ApiResponse(
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "the config was reloaded successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_ERROR), ref = "#/components/responses/UnknownError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {


        kiosk.sendMessage(request.getBodyAsJSONObject());
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
    }


}

