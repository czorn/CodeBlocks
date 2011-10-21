package com.zornchris.codeblocks.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class ChallengeEvent extends Event implements Cancellable {
	protected static final long serialVersionUID = 1L;
	protected boolean cancelled;
	
	public ChallengeEvent(String name) {
		super(name);
	}

	@Override
	public void setCancelled(boolean bln) { cancelled = bln; }
	@Override
	public boolean isCancelled() { return cancelled; }
	
	//public String getText() { return text; }
    //public void setText(String text) { this.text = text; }
}
