package src;

import services.AppService;

public class Main {
    public static void main(String[] args) {
        AppService.getInstance().run();
    }
}
