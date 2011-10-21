package com.zornchris.codeblocks;
// 
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Lever;
import org.bukkit.plugin.PluginManager;

import com.zornchris.codeblocks.challenges.ChallengeController;
import com.zornchris.codeblocks.robot.Program;

public class CBPlayerListener extends PlayerListener {
    private final CodeBlocksPlugin plugin;
    
    
    public CBPlayerListener(CodeBlocksPlugin instance) {//, PluginManager pm) {
        plugin = instance;
    }
    
    /* Called when players left or right click
     * on blocks
     */
    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	Block b = event.getClickedBlock();
    	Player p = event.getPlayer();
    	
    	if(b == null)
    		return;
    	
    	// If the player right or left clicks a switch, start it
    	if( b.getType() == Material.LEVER ) {
    		Lever lever = (Lever) (b.getState().getData());
    		Block buttonIsSittingOn = b.getRelative(lever.getAttachedFace());
    		if( Program.isStartBlock(buttonIsSittingOn) ) {
    		    if(p.hasPermission("codeblocks.run"))
    		        plugin.programController.programInteraction(buttonIsSittingOn, lever.isPowered());
    		}
    	}
    	// If the player right clicks any other blocks
    	if( event.getAction() == Action.RIGHT_CLICK_BLOCK )
    		this.handleUseInteraction(b, p);
    }
    
    /* Called when users right click a block
     */
    private void handleUseInteraction(Block b, Player p) {
    	if( b.getType() == Material.SIGN_POST ) {
    		Sign sign = (Sign) b.getState();
    		
    		if(ChallengeController.isChallengeSign(sign.getLine(0))) {
    		    if(p.hasPermission("codeblocks.loadchallenge"))
    		        plugin.challengeController.resetChallenge(b, sign.getLines(), p);
    		}
    	}
    }
}

