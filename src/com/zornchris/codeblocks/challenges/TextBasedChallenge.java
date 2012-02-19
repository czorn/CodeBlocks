package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.zornchris.codeblocks.events.ChallengeLoadEvent;
import com.zornchris.codeblocks.program.Program;
import com.zornchris.codeblocks.program.Robot;
import com.zornchris.codeblocks.program.TextProgram;

public class TextBasedChallenge extends Challenge {
	
	private ArrayList<String> blockLocations;
	private Plugin plugin;
	
	public static final int MOVE_TO = 0;
	public static final int DESTROY = 1;
	private ArrayList<Goal> goals = new ArrayList<Goal>();
	private int currentVersion = 0;
	private Random rand = new Random();
	private String name;
	
	public TextBasedChallenge(Plugin plugin, Player player, Block sign, String name, String fileName) {
		this.plugin = plugin;
	    this.sign = sign;
	    this.name = name;
	    this.player = player;
		
	    loadFile(name);
		
		// Is this challenge being run by a text file?
		if(fileName.length() > 0)
		    startBlock = TextProgram.createStartBlock(sign.getRelative(BlockFace.SOUTH)
                    .getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST)
                    .getRelative(BlockFace.EAST), name);
		else
    		startBlock = Program.createStartBlock(sign.getRelative(BlockFace.SOUTH)
                    .getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST)
                    .getRelative(BlockFace.EAST));
		
		generateBlocks(blockLocations);
	}
	
	private void loadFile(String name) {
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
	        //System.out.println("Checking Goal");
	        switch(g.type) {
	            case MOVE_TO:
	                //System.out.println("Checking Move To");
	                if(!robot.isOccupyingBlock(robotGoalLocation))
	                    return false;
	                break;
	            
	            case DESTROY:
	                for(Block b : blocksToDestroy) {
	                    //System.out.println("Checking for destroyed block");
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
					case 'b':
					case 'x':
					case '.':
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
						break;
					case 'w':
					    blockType = Material.CROPS;
					    break;
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
				currentBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.DIRT);
				if(blockChar == 'r') {
				    currentBlock.getRelative(BlockFace.DOWN).setType(Material.WOOL);
				    currentBlock.getRelative(BlockFace.DOWN).setData(Program.ROBOT_START_DATA);
				}
				else if(blockChar == 'b') {
				    currentBlock.getRelative(BlockFace.DOWN).setType(Material.WATER);
				}
				else if(blockChar == 'w')
				    currentBlock.getRelative(BlockFace.DOWN).setType(Material.SOIL);
				else
				    currentBlock.getRelative(BlockFace.DOWN).setType(Material.SAND);
				currentBlock.getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock.setType(blockType);
				if(blockType == Material.CROPS)
				    currentBlock.setData((byte) 7);
				currentBlock = currentBlock.getRelative(BlockFace.EAST);
			}
			rowStartBlock = rowStartBlock.getRelative(BlockFace.NORTH);
			currentBlock = rowStartBlock;
		}
		
		plugin.getServer().getPluginManager().callEvent(new ChallengeLoadEvent(this));
	}
	
	
	
	@Override
	public void reset() {
	    //int nextVersion = (int)(rand.nextDouble() * 3);
	    currentVersion = (currentVersion + 1) % 3;
	    String fileName = name + "-" + currentVersion;
	    System.out.println(fileName);
	    File f = new File("plugins/CodeBlocks/Challenges/" + fileName + ".txt");
	    
	    if(f.exists())
	        loadFile(fileName);
	    else
	        loadFile(name);
	    
		generateBlocks(blockLocations);
	}
	
	@Override
	public String getDescription() {
	    return name;
	}
	
	@Override
    public String getName() { return name; }
	
	private class Goal {
	    public int type;
	    public char blockType;
	    
	    public Goal(int t, char b) { type = t; blockType = b; }
	}
}
