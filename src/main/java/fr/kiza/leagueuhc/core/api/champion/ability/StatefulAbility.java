package fr.kiza.leagueuhc.core.api.champion.ability;

import fr.kiza.leagueuhc.core.game.GamePlayer;

import java.util.*;

/**
 * Ability avec état persistant.
 * Utilisé pour les mécaniques qui doivent garder une trace d'objets/positions
 * (pièges, zones, marques sur les joueurs, etc.)
 *
 * @param <T> Le type d'état à stocker (Location, UUID, custom object, etc.)
 */
public abstract class StatefulAbility<T> extends Ability {

    private final Map<UUID, Set<T>> states = new HashMap<>();

    /**
     * Récupère tous les états pour un joueur.
     * Crée un Set vide si aucun état n'existe.
     */
    protected Set<T> getStates(UUID playerId) {
        return states.computeIfAbsent(playerId, k -> new HashSet<>());
    }

    /**
     * Récupère les états en lecture seule.
     */
    protected Set<T> getStatesReadOnly(UUID playerId) {
        Set<T> set = states.get(playerId);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    /**
     * Ajoute un état pour un joueur.
     */
    protected void addState(UUID playerId, T state) {
        getStates(playerId).add(state);
    }

    /**
     * Retire un état spécifique pour un joueur.
     */
    protected boolean removeState(UUID playerId, T state) {
        Set<T> set = states.get(playerId);
        return set != null && set.remove(state);
    }

    /**
     * Vérifie si un état existe pour un joueur.
     */
    protected boolean hasState(UUID playerId, T state) {
        Set<T> set = states.get(playerId);
        return set != null && set.contains(state);
    }

    /**
     * Compte le nombre d'états pour un joueur.
     */
    protected int countStates(UUID playerId) {
        Set<T> set = states.get(playerId);
        return set == null ? 0 : set.size();
    }

    /**
     * Efface tous les états pour un joueur.
     */
    protected void clearStates(UUID playerId) {
        states.remove(playerId);
    }

    /**
     * Efface tous les états de tous les joueurs.
     */
    protected void clearAllStates() {
        states.clear();
    }

    /**
     * Récupère tous les joueurs qui ont au moins un état.
     */
    protected Set<UUID> getPlayersWithStates() {
        return Collections.unmodifiableSet(states.keySet());
    }

    /**
     * Exécute une action sur tous les états d'un joueur.
     * Permet de modifier/supprimer pendant l'itération de manière safe.
     */
    protected void forEachState(UUID playerId, StateConsumer<T> consumer) {
        Set<T> set = states.get(playerId);
        if (set == null || set.isEmpty()) return;

        Iterator<T> iterator = set.iterator();
        while (iterator.hasNext()) {
            T state = iterator.next();
            StateAction action = consumer.accept(state);
            if (action == StateAction.REMOVE) {
                iterator.remove();
            } else if (action == StateAction.STOP) {
                break;
            }
        }
    }

    /**
     * Exécute une action sur tous les états de tous les joueurs.
     */
    protected void forEachStateGlobal(GlobalStateConsumer<T> consumer) {
        for (Map.Entry<UUID, Set<T>> entry : states.entrySet()) {
            UUID playerId = entry.getKey();
            Iterator<T> iterator = entry.getValue().iterator();

            while (iterator.hasNext()) {
                T state = iterator.next();
                StateAction action = consumer.accept(playerId, state);
                if (action == StateAction.REMOVE) {
                    iterator.remove();
                } else if (action == StateAction.STOP) {
                    return;
                }
            }
        }
    }

    /**
     * Appelé quand l'ability est désactivée.
     * Par défaut, nettoie tous les états du joueur.
     */
    @Override
    public void onDisable(GamePlayer owner) {
        clearStates(owner.getUUID());
    }

    /**
     * Actions possibles lors de l'itération sur les états.
     */
    public enum StateAction {
        /** Continue l'itération normalement */
        CONTINUE,
        /** Supprime l'état actuel et continue */
        REMOVE,
        /** Arrête l'itération */
        STOP
    }

    /**
     * Interface fonctionnelle pour traiter les états d'un joueur.
     */
    @FunctionalInterface
    public interface StateConsumer<T> {
        StateAction accept(T state);
    }

    /**
     * Interface fonctionnelle pour traiter les états globalement.
     */
    @FunctionalInterface
    public interface GlobalStateConsumer<T> {
        StateAction accept(UUID playerId, T state);
    }
}