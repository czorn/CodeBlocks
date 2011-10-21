package com.zornchris.codeblocks.events;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class ChallengeCompleteEvent extends ChallengeEvent  {
	private static final long serialVersionUID = 1L;
	
    public ChallengeCompleteEvent() {
    	super("ChallengeCompleteEvent");
    }
    
    public ChallengeCompleteEvent(String name) {
        super(name);
    }
}
