package motti;

import java.io.File;

public interface Motti {
    File download(String target, String path) throws Exception;
    File forceDownload(String target, String path) throws Exception;

    int getThreadCount();
    void setThreadCount(int threadCount);
}
