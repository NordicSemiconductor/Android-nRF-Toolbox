package no.nordicsemi.android.toolbox.profile.repository

import no.nordicsemi.android.toolbox.profile.data.uart.MacroIcon
import org.simpleframework.xml.strategy.Type
import org.simpleframework.xml.strategy.Visitor
import org.simpleframework.xml.stream.InputNode
import org.simpleframework.xml.stream.NodeMap
import org.simpleframework.xml.stream.OutputNode

/**
 * The comment visitor will add comments to the XML during saving.
 */
internal class CommentVisitor : Visitor {
    override fun read(type: Type, node: NodeMap<InputNode>) {
        // do nothing
    }

    override fun write(type: Type, node: NodeMap<OutputNode>) {
        if (type.type == Array<XmlMacro>::class.java) {
            val element = node.node
            val builder =
                StringBuilder("A configuration must have 9 commands, one for each button.\n        Possible icons are:")
            for (icon in MacroIcon.entries) builder.append("\n          - ")
                .append(icon.toString())
            element.comment = builder.toString()
        }
    }
}