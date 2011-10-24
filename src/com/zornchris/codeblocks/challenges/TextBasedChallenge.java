package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

import com.zornchris.codeblocks.events.ChallengeLoadEvent;
import com.zornchris.codeblocks.robot.Robot;

public class TextBasedChallenge extends Challenge {
	
	private ArrayList<String> blockLocations;
	private Plugin plugin;
	
	public static final int MOVE_TO = 0;
	public static final int DESTROY = 1;
	private ArrayList<Goal> goals = new ArrayList<Goal>();
	
	public TextBasedChallenge(Plugin plugin, Block sign, String name) {
		this.plugin = plugin;
	    this.sign = sign;
		
		File f = new File("plugins/CodeBlocks/Challenges/" + name + ".txt");
		ArrayList<String> lines = new ArrayList<String>();
		
		try {
			Scanner scn = new Scanner(f);
			while(scn.hasNextLine())
				lines.add(scn.nextLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		generateConditions(lines.remove(0));
		setRobotDirection(lines.remove(0));
		blockLocations = lines;
		
		startBlock = createStartBlock(sign.getRelative(BlockFace.SOUTH)
                .getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST)
                .getRelative(BlockFace.EAST));
		
		generateBlocks(blockLocations);
	}
	
	public void generateConditions(String s) {
		String[] conditions = s.split(" ");
		
		if(conditions.length < 2)
		    return;
		
		for(int i = 0; i < conditions.length; i += 2) {
		    if(conditions[i].equalsIgnoreCase("moveto")) 
		        goals.add(new Goal(MOVE_TO, (char)0));
		    else if(conditions[i].equalsIgnoreCase("remove"))
		        goals.add(new Goal(DESTROY, conditions[i+1].charAt(0)));
		}
	}
	
	@Override
	public boolean isComplete(Robot robot) {
	    for(Goal g : goals) {
	        System.out.println("Checking Goal");
	        switch(g.type) {
	            case MOVE_TO:
	                System.out.println("Checking Move To");
	                if(!robot.isOccupyingBlock(robotGoalLocation))
	                    return false;
	                break;
	            
	            case DESTROY:
	                for(Block b : blocksToDestroy) {
	                    System.out.println("Checking for destroyed block");
	                    if(b.getType() != Material.AIR)
	                        return false;
	                }
	                break;
	        }
	    }
	    
	    return true;
	}
	
	public void setRobotDirection(String s) {
		
	}
	
	private void generateBlocks(ArrayList<String> rows) {
		Block currentBlock = sign.getRelative(BlockFace.NORTH);
		Block rowStartBlock = currentBlock;
		
		for (int row = rows.size() - 1; row >= 0; row--) {
			currentBlock = rowStartBlock;
			String rowChars = rows.get(row);
			
			for(int col = 0; col < rowChars.length(); col++) {
				char blockChar = rowChars.charAt(col);
				
				Material blockType = Material.AIR;
				switch(blockChar) {
					case 'r':
					    robotStartLocation = currentBlock;
					case 'z':
					    robotGoalLocation = currentBlock;
					case 'x':
						blockType = Material.AIR;
						break;
					case 'd':
						blockType = Material.DIRT;
						break;
					case 't':
						blockType = Material.TNT;
						break;
					case 'g':
						blockType = Material.GOLD_BLOCK;
						break;
					case 's':
						blockType = Material.STONE;
				}
				
				// Keep track of which blocks need to be destroyed
				for(Goal g : goals) {
				    if(g.type == DESTROY) {
				        if(g.blockType == blockChar) {
				            blocksToDestroy.add(currentBlock);
				            break;
				        }
				    }
				}
				
				// Setup the stack of blocks for this X,Z location
				currentBlock.setType(blockType);
				currentBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.DIRT);
				currentBlock.getRelative(BlockFace.DOWN).setType(Material.SAND);
				currentBlock.getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock = currentBlock.getRelative(BlockFace.EAST);
			}
			rowStartBlock = rowStartBlock.getRelative(BlockFace.NORTH);
			currentBlock = rowStartBlock;
		}
		
		plugin.getServer().getPluginManager().callEvent(new ChallengeLoadEvent(this));
	}
	
	
	
	@Override
	public void reset() {
		generateBlocks(blockLocations);
	}
	
	private class Goal {
	    public int type;
	    public char blockType;
	    
	    public Goal(int t, char b) { type = t; blockType = b; }
	}
}
