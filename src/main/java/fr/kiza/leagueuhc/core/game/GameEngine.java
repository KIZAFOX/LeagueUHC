package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.game.state.StateManager;
import fr.kiza.leagueuhc.core.game.state.states.*;
import fr.kiza.leagueuhc.core.game.state.transition.StateTransition;
import org.bukkit.scheduler.BukkitRunnable;

public class GameEngine extends BukkitRunnable {

    protected final LeagueUHC instance;

    private final GameHelper gameHelper;

    private final StateManager stateManager;
    private final GameContext context;

    private long lastUpdate;
    private boolean isRunning;

    public GameEngine(LeagueUHC instance) {
        this.gameHelper = new GameHelper();
        this.gameHelper.init(instance);

        this.instance = instance;
        this.stateManager = new StateManager();
        this.context = new GameContext();

        this.lastUpdate = System.currentTimeMillis();
        this.isRunning = false;

        this.initializeStates();
        this.setupTransitions();

        instance.getServer().getPluginManager().registerEvents(new GameListener(LeagueUHC.getInstance()), instance);
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
        this.runTaskTimer(this.instance, 0L, 1L);
    }

    public void stop() {
        if (gameHelper != null) gameHelper.onDisable();

        this.isRunning = false;
        this.cancel();
    }

    public void handleInput(final GameInput input) {
        this.stateManager.handleInput(context, input);
    }

    private void initializeStates() {
        this.stateManager.registerState(new IdleState());
        this.stateManager.registerState(new StartingState());
        this.stateManager.registerState(new PlayingState());
        this.stateManager.registerState(new EndingState());
        this.stateManager.registerState(new FinishedState());
    }

    private void setupTransitions() {
        // IDLE -> STARTING (quand le host lance la partie)
        stateManager.registerTransition(new StateTransition(
                GameState.IDLE.getName(),
                GameState.STARTING.getName(),
                ctx -> ctx.<Boolean>getData("hostStarted", false)
        ));

        // STARTING -> PLAYING (countdown terminé)
        stateManager.registerTransition(new StateTransition(
                GameState.STARTING.getName(),
                GameState.PLAYING.getName(),
                ctx -> ctx.getCountdown() <= 0
        ));

        // PLAYING -> ENDING (un seul joueur vivant ou moins, ou partie terminée manuellement)
//        stateManager.registerTransition(new StateTransition(
//                GameState.PLAYING.getName(),
//                GameState.ENDING.getName(),
//                ctx -> ctx.getAlivePlayers().size() <= 1 ||
//                        ctx.<Boolean>getData("gameEnded", false)
//        ));

        stateManager.registerTransition(new StateTransition(
                GameState.PLAYING.getName(),
                GameState.ENDING.getName(),
                ctx -> ctx.getData("gameEnded", false) // Seulement si fin manuelle
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

    public GameHelper getGameHelper() {
        return gameHelper;
    }
}