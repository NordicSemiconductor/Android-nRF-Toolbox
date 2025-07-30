package no.nordicsemi.android.toolbox.profile.repository.uartXml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol;
import no.nordicsemi.android.toolbox.profile.data.uart.MacroIcon;

@Root
public class XmlMacro {

    @Text(required = false)
    private String command;

    @Attribute(required = false)
    private boolean active = false;

    @Attribute(required = false)
    private MacroEol eol = MacroEol.LF;

    @Attribute(required = false)
    private MacroIcon icon = MacroIcon.LEFT;

    /**
     * Sets the command.
     * @param command the command that will be sent to UART device
     */
    public void setCommand(final String command) {
        this.command = command;
    }

    /**
     * Sets whether the command is active.
     * @param active true to make it active
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Sets the new line type.
     * @param eol end of line terminator
     */
    public void setEol(final int eol) {
        this.eol = MacroEol.getEntries().get(eol);
    }

    /**
     * Sets the icon index.
     * @param index index of the icon.
     */
    public void setIconIndex(final int index) {
        this.icon = MacroIcon.getEntries().get(index);
    }

    /**
     * Returns the command that will be sent to UART device.
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns whether the icon is active.
     * @return true if it's active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the new line type.
     * @return end of line terminator
     */
    public MacroEol getEol() {
        return eol;
    }

    /**
     * Returns the icon index.
     * @return the icon index
     */
    public int getIconIndex() {
        return icon.getIndex();
    }
    /**
     * Returns the EOL index.
     * @return the EOL index
     */
    public int getEolIndex() {
        return eol.ordinal();
    }
}
