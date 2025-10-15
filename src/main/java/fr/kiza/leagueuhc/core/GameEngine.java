package fr.kiza.leagueuhc.core;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.observer.IGameObserver;
import fr.kiza.leagueuhc.core.state.GameState;
import fr.kiza.leagueuhc.core.state.StateManager;
import fr.kiza.leagueuhc.core.state.states.*;
import fr.kiza.leagueuhc.core.state.transition.StateTransition;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameEngine extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final StateManager stateManager;
    private final GameContext context;

    private long lastUpdate;
    private boolean isRunning;

    public GameEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stateManager = new StateManager();
        this.context = new GameContext();
        this.lastUpdate = System.currentTimeMillis();
        this.isRunning = false;

        this.initializeStates();
        this.setupTransitions();

        plugin.getServer().getPluginManager().registerEvents(new GameListener(LeagueUHC.getInstance()), plugin);
    }

    @Override
    public void run() {
        if (!this.isRunning) return;

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - this.lastUpdate;
        lastUpdate = currentTime;

        this.stateManager.update(context, deltaTime);
    }

    public void start() {
        if (this.isRunning) return;

        this.isRunning = true;
        this.stateManager.changeState(GameState.IDLE.getName(), context);
        this.runTaskTimer(this.plugin, 0L, 1L);
    }

    public void stop() {
        this.isRunning = false;
        this.cancel();
    }

    public void handleInput(final GameInput input) {
        this.stateManager.handleInput(context, input);
    }

    public void addObserver(final IGameObserver observer) {
        this.stateManager.addObserver(observer);
    }

    private void initializeStates() {
        this.stateManager.registerState(new IdleState());
        this.stateManager.registerState(new WaitingState());
        this.stateManager.registerState(new StartingState());
        this.stateManager.registerState(new PlayingState());
        this.stateManager.registerState(new EndingState());
        this.stateManager.registerState(new FinishedState());
    }

    private void setupTransitions() {
        // IDLE -> WAITING (quand le host lance la partie)
        stateManager.registerTransition(new StateTransition(
                GameState.IDLE.getName(),
                GameState.WAITING.getName(),
                ctx -> ctx.<Boolean>getData("hostStarted", false)
        ));

        // WAITING -> STARTING (dès qu'il y a au moins 1 joueur - pas de minimum requis)
        stateManager.registerTransition(new StateTransition(
                GameState.WAITING.getName(),
                GameState.STARTING.getName(),
                ctx -> ctx.getPlayerCount() >= 1
        ));

        // WAITING -> IDLE (si plus aucun joueur)
        stateManager.registerTransition(new StateTransition(
                GameState.WAITING.getName(),
                GameState.IDLE.getName(),
                ctx -> ctx.getPlayerCount() < 1
        ));

        // STARTING -> WAITING (si plus aucun joueur)
        stateManager.registerTransition(new StateTransition(
                GameState.STARTING.getName(),
                GameState.WAITING.getName(),
                ctx -> ctx.getPlayerCount() < 1
        ));

        // STARTING -> PLAYING (countdown terminé)
        stateManager.registerTransition(new StateTransition(
                GameState.STARTING.getName(),
                GameState.PLAYING.getName(),
                ctx -> ctx.getCountdown() <= 0
        ));

        // PLAYING -> ENDING (un seul joueur vivant ou moins, ou partie terminée manuellement)
        stateManager.registerTransition(new StateTransition(
                GameState.PLAYING.getName(),
                GameState.ENDING.getName(),
                ctx -> ctx.getAlivePlayers().size() <= 1 ||
                        ctx.<Boolean>getData("gameEnded", false)
        ));

        // ENDING -> FINISHED (après 5 secondes)
        stateManager.registerTransition(new StateTransition(
                GameState.ENDING.getName(),
                GameState.FINISHED.getName(),
                ctx -> ctx.<Long>getData("endingStartTime", 0L) + 5000 < System.currentTimeMillis()
        ));

        // FINISHED → IDLE automatique après 10 secondes
        stateManager.registerTransition(new StateTransition(
                GameState.FINISHED.getName(),
                GameState.IDLE.getName(),
                ctx -> {
                    Long finishedTime = ctx.getData("finishedTime", 0L);
                    return finishedTime > 0 && System.currentTimeMillis() - finishedTime >= 10000;
                }
        ));
    }

    public GameContext getContext() {
        return context;
    }

    public String getCurrentState() {
        return stateManager.getCurrentStateName();
    }
}