package com.zornchris.codeblocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import com.zornchris.codeblocks.robot.Program;

public class CBBlockListener extends BlockListener {

	private CodeBlocksPlugin plugin;
	
	public CBBlockListener(CodeBlocksPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onSignChange(SignChangeEvent event) {
		Block b = event.getBlock();
		Player p = event.getPlayer();
		
		Program.setupBranchBlocks(b, event.getLine(0));
		
		if(p.hasPermission("codeblocks.loadchallenge"))
		    plugin.challengeController.createChallenge(b, event.getLines());
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if(b.getType() == Material.SIGN_POST)
			plugin.challengeController.removeChallenge(b);
	}
}
