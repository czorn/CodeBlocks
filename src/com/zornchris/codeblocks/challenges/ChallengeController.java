package com.zornchris.codeblocks.challenges;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.zornchris.codeblocks.CodeBlocksPlugin;
import com.zornchris.codeblocks.robot.Program;

public class ChallengeController {
    private CodeBlocksPlugin plugin;
	private HashMap<Block, Challenge> challenges = new HashMap<Block, Challenge>();
	
	public ChallengeController(CodeBlocksPlugin p) {
	    plugin = p;
	}
	
	public void createChallenge(Block b, String[] lines, Player p) {
		if(isChallengeSign(lines[0])) {
		    Challenge c = new TextBasedChallenge(b, lines[1], p);
			challenges.put(b, c);
		    plugin.linkChallengeAndProgram(c, new Program(c.startBlock, plugin));
		}
	}
	
	public void resetChallenge(Block b, String[] lines, Player p) {
		Challenge c = challenges.get(b);
		if(c == null)
			createChallenge(b, lines, p);
		else
			c.reset();
	}
	
	public void removeChallenge(Block b) {
		challenges.remove(b);
	}
	
	public static boolean isChallengeSign(String line0) {
		return line0.equalsIgnoreCase("challenge");
	}
}
