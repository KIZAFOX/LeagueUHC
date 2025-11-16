package fr.kiza.leagueuhc.core.api.gui.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GuiInfo {
    String title() default "Menu";
    int size() default 9;
}
