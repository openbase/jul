package org.openbase.jul.communication.controller.jp;

import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.type.communication.ScopeType.Scope;

import java.util.List;

public class JPScope extends AbstractJavaProperty<Scope> {

    public final static String[] COMMAND_IDENTIFIERS = {"-s", "--scope"};

    public JPScope() {
        super(COMMAND_IDENTIFIERS);
    }

    public JPScope(String[] commandIdentifiers) {
        super(commandIdentifiers);
    }

    @Override
    protected Scope getPropertyDefaultValue() throws JPNotAvailableException {
        try {
            if (JPService.testMode()) {
                String user = ScopeProcessor.convertIntoValidScopeComponent(System.getProperty("user.name"));
                return ScopeProcessor.generateScope("/test/" + user);
            }
            return ScopeProcessor.generateScope("/");
        } catch (CouldNotPerformException ex) {
            throw new JPNotAvailableException(JPScope.class, ex);
        }
    }

    @Override
    protected Scope parse(List<String> list) throws Exception {
        return ScopeProcessor.generateScope(getOneArgumentResult());
    }

    @Override
    public String getDescription() {
        return "Setup the application scope which is used for the rsb communication.";
    }

    @Override
    protected String[] generateArgumentIdentifiers() {
        String[] args = {"SCOPE"};
        return args;
    }

    public static String convertIntoValidScopeComponent(String scopeComponent) {
        return StringProcessor.transformToIdString(scopeComponent.toLowerCase()).replaceAll("_", "");
    }
}
