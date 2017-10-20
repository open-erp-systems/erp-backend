package com.jukusoft.erp.lib.logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.jukusoft.erp.lib.logging.ILogging;
import org.json.JSONObject;

public class HzLogger implements ILogging {

    protected HazelcastInstance hazelcastInstance = null;

    //logger topic
    protected ITopic<String> loggerTopic = null;

    //node id
    protected String nodeID = "";

    public HzLogger (HazelcastInstance hazelcastInstance, String nodeID) {
        this.hazelcastInstance = hazelcastInstance;

        this.loggerTopic = hazelcastInstance.getReliableTopic("logger");

        this.nodeID = nodeID;
    }

    @Override
    public void debug(long messageID, String tag, String message) {
        this.log("DEBUG", messageID, tag, message);
    }

    @Override
    public void debug(String tag, String message) {
        this.log("DEBUG", -1, tag, message);
    }

    @Override
    public void info(long messageID, String tag, String message) {
        this.log("INFO", messageID, tag, message);
    }

    @Override
    public void info(String tag, String message) {
        this.log("INFO", -1, tag, message);
    }

    @Override
    public void warn(long messageID, String tag, String message) {
        this.log("WARN", messageID, tag, message);
    }

    @Override
    public void warn(String tag, String message) {
        this.log("INFO", -1, tag, message);
    }

    @Override
    public void error(long messageID, String tag, String message) {
        this.log("ERROR", messageID, tag, message);
    }

    @Override
    public void error(String tag, String message) {
        this.log("INFO", -1, tag, message);
    }

    protected void log (String logLevel, long messageID, String tag, String message) {
        JSONObject json = new JSONObject();

        if (messageID != -1) {
            json.put("is_message_log", true);
        }

        json.put("timestamp", System.currentTimeMillis());
        json.put("nodeID", this.nodeID);
        json.put("log_level", logLevel);
        json.put("messageID", message);
        json.put("tag", tag);
        json.put("message", message);

        this.loggerTopic.publish(json.toString());

        //also log to console
        if (messageID == -1) {
            System.out.println("messageID=null,[nodeID=" + this.nodeID + "] " + logLevel.toUpperCase() + ": " + tag + ": " + message);
        } else {
            System.out.println("messageID=" + messageID + ",[nodeID=" + this.nodeID + "]" + logLevel.toUpperCase() + ": " + tag + ": " + message);
        }
    }

}
