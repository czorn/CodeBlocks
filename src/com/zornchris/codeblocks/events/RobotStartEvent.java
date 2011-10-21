package com.zornchris.codeblocks.events;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class RobotStartEvent extends RobotEvent  {
	private static final long serialVersionUID = 1L;
	
    public RobotStartEvent() {
    	super("RobotStartEvent");
    }
    
    public RobotStartEvent(String name) {
        super(name);
    }
}
