package com.zornchris.codeblocks.robot;
//
import java.util.HashMap;
import java.util.Stack;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.events.ChallengeCompleteEvent;
import com.zornchris.codeblocks.events.RobotExplodeEvent;
import com.zornchris.codeblocks.events.RobotStartEvent;

/* Process:
 * Need to update
 */
public class Program
{	 
	public static final int INSTRUCTION_BLOCK = 2;		// Codes for the different
	public static final int FUNCTION_BLOCK = 3;			// types of blocks
	public static final int INVALID_BLOCK = 4;			//
	
	public static final Material ROBOT_DIRECTION_MATERIAL = Material.SIGN_POST;

	public Player player;
    public Block startBlock;
    public Block robotStartingLocation;
    public Challenge challenge;
	
	private Robot robot = null;		// The Robot Object
	private Plugin plugin;
	private RobotTask task;			// Task called every second to move the robot to the next block
	private int taskId = -1;		// Id of Task needed to cancel the task on occassion
	private Block programCounter;	// A pointer to the block currently being executed
	private Stack<Block> functionCalls;	// The stack of function calls
	private int speed = 20; // How many server ticks pass in between
	
	private Block lastPC;
	private Material lastPCMaterial;
	private boolean showPC = false;
	private boolean isRunning = false;
	
	// The directions aren't in order by increasing value, so I've put them in order
										// [west, north, east, south]
	public static final byte[] BYTE_DIRS =  {3,       4,    2,     5};
	public static final BlockFace[] BLOCKFACE_DIRS = {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH};
	public static final int DIR_INDEX_WEST = 0;
	public static final int DIR_INDEX_NORTH = 1;
	public static final int DIR_INDEX_EAST = 2;
	public static final int DIR_INDEX_SOUTH = 3;
	
	
	
	private HashMap<String, FuncBlock> functionBlocks;	// Key = Material Types as a String, Value = Struct containing blocks
	
	
	/*public Program(Plugin plugin, Block start)//, PluginManager pm)
	{
		this.plugin = plugin;
		startBlock = start;
		robotStartingLocation = startBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN);
		plugin.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new RobotCustomListener(), Priority.Normal, plugin);
		System.out.println("Program without challenge");
	}*/
	
	public Program(Plugin plugin, Block start, Challenge c)//, PluginManager pm)
    {
        this.plugin = plugin;
        startBlock = start;
        challenge = c;
        if(c != null) {
            robotStartingLocation = c.getRobotStartLocation();
        }
        else
            robotStartingLocation = startBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN);
        plugin.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new RobotCustomListener(), Priority.Normal, plugin);
        System.out.println("Program with challenge");
    }

	/**
	 * Starts or stops the Robot depending upon the state of the lever
	 * @param start
	 * @param leverIsOn the state of the lever
	 */
	public void startStop(boolean leverIsOn)
	{
		if (leverIsOn)
			start();
		else {
			stop();
		}
	}
	
	/**
	 * Stops the Robot from processing anymore blocks
	 */
	public void stop()
	{
		System.out.println("[CodeBlocks] Stopping Robot");
		if(taskId != -1)
			plugin.getServer().getScheduler().cancelTask(taskId);
		isRunning = false;
	}
	
	/**
	 * Starts the Robot
	 */
	public void start()
	{		
	    isRunning = true;
		programCounter = startBlock.getRelative(BlockFace.SOUTH);
		task = new RobotTask();
		
		// Reset the pink indicator if it's on
		if(showPC) {
			if(lastPC != null && lastPCMaterial != null)
				lastPC.setType(Material.GOLD_BLOCK);//lastPCMaterial);
		}
		
		lastPCMaterial = programCounter.getType();
		lastPC = programCounter;
		
		// Analyze and store all of the function blocks
		functionBlocks = new HashMap<String, FuncBlock>();
		determineFunctionBlocks(startBlock);
		
		// Reset the stack trace
		functionCalls = new Stack<Block>();
		
		// Reset the robot if it's already running
		if(robot != null && robot.blockIsRobot()) {
			robot.clear();
			robot.moveTo(robotStartingLocation);
		}
		else
		    robot = new Robot(this, plugin, robotStartingLocation);
		
		if(taskId != -1)
			plugin.getServer().getScheduler().cancelTask(taskId);
		
		// Determine the speed of the program
		evaluateProgramParameters(startBlock);
		
		taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, speed);
		
		plugin.getServer().getPluginManager().callEvent(new RobotStartEvent(this));
	}
	
	/**
	 * Given a block b, determine what to do with the block (e.g.
	 * run a command, call a function) and then move the program counter
	 */
	public void evaluateBlock(Block b)
	{
		int blockType = getCodeBlockType(b);
		
		if (showPC) {
			// Restore last block
			lastPC.setType(lastPCMaterial);
			
			// Save this block's material
			lastPCMaterial = Material.GOLD_BLOCK;//programCounter.getType();
			lastPC = programCounter;
		}
		
		switch(blockType)
		{
			case INSTRUCTION_BLOCK:
			    if(showPC) {
    				programCounter.setType(Material.WOOL);
    				programCounter.setData(DyeColor.MAGENTA.getData());
			    }
				processInstruction(b);
				break;
				
			case FUNCTION_BLOCK:
				functionCalls.push(b);		// Add the location to the stack
				FuncBlock fb = functionBlocks.get( convertBlockToFunctionKey(b) );
				if( fb != null ) {
					//programCounter.setType(Material.WOOL);
					programCounter = fb.getFirstInstructionBlock();
					evaluateBlock( programCounter );
				}
				break;
				
			case INVALID_BLOCK:
				// If you've run out of blocks, check the stack to see if you should head back to before a function call
				if( !functionCalls.isEmpty() ) {
					programCounter = functionCalls.pop();
					incrementProgramCounter();
					evaluateBlock( programCounter );
				}
				break;
		}
	}
	
	/**
	 * Look at the sign on the start block and change
	 * the value of certain parameters based on the text
	 */
	public void evaluateProgramParameters(Block b) {
		Block signBlock = b.getRelative(BlockFace.UP);

		if(signBlock.getType() == Material.SIGN_POST) {
			Sign sign = (Sign) signBlock.getState();
			
			for(int i = 0; i < 4; i++) {
				String[] cmd = sign.getLine(i).split(" ");
				if( cmd.length == 2) {
					if(cmd[0].equalsIgnoreCase("speed"))
						speed = Integer.parseInt(cmd[1]);
				}
			}
			
		}
	}
	
	/**
	 * Get the text on the sign of an instruction block
	 * and perform the specified command.
	 * 
	 * After performing the command, move the program counter
	 * to the next block and call the task again after the delay
	 */
	public void processInstruction(Block b)
	{
		Block signBlock = b.getRelative(BlockFace.UP);

		if(signBlock.getType() == Material.SIGN_POST) {
			Sign sign = (Sign) signBlock.getState();
			String cmd = sign.getLine(0);
			
			if(cmd.equalsIgnoreCase("forward")) {
				robot.moveForward();
			}
			else if(cmd.equalsIgnoreCase("turn left")) {
				robot.turn(-1);
			}
			else if(cmd.equalsIgnoreCase("turn right")) {
				robot.turn(1);
			}
			else if(cmd.equalsIgnoreCase("fire")) {
				robot.fire();
			}
			else if(cmd.equalsIgnoreCase("destroy")) {
				robot.destroy();
			}
			else if(cmd.equalsIgnoreCase("defuse")) {
				robot.defuse();
			}
			else if(cmd.equalsIgnoreCase("harvest")) {
                robot.harvest();
            }
			else {
				// If we're branching, moving the program counter requires
				// extra work
				boolean conditionValue = false;
				if(cmd.equalsIgnoreCase("sense")) {
					conditionValue = robot.senseBlockInFront(sign.getLine(1));
				}
				else if(cmd.equalsIgnoreCase("sense below")) {
					conditionValue = robot.senseBlockBelow(sign.getLine(1));
				}
				
			    if(conditionValue) {
			    	incrementProgramCounter();
			    }
			    else {
		    		programCounter = programCounter.getRelative(BlockFace.EAST);
		    		if( programCounter.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_WIRE )
		    			programCounter = followRedStone(DIR_INDEX_EAST, programCounter);
			    }
				
			    if(isRunning)
			    	taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, speed);
				return;
			}
			
			incrementProgramCounter();
			if(isRunning)
				taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, speed);
		}
			
	}
	
	/**
	 * Move the program counter to the next block, or follow the redstone wire
	 * to the next block
	 */
	public void incrementProgramCounter() {
		programCounter = programCounter.getRelative(BlockFace.SOUTH);
		if( programCounter.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_WIRE )
			programCounter = followRedStone(DIR_INDEX_SOUTH, programCounter);
	}
	
	/**
	 * Given a block, determine the String representation of the stack of blocks.
	 * This representation is the key for the Hashmap of FuncBlocks
	 */
	public String convertBlockToFunctionKey(Block b) {
		String functionCombination = b.getType().toString();
		functionCombination += b.getRelative(BlockFace.DOWN).getType().toString();
		return functionCombination;
	}
	
	
	
	/**
	 * Follow the redstone wire starting at Block b and in the direction
	 * specified by dirIndex until you run out of wire. Return the block
	 * located at the end of the wire.
	 */
	public Block followRedStone(int dirIndex, Block b) {
		int last = (dirIndex + 2) % 4;
		Block next;
		Block bot = b.getRelative(BlockFace.DOWN);
		boolean foundNext;
		
		do {
			foundNext = false;
			
			for(int i = 0; i < 4; i++) {
				next = b.getRelative(BLOCKFACE_DIRS[i]);
				bot = next.getRelative(BlockFace.DOWN);
				
				if(bot.getType() == Material.REDSTONE_WIRE && i != last) {
					b = next;
					last = (i + 2) % 4;
					foundNext = true;
					break;
				}
				
			}
		}
		while(foundNext);
		
		return b.getRelative(BLOCKFACE_DIRS[dirIndex]);
	}
	
	/**
	 * Place the Red and Green helper blocks next
	 * to the branching block
	 * @param b		the top of the Command Block
	 * @param text	the text on the sign of the Command Block
	 */
	public static void setupBranchBlocks(Block b, String text)
	{
		if( text.equalsIgnoreCase("Sense") || text.equalsIgnoreCase("Sense Below") ) {
			
			Block ifBlock = b.getRelative(BlockFace.SOUTH)
				.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN)
				.getRelative(BlockFace.DOWN);
			ifBlock.setType(Material.WOOL);
			ifBlock.setData(DyeColor.GREEN.getData());
			
			Block elseBlock = b.getRelative(BlockFace.EAST)
				.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN)
				.getRelative(BlockFace.DOWN);
			elseBlock.setType(Material.WOOL);
			elseBlock.setData(DyeColor.RED.getData());
		}
	}
	
	/**
	 * Prototype the function blocks.
	 * Look at the blocks to the west of the start block, or
	 * follow the redstone wire. Add each stack of blocks to the
	 * hashmap as potential starts of functions.
	 */
	public void determineFunctionBlocks(Block start) {
		Block nextFuncBlock = start;
		Block botFuncBlock;
		
		do {
			// Look at next two blocks
			nextFuncBlock = nextFuncBlock.getRelative(BlockFace.WEST);
			botFuncBlock = nextFuncBlock.getRelative(BlockFace.DOWN);
			
			// Store them if valid
			if( nextFuncBlock.getType() != Material.AIR && botFuncBlock.getType() != Material.AIR)
			{
				FuncBlock fb = new FuncBlock(nextFuncBlock, botFuncBlock);
				functionBlocks.put(fb.toString(), fb);
			}
			else if (botFuncBlock.getType() == Material.REDSTONE_WIRE) {
				nextFuncBlock = followRedStone(DIR_INDEX_WEST, nextFuncBlock);
				nextFuncBlock = nextFuncBlock.getRelative(BlockFace.EAST);
			}
			else
				break;
		}
		while(true);
		
	}

	/*
	 * Analyze the stack of blocks to determine
	 * whether it is an instruction, a function call,
	 * or an invalid stack of blocks
	 */
	public static int getCodeBlockType(Block b)
	{
		Block below = b.getRelative(BlockFace.DOWN);
		
		if( b.getType() == Material.GOLD_BLOCK && below.getType() == Material.DIAMOND_BLOCK)
			return INSTRUCTION_BLOCK;
		
		else if( b.getType() == Material.AIR || below.getType() == Material.AIR )
			return INVALID_BLOCK;
		
		else
			return FUNCTION_BLOCK;
    }

	/*
     * Helper method for checking if the block
     * is part of the start stack
     */
    public static boolean isStartBlock(Block b)
    {
    	Block below = b.getRelative(BlockFace.DOWN);
    	
    	if( b.getType() == Material.GOLD_BLOCK && below.getType() == Material.GOLD_BLOCK )
    		return true;
    	else
    		return false;
    }

    /*
     * The task that gets called after a delay in order to read and
     * process the next instruction in the 'program' that makes the
     * robot run.
     */
    private class RobotTask implements Runnable
    {
    	@Override
		public void run() {
    	    if(challenge != null) {
    	        if(!challenge.isComplete(robot)) {
    	            evaluateBlock(programCounter);
    	        }
    	        else {
    	            System.out.println("[CodeBlocks] Challenge Completed");
    	            plugin.getServer().getPluginManager().callEvent(new ChallengeCompleteEvent(challenge));
    	        }
    	    }
    	    else
    	        evaluateBlock(programCounter); 
		}
    }
    
    /*
     * A class that holds the necessary information about
     * functions
     */
    private class FuncBlock
    {
    	public Block topBlock;
    	public Block botBlock;
    	
    	public FuncBlock(Block t, Block b)
    	{
    		topBlock = t;
    		botBlock = b;
    	}
    	
    	/*
    	 * Returns the first block in the function
    	 * that holds a valid instruction or function call
    	 */
    	public Block getFirstInstructionBlock()
    	{
    		Block first = topBlock.getRelative(BlockFace.SOUTH);
    		if( first.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_WIRE ) {
    			//first = followRedStone(DIR_INDEX_SOUTH, first);
    			//return first.getRelative(BlockFace.SOUTH);
    			return followRedStone(DIR_INDEX_SOUTH, first);
    		}
    		else
    			return first;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return topBlock.getType().toString() + botBlock.getType().toString();
    	}
    }
    
    private class RobotCustomListener extends CustomEventListener {
    	public RobotCustomListener() {}
    	
    	@Override
    	public void onCustomEvent(Event event) {
    		if(event instanceof RobotExplodeEvent) {
    			System.out.println("[CodeBlocks] Robot Exploded");
    			stop();
    		}
    	}
    }
    public void givePlayerCrops(Player p) {
        int numWheat = robot.popWheat();
        p.sendMessage("[CodeBlocks] This robot has harvested " + numWheat + " wheat(s) for you.");
        if(numWheat > 0)
            p.getInventory().addItem(new ItemStack(Material.WHEAT, numWheat));        
    }
}
