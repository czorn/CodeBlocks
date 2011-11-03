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
import org.bukkit.material.Lever;
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
	
	protected Robot robot = null;		// The Robot Object
	protected Plugin plugin;
	protected RobotTask task;			// Task called every second to move the robot to the next block
	protected int taskId = -1;		// Id of Task needed to cancel the task on occassion
	protected Block programCounter;	// A pointer to the block currently being executed
	protected Stack<Block> functionCalls;	// The stack of function calls
	protected int speed = 20; // How many server ticks pass in between
	protected Player lastPlayer;
	
	protected Block lastPC;
	protected Material lastPCMaterial;
	protected boolean showPC = true;
	protected boolean isRunning = false;
	
	// The directions aren't in order by increasing value, so I've put them in order
										// [west, north, east, south]
	public static final byte[] BYTE_DIRS =  {3,       4,    2,     5};
	public static final BlockFace[] BLOCKFACE_DIRS = {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH};
	public static final int DIR_INDEX_WEST = 0;
	public static final int DIR_INDEX_NORTH = 1;
	public static final int DIR_INDEX_EAST = 2;
	public static final int DIR_INDEX_SOUTH = 3;
	
	public static byte FORWARD_DATA = DyeColor.BLACK.getData();
	public static byte RIGHT_DATA = DyeColor.GRAY.getData();
	public static byte LEFT_DATA = DyeColor.RED.getData();
	public static byte HARVEST_DATA = DyeColor.PINK.getData();
	public static byte FIRE_DATA = DyeColor.GREEN.getData();
	public static byte DESTROY_DATA = DyeColor.LIME.getData();
	public static byte DEFUSE_DATA = DyeColor.BROWN.getData();
	public static byte BRANCH_DATA = DyeColor.YELLOW.getData();
	public static byte POS_BRANCH_DATA = DyeColor.CYAN.getData();
	public static byte NEG_BRANCH_DATA = DyeColor.ORANGE.getData();
	public static byte ROBOT_START_DATA = DyeColor.SILVER.getData();
	
	
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
            robotStartingLocation = startBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH);
        plugin.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new RobotCustomListener(), Priority.Normal, plugin);
        //System.out.println("Program with challenge");
    }

	/**
	 * Starts or stops the Robot depending upon the state of the lever
	 * @param player   the last player to turn on or stop the robot
	 * @param leverIsOn the state of the lever
	 */
	public void startStop(boolean leverIsOn, Player player)
	{
	    lastPlayer = player;
	    
		if (leverIsOn && !isRunning)
			start();
		
		else if (!leverIsOn && isRunning) {
		    plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, 
		            new Runnable() 
		            { 
		                @Override
                        public void run() {
                            stop();
                        }
                    },
        		    3);
		}
	}
	
	/**
	 * Stops the Robot from processing anymore blocks
	 */
	public void stop()
	{
		//System.out.println("[CodeBlocks] Stopping Robot");
		if(taskId != -1)
			plugin.getServer().getScheduler().cancelTask(taskId);
		isRunning = false;
		setLeverState(false);
		lastPlayer.sendMessage("[CodeBlocks] Robot Stopped");
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
		clearPCIndicator();
		
		lastPCMaterial = programCounter.getType();
		lastPC = programCounter;
		
		// Analyze and store all of the function blocks
		functionBlocks = new HashMap<String, FuncBlock>();
		determineFunctionBlocks(startBlock);
		
		// Reset the stack trace
		functionCalls = new Stack<Block>();
		
		// Reset the robot if it's already running
		if(robot != null && robot.blockIsRobot()) {
			robot.reset();
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
		clearPCIndicator();
		
		switch(blockType)
		{
			case INSTRUCTION_BLOCK:
			    showPCIndicator();
				processInstruction(b);
				break;
				
			case FUNCTION_BLOCK:
			    if(functionCalls.size() < 100) {
    				functionCalls.push(b);		// Add the location to the stack
    				FuncBlock fb = functionBlocks.get( convertBlockToFunctionKey(b) );
    				if( fb != null ) {
    					//programCounter.setType(Material.WOOL);
    				    showPCIndicator();
    				    taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, speed);
    					programCounter = fb.getFirstInstructionBlock();
    					//evaluateBlock( programCounter );
    				}
			    }
				break;
				
			case INVALID_BLOCK:
				// If you've run out of blocks, check the stack to see if you should head back to before a function call
				if( !functionCalls.isEmpty() ) {
					programCounter = functionCalls.pop();
					incrementProgramCounter();
					evaluateBlock( programCounter );
				}
				
				// out of blocks and out of function returns, stop the program
				else {
				    stop();
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
		if(b.getData() == FORWARD_DATA) {
			robot.moveForward();
		}
		else if(b.getData() == LEFT_DATA) {
			robot.turn(-1);
		}
		else if(b.getData() == RIGHT_DATA) {
			robot.turn(1);
		}
		else if(b.getData() == FIRE_DATA) {
			robot.fire();
		}
		else if(b.getData() == DESTROY_DATA) {
			robot.destroy();
		}
		else if(b.getData() == DEFUSE_DATA) {
			robot.defuse();
		}
		else if(b.getData() == HARVEST_DATA) {
            robot.harvest();
        }
		else if(b.getData() == BRANCH_DATA){
			// If we're branching, moving the program counter requires
			// extra work
		    Block signBlock = b.getRelative(BlockFace.UP);
		    Sign sign = (Sign) signBlock.getState();
		    
			boolean conditionValue = robot.senseBlockInFront(sign.getLine(0));
			
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
	
	/**
	 * Move the program counter to the next block, or follow the redstone wire
	 * to the next block
	 */
	public void incrementProgramCounter() {
		programCounter = programCounter.getRelative(BlockFace.SOUTH);
		if( programCounter.getType() == Material.REDSTONE_WIRE )
			programCounter = followRedStone(DIR_INDEX_SOUTH, programCounter);
	}
	
	/**
	 * Given a block, determine the String representation of the stack of blocks.
	 * This representation is the key for the Hashmap of FuncBlocks
	 */
	public String convertBlockToFunctionKey(Block b) {
		String functionCombination = b.getType().toString();
		//functionCombination += b.getRelative(BlockFace.DOWN).getType().toString();
		return functionCombination;
	}
	
	
	
	/**
	 * Follow the redstone wire starting at Block b and in the direction
	 * specified by dirIndex until you run out of wire. Return the block
	 * located at the end of the wire.
	 */
	public Block followRedStone(int dirIndex, Block b) {
		int last = (dirIndex + 2) % 4;
		boolean foundNext;
		
		do {
			foundNext = false;
			
			for(int i = 0; i < 4; i++) {
			    Block temp = b.getRelative(BLOCKFACE_DIRS[i]);
				//System.out.println("Checking FRS: " + temp.getType().toString());
				
				if(temp.getType() == Material.REDSTONE_WIRE && i != last) {
					b = temp;
					last = (i + 2) % 4;
					foundNext = true;
					break;
				}
				
			}
			//System.out.println("Picked FRS: " + b.getType().toString());
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
	public static void setupBranchBlocks(Block b)
	{
		if( b.getType() == Material.WOOL && b.getData() == DyeColor.YELLOW.getData() ) {
			
			Block ifBlock = b.getRelative(BlockFace.SOUTH)
				.getRelative(BlockFace.DOWN);
			ifBlock.setType(Material.WOOL);
			ifBlock.setData(POS_BRANCH_DATA);
			
			Block elseBlock = b.getRelative(BlockFace.EAST)
				.getRelative(BlockFace.DOWN);
			elseBlock.setType(Material.WOOL);
			elseBlock.setData(NEG_BRANCH_DATA);
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
			// Look at next  block
			nextFuncBlock = nextFuncBlock.getRelative(BlockFace.WEST);
			
			// Store it if valid
			if( nextFuncBlock.getType() != Material.AIR && nextFuncBlock.getType() != Material.WOOL && nextFuncBlock.getType() != Material.REDSTONE_WIRE)
			{
				FuncBlock fb = new FuncBlock(nextFuncBlock);
				functionBlocks.put(fb.toString(), fb);
			}
			else if (nextFuncBlock.getType() == Material.REDSTONE_WIRE) {
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
		//Block below = b.getRelative(BlockFace.DOWN);
		
		if( b.getType() == Material.WOOL)
			return INSTRUCTION_BLOCK;
		
		else if( b.getType() == Material.AIR)
			return INVALID_BLOCK;
		
		else
			return FUNCTION_BLOCK;
    }
	
	public void showPCIndicator() {
	    if(showPC) {
            lastPC = programCounter;
            Block toChange = lastPC;
            
            if(lastPC.getData() == BRANCH_DATA)
                toChange = lastPC.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
            else
                toChange = lastPC.getRelative(BlockFace.UP);
            
            toChange.setType(programCounter.getType());
            toChange.setData(lastPC.getData());
	    }
	}
	
	public void clearPCIndicator() {
	    if(showPC) {
            if(lastPC != null && lastPCMaterial != null) {
                if(lastPC.getData() == BRANCH_DATA)
                    lastPC.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.AIR);
                else
                    lastPC.getRelative(BlockFace.UP).setType(Material.AIR);
	        }
        }
	}
	
	public void givePlayerCrops(Player p) {
        int numWheat = robot.popWheat();
        p.sendMessage("[CodeBlocks] This robot has harvested " + numWheat + " wheat(s) for you.");
        if(numWheat > 0)
            p.getInventory().addItem(new ItemStack(Material.WHEAT, numWheat));        
    }
	
	private void setLeverState(boolean state) {
	    
	    Block l = null;
	    int i;
	    
	    for(i = 0; i < 4; i++) {
	        l = startBlock.getRelative(BLOCKFACE_DIRS[i]);
	        System.out.println(l.getType());
	        if(l.getType() == Material.LEVER)
	            break;
	    }
	    
	    if(i == 4)
	        l = startBlock.getRelative(BlockFace.UP);
	    
	    if(l.getType() != Material.LEVER)
	        return;
	    
	    byte value = (byte) (state ? 0 : 8);
	    byte currentValue = l.getData();
	    
	    if(currentValue - 8 >= 0)
	        currentValue -= 8; 
	    
	    System.out.println("Setting state to: " + (currentValue + value));
	    l.setData((byte) (currentValue + value));
	    //l.setData((byte) (2));
	    Lever lever = (Lever)(l.getState().getData());
	    lever.setPowered(state);
	}
	
	public static Block createStartBlock(Block b) {
        Block returnBlock;
        
        Block x = b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
        x.setType(Material.DIAMOND_BLOCK);
        
        b.setType(Material.GOLD_BLOCK);
        returnBlock = b;
        
        b = b.getRelative(BlockFace.NORTH);
        b.setType(Material.LEVER);
        
        Lever l = (Lever)(b.getState().getData());
        l.setPowered(false);
        b.setData((byte) 10);
        
        return returnBlock;
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
    	    System.out.println("task");
    	    if(challenge != null) {
    	        if(!challenge.isComplete(robot)) {
    	            evaluateBlock(programCounter);
    	        }
    	        else {
    	            //System.out.println("[CodeBlocks] Challenge Completed");
    	            lastPlayer.sendMessage("[CodeBlocks] Challenge Completed");
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
    	
    	public FuncBlock(Block t)
    	{
    		topBlock = t;
    	}
    	
    	/*
    	 * Returns the first block in the function
    	 * that holds a valid instruction or function call
    	 */
    	public Block getFirstInstructionBlock()
    	{
    		Block first = topBlock.getRelative(BlockFace.SOUTH);
    		if( first.getType() == Material.REDSTONE_WIRE ) {
    			return followRedStone(DIR_INDEX_SOUTH, first);
    		}
    		else
    			return first;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return topBlock.getType().toString();
    	}
    }
    
    private class RobotCustomListener extends CustomEventListener {
    	public RobotCustomListener() {}
    	
    	@Override
    	public void onCustomEvent(Event event) {
    		if(event instanceof RobotExplodeEvent) {
    			//System.out.println("[CodeBlocks] Robot Exploded");
    		    lastPlayer.sendMessage("[CodeBlocks] Robot Exploded");
    			stop();
    		}
    	}
    }
    
}
