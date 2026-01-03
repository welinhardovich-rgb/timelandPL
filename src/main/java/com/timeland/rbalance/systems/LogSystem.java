package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogSystem {
    private final File logsFolder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogSystem(RBalancePlugin plugin) {
        this.logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
    }

    public void log(String fileName, String message) {
        File logFile = new File(logsFolder, fileName);
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(formatter);
            pw.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logTransaction(String message) {
        log("transactions.log", message);
    }

    public void logTrade(String message) {
        log("trades.log", message);
    }

    public void logError(String message) {
        log("errors.log", message);
    }

    public void logAdmin(String message) {
        log("admin.log", message);
    }
}
