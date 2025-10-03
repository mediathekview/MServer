package de.mediathekview.mserver.base.messages.listener;

import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.MessageUtil;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A default implementation of the {@link MessageListener} interface.<br>
 * It prints messages to the Logger.
 */
public class LogMessageListener implements MessageListener {
  private static final String LOGGER_NAME = "Mediathekview";
  private static final Logger LOG = LogManager.getLogger(LOGGER_NAME);

  private final String bundleName;
  private final Locale locale;

  public LogMessageListener() {
    this(null, null);
  }

  public LogMessageListener(final String aBundleName, final Locale aLocale) {
    super();
    bundleName = aBundleName;
    locale = aLocale;
  }

  @Override
  public void consumeMessage(final Message aMessage, final Object... args) {
    final String messageText;
    if (args == null || args.length == 0) {
      messageText = loadMessage(aMessage);
    } else {
      messageText = String.format(loadMessage(aMessage), args);
    }

    switch (aMessage.getMessageType()) {
      case DEBUG:
        LOG.debug(messageText);
        break;

      case INFO:
        LOG.info(messageText);
        break;

      case WARNING:
        LOG.warn(messageText);
        break;

      case ERROR:
        LOG.error(messageText);
        break;

      case FATAL_ERROR:
        LOG.fatal(messageText);
        break;
    }
  }

  public String loadMessage(final Message aMessage) {
    final MessageUtil messageUtil = MessageUtil.getInstance();
    if (bundleName == null && locale == null) {
      return messageUtil.loadMessageText(aMessage);
    } else if (locale == null) {
      return messageUtil.loadMessageText(aMessage, bundleName);
    } else {
      return messageUtil.loadMessageText(aMessage, bundleName, locale);
    }
  }
}
