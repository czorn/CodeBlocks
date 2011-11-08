package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.Robot;

public class Challenge {
    
    protected Block sign;
    protected Block startBlock;
    protected Block robotStartLocation;
    protected Block robotGoalLocation;
    protected ArrayList<Block> blocksToDestroy = new ArrayList<Block>();
    protected Player player;
    public Program program;

	public Challenge() {}
	
	public void reset() {}
	public boolean isComplete() { return false; }
	public boolean isComplete(Robot robot) { return false; }
	public Block getRobotStartLocation() { return robotStartLocation; }
	public Block getStartBlock() { return startBlock; }
	public Player getPlayer() { return player; }
}
