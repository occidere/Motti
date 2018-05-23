import motti.Motti;
import motti.MottiImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class MottiTest {

    @Test
    public void successTest() throws Exception {
        String target = "https://www.sample-videos.com/img/Sample-jpg-image-2mb.jpg";
        String fileName = StringUtils.substringAfterLast(target, "/");
        String path = System.getProperty("user.home") + "/" + fileName;

        callMotti(target, path);
    }

    @Test
    public void successNoContentRangeHeaderTest() throws Exception {
        String target = "https://github.com/occidere/MMDownloader/archive/v0.5.0.8.zip";
        String fileName = StringUtils.substringAfterLast(target, "/");
        String path = System.getProperty("user.home") + "/" + fileName;

        callMotti(target, path);
    }

    @Test
    public void failedWrongLinkTest() throws Exception {
        String target = "https://fakeurl.not-exist.net/fake-resources.zip";
        String fileName = StringUtils.substringAfterLast(target, "/");
        String path = System.getProperty("user.home") + "/" + fileName;

        callMotti(target, path);
    }

    private void callMotti(String target, String path) throws Exception {
        Motti motti = new MottiImpl();
        motti.forceDownload(target, path);
    }
}
