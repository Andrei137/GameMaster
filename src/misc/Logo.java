// Credit - https://patorjk.com/software/taag/#p=display&f=Small&t=GameMaster

package misc;

import static misc.Constants.BLUE;
import static misc.Constants.END_COLOR;

import org.fusesource.jansi.AnsiConsole;

public enum Logo {
    // Singleton instance
    INSTANCE;


    // Getter
    public static final String getLogo() {
        return BLUE + "   ___                __  __         _                " + END_COLOR + '\n' +
               BLUE + "  / __|__ _ _ __  ___|  \\/  |__ _ __| |_ ___ _ _     " + END_COLOR + '\n' +
               BLUE + " | (_ / _` | '  \\/ -_) |\\/| / _` (_-<  _/ -_) '_|   " + END_COLOR + '\n' +
               BLUE + "  \\___\\__,_|_|_|_\\___|_|  |_\\__,_/__/\\__\\___|_| " + END_COLOR;
    }

}
