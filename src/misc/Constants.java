package misc;

import repository.GameMasterRepository;
import services.AuditService;
import services.FormatterService;
import services.IOService;

import java.text.SimpleDateFormat;

public final class Constants {
    // Field validation constants
    public final static int MIN_PASSWORD_LENGTH = 4;
    public final static String EMAIL_REGEX = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
    public final static String PHONE_REGEX = "^[0-9]{10}$";
    public final static String DATE_REGEX = "^\\d{2}\\.\\d{2}\\.\\d{4}$";
    public final static String WEBSITE_REGEX = "^(http|https)://.*$";


    // Database
    public static final String DATABASE_URL = "jdbc:mysql://localhost:3306/gamemaster";
    public static final String DATABASE_USER = "root";
    public static final String DATABASE_PASSWORD = "root";


    // Logger
    public static final String LOG_FOLDER = "log";
    public static final String ACTIONS_FILE = "actions.csv";
    public static final String LIBRARIES_FILE = "libraries.csv";


    // Colors
    public static final String RED = "\u001B[31m";
    public static final String BLUE = "\u001B[94m";
    public static final String END_COLOR = "\u001B[0m";


    // Date and time formats
    public static final String DATE_FORMAT = "%02d/%02d/%04d"; 
    public static final String TIME_FORMAT = "%02d:%02d:%02d";


    // General utils 
    public static final int PAUSE_DURATION = 1250;
    public static final AuditService AUDIT = AuditService.getInstance();
    public static final IOService IO = IOService.getInstance();
    public static final FormatterService FORMAT = FormatterService.getInstance();
    public static final GameMasterRepository DB = GameMasterRepository.getInstance();
    public static final SimpleDateFormat DATE = new SimpleDateFormat("dd.MM.yyyy");
}
