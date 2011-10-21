package com.zornchris.codeblocks;
//
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.challenges.ChallengeController;
import com.zornchris.codeblocks.events.*;
import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.ProgramController;

public class CodeBlocksPlugin extends JavaPlugin {
    private CBPlayerListener playerListener;
    private CBBlockListener blockListener;
    public ChallengeController challengeController;
    public ProgramController programController;

    public void onDisable() {
       System.out.println("[CodeBlocks] Plugin Disabled");
    }

    public void onEnable() {
    	challengeController = new ChallengeController(this);
    	programController = new ProgramController(this);
    	
    	playerListener = new CBPlayerListener(this);
        blockListener = new CBBlockListener(this);
    	
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "[CodeBlocks] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void linkChallengeAndProgram(Challenge c, Program p) {
        p.challenge = c;
        programController.addNewProgram(c.getStartBlock(), p);
    }
}