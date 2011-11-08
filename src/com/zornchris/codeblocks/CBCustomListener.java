package com.zornchris.codeblocks;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.zornchris.codeblocks.challenges.Challenge;
import com.zornchris.codeblocks.events.ChallengeEvent;
import com.zornchris.codeblocks.events.RobotEvent;
import com.zornchris.codeblocks.robot.Program;

public class CBCustomListener extends CustomEventListener{
    private CodeBlocksPlugin plugin;
    
    public CBCustomListener(CodeBlocksPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onCustomEvent(Event event) {
        String playerName;
        String eventType;
        
        if(event instanceof RobotEvent) {
            RobotEvent re = (RobotEvent) event;
            Program prog = re.getProgram();
            playerName = prog.getPlayer().getName();
            eventType = re.getEventName();
        }
        else if(event instanceof ChallengeEvent) {
            ChallengeEvent ce = (ChallengeEvent) event;
            Challenge c = ce.getChallenge();
            playerName = c.getPlayer().getName();
            eventType = ce.getEventName();
        }
        else
            return;            
            
        plugin.dbh.logEvent(playerName, eventType);
    }
}
