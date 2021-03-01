package com.mindlinksoft.recruitment.mychat;


import java.util.*;

public final class Activity {
    public String name = "activity";
    public Collection<Report> reports;

    public void extractStats(Conversation conversation) {
        HashSet<Report> reportSet = new HashSet<>();
        List<Message> msgs = (List<Message>) conversation.messages;
        for (Message msg : msgs) {
            Conversation c = new Conversation(conversation.name, conversation.messages); // copy conversation
            ConversationBuilder cb = new ConversationBuilder(c);
            c = cb.filterByUser(msg.senderId).build();
            if (c.messages.size() > 0) {
                Report r = new Report(msg.senderId, c.messages.size());
                reportSet.add(r);
            }
        }
        reports = new ArrayList<>();
        reports.addAll(reportSet);

        ((List<Report>) reports).sort(new SortReport());
        Collections.reverse((List<Report>) reports);
    }

    /**
     * Inner class to provide way to sort the Activity
     *
     */
    static class SortReport implements Comparator<Report>{
        /**
         *
         * @param o1 first report to compare
         * @param o2 second report to compare
         * @return if result is positive, o1 has more messages.
         * If result is 0 they have the same amount. If it's negative
         * o2 is larger
         */
        @Override
        public int compare(Report o1, Report o2) {
            return o1.count - o2.count;
        }
    }

}