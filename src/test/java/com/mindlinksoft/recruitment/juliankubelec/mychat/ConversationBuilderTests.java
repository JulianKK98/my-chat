package com.mindlinksoft.recruitment.juliankubelec.mychat;


import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConversationBuilderTests {

    /**
     * Tests if the conversation has been correctly filtered according to the specified userId
     */
    @Test
    public void testFilterByUser(){
        try
        {
            ConversationBuilder cb = prepareConversation();
            String user;

            user = "bob";

            Conversation c = cb
                    .filter()
                        .byUser(user)
                    .build();

            assertEquals(3, c.getMessages().size()); // should contain 3 messages

            user = "CompleteStranger";
            cb = prepareConversation();
            c = cb
                .filter()
                    .byUser(user)
                .build();
            assertEquals(0, c.getMessages().size()); // should contain 0 messages
        }catch(Exception e){
            fail("chat.txt not found");
        }

    }

    /**
     * Tests if the conversation has been correctly filtered according to the specified userId
     */
    @Test
    public void testFilterByKeyword(){
        try{
            ConversationBuilder cb = prepareConversation();
            String keyword;

            keyword = "pie";
            Conversation c = cb
                    .filter()
                        .byKeyword(keyword)
                    .build();

            assertEquals(4, c.getMessages().size()); // should contain 3 messages

            keyword = "pasty";
            cb = prepareConversation();
            c = cb
                .filter()
                    .byUser(keyword)
                .build();
            assertEquals(0, c.getMessages().size()); // should contain 0 messages
        }
        catch (Exception e)
        {
            fail("unable to retrieve chat.txt");
        }

    }

    /**
     * Test helper method to build regex that is used find blacklisted words
     */
    @Test
    public void testBlacklistWord()
    {
        try
        {
            String blacklistWord = "pie";
            String blacklistWord2 = "no";
            ConversationBuilder cb = prepareConversation();
            Conversation c = cb
                    .redact()
                        .byBlacklistedWord(blacklistWord)
                        .byBlacklistedWord(blacklistWord2)
                    .build();

            Message[] ms = new Message[c.getMessages().size()];
            c.getMessages().toArray(ms);

            assertEquals("Hello there!", ms[0].getContent());

            assertEquals("how are you?", ms[1].getContent());

            assertEquals("I'm good thanks, do you like *redacted*?", ms[2].getContent());

            assertEquals("*redacted*, let me ask Angus...", ms[3].getContent());

            assertEquals("Hell yes! Are we buying some *redacted*?", ms[4].getContent());

            assertEquals("*redacted*, just want to know if there's anybody else in the *redacted* society...", ms[5].getContent());

            assertEquals("YES! I'm the head *redacted* eater there...", ms[6].getContent());


        } catch (Exception e)
        {
            fail("unable to retrieve chat.txt");
        }
    }


    /**
     * Helper function to initialise a mock of the conversation from chat.txt
     * @return the original conversation
     */
    private ConversationBuilder prepareConversation()
    {
        String name = "My Conversation";
        ArrayList<Message> messages = new ArrayList<>();
                messages.add(new Message(Instant.ofEpochSecond(1448470901),
                        "bob", "Hello there!"));
                messages.add(new Message(Instant.ofEpochSecond(1448470905),
                        "mike", "how are you?"));
                messages.add(new Message(Instant.ofEpochSecond(1448470906),
                        "bob", "I 'm good thanks, do you like pie?"));
                messages.add(new Message(Instant.ofEpochSecond(1448470910),
                        "mike", "no, let me ask Angus..."));
                messages.add(new Message(Instant.ofEpochSecond(1448470912),
                        "angus", "Hell yes !Are we buying some pie?"));
                messages.add(new Message(Instant.ofEpochSecond(1448470914),
                        "bob", "No, just want to know if there 's anybody else in the pie society..."));
                messages.add(new Message(Instant.ofEpochSecond(1448470915),
                        "angus", "YES !I 'm the head pie eater there..."));

        Conversation conversation = new Conversation(name, messages);

        return new ConversationBuilder(conversation);
    }

}
