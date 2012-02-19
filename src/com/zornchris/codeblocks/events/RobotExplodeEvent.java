package com.zornchris.codeblocks.events;

import com.zornchris.codeblocks.program.Program;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class RobotExplodeEvent extends RobotEvent  {
	private static final long serialVersionUID = 1L;
	
    public RobotExplodeEvent(Program program) {
    	super("RobotExplodeEvent", program);
    }
}
