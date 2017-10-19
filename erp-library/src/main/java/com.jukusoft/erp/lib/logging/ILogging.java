package com.jukusoft.erp.lib.logging;

public interface ILogging {

    public void debug (long messageID, String tag, String message);

    public void debug (String tag, String message);

    public void info (long messageID, String tag, String message);

    public void info (String tag, String message);

    public void warn (long messageID, String tag, String message);

    public void warn (String tag, String message);

    public void error (long messageID, String tag, String message);

    public void error (String tag, String message);

}
