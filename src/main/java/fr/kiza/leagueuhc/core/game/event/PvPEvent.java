package fr.kiza.leagueuhc.core.game.event;

import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;

public class PvPEvent {
    private final boolean isEnabled;

    public PvPEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public static class PvPHandler {
        private static final PvPHandler INSTANCE = new PvPHandler();

        private boolean isEnabled;

        private PvPHandler() {
            GameEventBus.getInstance().subscribe(PvPEvent.class, event -> this.isEnabled = event.isEnabled);
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.isEnabled = enabled;
            GameEventBus.getInstance().publish(new PvPEvent(enabled));
        }

        public static PvPHandler getInstance() {
            return INSTANCE;
        }
    }
}
