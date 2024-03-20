package com.eischet.janitor.cleanup.experimental.fresh;

public class JPropReadonly<T extends JObject> implements JProp {

    private final JClass propertyClass;
    private final PropertyReader propertyReader;

    public JPropReadonly(final JClass propertyClass, final PropertyReader propertyReader) {
        this.propertyClass = propertyClass;
        this.propertyReader = propertyReader;
    }

    @Override
    public JClass getPropertyClass() {
        return propertyClass;
    }

    @Override
    public JObject getValue(final JObject instance) {
        return propertyReader.getValue(instance);
    }

    @Override
    public JAssignmentResult assignProperty(final JObject instance) {
        return JAssignmentResult.REJECTED;
    }

    @Override
    public JAssignmentResult assignable(final JClass jClass) {
        return JAssignmentResult.REJECTED;
    }

}
