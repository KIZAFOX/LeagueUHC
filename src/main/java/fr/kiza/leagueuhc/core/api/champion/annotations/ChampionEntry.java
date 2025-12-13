package fr.kiza.leagueuhc.core.api.champion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marquant une classe comme un champion à enregistrer automatiquement.
 * La classe doit hériter de Champion et avoir un constructeur sans arguments.
 *
 * Exemple d'utilisation:
 * <pre>
 * {@code
 * @ChampionEntry
 * public class Teemo extends Champion {
 *     public Teemo() {
 *         super("Teemo", "Scout agile", Category.ASSASSIN, Material.BROWN_MUSHROOM);
 *         registerAbility(new MushroomTrapAbility());
 *     }
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChampionEntry {

    /**
     * Si true, le champion est activé et sera chargé.
     * Mettre à false pour désactiver temporairement un champion sans supprimer le code.
     */
    boolean enabled() default true;
}