package com.zornchris.codeblocks.events;

import com.zornchris.codeblocks.challenges.Challenge;

/*
 * http://forums.bukkit.org/threads/example-of-custom-events.32376/
 */

public class ChallengeCompleteEvent extends ChallengeEvent  {
	private static final long serialVersionUID = 1L;
	
    public ChallengeCompleteEvent(Challenge challenge) {
    	super("ChallengeCompleteEvent", challenge);
    }
}
