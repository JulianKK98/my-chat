package com.mindlinksoft.recruitment.mychat;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to manipulate the conversation according to command-line arguments
 */
public class ConversationBuilder {
    private Conversation conversation;
    private final String redacted = "*redacted*";
    /**
     * Initialises instance of a ConversationConfigurator
     * @param conversation The conversation that will be exported
     */
    public ConversationBuilder(Conversation conversation){
        this.conversation = conversation;
    }

    /**
     * @return the configurator's conversation
     */
    public Conversation build(){
        return this.conversation;
    }

    /**
     * This function replaces the current conversation with a filtered conversation
     * that only contains messages from senderId = userId
     * @param userId used as a filter
     */
    public ConversationBuilder filterByUser(String userId)
    {
        List<Message> messages = (List<Message>)conversation.messages;
        List<Message> newMessages = new ArrayList<>();
        for(Message msg: messages)
        {
            if(msg.senderId.equals(userId)){
                newMessages.add(msg);
            }
        }
        conversation.messages = newMessages;
        //TODO: Complete implementation of filterByUser
        return this;
    }

    /**
     * This function removes messages that don't contain keyword
     * @param keyword used as a filter
     */
    public ConversationBuilder filterByKeyword(String keyword)
    {
        //TODO: Complete implementation of filterByKeyword
        return this;
    }

    /**
     * This function replaces any blacklisted word with "*redacted*"
     * @param word The blacklisted word to replace
     */
    public ConversationBuilder blacklistWord(String word)
    {
        return this;
        //TODO: Complete implementation of blacklistWord
    }
}
