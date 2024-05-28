// Credit - https://github.com/liuyubobobo/heart-curve-cplusplus
// Translated from C++ to Java by me
// The message and the color red are also added by me
// I don't know enough math to explain how this works :3

package misc;

import static misc.Constants.RED;
import static misc.Constants.END_COLOR;

import org.fusesource.jansi.AnsiConsole;

public enum Heart {
    // Singleton instance
    INSTANCE;


    // Other members
    private static final String[] MESSAGE = {"THANK", "  U FOR  ", "STAYING"};


    // Helpers
    // Checks if x is inside (value1, value2) or [value1, value2], based on the given sign
    private static final boolean fastCheck(double x, double value1, double value2, char sign) {
        if (sign == '|') {
            return x <= value1 || x >= value2;
        } 
        else {
            return x > value1 && x < value2;
        }
    }

    // Checks if a point (x, y) is inside the heart shape
    private static final boolean isInsideHeart(double x, double y) {
        return x * x + Math.pow(5.0 * y / 4.0 - Math.sqrt(Math.abs(x)), 2) - 1 <= 0.0;
    }

    // Prints a single heart character '*' or ' ' based on conditions
    private static final void printHeart(double x, double y, boolean condition) {
        if (isInsideHeart(x, y) && condition) {
            System.out.print(RED + "*" + END_COLOR);
        } 
        else {
            System.out.print(" ");
        }
    }

    // Prints a message inside the heart
    private static final boolean printMessage(double x, double y, boolean condition, int i, int j) {
        if (isInsideHeart(x, y)) {
            if (fastCheck(x, -0.33, 0.33, '|')) {
                System.out.print(RED + "*" + END_COLOR);
            } 
            else if (condition) {
                // Print blank space
                System.out.print(" ");
            } 
            else {
                // Print message
                System.out.print(MESSAGE[i].charAt(j));

                // Increment MESSAGE column index
                return true;
            }
        } 
        else {
            System.out.print(" ");
        }

        // Don't increment MESSAGE column index
        return false;
    }

    // Prints heart or message based on condition
    private static final void heartFor(double y, int condition) {
        double i = -1.1;
        while (i <= 1.1) {
            if (condition == 1) {
                printHeart(i, y, fastCheck(i, -0.33, 0.33, '|'));
            } 
            else {
                printHeart(i, y, true);
            }
            i += 0.025;
        }
    }

    // Prints message inside the heart based on condition
    private static final void messageFor(double y, int condition) {
        int[] k = {0, 0, 0};
        double i = -1.1;
        while (i < 1.1) {
            if (condition == 1) {
                if (printMessage(i, y, fastCheck(i, -0.06, 0.06, '|'), 0, k[0])) {
                    ++k[0];
                }
            }
            else if (condition == 2) {
                if (printMessage(i, y, fastCheck(i, -0.12, 0.12, '|'), 1, k[1])) {
                    ++k[1];
                }
            }
            else {
                if (printMessage(i, y, fastCheck(i, -0.08, 0.08, '|'), 2, k[2])) {
                    ++k[2];
                }
            }
            i += 0.025;
        }
    }

    // Prints the full heart shape
    public static final void printFullHeart() {
        double y = 1.3;
        while (y >= -0.7) {
            if (fastCheck(y, 0.5, 0.55, '&')) {
                messageFor(y, 1);
            } 
            else if (fastCheck(y, 0.39, 0.45, '&')) {
                messageFor(y, 2);
            } 
            else if (fastCheck(y, 0.25, 0.3, '&')) {
                messageFor(y, 3);
            } 
            else if (fastCheck(y, 0.2, 0.6, '&')) {
                heartFor(y, 1);
            } 
            else {
                heartFor(y, 2);
            }
            System.out.println();
            y -= 0.06;
        }
        System.out.println();
    }
}
