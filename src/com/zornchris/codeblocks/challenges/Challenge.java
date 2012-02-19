package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

import com.zornchris.codeblocks.program.Program;
import com.zornchris.codeblocks.program.Robot;

public class Challenge {
    
    protected Block sign;
    protected Block startBlock;
    protected Block robotStartLocation;
    protected Block robotGoalLocation;
    protected ArrayList<Block> blocksToDestroy = new ArrayList<Block>();
    protected Player player;
    protected Program program;

	public Challenge() {}
	
	public void reset() {}
	public boolean isComplete() { return false; }
	public boolean isComplete(Robot robot) { return false; }
	public Block getRobotStartLocation() { return robotStartLocation; }
	public Block getStartBlock() { return startBlock; }
	public Player getPlayer() { return player; }
	public String getDescription() { return ""; }
	public Program getProgram() { return program; }
	public void setProgram(Program p) { program = p; }
	public String getName() { return null; }
}
