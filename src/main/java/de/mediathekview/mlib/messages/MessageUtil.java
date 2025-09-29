package de.mediathekview.mlib.messages;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A util class to read messages from system.
 */
public final class MessageUtil
{
    private static final Logger LOG = LogManager.getLogger(MessageUtil.class);
    private static final String DEFAULT_BUNDLE_NAME = "MediathekView_Messages";
    private static final String MLIB_BUNDLE_NAME = "MLib_Messages";
    private static final String MESSAGE_NOT_IN_BUNDLE_ERROR_TEXT_TEMPLATE =
            "Tried to load the message with the key \"%s\" from the resource bundle \"%s\" but the bundle doesn't contains a message with this key.";
    private static MessageUtil instance;

    public static MessageUtil getInstance()
    {
        if (instance == null)
        {
            instance = new MessageUtil();
        }
        return instance;
    }

    private MessageUtil()
    {
        super();
    }

    public String loadMessageText(final Message aMessage, final String aBundleName, final Locale aLocale)
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(aBundleName, aLocale);
        try
        {
            if (!resourceBundle.containsKey(aMessage.getMessageKey()))
            {
                // Try shared lib Bundle.
                resourceBundle = ResourceBundle.getBundle(MLIB_BUNDLE_NAME, aLocale);
            }
            return resourceBundle.getString(aMessage.getMessageKey());
        }
        catch (final MissingResourceException missingResourceException)
        {
            LOG.fatal(String.format(MESSAGE_NOT_IN_BUNDLE_ERROR_TEXT_TEMPLATE, aMessage.getMessageKey(), aBundleName),
                    missingResourceException);
            throw missingResourceException;
        }
    }

    public String loadMessageText(final Message aMessage, final String aBundleName)
    {
        return loadMessageText(aMessage, aBundleName, Locale.getDefault());
    }

    public String loadMessageText(final Message aMessage)
    {
        return loadMessageText(aMessage, DEFAULT_BUNDLE_NAME);
    }
}
