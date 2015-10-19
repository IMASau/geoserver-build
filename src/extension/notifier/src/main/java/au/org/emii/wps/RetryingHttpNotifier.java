package au.org.emii.wps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class RetryingHttpNotifier implements HttpNotifier {
    private static final Logger logger = LoggerFactory.getLogger(RetryingHttpNotifier.class);

    private final HttpNotifier httpNotifier;
    private final int maxNotificationAttempts;
    private final int retryInterval;

    public RetryingHttpNotifier(HttpNotifier httpNotifier, int maxNotificationAttempts, int retryInterval) {
        this.httpNotifier = httpNotifier;
        this.maxNotificationAttempts = maxNotificationAttempts;
        this.retryInterval = retryInterval;
    }

    public void notify(
        URL notificationUrl,
        URL wpsServerUrl,
        String uuid,
        String notificationParams) throws IOException
    {
        IOException lastException;

        int attempt = 1;
        do {
            try {
                logger.debug("Notification attempt #" + attempt);
                httpNotifier.notify(notificationUrl, wpsServerUrl, uuid, notificationParams);
                return;
            }
            catch (IOException e) {
                logger.info("Attempt #" + attempt + " failed", e);

                lastException = e;

                try {
                    Thread.sleep(retryInterval);
                }
                catch (InterruptedException ie) {
                    logger.debug("Sleep interrupted", ie);
                }
            }
        }
        while (attempt++ < maxNotificationAttempts);

        throw lastException;
    }
}
