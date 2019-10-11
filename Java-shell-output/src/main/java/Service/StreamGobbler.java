package Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class StreamGobbler extends Thread {
    private static Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);
    private InputStream inputStream;
    private String streamType;
    private StringBuilder buf;
    private volatile boolean isStopped;

    public StreamGobbler(final InputStream inputStream, final String streamType) {
        this.inputStream = inputStream;
        this.streamType = streamType;
        this.buf = new StringBuilder();
        this.isStopped = false;
    }

    @Override
    public void run() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.buf.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.error("run fail...streamType={}", streamType, e);
        } finally {
            this.isStopped = true;
            synchronized (this) {
                notify();
            }
        }
    }

    public String obtainContent() {
        if (!this.isStopped) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
        }
        return this.buf.toString();
    }
}