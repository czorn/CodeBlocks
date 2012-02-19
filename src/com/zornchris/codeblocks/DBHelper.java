package com.zornchris.codeblocks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.zornchris.codeblocks.events.ChallengeLoadEvent;
import com.zornchris.codeblocks.events.RobotStartEvent;

import moc.MOCDBLib.DBConnector;
import moc.MOCDBLib.MOCDBLib;

public class DBHelper {
    private MOCDBLib dbPlugin;
    private Logger logger;
    private CodeBlocksPlugin plugin;
    private DBConnector dbc;
    
    private final String PLAYER_TABLE = "cb_player";
    private final String EVENT_TABLE = "cb_event";
    
    public DBHelper(MOCDBLib dbPlugin, CodeBlocksPlugin plugin) {
        this.dbPlugin = dbPlugin;
        this.plugin = plugin;
        
        logger = Logger.getLogger("CB_Log");
        dbc = dbPlugin.getMineCraftDB("CodeBlocks", logger);
        boolean playerExist = dbc.ensureTable(PLAYER_TABLE, "id int NOT NULL AUTO_INCREMENT, name varchar(50), PRIMARY KEY (id)");
        boolean eventExist = dbc.ensureTable(EVENT_TABLE, "id int NOT NULL AUTO_INCREMENT, pId int, event_type varchar(50), " +
        		"challenge varchar(50), program varchar(500), event_time timestamp, PRIMARY KEY (id)");
        
        //System.out.println(playerExist + " " + eventExist);
    }
    
    public void logEvent(String playerName, String eventType, String challengeData, String programDescr) {
        ResultSet rs = dbc.sqlSafeQuery("SELECT * FROM " + PLAYER_TABLE + " WHERE name = '" + playerName + "'");
        
        try {
            if(rs.first() == false) {
                addNewPlayer(playerName);
                rs = dbc.sqlSafeQuery("SELECT * FROM " + PLAYER_TABLE + " WHERE name = '" + playerName + "'");
                rs.first();
            }
            
            int pId = rs.getInt("id");
            //System.out.println("pid " + pId);
            PreparedStatement ps = dbc.prepareStatement("INSERT INTO " + EVENT_TABLE + " (pId, event_type, challenge, program, event_time) " +
            		"VALUES(" + pId + ", '" + eventType + "', '" + challengeData + "', '" + programDescr + "', CURRENT_TIMESTAMP)");
            dbc.insertQuery(ps);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addNewPlayer(String playerName) {
        PreparedStatement ps = dbc.prepareStatement("INSERT INTO " + PLAYER_TABLE + " (name) VALUES('" + playerName + "')");
        dbc.insertQuery(ps);
    }
}
