package com.zornchris.codeblocks.challenges;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

public class Challenge {
    
    protected Block sign;
    protected Block startBlock;
    protected Block robotStartLocation;
    protected Block robotGoalLocation;
    protected Player player;

	public Challenge() {}
	
	public void reset() {}
	
	protected Block createStartBlock(Block b) {
	    Block returnBlock;
	    
        Block x = b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
        x.setType(Material.DIAMOND_BLOCK);
        
        b.setType(Material.GOLD_BLOCK);
        b = b.getRelative(BlockFace.UP);
        b.setType(Material.GOLD_BLOCK);
        returnBlock = b;
        
        b = b.getRelative(BlockFace.WEST);
        b.setType(Material.LEVER);
        
        Lever l = (Lever)(b.getState().getData());
        l.setPowered(false);
        b.setData((byte) 3);
        
        return returnBlock;
    }
	
	
	public Block getStartBlock() { return startBlock; }
}
