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
    CRAWLER_TIMEOUT("crawlerTimeout", MessageTypes.ERROR),
    CRAWLER_DOCUMENT_LOAD_ERROR("crawlerDocumentLoadError", MessageTypes.DEBUG),
    SERVER_TIMEOUT("serverTimeout", MessageTypes.FATAL_ERROR),
    SERVER_ERROR("serverError", MessageTypes.FATAL_ERROR),
    FILMLIST_SAVE_PATH_MISSING_RIGHTS("filmlistSavePathMissingRights", MessageTypes.FATAL_ERROR),
    FILMLIST_SAVE_PATH_INVALID("filmlistSavePathInvalid", MessageTypes.FATAL_ERROR),
    NO_FILMLIST_FORMAT_CONFIGURED("noFilmlistFormatConfigured", MessageTypes.FATAL_ERROR),
    NO_FILMLIST_SAVE_PATHS_CONFIGURED("noFilmlistSavePathsConfigured", MessageTypes.FATAL_ERROR),
    NO_FILMLIST_SAVE_PATH_FOR_FORMAT_CONFIGURED("noFilmlistSavePathForFormatConfigured", MessageTypes.FATAL_ERROR),
    FILMLIST_IMPORT_URL_INVALID("filmlistImportUrlInvalid", MessageTypes.FATAL_ERROR),
    FILMLIST_IMPORT_FILE_NOT_FOUND("filmlistImportFileNotFound", MessageTypes.FATAL_ERROR),
    FILMLIST_IMPORT_FILE_NO_READ_PERMISSION("filmlistImportFileNoReadPermission", MessageTypes.FATAL_ERROR),
    NO_FILMLIST_IMPORT_FORMAT_IN_CONFIG("noFilmlistImportFormatInConfig", MessageTypes.FATAL_ERROR),
    NO_FILMLIST_IMPORT_LOCATION_IN_CONFIG("noFilmlistImportLocationInConfig", MessageTypes.FATAL_ERROR),
    FTP_UPLOAD_ERROR("ftpUploadError", MessageTypes.ERROR),
    FTP_URL_ERROR("ftpUrlError", MessageTypes.ERROR),
    FILMLIST_FTP_UPLOAD_ERROR("filmlistFtpUploadError", MessageTypes.ERROR),
    FTP_FILE_SIZE_ERROR("ftpFileSizeError", MessageTypes.ERROR),
    FTP_FORMAT_NOT_IN_SAVE_FORMATS("ftpFormatNotInSaveFormats", MessageTypes.ERROR),
    UI_TO_MANY_ARGUMENTS("uiToManyArguments", MessageTypes.ERROR),
    UI_UNKNOWN_ARGUMENT("uiUnknownArgument", MessageTypes.FATAL_ERROR),
    UI_GENERATE_DEFAULT_CONFIG_FILE_FAILED("uiGenerateDefaultConfigFileFailed", MessageTypes.FATAL_ERROR),
    
    DEBUG_MSSING_SENDUNGFOLGEN_COUNT("debugMissingSendungfolgenCount",MessageTypes.DEBUG),
    DEBUG_ALL_SENDUNG_COUNT("debugAllSendungCount",MessageTypes.DEBUG),
    DEBUG_ALL_SENDUNG_FOLGEN_COUNT("debugAllSendungFolgenCount",MessageTypes.DEBUG), 
    DEBUG_INVALID_URL("debugInvalidUrl",MessageTypes.DEBUG),
    DEBUG_MISSING_ELEMENT("debugMissingElement",MessageTypes.DEBUG);

    private String messageKey;
    private MessageTypes messageType;

    ServerMessages(final String aMessageKey, final MessageTypes aMessageType)
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
