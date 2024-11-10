package Utilities.Scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Basically don't change the function signature, function name, parameters, parameter names,..., otherwise Lambda might break
 */
@Target({ElementType.TYPE_USE, ElementType.METHOD, ElementType.MODULE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalLambdaUsage
{}
