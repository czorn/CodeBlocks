package com.zornchris.codeblocks.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.zornchris.codeblocks.robot.Program;

public class RobotEvent extends Event implements Cancellable {
	protected static final long serialVersionUID = 1L;
	protected boolean cancelled;
	protected Program program;
	
	public RobotEvent(String name, Program program) {
		super(name);
		this.program = program;
	}

	@Override
	public void setCancelled(boolean bln) { cancelled = bln; }
	@Override
	public boolean isCancelled() { return cancelled; }
	
	public Program getProgram() { return program; }
	
	//public String getText() { return text; }
    //public void setText(String text) { this.text = text; }
}
