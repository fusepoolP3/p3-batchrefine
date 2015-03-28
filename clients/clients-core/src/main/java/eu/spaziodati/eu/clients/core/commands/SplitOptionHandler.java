package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.split.SplitterEngine.Split;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.kohsuke.args4j.CmdLineException;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by andrey on 26/03/15.
 */
public class SplitOptionHandler extends OneArgumentOptionHandler<ImmutablePair> {

    public SplitOptionHandler(CmdLineParser parser, OptionDef option, Setter<ImmutablePair> setter) {
        super(parser, option, setter);
    }

    @Override
    public ImmutablePair parse(String argument) throws CmdLineException, NumberFormatException {

        String[] typeParams = argument.split(":");

        if (typeParams.length != 2)
            throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, option.toString(), argument);

        Split type = null;
        for (Split o : Split.class.getEnumConstants())
            if (o.name().equalsIgnoreCase(typeParams[0])) {
                type = o;
                break;
            }

        List<Integer> parameters = new ArrayList<>();
        try {
            for (String p : typeParams[1].split(",")) {
                parameters.add(Integer.parseInt(p));
            }
        } catch (NumberFormatException e) {
            throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, option.toString(), argument);
        }

        if (type == null || parameters.size() == 0) {
            throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, option.toString(), argument);
        }

        return new ImmutablePair(type, typeParams[1]);

    }


    @Override
    public String getDefaultMetaVariable() {
        StringBuffer rv = new StringBuffer();
        rv.append("[");
        for (Split t : Split.class.getEnumConstants()) {
            rv.append(t).append(":int")
                    .append(" | ");
        }
        rv.delete(rv.length() - 3, rv.length());
        rv.append("]");
        return rv.toString();
    }


    @Override
    public String getMetaVariable(ResourceBundle rb) {
        return getDefaultMetaVariable();
    }

}