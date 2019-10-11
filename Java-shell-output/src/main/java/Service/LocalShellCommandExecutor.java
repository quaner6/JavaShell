package Service;



import Model.ExecuteResult;
import Model.ResultJson;
import Model.StreamTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

/**
 * java调用shell工具类
 *
 */

@Service
public class LocalShellCommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalShellCommandExecutor.class);
    private static ExecutorService POOL = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());


    public ExecuteResult exec(String command) {
        LOGGER.info("exec command={}", command);
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        Future<Integer> future = null;
        try {
            String[] cmd = new String[]{"/bin/sh", "-c", command};
            process = Runtime.getRuntime().exec(cmd);
            final Process p = process;
            p.getOutputStream().close();
            pIn = process.getInputStream();
            outputGobbler = new StreamGobbler(pIn, StreamTypeEnum.OUT.value());
            outputGobbler.start();
            pErr = process.getErrorStream();
            errorGobbler = new StreamGobbler(pErr, StreamTypeEnum.ERROR.value());
            errorGobbler.start();
            Callable<Integer> call = new Callable<Integer>() {
                public Integer call() throws Exception {
                    p.waitFor();
                    return p.exitValue();
                }
            };
            future = POOL.submit(call);
            int exitCode = future.get();
            return new ExecuteResult(exitCode, new ResultJson(outputGobbler.obtainContent(), errorGobbler.obtainContent()));
        } catch (Exception e) {
            LOGGER.error("", e);
            return new ExecuteResult(-1, null);
        } finally {
            if (future != null) {
                try {
                    //determines whether the thread executing this task should be interrupted in an attempt to stop the task
                    future.cancel(true);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
            if (pIn != null) {
                this.close(pIn);
                if (outputGobbler != null && !outputGobbler.isInterrupted()) {
                    outputGobbler.interrupt();
                }
            }
            if (pErr != null) {
                this.close(pErr);
                if (errorGobbler != null && !errorGobbler.isInterrupted()) {
                    errorGobbler.interrupt();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
