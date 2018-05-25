package org.openbase.jul.annotation;

import java.lang.annotation.*;

/**
 * This annotations is used to tag methods which are experimental.
 * This means that the method may be changed or removed in the future.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Experimental {
}
