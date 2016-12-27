package com.chanapp.chanjet.web.aop;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.appmanager.AppWorkManager;

/**
 * service日志拦截器
 * 
 * @author tds
 *
 */
public class ServiceLogInterceptorImpl extends BaseServiceInterceptorImpl {
    private static Logger logger = LoggerFactory.getLogger(ServiceLogInterceptorImpl.class);

    private Queue<Log> logs = new LinkedList<Log>();
    private Stack<Long> times = new Stack<Long>();

    private ExecutorService logThreadExecutor = Executors.newSingleThreadExecutor();

    private class Log {
        String message;
        Object[] params;

        public Log(String message, Object... params) {
            this.message = message;
            this.params = params;
        }
    }

    private class LogThread implements Runnable {
        private String name = "";
        private Queue<Log> logs = new LinkedList<Log>();

        public LogThread(String name, Queue<Log> logs) {
            this.name = name;
            this.logs = logs;
        }

        public void run() {
            Log log;
            while ((log = this.logs.poll()) != null) {
                logger.info(this.name + "--" + log.message, log.params);
            }
        }
    }

    @Override
    public ServiceInterceptorReturn before(ServiceInterceptorInfo info) {
        Long currentTime = System.currentTimeMillis();
        times.push(currentTime);

        String caller = "";
        StackTraceElement[] stackTraces = info.getStackTraces();
        for (StackTraceElement stackTrace : stackTraces) {
            if (stackTrace.getLineNumber() > 0) {
                caller = stackTrace.getClassName() + "." + stackTrace.getMethodName() + ":"
                        + stackTrace.getLineNumber();
                break;
            }
        }
        String serviceName = info.getServiceName();
        String methodName = info.getMethod().getName();
/*        String params = AppWorkManager.getDataManager().toJSONString(info.getParams());

        logs.offer(
                new Log("##### BEFORE: service:{}#{}, caller:{}, params:{}", serviceName, methodName, caller, params));*/

        return ServiceInterceptorReturn.CONTINUE();
    }

    @Override
    public ServiceInterceptorReturn after(ServiceInterceptorInfo info) {
        Long currentTime = times.isEmpty() ? System.currentTimeMillis() : times.pop();

        String serviceName = info.getServiceName();

        logs.offer(new Log("##### AFTER: service:{}#{}, used time:{}", serviceName, info.getMethod().getName(),
                System.currentTimeMillis() - currentTime));

        if (times.isEmpty()) {
            Queue<Log> _logs = new LinkedList<Log>();
            Log log;
            while ((log = this.logs.poll()) != null) {
                _logs.offer(new Log(log.message, log.params));
            }
            logThreadExecutor.execute(new LogThread(Thread.currentThread().getName(), _logs));
        }

        return ServiceInterceptorReturn.CONTINUE();
    }

    @Override
    public ServiceInterceptorReturn exception(ServiceInterceptorInfo info) {
        String serviceName = info.getServiceName();

        logs.offer(new Log("##### EXCEPTION: service:{}#{}, exception:{}", serviceName, info.getMethod().getName(),
                info.getException().getMessage()));

        return ServiceInterceptorReturn.CONTINUE();
    }

}
