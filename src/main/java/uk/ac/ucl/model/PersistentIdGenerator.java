package uk.ac.ucl.model;


import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentIdGenerator {
    private static final String FILE_PATH = "C:\\Users\\jeetu\\Desktop\\Java\\JavaCoursework\\src\\main\\webapp\\data\\persistent-id.txt";
    private static final AtomicInteger UNIQUE_ID = new AtomicInteger(readInitialValue());

    private static int readInitialValue() {
        int value = 1;
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                value = Integer.parseInt(line.trim());
            } catch (Exception e) {
                // Use default value if error occurs
            }
        }
        return value;
    }

    private static void saveValue(int value) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            bw.write(Integer.toString(value));
        } catch (Exception e) {
            // Handle file write error appropriately
        }
    }

    public static synchronized int getNextId() {
        int id = UNIQUE_ID.getAndIncrement();
        saveValue(id);
        return id;
    }
}