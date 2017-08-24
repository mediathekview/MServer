package de.mediathekview.mserver.base.messages;

import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;

/**
 * The server messages.
 */
public enum ServerMessages implements Message
{
    CRAWLER_START("crawlerStart", MessageTypes.INFO),
    CRAWLER_PROGRESS("crawlerProgress", MessageTypes.INFO),
    CRAWLER_END("crawlerEnd", MessageTypes.INFO),
    CRAWLER_ERROR("crawlerError", MessageTypes.ERROR),
    CRAWLER_TIMEOUT("crawlerTimeout",MessageTypes.ERROR),
    SERVER_TIMEOUT("serverTimeout",MessageTypes.FATAL_ERROR),
    SERVER_ERROR("serverError",MessageTypes.FATAL_ERROR), 
    FILMLIST_SAVE_PATH_MISSING_RIGHTS("filmlistSavePathMissingRights",MessageTypes.FATAL_ERROR),
    FILMLIST_SAVE_PATH_INVALID("filmlistSavePathInvalid",MessageTypes.FATAL_ERROR), 
    NO_FILMLIST_FORMAT_CONFIGURED("noFilmlistFormatConfigured",MessageTypes.FATAL_ERROR), 
    NO_FILMLIST_SAVE_PATHS_CONFIGURED("noFilmlistSavePathsConfigured",MessageTypes.FATAL_ERROR),
    NO_FILMLIST_SAVE_PATH_FOR_FORMAT_CONFIGURED("noFilmlistSavePathForFormatConfigured",MessageTypes.FATAL_ERROR), 
    FILMLIST_IMPORT_URL_INVALID("filmlistImportUrlInvalid",MessageTypes.FATAL_ERROR), 
    FILMLIST_IMPORT_FILE_NOT_FOUND("filmlistImportFileNotFound",MessageTypes.FATAL_ERROR), 
	FILMLIST_IMPORT_FILE_NO_READ_PERMISSION("filmlistImportFileNoReadPermission",MessageTypes.FATAL_ERROR), 
	NO_FILMLIST_IMPORT_FORMAT_IN_CONFIG("noFilmlistImportFormatInConfig",MessageTypes.FATAL_ERROR), 
	NO_FILMLIST_IMPORT_LOCATION_IN_CONFIG("noFilmlistImportLocationInConfig",MessageTypes.FATAL_ERROR);

    private String messageKey;
    private MessageTypes messageType;

    ServerMessages(String aMessageKey, MessageTypes aMessageType)
    {
        messageKey = aMessageKey;
        messageType = aMessageType;
    }

    @Override
    public String getMessageKey()
    {
        return messageKey;
    }

    @Override
    public MessageTypes getMessageType()
    {
        return messageType;
    }
}
