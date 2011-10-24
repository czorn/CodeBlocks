package com.zornchris.codeblocks.events;

import com.zornchris.codeblocks.robot.Program;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class RobotStartEvent extends RobotEvent  {
	private static final long serialVersionUID = 1L;
	
    public RobotStartEvent(Program program) {
    	super("RobotStartEvent", program);
    }
}
