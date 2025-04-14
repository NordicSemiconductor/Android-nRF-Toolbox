package no.nordicsemi.android.toolbox.profile.repository;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Validate;

@Root
public class XmlConfiguration {
    public static final int COMMANDS_COUNT = 8;// TODO: use constant from Macro class

    @Attribute(required = false, empty = "Unnamed")
    private String name;

    @ElementArray
    private XmlMacro[] commands = new XmlMacro[COMMANDS_COUNT];

    /**
     * Returns the field name
     *
     * @return optional name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name to specified value
     * @param name the new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the array of commands. There is always 9 of them.
     * @return the commands array
     */
    public XmlMacro[] getCommands() {
        return commands;
    }

    public void setCommands(XmlMacro[] commands) {
        this.commands = commands;
    }

    @Validate
    private void validate() throws PersistenceException {
        if (commands == null || commands.length != COMMANDS_COUNT)
            throw new PersistenceException("There must be always " + COMMANDS_COUNT + " commands in a configuration.");
    }
}
