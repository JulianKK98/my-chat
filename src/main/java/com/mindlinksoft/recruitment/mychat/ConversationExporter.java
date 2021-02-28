package com.mindlinksoft.recruitment.mychat;

import com.google.gson.*;

import com.mindlinksoft.recruitment.mychat.exceptions.EmptyTextFileException;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation exporter that can read a conversation and write it out in JSON.
 */
public class ConversationExporter {

    private String filterUserId;
    private String filterKeyword;
    private List<String> blacklist;
    private boolean includeReport = false;



    private CommandLine cmd;

    /**
     * The application entry point.
     * @param args The command line arguments.
     * //@throws Exception Thrown when something bad happens.
     */

    public static void main(String[] args) {
        // We use picocli to parse the command line - see https://picocli.info/
        ConversationExporterConfiguration configuration = new ConversationExporterConfiguration();
        ConversationExporter exporter = new ConversationExporter();
        exporter.setCmd(new CommandLine(configuration));
        CommandLine cmd = exporter.getCmd();

        try {
            ParseResult parseResult = cmd.parseArgs(args);
            if (parseResult.isUsageHelpRequested()) {
                cmd.usage(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnUsageHelp());
                return;
            }

            if (parseResult.isVersionHelpRequested()) {
                cmd.printVersionHelp(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnVersionHelp());
                return;
            }

            if(parseResult.hasMatchedOption(configuration.userOpt)) {
                exporter.setFilterUserId(configuration.filterUserId);
            }
            else if(parseResult.hasMatchedOption(configuration.filterKeyword)) {
                exporter.setFilterKeyword(configuration.filterKeyword);
            }
            else if(parseResult.hasMatchedOption(configuration.blacklistOpt)) {
                exporter.setBlacklist(configuration.blacklist);
            }else if(parseResult.hasMatchedOption(configuration.reportOpt)){
                exporter.setIncludeReport(configuration.reportIncluded);
            }

            exporter.exportConversation(configuration.inputFilePath, configuration.outputFilePath);

            System.exit(cmd.getCommandSpec().exitCodeOnSuccess());
        } catch (ParameterException ex) {
            cmd.getErr().println(ex.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(ex, cmd.getErr())) {
                ex.getCommandLine().usage(cmd.getErr());
            }

            System.exit(cmd.getCommandSpec().exitCodeOnInvalidInput());
        } catch (Exception ex) {
            ex.printStackTrace(cmd.getErr());
            System.exit(cmd.getCommandSpec().exitCodeOnExecutionException());
        }
    }

    /**
     * Exports the conversation at {@code inputFilePath} as JSON to {@code outputFilePath}.
     * @param inputFilePath The input file path.
     * @param outputFilePath The output file path.
     * @throws Exception Thrown when something bad happens.
     */
    public void exportConversation(String inputFilePath, String outputFilePath) throws Exception {
        Conversation conversation = this.readConversation(inputFilePath);

        this.writeConversation(conversation, outputFilePath);

        if(getFilterUserId() !=null) {
            System.out.println("Showing messages with userId:" + filterUserId);
        }
        else if(getFilterKeyword() !=null) {
            System.out.println("Showing messages with keyword:" + filterKeyword);
        }else if(getBlacklist() != null) {
            String msg = "Hiding messages with following word(s):";
            StringBuilder sb = new StringBuilder(msg);
            int i = 0;
            for(String word: getBlacklist()) {
                if(i == 0) {
                    String str = " "+ word;
                    sb.append(str);
                }
                if(i > 0) {
                    String str = ", "+ word;
                    sb.append(str);
                }
                i++;
            }
            System.out.println(msg + sb.toString());
        }
        else if(includeReport){
            System.out.println("Including activity report to output");
        }
        System.out.println("Conversation exported from '" + inputFilePath + "' to '" + outputFilePath);
    }

    /**
     * Helper method to write the given {@code conversation} as JSON to the given {@code outputFilePath}.
     * @param conversation The conversation to write.
     * @param outputFilePath The file path where the conversation should be written.
     * @throws Exception Thrown when something bad happens.
     */
    public void writeConversation(Conversation conversation, String outputFilePath) throws Exception {
        String errorMsg =  "Incorrect file extension for output. file: \""+ outputFilePath
                + "\" should have extension: \".json\"";
        IOException extensionError = new IOException(errorMsg);

        try (OutputStream os = new FileOutputStream(outputFilePath, false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            if(!outputFilePath.contains(".json")){
                throw extensionError;
            }
            Conversation c = configureConversation(conversation);
            String ob = buildJson(c);
            bw.write(ob);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("The file was not found.");
        } catch (IOException e) {
            if(e.equals(extensionError)) {
                throw new ParameterException(getCmd(), e.getMessage());
            }
            else {
                throw new Exception("Something went wrong." + e.getMessage());
            }
        }
    }

    /**
     * Handles the creation of the JSON string used in writeConversation()
     * @param conversation The final conversation that will be written to the output
     * @return the resulting JSON string
     */
    private String buildJson(Conversation conversation) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantSerializer());
        gsonBuilder.setPrettyPrinting();
        Gson g = gsonBuilder.create();
        JsonElement json = g.toJsonTree(conversation);
        if(includeReport) {
            Activity activity = new Activity();
            activity.extractStats(conversation);
            json.getAsJsonObject().add(activity.name, g.toJsonTree(activity.reports));
        }
        return g.toJson(json);
    }

    /**
     * configures the conversation according to the command-line arguments
     * If there are no optional arguments used, the original conversation will be passed
     * @param c The original un-edited conversation
     * @return The edited conversation (if optional arguments used)
     */
    private Conversation configureConversation(Conversation c) {
        ConversationBuilder cb = new ConversationBuilder(c);
        if(filterUserId !=null) {
            cb.filterByUser(filterUserId);
        }
        else if(filterKeyword !=null) {
            cb.filterByKeyword(filterKeyword);
        }
        else if(blacklist != null) {
            for(String word: blacklist) {
                cb.blacklistWord(word);
            }
        }
        return cb.build();
    }

    /**
     * Represents a helper to read a conversation from the given {@code inputFilePath}.
     * @param inputFilePath The path to the input file.
     * @return The {@link Conversation} representing by the input file.
     * @throws Exception Thrown when something bad happens.
     */
    public Conversation readConversation(String inputFilePath) throws Exception{
        IOException emptyError = new IOException(" "+inputFilePath + " was empty");
        try(InputStream is = new FileInputStream(inputFilePath);
            BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            List<Message> messages = new ArrayList<>();
            String conversationName;
            conversationName = r.readLine();
            if (conversationName == null) {
                throw emptyError;
            }

            String line;
            while ((line = r.readLine()) != null) {
                String[] split = line.split(" ", 3);
                messages.add(new Message(Instant.ofEpochSecond(Long.parseUnsignedLong(split[0])), split[1], split[2]));
            }

            return new Conversation(conversationName, messages);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The file was not found.");
        } catch (IOException e) {
            if(e.equals(emptyError)) {
                throw new EmptyTextFileException(e.getMessage(), e);
            }
            else {
                throw new Exception("Something went wrong." + e.getMessage());
            }
        }
    }

    //Getters and setters for arguments
    public String getFilterUserId() {
        return this.filterUserId;
    }
    public void setFilterUserId(String filterUserId) {
        this.filterUserId = filterUserId;
    }
    public String getFilterKeyword() {
        return this.filterUserId;
    }
    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }
    public List<String> getBlacklist() {
        return this.blacklist;
    }
    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }
    public void setIncludeReport(boolean includeReport) {
        this.includeReport = includeReport;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public void setCmd(CommandLine cmd) {
        this.cmd = cmd;
    }

    static class InstantSerializer implements JsonSerializer<Instant> {
        @Override
        public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(instant.getEpochSecond());
        }
    }
}
