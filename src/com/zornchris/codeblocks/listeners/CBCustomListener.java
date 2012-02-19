package com.zornchris.codeblocks.listeners;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.zornchris.codeblocks.CodeBlocksPlugin;
import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.events.ChallengeEvent;
import com.zornchris.codeblocks.events.RobotEvent;
import com.zornchris.codeblocks.program.Program;

public class CBCustomListener extends CustomEventListener{
    private CodeBlocksPlugin plugin;
    
    public CBCustomListener(CodeBlocksPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onCustomEvent(Event event) {
        String playerName;
        String eventType;
        String challengeDescr;
        String progDescr;
        Challenge c;
        Program prog;
        
        if(event instanceof RobotEvent) {
            RobotEvent re = (RobotEvent) event;
            prog = re.getProgram();
            playerName = prog.getPlayer().getName();
            c = prog.challenge;
            eventType = re.getEventName();
        }
        else if(event instanceof ChallengeEvent) {
            ChallengeEvent ce = (ChallengeEvent) event;
            c = ce.getChallenge();
            playerName = c.getPlayer().getName();
            eventType = ce.getEventName();
            prog = c.getProgram();
        }
        else
            return;            
            
        if(c == null)
            challengeDescr = "No Challenge";
        else
            challengeDescr = c.getDescription();
        
        if(prog == null)
            progDescr = "";
        else
            progDescr = prog.getDescription();
        
        plugin.dbh.logEvent(playerName, eventType, challengeDescr, progDescr);
    }
}
