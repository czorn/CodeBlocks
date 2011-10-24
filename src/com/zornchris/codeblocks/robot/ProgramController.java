package com.zornchris.codeblocks.robot;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class ProgramController {

    private Plugin plugin;
    private HashMap<Block, Program> programs = new HashMap<Block, Program>();
    
    public ProgramController(Plugin p) {
        plugin = p;
    }
    
    public void programInteraction(Block start, boolean leverState) {
        Program p = programs.get(start);
        
        if(p == null) {
            p = new Program(plugin, start, null);
            addNewProgram(start, p);
        }
        
        p.startStop(leverState);
    }
    
    public void addNewProgram(Block start, Program p) {
        System.out.println(start.getLocation());
        programs.put(start, p);
    }

    public Program getProgram(Block b) {
        System.out.println(b.getLocation());
        return programs.get(b);
    }
    
}
