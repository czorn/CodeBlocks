package com.zornchris.codeblocks;
//
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

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
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "[CodeBlocks] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void linkChallengeAndProgram(Challenge c, Program p) {
        System.out.println("Linking Program & Challenge");
        p.challenge = c;
        c.program = p;
        programController.addNewProgram(c.getStartBlock(), p);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("cblocks")) {
            Player p = (Player) sender;
            
            if(args.length > 0 && args[0].equalsIgnoreCase("wheat")) {
                Block lookingAt = p.getTargetBlock(null, 100);
                Program prog = programController.getProgram(lookingAt);
                if(prog != null)
                    prog.givePlayerCrops(p);
            }
            
            return true;
        }
        
        return false;
    }
}