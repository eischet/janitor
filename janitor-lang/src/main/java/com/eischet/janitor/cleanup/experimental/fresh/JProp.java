package com.eischet.janitor.cleanup.experimental.fresh;

public interface JProp {
    JClass getPropertyClass();
    JObject getValue(JObject instance);
    JAssignmentResult assignProperty(JObject instance);
    JAssignmentResult assignable(JClass jClass);
}
