package gg.projecteden.titan.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Group {
    String value();

    String SATURN = "Saturn";
    String UTILITIES = "Utilities";
    String BACKPACKS = "Backpacks";
    String DEVELOPER = "Developer";

}
