package misc;

public final class Enums {
    public enum MainMenu {
        QUIT,
        LOGIN_ADMIN,
        LOGIN_PROVIDER,
        LOGIN_CLIENT,
        REGISTER_CLIENT,
        SHOW_AUDIT,
        RESET_AUDIT;
    }

    public enum AuditMenu {
        GO_BACK,
        ACTIONS,
        LIBRARIES;
    }

    public enum UserMenu {
        GO_BACK,
        SHOW,
        EDIT;
    }

    public enum AdminMenu {
        LOG_OUT,
        MANAGE_ACCOUNTS,
        PROFILE;
    }

    public enum AdminAccounts {
        GO_BACK,
        SHOW_ALL,
        SHOW,
        ADD,
        BAN,
        UNBAN,
        REMOVE,
        CHANGE_FILTER;
    }

    public enum AdminEdit {
        GO_BACK,
        EDIT_USERNAME,
        EDIT_PASSWORD,
        EDIT_EMAIL,
        EDIT_CUT;
    }

    public enum ClientMenu {
        LOG_OUT,
        LIBRARY,
        WISHLIST,
        PROFILE;
    }

    public enum ClientLibrary {
        GO_BACK,
        SHOW_ALL,
        SHOW,
        BUY,
        REMOVE,
        CHANGE_FILTER,
        CHANGE_SORT;
    }

    public enum ClientWishlist {
        GO_BACK,
        SHOW_ALL,
        SHOW,
        BUY,
        ADD,
        REMOVE,
        CHANGE_FILTER,
        CHANGE_SORT;
    }

    public enum ClientEdit {
        GO_BACK,
        EDIT_USERNAME,
        EDIT_PASSWORD,
        EDIT_EMAIL,
        EDIT_FIRST_NAME,
        EDIT_LAST_NAME,
        EDIT_PHONE_NUMBER;
    }

    public enum ProviderMenu {
        LOG_OUT,
        MANAGE_GAMES,
        CONTRACTS,
        PROFILE,
    }

    public enum GamesEdit {
        GO_BACK,
        EDIT_NAME,
        EDIT_PRICE;
    }

    public enum PublisherGames {
        GO_BACK,
        SHOW_ALL,
        SHOW_PUBLISHED,
        SHOW_UNPUBLISHED,
        PUBLISH,
        EDIT,
        DELIST,
        RELIST,
        REMOVE,
        CHANGE_FILTER,
        CHANGE_SORT;
    }

    public enum DeveloperGames {
        GO_BACK,
        SHOW_ALL,
        SHOW_PUBLISHED,
        SHOW_UNPUBLISHED,
        DEVELOP,
        PUBLISH,
        EDIT,
        DELIST,
        RELIST,
        REMOVE,
        CHANGE_FILTER,
        CHANGE_SORT;
    }

    public enum PublisherContracts {
        GO_BACK,
        SHOW_ALL,
        ISSUE,
        EDIT,
        CANCEL,
        NULLIFY;
    }

    public enum DeveloperContracts {
        GO_BACK,
        SHOW_ALL,
        ACCEPT,
        REJECT;
    }

    public enum ProviderEdit {
        GO_BACK,
        EDIT_USERNAME,
        EDIT_PASSWORD,
        EDIT_EMAIL,
        EDIT_WEBSITE;
    }
}
