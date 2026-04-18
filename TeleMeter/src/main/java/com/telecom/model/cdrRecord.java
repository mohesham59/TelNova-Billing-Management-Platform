/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telecom.model;
import java.time.LocalDateTime;
/**
 *
 * @author mohesham
 */
public class cdrRecord {
    private int id;
    private int fileId;
    private String callerMsisdn;    //dial_a
    private String receiverMsisdn;  //dial_b
    private LocalDateTime startTime;
    private long duration;  //seconds / messages / bytes 
    private int serviceId;  //1=voice , 2=sms , 3=data
    private double externalCharges;  
    
//-----------------------------------------------------
//-----------------------------------------------------
    
    // Getters & Setters
    public int getId() 
    { 
        return id; 
    }
    public void setId(int id) 
    { 
        this.id = id; 
    }
//-----------------------------------------------------
    public int getFileId() 
    { 
        return fileId; 
    }
    public void setFileId(int fileId) 
    { 
        this.fileId = fileId; 
    }
//-----------------------------------------------------
    public String getCallerMsisdn() 
    { 
        return callerMsisdn; 
    }
    public void setCallerMsisdn(String callerMsisdn) 
    { 
        this.callerMsisdn = callerMsisdn; 
    }
//-----------------------------------------------------
    public String getReceiverMsisdn() 
    { 
        return receiverMsisdn; 
    }
    public void setReceiverMsisdn(String receiverMsisdn) 
    { 
        this.receiverMsisdn = receiverMsisdn; 
    }
//-----------------------------------------------------
    public LocalDateTime getStartTime() 
    { 
        return startTime; 
    }
    public void setStartTime(LocalDateTime startTime) 
    { 
        this.startTime = startTime; 
    }
//-----------------------------------------------------
    public long getDuration() 
    { 
        return duration; 
    }
    public void setDuration(long duration) 
    { 
        this.duration = duration; 
    }
//-----------------------------------------------------
    public int getServiceId() 
    { 
        return serviceId; 
    }
    public void setServiceId(int serviceId) 
    { 
        this.serviceId = serviceId; 
    }
//-----------------------------------------------------
    public double getExternalCharges() 
    { 
        return externalCharges; 
    }
    public void setExternalCharges(double externalCharges) 
    { 
        this.externalCharges = externalCharges; 
    }
    
}
