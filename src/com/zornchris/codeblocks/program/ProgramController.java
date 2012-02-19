package com.zornchris.codeblocks.program;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProgramController {

    private Plugin plugin;
    private HashMap<Block, Program> programs = new HashMap<Block, Program>();
    
    public ProgramController(Plugin p) {
        plugin = p;
    }
    
    public void programInteraction(Player player, Block start, boolean leverState) {
        Program p = programs.get(start);
        
        if(p == null) {
            if(isTextProgramStartBlock(start)) {
                Block b = start.getRelative(BlockFace.UP);
                
                
                if( b.getType() == Material.SIGN_POST ) {
                    Sign sign = (Sign) b.getState();
                    String file = sign.getLine(0).trim();
                    System.out.println(file);
                    
                    file = TextProgram.formatFileName(file, player);
                    
                    p = new TextProgram(plugin, start, file, null, player);
                }
            }
            else {
                p = new Program(plugin, start, null);
            }
            addNewProgram(start, p);
        }
        
        p.startStop(leverState, player);
    }
    
    public void addNewProgram(Block start, Program p) {
        Program old = getProgram(start);
        if(old != null)
            old.stop();
        
        programs.put(start, p);
    }

    public Program getProgram(Block b) {
        //System.out.println(b.getLocation());
        return programs.get(b);
    }
    
    /*
     * Helper method for checking if the block
     * is a start block
     */
    public static boolean isStartBlock(Block b)
    {
        return b.getType() == Material.GOLD_BLOCK;
    }
    
    /*
     * Helper method for checking if the block
     * is a start block for text programs
     */
    public static boolean isTextProgramStartBlock(Block b)
    {
        return b.getType() == Material.DIAMOND_BLOCK;
    }

    public void removeProgram(Block b) {
        Program p = programs.get(b);
        if(p != null) {
            p.stop();
            programs.remove(b);
        }
    }
    
}
