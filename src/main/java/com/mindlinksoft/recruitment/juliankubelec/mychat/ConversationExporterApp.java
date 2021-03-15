package com.mindlinksoft.recruitment.juliankubelec.mychat;

import picocli.CommandLine;

import java.io.IOException;


public class ConversationExporterApp {
    /**
     * The application entry point.
     * @param args The command line arguments.
     */

    public static void main(String[] args) {
        // We use picocli to parse the command line - see https://picocli.info/
        ConversationExporterConfiguration configuration = new ConversationExporterConfiguration();
        ConversationExporter exporter = new ConversationExporter();
        CommandLine cmd =new CommandLine(configuration);

        try {
            CommandLine.ParseResult parseResult = cmd.parseArgs(args);
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
            if(parseResult.hasMatchedOption(configuration.keywordOpt)) {
                exporter.setFilterKeyword(configuration.filterKeyword);
            }
            if(parseResult.hasMatchedOption(configuration.blacklistOpt)) {
                exporter.setBlacklist(configuration.blacklist);
            }
            if(parseResult.hasMatchedOption(configuration.reportOpt)){
                exporter.setIncludeReport(configuration.reportIncluded);
            }

            try{
                exporter.exportConversation(configuration.inputFilePath, configuration.outputFilePath);

            }catch (IOException e){
                if(e.equals(exporter.extensionError)){
                    throw new CommandLine.ParameterException(cmd, e.getMessage());
                }
            }

            System.exit(cmd.getCommandSpec().exitCodeOnSuccess());
        } catch (CommandLine.ParameterException ex) {
            cmd.getErr().println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, cmd.getErr())) {
                ex.getCommandLine().usage(cmd.getErr());
            }

            System.exit(cmd.getCommandSpec().exitCodeOnInvalidInput());
        } catch (Exception ex) {
            ex.printStackTrace(cmd.getErr());
            System.exit(cmd.getCommandSpec().exitCodeOnExecutionException());
        }
    }
}
