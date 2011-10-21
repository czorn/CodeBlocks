package com.zornchris.codeblocks.events;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class ChallengeLoadEvent extends ChallengeEvent  {
	private static final long serialVersionUID = 1L;
	
    public ChallengeLoadEvent() {
    	super("ChallengeLoadEvent");
    }
    
    public ChallengeLoadEvent(String name) {
        super(name);
    }
}
