package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Lever;

import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.Robot;

public class Challenge {
    
    protected Block sign;
    protected Block startBlock;
    protected Block robotStartLocation;
    protected Block robotGoalLocation;
    protected ArrayList<Block> blocksToDestroy = new ArrayList<Block>();
    public Program program;

	public Challenge() {}
	
	
	
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
	
	public void reset() {}
	public boolean isComplete() { return false; }
	public boolean isComplete(Robot robot) { return false; }
	public Block getRobotStartLocation() { return robotStartLocation; }
	public Block getStartBlock() { return startBlock; }
}
