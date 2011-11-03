package com.zornchris.codeblocks.challenges;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.zornchris.codeblocks.CodeBlocksPlugin;
import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.TextProgram;

public class ChallengeController {
    private CodeBlocksPlugin plugin;
	private HashMap<Block, Challenge> challenges = new HashMap<Block, Challenge>();
	
	public ChallengeController(CodeBlocksPlugin p) {
	    plugin = p;
	}
	
	public void createChallenge(Block b, String[] lines) {
		if(isChallengeSign(lines[0])) {
		    Challenge c = new TextBasedChallenge(plugin, b, lines[1], lines[2]);
			challenges.put(b, c);
			if(lines[2].length() > 0)
			    plugin.linkChallengeAndProgram(c, new TextProgram(plugin, c.startBlock, lines[2], c));
			else
			    plugin.linkChallengeAndProgram(c, new Program(plugin, c.startBlock, c));
		}
	}
	
	public void resetChallenge(Block b, String[] lines, Player p) {
		Challenge c = challenges.get(b);
		if(c == null)
			createChallenge(b, lines);
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
