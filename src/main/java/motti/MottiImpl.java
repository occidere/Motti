package motti;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MottiImpl implements Motti {
    private int threadCount = Math.max(1, Thread.activeCount());

    public int getThreadCount() {
        return threadCount;
    }

    /**
     * 스레드 개수 지정(최소 1개)
     * @param threadCount 지정할 스레드 수 (최소 1개 이상)
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = Math.max(1, threadCount);
    }

    /**
     * target을 path에 다운로드 한다.
     * 만약 이미 path에 동일한 이름의 파일이 있으면 Exception을 발생시킨다.
     * @param target 다운로드 받을 대상
     * @param path 저장할 대상
     * @return 성공 시 다운받은 File 객체
     * @throws Exception 이미 path에 파일이 있는 경우
     */
    public File download(String target, String path) throws Exception {
        if(new File(path).exists()) {
            throw new Exception(String.format("File Already Exist! [%s]", path));
        }

        return forceDownload(target, path);
    }

    /**
     * target을 path에 다운로드 한다.
     * 만약 이미 path에 동일한 이름의 파일이 있으면, 예전 파일을 삭제하고 새로 다운받는다.
     * @param target 다운로드 받을 대상
     * @param path 저장할 대상
     * @return 성공 시 다운받은 File 객체
     * @throws Exception
     */
    public File forceDownload(String target, String path) throws Exception {
        new File(path).delete();

        Worker initWorker = initConnetcion(target);

        /* Content-Range 헤더가 없어서 분할 다운로드가 불가능 할 시 싱글스레드로 전체 다운로드 */
        if(initWorker.isSplittable() == false) {
            threadCount = 1;
        }

        int workerIdx = 0;
        int fullLength = initWorker.getFullLength();
        int chunk = (int) Math.ceil((double) fullLength / threadCount);

        List<Future<File>> futureList = new LinkedList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        Worker workers[] = new Worker[threadCount];
        for(int offset=0; offset<=fullLength; offset += chunk+1, workerIdx++) {
            workers[workerIdx] = new Worker(target, offset, offset + chunk);
            futureList.add(executor.submit(workers[workerIdx]));
        }

        File mergedFile = merge(futureList, path);

        executor.shutdown();

        return mergedFile;
    }

    /**
     * Content-Range 헤더 정보를 가져오기 위한 최초 1회 연결
     * @param target 연결할 대상의 주소
     * @return 초기 커넥션을 수행한 Worker 객체
     * @throws Exception
     */
    private Worker initConnetcion(String target) throws Exception {
        return new Worker(target, 0, 1);
    }

    /**
     * 병렬으로 다운로드한 파일을 합친다. chunk 파일들은 삭제된다.
     * @param futureList 벙렬로 다운받아질 future 객체들이 담긴 리스트
     * @param path 하나로 병합하여 생성할 파일 경로
     * @return 하나로 병합된 파일 객체
     * @throws Exception
     */
    private File merge(List<Future<File>> futureList, String path) throws Exception {
        File mergedFile = new File(path);
        for(Future<File> fileFuture : futureList) {
            File chunk = fileFuture.get();
            FileUtils.writeByteArrayToFile(mergedFile, FileUtils.readFileToByteArray(chunk), true);
            chunk.delete();
        }
        return mergedFile;
    }
}