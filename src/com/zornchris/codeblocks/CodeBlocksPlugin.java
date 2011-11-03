package com.zornchris.codeblocks;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.challenges.ChallengeController;
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
        pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "[CodeBlocks] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void linkChallengeAndProgram(Challenge c, Program p) {
        //System.out.println("Linking Program & Challenge");
        p.challenge = c;
        c.program = p;
        programController.addNewProgram(c.getStartBlock(), p);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("cblocks")) {
            Player p = (Player) sender;
            
            if(args.length > 0 && args[0].equalsIgnoreCase("tutorial")) {
                String playerName = p.getName();
                String worldName = "CBTutorial_" + playerName;
                
                p.sendMessage("[CodeBlocks] Duplicating Tutorial World...");
                CodeBlocksPlugin.duplicateWorldFiles("CBTutorial", worldName);
                
                p.setOp(true);
                
                p.sendMessage("[CodeBlocks] Importing Tutorial World...");
                p.sendMessage("[CodeBlocks] Joining Tutorial World...");
                CodeBlocksPlugin.importAndTeleportToWorld(p, worldName, "NORMAL");
                p.performCommand("mv modify set monsters false CBTutorial_" + playerName);
                p.performCommand("pex world " + worldName + " inherit CBTutorial");
                
                p.setOp(false);
            }
            
            /*if(args.length > 0 && args[0].equalsIgnoreCase("wheat")) {
                Block lookingAt = p.getTargetBlock(null, 100);
                Program prog = programController.getProgram(lookingAt);
                if(prog != null)
                    prog.givePlayerCrops(p);
            }*/
            
            return true;
        }
        
        return false;
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
        
        try
        {
            File dir = new File(fromWorld);
            
            if(!dir.exists()) {
                p = rt.exec( "cp -r " + fromWorld + " " + toWorld);
                
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) { e.printStackTrace(); }
                
                p = rt.exec( "rm " + toWorld + "/uid.dat" );
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