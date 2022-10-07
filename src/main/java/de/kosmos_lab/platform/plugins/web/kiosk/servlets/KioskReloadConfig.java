package de.kosmos_lab.platform.plugins.web.kiosk.servlets;

import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.Explode;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskController;
import jakarta.servlet.http.HttpServletResponse;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
@ApiEndpoint(
        path = "/kiosk/reloadConfig",
        userLevel = 1
)
public class KioskReloadConfig extends KosmoSAuthedServlet implements ExtensionPoint {


    private final KioskController kiosk;

    public KioskReloadConfig(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
        this.kiosk = KioskController.getInstance(controller);
    }

    @Operation(

            tags = {"kiosk"},

            description = "Reloads the configs for Kiosk, also reloads external sources if available in config",
            summary = "reload config",


            responses = {
                    @ApiResponse(
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "the config was reloaded successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_ERROR), ref = "#/components/responses/UnknownError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {


        kiosk.reload();

        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
    }


}

