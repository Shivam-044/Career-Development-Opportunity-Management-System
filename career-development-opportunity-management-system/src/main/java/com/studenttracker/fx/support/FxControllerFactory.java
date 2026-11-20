package com.studenttracker.fx.support;

import java.lang.reflect.Constructor;
import javafx.util.Callback;

/** Creates FXML controllers, handing each one the shared {@link FxContext} when it asks for it. */
public final class FxControllerFactory implements Callback<Class<?>, Object> {

    private final FxContext context;

    public FxControllerFactory(FxContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> type) {
        try {
            for (Constructor<?> constructor : type.getConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if (parameters.length == 1 && parameters[0] == FxContext.class) {
                    return constructor.newInstance(context);
                }
            }
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Couldn't create controller " + type.getName(), ex);
        }
    }
}
