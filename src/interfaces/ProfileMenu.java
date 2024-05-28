package interfaces;

import static misc.Constants.IO;
import misc.Enums.UserMenu;

import java.sql.SQLException;

public interface ProfileMenu<T extends Formattable> {
    default void profileMenu(EditMenuCallback editMenuCallback) throws SQLException {
        String[] options = {
            "Show profile\n", 
            "Edit profile\n", 
            "Go back\n"
        };
        UserMenu option = UserMenu.values()[IO.getValidInput(options)];

        if (option == UserMenu.GO_BACK) {
            return;
        }

        if (option == UserMenu.SHOW) {
            IO.printLogo();

            @SuppressWarnings("unchecked")
            T item = ((Crud<T>)this).read();
            System.out.println(item.format());

            IO.pressAnyKeyToContinue();
            profileMenu(editMenuCallback);
        }
        else if (option == UserMenu.EDIT) {
            editMenuCallback.execute();
        }
    }

    @FunctionalInterface
    public interface EditMenuCallback {
        void execute() throws SQLException;
    }
}
