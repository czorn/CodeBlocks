package com.zornchris.codeblocks.challenges;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.zornchris.codeblocks.CodeBlocksPlugin;
import com.zornchris.codeblocks.program.Program;
import com.zornchris.codeblocks.program.TextProgram;

public class ChallengeController {
    private CodeBlocksPlugin plugin;
	private HashMap<Block, Challenge> challenges = new HashMap<Block, Challenge>();
	
	public ChallengeController(CodeBlocksPlugin p) {
	    plugin = p;
	}
	
	public void createChallenge(Block b, String[] lines, Player p) {
		if(ChallengeController.isChallengeSign(lines[0])) {
		    Challenge c = new TextBasedChallenge(plugin, p, b, lines[1], lines[2]);
			challenges.put(b, c);
			
			if(lines[2].length() > 0) {
			    String fileName = TextProgram.formatFileName(lines[1].trim(), p);
			    System.out.println(fileName);
			    plugin.linkChallengeAndProgram(c, new TextProgram(plugin, c.startBlock, fileName, c, p));
			}
			else
			    plugin.linkChallengeAndProgram(c, new Program(plugin, c.startBlock, c));
		}
	}
	
	public void resetChallenge(Block b, String[] lines, Player p) {
		Challenge c = challenges.get(b);
		if(c == null)
			createChallenge(b, lines, p);
		else
			c.reset();
		
		plugin.linkChallengeAndProgram(c, c.getProgram());
	}
	
	public void removeChallenge(Block b) {
		challenges.remove(b);
	}
	
	public static boolean isChallengeSign(String line0) {
	    line0 = line0.trim();
		return line0.equalsIgnoreCase("challenge");
	}
}
