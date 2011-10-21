package com.zornchris.codeblocks.events;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class RobotExplodeEvent extends RobotEvent  {
	private static final long serialVersionUID = 1L;
	
    public RobotExplodeEvent() {
    	super("RobotExplodeEvent");
    }
    
    public RobotExplodeEvent(String name) {
        super(name);
    }
}
