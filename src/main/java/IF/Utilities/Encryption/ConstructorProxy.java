package IF.Utilities.Encryption;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;

public class ConstructorProxy
{
    public void intercept(@This Object self, @Argument(0) Object arg) throws Throwable
    {
        var field = self.getClass().getField("WhyAreYouReadingThis");
    }
}
