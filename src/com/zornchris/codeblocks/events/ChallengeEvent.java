package com.zornchris.codeblocks.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.zornchris.codeblocks.challenges.Challenge;

public class ChallengeEvent extends Event implements Cancellable {
	protected static final long serialVersionUID = 1L;
	protected boolean cancelled;
	protected Challenge challenge;
		
	public ChallengeEvent(String name, Challenge challenge) {
		super(name);
	    this.challenge = challenge;
	}

	@Override
	public void setCancelled(boolean bln) { cancelled = bln; }
	@Override
	public boolean isCancelled() { return cancelled; }
	
	//public String getText() { return text; }
    //public void setText(String text) { this.text = text; }
}
