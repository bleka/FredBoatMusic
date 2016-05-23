package fredboat.commandmeta;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandRegistry {

    public static HashMap<String, CommandEntry> registry = new HashMap<>();

    public static void registerCommand(int scope, String name, Command command) {
        CommandEntry entry = new CommandEntry(scope, command, name);
        registry.put(name, entry);
    }
    
    public static void registerAlias(String command, String alias) {
        registry.put(alias, registry.get(command));
    }

    public static CommandEntry getCommandFromScope(int scope, String name) {
        CommandEntry entry = registry.get(name);
        if (entry != null && (entry.getScope() & scope) == scope) {
            return entry;
        }
        return null;
    }

    public static class CommandEntry {

        public int scope;
        public Command command;
        public String name;

        public CommandEntry(int scope, Command command, String name) {
            this.scope = scope;
            this.command = command;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getScope() {
            return scope;
        }

        public void setCommand(Command command) {
            this.command = command;
        }
    }
}
