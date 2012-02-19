package com.zornchris.codeblocks;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import moc.MOCDBLib.MOCDBLib;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.inventory.SpoutItemStack;

import ru.tehkode.permissions.bukkit.PermissionsEx;


import com.zornchris.codeblocks.blocks.VariableBlock;
import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.challenges.ChallengeController;
import com.zornchris.codeblocks.listeners.CBBlockListener;
import com.zornchris.codeblocks.listeners.CBCustomListener;
import com.zornchris.codeblocks.listeners.CBPlayerListener;
import com.zornchris.codeblocks.program.Program;
import com.zornchris.codeblocks.program.ProgramController;

public class CodeBlocksPlugin extends JavaPlugin {
    private CBPlayerListener playerListener;
    private CBBlockListener blockListener;
    private CBCustomListener customListener;
    
    public ChallengeController challengeController;
    public ProgramController programController;
    public DBHelper dbh;
    
    public static VariableBlock vb;

    public void onDisable() {
       System.out.println("[CodeBlocks] Plugin Disabled");
    }

    public void onEnable() {
    	challengeController = new ChallengeController(this);
    	programController = new ProgramController(this);
    	
    	playerListener = new CBPlayerListener(this);
        blockListener = new CBBlockListener(this);
        customListener = new CBCustomListener(this);
        
    	
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.CUSTOM_EVENT, customListener, Priority.Normal, this);
        
        dbh = new DBHelper((MOCDBLib) pm.getPlugin("MOCDBLib"), this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "[CodeBlocks] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
        vb = new VariableBlock(this);
    }
    
    public void linkChallengeAndProgram(Challenge c, Program p) {
        //System.out.println("Linking Program & Challenge");
        p.challenge = c;
        c.setProgram(p);
        programController.addNewProgram(c.getStartBlock(), p);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("cblocks")) {
            Player p = (Player) sender;
            
            if(args.length > 0 && args[0].equalsIgnoreCase("tutorial")) {
                String playerName = p.getName();
                String worldName = "CBTutorial_" + playerName;
                
                joinTutorialWorld(p, playerName, "CBTutorial", worldName);
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("load")) {
                String playerName = p.getName();
                String worldName = "CBTutorialText_" + playerName;
                
                joinTutorialWorld(p, playerName, "CBTutorialText", worldName);
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("vb")) {
                Player player = (Player) sender;
                player.getInventory().addItem(new SpoutItemStack(vb, 1));
            }
            
            return true;
        }
        
        return false;
    }
    
    private void joinTutorialWorld(Player p, String playerName, String fromWorld, String worldName) {
        p.sendMessage("[CodeBlocks] Duplicating Tutorial World...");
        CodeBlocksPlugin.duplicateWorldFiles(fromWorld, worldName);
        
        p.setOp(true);
        
        p.sendMessage("[CodeBlocks] Importing Tutorial World...");
        p.sendMessage("[CodeBlocks] Joining Tutorial World...");
        CodeBlocksPlugin.importAndTeleportToWorld(p, worldName, "NORMAL");
        p.performCommand("mv modify set monsters false " + worldName);
        p.performCommand("mv modify set animals false " + worldName);
        p.performCommand("pex world " + worldName + " inherit CBTutorial");
        p.performCommand("mvm set mode creative");
        p.performCommand("gamemode " + playerName + " 1");
        
        p.setOp(false);
    }
    
    //http://www.unix.com/shell-programming-scripting/13932-execute-command-unix-java-possible-not.html
    /**
     * Copies all of the necessary files to create a duplicate
     * of 'fromworld'
     * 
     * @param fromWorld     The name world being copied
     * @param toWorld       The name of the copy
     */
    public static void duplicateWorldFiles(String fromWorld, String toWorld) {
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        System.out.println(fromWorld + " " + toWorld);
        
        try
        {
            File dir = new File(toWorld);
            
            if(!dir.exists()) {
                p = rt.exec( "cp -r " + fromWorld + " " + toWorld );
                //System.out.println("cp -r " + fromWorld + " " + toWorld);
                
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) { e.printStackTrace(); }
                
                p = rt.exec( "rm " + toWorld + "/uid.dat" );
                //System.out.println("rm " + toWorld + "/uid.dat");
            }
        }
        catch ( IOException ioe )
        {
            System.out.println( "Error executing file" );
            ioe.printStackTrace();
        }   
    }
    
    /**
     * Loads the world into the server and teleports 'p' to
     * the world's spawn
     * @param p             Player to tp
     * @param world         World to import and tp to
     * @param worldType     As in NORMAL, SKYLAND, NETHER, etc.
     */
    public static void importAndTeleportToWorld(Player p, String world, String worldType) {
        p.setOp(true);
        p.performCommand("mvim " + world + " " + worldType);
        p.performCommand("mv tp " + world);
        p.setOp(false);
    }
}