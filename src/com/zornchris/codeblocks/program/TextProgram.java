package com.zornchris.codeblocks.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;

import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.events.ChallengeCompleteEvent;
import com.zornchris.codeblocks.events.RobotStartEvent;

public class TextProgram extends Program {

    public enum BlockType {
    Main, DoNothing, Forward, TurnLeft, TurnRight, Fire, Harvest, Destroy, Defuse, Sense, FunctionCall, FunctionDef
    };

    private String fileName;
    private ArrayList<String> lines;
    private HashMap<String, CodeBlock> functions;
    private CodeBlock main;
    private CodeBlock programCounter;
    private Stack<CodeBlock> functionCalls;
    private RobotTask task;
    private boolean programIsReady = false;
    private String errorLine = "";

    public TextProgram(Plugin plugin, Block start, String fileName,
            Challenge challenge, Player player) {
        super(plugin, start, challenge);
        this.fileName = fileName;
        this.lastPlayer = player;

        loadProgram();

        this.showPC = false;
    }

    private void loadProgram() {
        functions = new HashMap<String, CodeBlock>();
        File file = new File("plugins/CodeBlocks/TextPrograms/" + fileName + ".txt");
        
        if(!file.exists()) {
            lastPlayer.sendMessage("No file found: " + fileName);
            programIsReady = false;
            return;
        }

        try {
            Scanner scn = new Scanner(file);

            lines = new ArrayList<String>();
            while (scn.hasNext()) {
                lines.add(scn.nextLine());
            }

            programIsReady = processLines();

        } catch (FileNotFoundException e) {
            lastPlayer.sendMessage("No file found: " + fileName);
            // e.printStackTrace();
            return;
        }
        // programIsReady = processLines();

        // System.out.println("");
        // traverse(main);
    }

    private boolean processLines() {
        CodeBlock previous = new CodeBlock(null, null);
        CodeBlock current = null;
        int lineNumber = 0;

        ArrayList<BranchingBlock> branches = new ArrayList<BranchingBlock>();

        for (String line : lines) {
            line = line.trim();
            if (line.length() > 0) {

                // Find Actions
                if (line.equalsIgnoreCase("Main:")) {
                    current = new CodeBlock(BlockType.Main, null);
                    main = current;
                } else if (line.equalsIgnoreCase(";")) {
                    current = new CodeBlock(BlockType.DoNothing, null);
                } else if (line.equalsIgnoreCase("Forward")) {
                    current = new CodeBlock(BlockType.Forward, null);
                } else if (line.equalsIgnoreCase("Turn Right")) {
                    current = new CodeBlock(BlockType.TurnRight, null);
                } else if (line.equalsIgnoreCase("Turn Left")) {
                    current = new CodeBlock(BlockType.TurnLeft, null);
                } else if (line.equalsIgnoreCase("Fire")) {
                    current = new CodeBlock(BlockType.Fire, null);
                } else if (line.equalsIgnoreCase("Destroy")) {
                    current = new CodeBlock(BlockType.Destroy, null);
                } else if (line.equalsIgnoreCase("Defuse")) {
                    current = new CodeBlock(BlockType.Defuse, null);
                } else if (line.equalsIgnoreCase("Harvest")) {
                    current = new CodeBlock(BlockType.Harvest, null);
                }

                // Find Branches
                else if (line.indexOf("If ") > -1) {
                    // System.out.println("Found if");
                    if (line.indexOf("Sense") > -1) {
                        int startParen = line.lastIndexOf("(");
                        int endParen = line.lastIndexOf(")");

                        // System.out.println("Found sense");
                        if (startParen > -1 && endParen > -1) {
                            String blockToCheck = line.substring(
                                    startParen + 1, endParen);
                            blockToCheck = blockToCheck.trim();
                            current = new BranchingBlock(BlockType.Sense,
                                    blockToCheck);
                            branches.add((BranchingBlock) current);
                            // System.out.println("creating branch " +
                            // blockToCheck);
                        } else {
                            errorLine = lineNumber + " >> " + line;
                            return false;
                        }
                    } else {
                        errorLine = lineNumber + " >> " + line;
                        return false;
                    }
                } else if (line.equalsIgnoreCase("Else:")) {
                    if (branches.size() > 0)
                        current = branches.remove(branches.size() - 1);
                    else {
                        errorLine = lineNumber + " >> " + line;
                        return false;
                    }
                } else if (line.indexOf("()") > -1) {
                    String funcName = line.substring(0, line.indexOf("()"));
                    // System.out.println("Function call " + funcName);
                    current = new FunctionBlock(BlockType.FunctionCall,
                            funcName, null);
                } else if (line.indexOf(":") > -1) {
                    String funcName = line.substring(0, line.indexOf(":"));
                    // System.out.println("Function Def " + funcName);
                    current = new FunctionBlock(BlockType.FunctionDef, null,
                            funcName);
                    functions.put(funcName, current);
                } else {
                    // Current line didn't match any cases
                    errorLine = lineNumber + " >> " + line;
                    return false;
                }

                // System.out.println("Previous " + previous.type);
                previous.next = current; // Previous CodeBlock points to current

                if (previous instanceof BranchingBlock) {
                    // System.out.println("Prev is bb");
                    if (((BranchingBlock) previous).ifBlock == null) {
                        // System.out.println("Setting ifblock");
                        ((BranchingBlock) previous).ifBlock = current;
                    } else if (((BranchingBlock) previous).elseBlock == null) {
                        // System.out.println("Setting elseblock");
                        ((BranchingBlock) previous).elseBlock = current;
                    }
                }
                if (current instanceof BranchingBlock
                        && (((BranchingBlock) current).ifBlock != null)
                        && (((BranchingBlock) current).elseBlock == null)) {
                    previous.next = null;
                }
                if (current.type == BlockType.FunctionDef)
                    previous.next = null;

                /*
                 * else if(previous.type == BlockType.FunctionStart)
                 * previous.next = null; else if(line.equalsIgnoreCase("fi"))
                 * previous.next = null;
                 */
                previous = current; // We're done with current, so move it to
                                    // previous
            }
            lineNumber++;
        }
        return true;
    }

    private void traverse(CodeBlock start) {
        if (start == null)
            return;
        if (start instanceof BranchingBlock) {
            System.out.println("If " + start.type);
            traverse(((BranchingBlock) start).ifBlock);
            System.out.println("Else");
            traverse(((BranchingBlock) start).elseBlock);
        } else
            traverse(start.next);
    }

    @Override
    public void start() {
        loadProgram();
        System.out.println("appadoo");
        if (programIsReady) {
            isRunning = true;
            programCounter = main.next;
            task = new RobotTask();

            // Reset the stack trace
            functionCalls = new Stack<CodeBlock>();

            // Reset the robot if it's already running
            if (robot != null && robot.blockIsRobot()) {
                robot.reset();
                robot.moveTo(robotStartingLocation);
            } else
                robot = new Robot(this, plugin, robotStartingLocation);

            if (taskId != -1)
                plugin.getServer().getScheduler().cancelTask(taskId);

            taskId = plugin.getServer().getScheduler()
                    .scheduleSyncDelayedTask(plugin, task, speed);

            plugin.getServer().getPluginManager()
                    .callEvent(new RobotStartEvent(this));
        } else {
            stop();
            this.lastPlayer
                    .sendMessage("[CodeBlocks] Program not loaded. Error with line");
            this.lastPlayer.sendMessage(errorLine);
        }
    }

    public void evaluateBlock(CodeBlock b) {
        // System.out.println("EvaluateBlock b: " + b);
        if (b == null) {
            if (!functionCalls.isEmpty()) {
                b = functionCalls.pop();
                programCounter = b.next;
                evaluateBlock(programCounter);
            } else
                stop();
        } else if (b.type == BlockType.FunctionCall) {

            if (functionCalls.size() < 100) {
                functionCalls.push(b);
                String funcName = ((FunctionBlock) b).functionToCall;
                // System.out.println("Evaluating Function call " + funcName);
                CodeBlock funcDef = functions.get(funcName);

                if (funcDef == null) {
                    stop();
                    return;
                }

                programCounter = funcDef.next;
                lastPlayer.sendMessage("[CodeBlocks] Robot: " + funcName + "()");
                // System.out.println("function call new pc " + programCounter);
                evaluateBlock(programCounter);
            }
        } else if (b.type == BlockType.DoNothing) {
            evaluateBlock(b.next);
        } else
            processInstruction(b);
    }

    public void processInstruction(CodeBlock b) {
        if (b.type == BlockType.Sense)
            lastPlayer.sendMessage("[CodeBlocks] Robot: " + b.type + "("
                    + ((BranchingBlock) b).param + ")");
        else
            lastPlayer.sendMessage("[CodeBlocks] Robot: " + b.type);

        switch (b.type) {
        case Forward:
        robot.moveForward();
            break;

        case TurnLeft:
        robot.turn(-1);
            break;

        case TurnRight:
        robot.turn(1);
            break;

        case Fire:
        robot.fire();
            break;

        case Destroy:
        robot.destroy();
            break;

        case Defuse:
        robot.defuse();
            break;

        case Harvest:
        robot.harvest();
            break;

        case Sense:
        boolean conditionValue = robot
                .senseBlockInFront(((BranchingBlock) b).param);
        // System.out.println("Condition " + conditionValue);
        programCounter = (conditionValue) ? ((BranchingBlock) b).ifBlock
                : ((BranchingBlock) b).elseBlock;
        }

        if (!(b instanceof BranchingBlock))
            programCounter = b.next;
        if (isRunning)
            taskId = plugin.getServer().getScheduler()
                    .scheduleSyncDelayedTask(plugin, task, speed);

        // System.out.println("New PC: " + programCounter.type);
    }

    public static Block createStartBlock(Block b, String text) {
        Block returnBlock;

        b.setType(Material.DIAMOND_BLOCK);
        returnBlock = b;

        b = b.getRelative(BlockFace.NORTH);
        b.setType(Material.LEVER);

        Lever l = (Lever) (b.getState().getData());
        l.setPowered(false);
        b.setData((byte) 10);

        b = returnBlock.getRelative(BlockFace.UP);
        b.setType(Material.SIGN_POST);
        Sign sign = (Sign) b.getState();
        sign.setLine(0, text);

        return returnBlock;
    }

    public static String formatFileName(String potentialFile, Player player) {
        if (potentialFile.indexOf("lesson") < 0)
            potentialFile = player.getName() + "_" + potentialFile;

        return potentialFile;
    }

    private class RobotTask implements Runnable {
        @Override
        public void run() {
            if (challenge != null) {
                if (!challenge.isComplete(robot)) {
                    evaluateBlock(programCounter);
                } else {
                    lastPlayer.sendMessage("[CodeBlocks] Challenge Completed");
                    plugin.getServer().getPluginManager()
                            .callEvent(new ChallengeCompleteEvent(challenge));
                }
            } else
                evaluateBlock(programCounter);
        }
    }

    private class CodeBlock {
        BlockType type;
        CodeBlock next;

        public CodeBlock(BlockType type, CodeBlock next) {
            this.type = type;
            this.next = next;
        }

        @Override
        public String toString() {
            return "(" + type + " " + next + ")";
        }
    }

    private class FunctionBlock extends CodeBlock {
        String functionToCall;
        String functionDefName;

        public FunctionBlock(BlockType type, String ftc, String fdn) {
            super(type, null);
            functionToCall = ftc;
            functionDefName = fdn;
        }
    }

    private class BranchingBlock extends CodeBlock {
        CodeBlock ifBlock;
        CodeBlock elseBlock;
        String param;

        public BranchingBlock(BlockType type, String param) {
            super(type, null);
            this.param = param;
        }

        @Override
        public String toString() {
            return type + " " + param + " " + ifBlock + " " + elseBlock;
        }
    }
}
