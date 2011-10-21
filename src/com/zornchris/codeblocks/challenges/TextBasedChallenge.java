package com.zornchris.codeblocks.challenges;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

public class TextBasedChallenge extends Challenge {
	
	private ArrayList<String> blockLocations;
	
	public TextBasedChallenge(Block sign, String name, Player player) {
		this.sign = sign;
		this.player = player;
		
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
				
				currentBlock.setType(blockType);
				currentBlock.getRelative(BlockFace.DOWN).setType(Material.SAND);
				currentBlock.getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.AIR);
				currentBlock = currentBlock.getRelative(BlockFace.EAST);
			}
			rowStartBlock = rowStartBlock.getRelative(BlockFace.NORTH);
			currentBlock = rowStartBlock;
		}
	}
	
	
	
	@Override
	public void reset() {
		generateBlocks(blockLocations);
	}
}
