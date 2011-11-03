package com.zornchris.codeblocks;
// 
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import com.zornchris.codeblocks.challenges.ChallengeController;
import com.zornchris.codeblocks.robot.Program;
import com.zornchris.codeblocks.robot.ProgramController;

public class CBPlayerListener extends PlayerListener {
    private final CodeBlocksPlugin plugin;
    
    
    public CBPlayerListener(CodeBlocksPlugin instance) {//, PluginManager pm) {
        plugin = instance;
    }
    
    /* Called when players left or right click
     * on blocks
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	super.onPlayerInteract(event);
    	
        Block b = event.getClickedBlock();
    	Player p = event.getPlayer();
    	
    	if(b == null)
    		return;
    	
    	// If the player right or left clicks a switch, start it
    	if( b.getType() == Material.LEVER ) {
    		Lever lever = (Lever) (b.getState().getData());
    		Block leverIsSittingOn = b.getRelative(lever.getAttachedFace());
    		
    		if( ProgramController.isStartBlock(leverIsSittingOn) ) {
    		    if(p.hasPermission("codeblocks.run"))
    		        plugin.programController.programInteraction(p, leverIsSittingOn, lever.isPowered());
    		}
    		if( ProgramController.isTextProgramStartBlock(leverIsSittingOn) ) {
                if(p.hasPermission("codeblocks.run"))
                    plugin.programController.programInteraction(p, leverIsSittingOn, lever.isPowered());
            }
    	}
    	
    	else if( b.getType() == Material.STONE_BUTTON) {
    	    Button button = (Button) (b.getState().getData());
    	    Block buttonIsSittingOn = b.getRelative(button.getAttachedFace());
    	    Program prog = plugin.programController.getProgram(buttonIsSittingOn);
    	    if(prog != null)
    	        prog.givePlayerCrops(p);
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
    		    if(p.hasPermission("codeblocks.loadchallenge")) {
    		        plugin.challengeController.resetChallenge(b, sign.getLines(), p);
    		        p.sendMessage("[CodeBlocks] Reset Challenge");
    		    }
    		}
    	}
    }
}

