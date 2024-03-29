package com.zornchris.codeblocks.program;

import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import org.bukkit.Material;
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
	private Program program;
	private Block robot;	// The dispenser Block that represents the Robot
	private Block sign;		// The sign on the Robot indicating the
							// facing direction
	private int facingDirIndex;
	private int wheatHarvested = 0;
	private HashMap<Material, Integer> itemsCollected = new HashMap<Material, Integer>();
	private Stack<Material> blocksPickedUp = new Stack<Material>();
	
	public static final Material ROBOT_MATERIAL = Material.DISPENSER;
	public static final Material ROBOT_DIRECTION_MATERIAL = Material.SIGN_POST;
	
	public Robot(Program program, Plugin plugin, Block robot) {
		this.plugin = plugin;
		this.robot = robot;
		this.program = program;
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
		Material type = inFront.getType();
		
		if(type == Material.DIRT || type == Material.GRASS)
			inFront.setType(Material.AIR);
		
		if(type == Material.TNT) {
		    inFront.setType(Material.AIR);
			explode();
		}
	}
	
	/**
	 * Defuses the block in front of the robot if it is
	 * a TNT Block.
	 */
	public void defuse() {
		Block inFront = getBlockInFront();
		
		if(inFront.getType() == Material.TNT)
			inFront.setType(Material.AIR);
	}
	
	/**
	 * Gets wheat from crop and replants the crop
	 */
	public void harvest() {
	    Block inFront = getBlockInFront();
	    // If the block is growing wheat
	    if(inFront.getType() == Material.CROPS) {
	        // If the wheat is fully grown
	        if(inFront.getData() == (byte) 7) {
	            inFront.setData((byte) 0);
	            
	            Object x = itemsCollected.get(Material.WHEAT);
                int wheatHarvested = (x == null) ? 1 : ((Integer) x);
                
                // Harvest 1-3 wheat
                Random rand = new Random();
                wheatHarvested += rand.nextInt(3) + 1;
                
                itemsCollected.put(Material.WHEAT, wheatHarvested);
	        }
	    }
	    //updateCropsInDispenser();
	}
	
	/**
	 * Picks up the block in front of the robot
	 */
	public void pickUpBlock() {
	    Block inFront = this.getBlockInFront();
	    if(inFront.getType() != Material.AIR) {
    	    blocksPickedUp.add(inFront.getType());
    	    inFront.setType(Material.AIR);
	    }
	}
	
	public void placeBlock() {
	    Block inFront = this.getBlockInFront();
	    if(inFront.getType() == Material.AIR && blocksPickedUp.size() > 0) {
	        inFront.setType(blocksPickedUp.pop());
	    }
	}
	
	/**
	 * Mines and holds onto the block below the robot,
	 * the robot then falls into the hole
	 */
	public void mineBelow() {
	    mineBlock(getBlockBelow());
        //succumbToGravity();
	}
	
	/**
	 * 
	 */
	public void mineAhead() {
	    mineBlock(getBlockInFront());
	}
	
/**************************
 * SHORT HELPER FUNCTIONS *
 **************************/
	
	/**
	 * Mines and saves the block
	 */
	public void mineBlock(Block b) {
	    Material mat = b.getType();
	    
	    if(mat != Material.BEDROCK && mat != Material.AIR) {
    	    Object x = itemsCollected.get(mat);
            int numMined = (x == null) ? 1 : ((Integer) x) + 1;
            itemsCollected.put(mat, numMined);
            b.setType(Material.AIR);
	    }
	}
	
	/**
	 * If the robot is floating, place the robot
	 * on top of the next block below it
	 */
	public void succumbToGravity() {
	    Block below = getBlockBelow();
	    while(below.getType() == Material.AIR) {
	        below = below.getRelative(BlockFace.DOWN);
	    }
	    
	    clear();
	    moveTo(below.getRelative(BlockFace.UP));
	}
	
	public Block getBlockInFront() {
	    return robot.getRelative(Program.BLOCKFACE_DIRS[facingDirIndex]);
	}
	
	public Block getBlockBelow() {
	    return robot.getRelative(BlockFace.DOWN);
	}
	
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
		plugin.getServer().getPluginManager().callEvent(new RobotExplodeEvent(program));
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
			((Sign) sign.getState()).setLine(1, "ROBOT FRONT");
		}
	}
	
	/**
	 * Adds all of the wheat the robot has harvested to its
	 * dispenser inventory. This should only be called when
	 * the robot is stopped, to prevent the player from ?
	 */
	public void updateCropsInDispenser() {
	    Dispenser d = (Dispenser)robot.getState();
        Inventory inv = d.getInventory();
        inv.clear();
        inv.addItem(new ItemStack(Material.WHEAT, wheatHarvested));
	}
	
	public boolean isOccupyingBlock(Block b) {
        return robot.equals(b);
    }

	public boolean blockIsRobot() {
        return robot.getType() == ROBOT_MATERIAL;
    }
	
	public int popWheat() {
	    int x = wheatHarvested;
	    wheatHarvested = 0;
	    return x;
	}
	
	public void reset() {
	    clear();
	    facingDirIndex = 1;
	    this.blocksPickedUp.clear();
	}



    public void clearItemsCollected() {
        itemsCollected = new HashMap<Material, Integer>();
    }



    public HashMap<Material, Integer> getItemsCollected() { return itemsCollected; }
	
}
