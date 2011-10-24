package com.zornchris.codeblocks.events;

import com.zornchris.codeblocks.challenges.Challenge;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class ChallengeLoadEvent extends ChallengeEvent  {
	private static final long serialVersionUID = 1L;
	
    public ChallengeLoadEvent(Challenge challenge) {
    	super("ChallengeLoadEvent", challenge);
    }
}
