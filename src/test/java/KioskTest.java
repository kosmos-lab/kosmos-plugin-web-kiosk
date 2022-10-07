
import de.kosmos_lab.platform.plugins.web.kiosk.KioskConstants;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskController;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskObjects;
import de.kosmos_lab.platform.plugins.web.kiosk.KioskType;
import de.kosmos_lab.utils.JSONChecker;
import de.kosmos_lab.utils.StringFunctions;
import de.kosmos_lab.utils.exceptions.CompareException;
import de.kosmos_lab.web.client.websocket.WebSocketTestClient;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class KioskTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KioskTest");

    public static boolean filesEqual(File fileA, File fileB) throws IOException {

        InputStream inputStream1 = new FileInputStream(fileA);
        InputStream inputStream2 = new FileInputStream(fileB);

        return (IOUtils.contentEquals(inputStream1, inputStream2));
    }

    @Test
    public static void testKioskFiles() {
        try {
            URL r = KioskController.class.getResource("/web");
            if (r != null) {
                URI uri = r.toURI();
                Path myPath;
                if (uri.getScheme().equals("jar")) {
                    FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    myPath = fileSystem.getPath("/web/");
                } else {
                    myPath = Paths.get(uri);
                }
                Stream<Path> walk = Files.walk(myPath);

                for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                    Path path = it.next();
                    String filteredName = path.toString().substring(myPath.toString().length());
                    if (filteredName.length() > 0) {

                        if (path.toFile().isFile()) {
                            logger.info("Found resource to check {} ", filteredName);
                            Assert.assertTrue(new File(String.format("web/%s", filteredName)).exists(), String.format("File did not exist! web/%s", filteredName));
                            Assert.assertTrue(filesEqual(new File(String.format("web/%s", filteredName)), path.toFile()), String.format("files did NOT match up %s", filteredName));
                        }


                    }

                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject testKioskObjectHelper(KioskObjects ko, int num_images, int num_videos, int num_pages) {
        JSONObject json = ko.toJSON();
        Assert.assertEquals(json.has("image"), true);
        Assert.assertEquals(json.has("page"), true);
        Assert.assertEquals(json.has("video"), true);
        JSONArray images = json.optJSONArray("image");
        Assert.assertNotNull(images);
        JSONArray videos = json.optJSONArray("video");
        Assert.assertNotNull(videos);
        JSONArray pages = json.optJSONArray("page");
        Assert.assertNotNull(pages);

        Assert.assertEquals(images.length(), num_images);
        Assert.assertEquals(videos.length(), num_videos);
        Assert.assertEquals(pages.length(), num_pages);
        return json;

    }

    @Test
    public static void testKioskObject() {
        KioskObjects ko = new KioskObjects();
        testKioskObjectHelper(ko, 0, 0, 0);
        ko.addEntry(KioskType.IMAGE, "test", "testurl", "test");
        ko.addEntry(KioskType.IMAGE, "test2", "test2url", "test2");
        ko.addEntry(KioskType.IMAGE, "test3", "test3url", "test");
        JSONObject json = testKioskObjectHelper(ko, 3, 0, 0);
        JSONArray images = json.getJSONArray("image");
        Assert.assertTrue(JSONChecker.contains(images, new JSONObject().put("name", "test").put("src", "testurl")));
        Assert.assertTrue(JSONChecker.contains(images, new JSONObject().put("name", "test2").put("src", "test2url")));
        Assert.assertTrue(JSONChecker.contains(images, new JSONObject().put("name", "test3").put("src", "test3url")));

        ko.deleteAllFromSource("test");
        json = testKioskObjectHelper(ko, 1, 0, 0);
        images = json.getJSONArray("image");
        Assert.assertFalse(JSONChecker.contains(images, new JSONObject().put("name", "test3").put("src", "test3url")));
        Assert.assertTrue(JSONChecker.contains(images, new JSONObject().put("name", "test2").put("src", "test2url")));
        Assert.assertFalse(JSONChecker.contains(images, new JSONObject().put("name", "test").put("src", "testurl")));

        //Assert.assertEquals(image.getJSONObject(0).getString("name"),"test2");


    }

    @Test
    public static void testKiosk() {

        JSONArray targets = KioskController.getInstance(TestBase.controller).getTargets();
        Assert.assertNotNull(targets, "targets on KioskController was null");
        JSONArray targetsRequested = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
        Assert.assertNotNull(targetsRequested, "result for listTargets was null!");
        Assert.assertEquals(targetsRequested.length(), targets.length());
        for (int i = 0; i < targetsRequested.length(); i++) {
            try {
                Assert.assertTrue(JSONChecker.equals(targetsRequested.get(i), targets.get(i)));
            } catch (CompareException e) {
                Assert.fail("comparison failed!", e);
            }

        }
        JSONObject objects = KioskController.getInstance(TestBase.controller).getObjects();
        Assert.assertNotNull(objects);
        JSONObject objects2 = TestBase.clientAdmin.getJSONObject("/kiosk/listObjects");
        try {
            Assert.assertTrue(JSONChecker.compare(objects, objects2));
        } catch (CompareException e) {
            Assert.fail("comparison failed!", e);
        }


    }

    @Test
    public void testWebSocketClient() {
        String id_targetList = "targetList";
        String id_auth = "auth";
        String id_show = "show";
        try {

            //prepare the websocket uri
            String wsUri = String.format("%s/kioskws", TestBase.baseUrl.replace("http://", "ws://"));

            //create admin websocket
            WebSocketTestClient adminWebSocket = new WebSocketTestClient(new URI(wsUri));
            //create a listener that shows all messages
            adminWebSocket.addMessageHandler(Pattern.compile(".*"), (message, matcher) -> logger.info("WS Admin: {}", message));
            //create a listener for targetList
            adminWebSocket.addMessageHandler(Pattern.compile(String.format(".*\"type\":\"%s\".*", KioskConstants.WS_Type_targetList)), (message, matcher) -> {
                //set the variable targetList to the current value
                JSONObject json = new JSONObject(message);
                adminWebSocket.set(id_targetList, json.getJSONArray("value"));
            });
            adminWebSocket.addMessageHandler(Pattern.compile(".*\"type\":\"auth\\-.*"), (message, matcher) -> {

                JSONObject json = new JSONObject(message);
                adminWebSocket.set(id_auth, json.getString("type"));
            });
            String testpw = StringFunctions.generateRandomKey();
            //will actually never happen - but better safe than sorry
            while (testpw.equals(TestBase.clientAdmin.getPassword())) {
                testpw = StringFunctions.generateRandomKey();
            }
            // auth with false password
            adminWebSocket.sendMessage(new JSONObject().put("type", KioskConstants.WS_Type_auth).put("username", TestBase.clientAdmin.getUserName()).put("password", testpw).toString());
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_auth, KioskConstants.WS_Type_authFailed, 1000), "did not get auth failed back");
            Assert.assertFalse(TestBase.waitForValue(adminWebSocket.getObjects(), id_auth, KioskConstants.WS_Type_authSuccess, 1000), "got auth success?!");
            Assert.assertFalse(adminWebSocket.getObjects().has(id_targetList), "targetList should have been missing");
            adminWebSocket.sendMessage(new JSONObject().put("type", KioskConstants.WS_Type_auth).put("username", TestBase.clientAdmin.getUserName()).put("password", TestBase.clientAdmin.getPassword()).toString());
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_auth, KioskConstants.WS_Type_authSuccess, 1000), "could not auth!");

            //auth as an admin

            //wait for an empty listTargets
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_targetList, new JSONArray(), 1000), "did not get an empty targetList after auth");
            Assert.assertTrue(adminWebSocket.getObjects().has(id_targetList), "targetList should not have been missing - ");
            //get the targetlist synchronous
            ContentResponse response = TestBase.clientAdmin.getResponse("/kiosk/listTargets", HttpMethod.GET);
            Assert.assertNotNull(response, "response was null");
            Assert.assertEquals(response.getStatus(), 200, "response was not 200!");
            JSONArray array = new JSONArray(response.getContentAsString());
            //JSONArray array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            //check that its not empty
            Assert.assertNotNull(array, "kiosk/listTargets returned null");
            // list should be empty
            Assert.assertEquals(array.length(), 0, "targets is not an empty Array");
            // open websocket
            final WebSocketTestClient clientWebSocket = new WebSocketTestClient(new URI(wsUri));

            // add listener for all events
            clientWebSocket.addMessageHandler(Pattern.compile(".*"), (message, matcher) -> {
                //System.out.println(String.format("WS client: %s",message));
                logger.info("WS Client: {}", message);
            });

            // set target of the clientSocket to test1
            clientWebSocket.sendMessage(new JSONObject().put("type", "setTarget").put("value", "test1").toString());
            // wait for the adminsocket to get the new listTargets events with a
            TestBase.waitForValue(adminWebSocket.getObjects(), "listTargets", new JSONArray().put("test1"), 1000);
            // check
            array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            Assert.assertNotNull(array);
            Assert.assertEquals(array.length(), 1, "listTargets is not equal to 1");
            Assert.assertEquals(array.getString(0), "test1");
            JSONObject json = new JSONObject().put("type", "show-image").put("value", "https://foo.bar/test.png").put("target", "test1");

            clientWebSocket.addMessageHandler(Pattern.compile(".*\"type\":\"show\\-.*"), (message, matcher) -> {
                System.out.println(String.format("WS show: %s", message));
                JSONObject json1 = new JSONObject(message);
                clientWebSocket.set(id_show, json1.getString("value"));
            });
            TestBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            Assert.assertTrue(TestBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.png", 5000));
            json = new JSONObject().put("type", "show-video").put("value", "https://foo.bar/test.mp4").put("target", "test1");
            TestBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            Assert.assertTrue(TestBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.mp4", 5000));
            json = new JSONObject().put("type", "show-video").put("value", "https://foo.bar/test.avi").put("target", "test2");
            TestBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            //reset message to an empty message
            clientWebSocket.set(id_show, "");
            //change own Target id
            clientWebSocket.sendMessage(new JSONObject().put("type", "setTarget").put("value", "test2").toString());
            //wait for the show to come in
            Assert.assertTrue(TestBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.avi", 5000));


            //check the targets synchronous
            array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            //
            Assert.assertNotNull(array, "the result of kiosk/targets was null!");
            Assert.assertEquals(array.length(), 1, "listTargets is not equal to 1");
            Assert.assertEquals(array.getString(0), "test2", "the only entry of the list should be test2");
            try {
                clientWebSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Thread.sleep(500);
            array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            Assert.assertNotNull(array);
            Assert.assertEquals(array.length(), 0, "listTargets is not empty");
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_targetList, new JSONArray(), 5000));

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }


}
