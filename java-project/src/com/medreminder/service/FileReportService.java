package com.medreminder.service;

import com.medreminder.interfaces.Exportable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles local file operations: exporting CSV reports using the Exportable
 * interface and writing/reading reminder activity logs.
 */
public class FileReportService {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + File.separator + "reminder_activity.log";
    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void appendActivityLog(String username, String actionDescription) {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String line = String.format("[%s] [%s] %s",
                LocalDateTime.now().format(TS_FMT),
                username,
                actionDescription);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Could not write to activity log: " + e.getMessage());
        }
    }

    public List<String> readRecentActivityLogs(int maxLines) {
        List<String> lines = new ArrayList<>();
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            lines.add("Error reading log file: " + e.getMessage());
        }

        int start = Math.max(0, lines.size() - maxLines);
        return lines.subList(start, lines.size());
    }

    /**
     * Exports any list of Exportable domain records (e.g., Medicine or MedicineHistory)
     * to a CSV file inside exports/.
     */
    public File exportToCsv(String fileName, List<? extends Exportable> items) throws IOException {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outFile = new File(dir, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            if (!items.isEmpty()) {
                writer.write(items.get(0).getCsvHeader());
                writer.newLine();
                for (Exportable item : items) {
                    writer.write(item.toCsvRow());
                    writer.newLine();
                }
            } else {
                writer.write("No records available to export.");
                writer.newLine();
            }
        }
        return outFile;
    }
}
