/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telecom.parser;

import com.telecom.db.DBConnection;
import com.telecom.model.cdrRecord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author mohesham
 */
public class CdrParser {
    
    //Extract the date from the filename => ex:CDR20250418103000 => 2025-04-18 10:30:00
    void parseFile(String filePath)
    {
        try {
            // Create File object from path
            File file = new File(filePath);

            // Get file name only (not full path)
            String fileName = file.getName();

            // Print for debugging
            System.out.println("File Name: " + fileName);

            String nameWithoutExt = fileName;
            // Remove extension if needed (CDR20250418103000.txt => CDR20250418103000)
            if (fileName.contains(".")) 
            {
                nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                System.out.println("File Name Without Extension: " + nameWithoutExt);
            }
            
            // Extract timestamp part from filename
            String timestampStr = nameWithoutExt.substring(3); // remove "CDR"

            // Convert string to LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime fileDateTime = LocalDateTime.parse(timestampStr, formatter);

            System.out.println("File DateTime: " + fileDateTime);
            
            //--------------------------------------------
            // Check connection successfully
            Connection con = DBConnection.getConnection();
            if (con == null) {
                System.out.println("DB Connection failed!");
                return;
            }
            else
            {
              System.out.println("DB Connection success!");  
            }

            //--------------------------------------------
            // INSERT in the file table and return the ID
            String sql = "INSERT INTO file(parsed_flag) VALUES (?) RETURNING id";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBoolean(1, false);
            
            ResultSet rs = ps.executeQuery();

            int fileId = 0;
            if (rs.next()) 
            {
                fileId = rs.getInt(1);
            }
            
            rs.close();
            ps.close();
            
            //--------------------------------------------
            // Read CDR file line by line
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            int cdrCount = 0;
            int errorCount = 0;

            System.out.println(">>> Starting to read file: " + fileName);

            while ((line = br.readLine()) != null)   // point the first line != null
            {
                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                // Parse line into CDR object
                cdrRecord record = parseLine(line, fileDateTime);

                // Insert CDR into database
                boolean success = insertCDR(record, fileId, con);

                // Increase counter
                cdrCount++;

                if (success) {
                    System.out.println("  [OK] CDR (" + cdrCount + "): " + line);
                } else {
                    System.out.println("  [FAILED] CDR (" + cdrCount + "): " + line);
                    errorCount++;
                }
            }

            br.close();

            System.out.println(">>> Finished file: " + fileName);
            System.out.println("    Total: " + cdrCount + " | Success: " + (cdrCount - errorCount) + " | Failed: " + errorCount);
            System.out.println("----------------------------------------");
            br.close();
            
            //--------------------------------------------
            // Mark file as processed
            String updateSql = "UPDATE file SET parsed_flag = true WHERE id = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setInt(1, fileId);
            updatePs.executeUpdate();

            updatePs.close();
            
            con.close();

            System.out.println("File parsed successfully!");

            // Move file to CDRArchive
            Path source = file.toPath();
            Path archiveDir = source.getParent().getParent().resolve("CDRArchive");
            Path destination = archiveDir.resolve(fileName);
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved to archive: " + destination);

            
        } catch (Exception e) {
        e.printStackTrace();
        }
    }

//----------------------------------------------------------
    private cdrRecord parseLine(String line, LocalDateTime fileDateTime){
        // Split CDR line by comma
        String[] parts = line.split(",");
        
        // Create new object
        cdrRecord record = new cdrRecord();
        
        // Example mapping (adjust based on your model)  => complete the object
        record.setCallerMsisdn(parts[0]);   //dial_a
        record.setReceiverMsisdn(parts[1]);          // dial_b
        
        int serviceCode = Integer.parseInt(parts[2]);
        String serviceType;
        switch (serviceCode) {
            case 1: serviceType = "voice"; break;
            case 2: serviceType = "sms"; break;
            case 3: serviceType = "data"; break;
            default: serviceType = "voice";
        }
        
        record.setServiceType(serviceType);
        record.setDuration(Integer.parseInt(parts[3]));
        
        // Time parsing (optional depending on model)  parts[4] = "12:30:00" => LocalTime = 12:30
        LocalTime time = LocalTime.parse(parts[4], DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime fullDateTime = LocalDateTime.of(fileDateTime.toLocalDate(), time);   // merge time and date => 2025-04-18T10:30:00
        record.setStartTime(fullDateTime);  //store in object
        
        record.setExternalCharges(Double.parseDouble(parts[5]));

        return record;
    }

//----------------------------------------------------------
    private boolean insertCDR(cdrRecord record, int fileId, Connection con) {
    try {
        // SQL query to insert a new CDR record into the database
        String sql = "INSERT INTO cdr " +
                     "(file_id, caller_id, receiver_id, start_time, duration, service_type, external_charges, rated_flag) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Prepare the statement to prevent SQL injection and improve performance
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, fileId);
        ps.setString(2, record.getCallerMsisdn());
        ps.setString(3, record.getReceiverMsisdn());
        ps.setTimestamp(4, java.sql.Timestamp.valueOf(record.getStartTime()));
        ps.setLong(5, record.getDuration());
        ps.setObject(6, record.getServiceType(), java.sql.Types.OTHER);        ps.setDouble(7, record.getExternalCharges());
        ps.setBoolean(8, false);
        
        // Execute the insert operation
        ps.executeUpdate();
        
        ps.close();
        return true;

        } catch (Exception e) {
            System.out.println("    Error: " + e.getMessage());
            return false;
        }
}

//----------------------------------------------------------
//----------------------------------------------------------
    
        public static void main(String[] args) {
        // Create object of parser
        CdrParser parser = new CdrParser();

        // Get project root directory and append CDRs folder
        // This makes the path dynamic for any user/machine
        String cdrFolderPath = System.getProperty("user.dir") + "/CDRs";
        
        // Create File object representing the CDRs folder
        File cdrFolder = new File(cdrFolderPath);

        // Only select files that start with "CDR" and end with ".txt"
        File[] cdrFiles = cdrFolder.listFiles((dir, name) -> name.startsWith("CDR") && name.endsWith(".txt"));

        // Check if no files found
        if (cdrFiles == null || cdrFiles.length == 0) {
            System.out.println("No CDR files found in: " + cdrFolderPath);
            return;
        }

        // Print number of files detected
        System.out.println("Found " + cdrFiles.length + " CDR file(s) to process.");

        // Loop through each CDR file
        for (File cdrFile : cdrFiles) {
            // Print current file being processed
            System.out.println("\n--- Processing: " + cdrFile.getName() + " ---");
            // Call parser method with full file path
            parser.parseFile(cdrFile.getAbsolutePath());
        }
        
        System.out.println("\nAll CDR files processed!");
    }  
}
