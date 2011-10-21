package com.zornchris.codeblocks.robot;

import java.util.HashMap;
import java.util.Stack;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.zornchris.codeblocks.events.RobotExplodeEvent;

public class Robot {
	private Plugin plugin;
	
	private Block robot;	// The dispenser Block that represents the Robot
	private Block sign;		// The sign on the Robot indicating the
							// facing direction
	private int facingDirIndex;
	
	public static final Material ROBOT_MATERIAL = Material.DISPENSER;
	public static final Material ROBOT_DIRECTION_MATERIAL = Material.SIGN_POST;
	
	public Robot(Plugin plugin, Block robot) {
		this.plugin = plugin;
		this.robot = robot;
		robot.setType(ROBOT_MATERIAL);	// Create the Robot
		
		facingDirIndex = 1;
		robot.setData(Program.BYTE_DIRS[facingDirIndex]);	// Set its initial direction
		
		sign = robot.getRelative(BlockFace.UP);		// Update the sign
		updateSign();
	}
	
	
	
/***************************************
 * COMMANDS YOU CAN ISSUE TO THE ROBOT *
 ***************************************/
	
	/**
	 * Move the robot forward if there is space.
	 * If the robot would occupy the same space as
	 * a TNT block, the robot explodes
	 */
	public void moveForward()
	{
		Block nextBlock = robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]);
		Material nextBlockType = nextBlock.getType();
		
		if( nextBlockType == Material.AIR  || nextBlockType == Material.TNT) {
			clear();			// Clear current location
			moveTo(nextBlock);	// Move to next block
			
			if( nextBlockType == Material.TNT )
				explode();
		}
	}
	
	/**
	 * Rotates the robot
	 * @param turningDir	1 is clockwise, -1 is counter-clockwise
	 */
	public void turn(int turningDir)
	{
		int newIndex = (facingDirIndex + turningDir) % 4;
		if(newIndex < 0)
			facingDirIndex = 4 + newIndex;
		else
			facingDirIndex = newIndex;
		robot.setData(Program.BYTE_DIRS[facingDirIndex]);
		updateSign();
	}
	
	/**
	 * Fire an arrow from the robot
	 */
	public void fire()
	{
		Dispenser d = (Dispenser)robot.getState();
		Inventory inv = d.getInventory();
		inv.addItem(new ItemStack(Material.ARROW, 1));
		d.dispense();
	}
	
	/**
	 * Returns whether the block in front of the
	 * robot matches 'blockType'
	 * @param blockType	the material type to compare to
	 */
	public boolean senseBlockInFront(String blockType)
	{
		return robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]).getType()
					.toString().equalsIgnoreCase(blockType);
	}
	
	/**
	 * Returns whether the block in below the
	 * robot matches 'blockType'
	 * @param blockType	the material type to compare to
	 */
	public boolean senseBlockBelow(String blockType)
	{
		return robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]).getType()
					.toString().equalsIgnoreCase(blockType);
	}
	
	/**
	 * Destroys the block in front of the robot if it is
	 * a certain type(s). If the block is TNT, the robot
	 * will explode and stop moving.
	 */
	public void destroy() {
		Block inFront = robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]);
		
		if(inFront.getType() == Material.DIRT || inFront.getType() == Material.GRASS
				|| inFront.getType() == Material.TNT)
			inFront.setType(Material.AIR);
		
		if(inFront.getType() == Material.TNT)
			explode();
	}
	
	/**
	 * Defuses the block in front of the robot if it is
	 * a TNT Block.
	 */
	public void defuse() {
		Block inFront = robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]);
		
		if(inFront.getType() == Material.TNT)
			inFront.setType(Material.AIR);
	}
	
/**************************
 * SHORT HELPER FUNCTIONS *
 **************************/
	
	/**
	 * Removes the robot from its current location
	 */
	public void clear() {
		sign.setType(Material.AIR);
		robot.setType(Material.AIR);
	}
	
	/**
	 * Moves the robot to the specified location
	 * @param newLocation
	 */
	public void moveTo(Block newLocation) {
		robot = newLocation;
		robot.setType(ROBOT_MATERIAL);
		robot.setData(Program.BYTE_DIRS[facingDirIndex]);
		updateSign();
	}
	
	/**
	 * Fires an event indicating that the robot interacted with at TNT
	 * block poorly. The RobotController listens for this event.
	 */
	public void explode() {
		System.out.println("exploding");
		plugin.getServer().getPluginManager().callEvent(new RobotExplodeEvent());
	}
	
	/**
	 * Update the orientation and location of the sign on
	 * top of the robot.
	 * Called every time the robot turns or moves.
	 */
	public void updateSign() {
		sign.setType(Material.AIR);
		sign = robot.getRelative(BlockFace.UP);
		if( sign.getType() ==  Material.AIR) {
			sign.setType(ROBOT_DIRECTION_MATERIAL);
			sign.setData((byte) ((facingDirIndex % 4) * 4));
			((Sign) sign.getState()).setLine(1, "FRONT");
		}
	}
	
}
