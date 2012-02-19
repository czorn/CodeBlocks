package com.zornchris.codeblocks.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.zornchris.codeblocks.CodeBlocksPlugin;
import com.zornchris.codeblocks.program.Program;

public class CBBlockListener extends BlockListener {

	private CodeBlocksPlugin plugin;
	
	public CBBlockListener(CodeBlocksPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
	    Block b = event.getBlock();
	    Program.setupBranchBlocks(b);
	}
	
	@Override
	public void onSignChange(SignChangeEvent event) {
		Block b = event.getBlock();
		Player p = event.getPlayer();
		
		//Program.setupBranchBlocks(b, event.getLine(0));
		
		if(p.hasPermission("codeblocks.loadchallenge"))
		    plugin.challengeController.createChallenge(b, event.getLines(), p);
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if(b.getType() == Material.SIGN_POST) {
			plugin.challengeController.removeChallenge(b);
			plugin.programController.removeProgram(b.getRelative(BlockFace.DOWN));
		}
		else if(b.getType() == Material.WOOL)
		    Program.removeBranchingBlock(b);
	}
}
