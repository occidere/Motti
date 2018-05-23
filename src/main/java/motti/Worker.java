package motti;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

/**
 * target으로 부터 연결 및 다운로드를 수행하는 클래스
 *
 * @author occidere
 */
class Worker implements Callable<File> {
    private String target;
    private String fileName;
    private int fullLength;
    private int startInclude;
    private int endInclude;
    private boolean isSplittable;
    private HttpURLConnection conn;

    /**
     * target으로 부터 연결 및 다운로드를 수행하는 클래스.
     * startInclude 오프셋 이상 ~ endInclude 오프셋 이하 만큼 다운로드를 한다.
     * @param target 다운로드 대상 주소
     * @param startInclude 시작 오프셋(포함)
     * @param endInclude 끝 오프셋(포함)
     * @throws Exception
     */
    public Worker(String target, int startInclude, int endInclude) throws Exception {
        this.target = target;
        this.fileName = String.format("_%d_%d", target.hashCode(), startInclude);
        this.startInclude = startInclude;
        this.endInclude = endInclude;

        openConnection();
    }

    /**
     * @return target의 전체 바이트 길이를 리턴
     */
    public int getFullLength() {
        return fullLength;
    }

    /**
     * Content-Range 가 있으면 true, 없으면 false
     */
    public boolean isSplittable() { return isSplittable; }

    @Override
    public File call() throws Exception {
        return getFile();
    }

    @Override
    public String toString() {
        return String.format("Worker[target=%s, fileName=%s, fullLength=%d, startInclude=%d, endInclude=%d]",
                target, fileName, fullLength, startInclude, endInclude);
    }

    /**
     * target과의 연결을 수립.
     * Range 헤더를 통해 지정한 구간 만큼의 다운로드를 준비한다.
     * 이 과정에서 전체 파일 크기(fullLength) 도 구한다.
     * @throws Exception
     */
    private void openConnection() throws Exception {
        conn = (HttpURLConnection) new URL(target).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Range", String.format("bytes=%d-%d", startInclude, endInclude));

        try {
            parseFullLength(conn.getHeaderField("Content-Range"));
            isSplittable = true;
        } catch (Exception e) {
            /* Content-Range 헤더가 없는 경우 */
            e.printStackTrace();
            fullLength = conn.getContentLength();
        }
    }

    /**
     * Content-Range 헤더로부터 전체 바이트 길이(fullLength)를 파싱한다.
     * @param contentRange Content-Range 헤더로부터 파싱한 전체 바이트 길이(fullLength)
     */
    private void parseFullLength(String contentRange) {
        if(contentRange == null) {
            throw new NoSuchElementException("Content-Range header is not exist!");
        }
        String fullContentLength = StringUtils.substringAfter(contentRange, "/");
        this.fullLength = Integer.parseInt(fullContentLength);
    }

    /**
     * 다운받은 chunk 파일을 리턴한다.
     * @return 다운받은 chunk 파일
     * @throws Exception
     */
    private File getFile() throws Exception {
        File chunk = new File(fileName);
        FileUtils.copyToFile(conn.getInputStream(), chunk);
        return chunk;
    }
}