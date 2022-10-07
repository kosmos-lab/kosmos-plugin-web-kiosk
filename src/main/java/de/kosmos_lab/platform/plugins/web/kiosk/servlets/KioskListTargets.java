package de.kosmos_lab.platform.plugins.web.kiosk.servlets;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import java.io.IOException;

@Extension
@ApiEndpoint(
        path = "/kiosk/listTargets",
        userLevel = 1
)
public class KioskListTargets extends KosmoSAuthedServlet implements ExtensionPoint {


    private final KioskController kiosk;

    public KioskListTargets(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
        this.kiosk = KioskController.getInstance(controller);
    }

    @Operation(

            tags = {"kiosk"},

            description = "Get the pre defined objects for the kiosk UI",
            summary = "get targets",


            responses = {
                    @ApiResponse(
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            description = "the targets",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    array = @ArraySchema(
                                            arraySchema = @Schema(
                                                    type = SchemaType.STRING
                                            )
                                    )
                            )
                    ),
                    // @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_ERROR), ref = "#/components/responses/UnknownError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        logger.info("reached listTargets servlet");

        sendJSON(request, response, kiosk.getTargets());

    }


}

