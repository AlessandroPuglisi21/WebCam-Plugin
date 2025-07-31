package com.nfcreader.plugin;

import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

import lc.comproCall;
import jRF.FM1208;

public class NFCReaderManager {
    
    private static final String TAG = "NFCReaderManager";
    
    // Costanti per i tipi di carta
    public static final byte CARD_AUTO_IDENTIFY = 0x00;
    public static final byte CARD_M1 = 0x20;
    public static final byte CARD_ULTRILIGHT = 0x21;
    public static final byte CARD_DESFIRE = 0x22;
    public static final byte CARD_CTL_CPU = 0x23;
    public static final byte CARD_MF_PLUS = 0x24;
    public static final byte CARD_ICODE = 0x25;
    public static final byte CARD_TYPEB = 0x26;
    public static final byte CARD_FM1208 = 0x27;
    public static final byte CARD_NDEF = (byte)0x80;
    public static final byte CARD_SSC = (byte)0x81;
    public static final byte CARD_PBOC_PAN = (byte)0x82;
    public static final byte CARD_UHF = (byte)0x90;
    
    // Costanti per i tipi di porta
    public static final char PT_SERIAL = 1;
    public static final char PT_USB = 2;
    public static final char PT_BLUETOOTH = 3;
    public static final char PT_TCPIP_CLIENT = 4;
    public static final char PT_TCPIP_SERVER = 5;
    
    private comproCall call_comPro;
    private FM1208 call_fm1208;
    private int deviceHandle = -1;
    
    public NFCReaderManager() {
        call_comPro = new comproCall();
        call_fm1208 = new FM1208();
    }
    
    /**
     * Inizializza la connessione al lettore NFC
     */
    public boolean initReader(int portType, String devicePath, int baudRate) {
        try {
            // Usa lc_init con parametri corretti
            if (portType == PT_USB) {
                deviceHandle = call_comPro.lc_init(100, baudRate); // 100 per USB
            } else {
                deviceHandle = call_comPro.lc_init(0, baudRate); // 0 per COM1
            }
            
            if (deviceHandle != -1) {
                Log.d(TAG, "Lettore NFC inizializzato con handle: " + deviceHandle);
                return true;
            } else {
                Log.e(TAG, "Errore nell'inizializzazione del lettore NFC");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Errore durante l'inizializzazione: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnette il lettore NFC
     */
    public boolean disconnect() {
        try {
            if (deviceHandle != -1) {
                int result = call_comPro.lc_exit(deviceHandle);
                deviceHandle = -1;
                return result == 0;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Errore durante la disconnessione: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ottiene la versione del firmware
     */
    public String getVersion() {
        try {
            if (deviceHandle != -1) {
                char[] version = new char[64];
                int result = call_comPro.lc_getver(deviceHandle, version);
                if (result == 0) {
                    return new String(version).trim();
                }
            }
            return "Versione non disponibile";
        } catch (Exception e) {
            Log.e(TAG, "Errore nel recupero della versione: " + e.getMessage());
            return "Errore";
        }
    }
    
    /**
     * Identifica una carta NFC
     */
    public JSONObject identifyCard() {
        try {
            if (deviceHandle == -1) {
                throw new Exception("Lettore non inizializzato");
            }
            
            byte[] snr = new byte[255];
            byte[] snrSize = new byte[64];
            int[] tag = new int[1];
            byte[] sak = new byte[1];
            
            // Prova a trovare una carta TypeA con parametri corretti
            int result = call_comPro.lc_card(deviceHandle, (byte)0, snr, snrSize, tag, sak);
            
            if (result == 0) {
                JSONObject cardInfo = new JSONObject();
                
                // Converte il SNR in stringa esadecimale
                StringBuilder snrHex = new StringBuilder();
                for (int i = 0; i < snrSize[0]; i++) {
                    snrHex.append(String.format("%02X", snr[i] & 0xFF));
                }
                
                cardInfo.put("found", true);
                cardInfo.put("snr", snrHex.toString());
                cardInfo.put("snrSize", snrSize[0]);
                cardInfo.put("cardType", "ISO14443-A");
                cardInfo.put("tag", tag[0]);
                cardInfo.put("sak", sak[0] & 0xFF);
                
                // Prova a identificare il tipo specifico di carta con parametri corretti
                byte[] cardType = new byte[1];
                byte[] compliant14443_4 = new byte[1];
                int identifyResult = call_comPro.lc_requestAndIdentifyTypeA(deviceHandle, (byte)0, snr, snrSize, cardType, compliant14443_4);
                
                if (identifyResult == 0) {
                    cardInfo.put("specificType", getCardTypeName(cardType[0]));
                    cardInfo.put("typeCode", cardType[0] & 0xFF);
                    cardInfo.put("compliant14443_4", compliant14443_4[0] == 1);
                }
                
                return cardInfo;
            }
            
            // Prova TypeB
            byte[] responseB = new byte[255];
            byte[] resLenB = new byte[1];
            result = call_comPro.lc_findTypeB(deviceHandle, (byte)1, responseB, resLenB);
            
            if (result == 0) {
                JSONObject cardInfo = new JSONObject();
                cardInfo.put("found", true);
                cardInfo.put("cardType", "ISO14443-B");
                cardInfo.put("responseLength", resLenB[0] & 0xFF);
                return cardInfo;
            }
            
            // Prova ISO15693
            result = call_comPro.lc_find15693(deviceHandle, responseB, resLenB);
            
            if (result == 0) {
                JSONObject cardInfo = new JSONObject();
                cardInfo.put("found", true);
                cardInfo.put("cardType", "ISO15693");
                cardInfo.put("responseLength", resLenB[0] & 0xFF);
                return cardInfo;
            }
            
            // Nessuna carta trovata
            JSONObject cardInfo = new JSONObject();
            cardInfo.put("found", false);
            cardInfo.put("message", "Nessuna carta rilevata");
            return cardInfo;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nell'identificazione della carta: " + e.getMessage());
            try {
                JSONObject errorInfo = new JSONObject();
                errorInfo.put("found", false);
                errorInfo.put("error", e.getMessage());
                return errorInfo;
            } catch (JSONException je) {
                return new JSONObject();
            }
        }
    }
    
    /**
     * Legge dati da una carta NFC
     */
    public JSONObject readCard(int cardType, int sector, int block, String key) {
        try {
            if (deviceHandle == -1) {
                return null;
            }
            
            byte[] keyBytes = hexStringToByteArray(key);
            byte[] dataOut = new byte[16];
            
            // Autenticazione - FIRMA CORRETTA: rimosso il parametro extra
            int authResult = call_comPro.lc_authentication(deviceHandle, (byte)0x60, (byte)sector, keyBytes);
            if (authResult != 0) {
                Log.e(TAG, "Errore nell'autenticazione: " + authResult);
                return null;
            }
            
            // Lettura
            int readResult = call_comPro.lc_read(deviceHandle, (byte)block, dataOut);
            if (readResult != 0) {
                Log.e(TAG, "Errore nella lettura: " + readResult);
                return null;
            }
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("data", byteArrayToHexString(dataOut));
            result.put("sector", sector);
            result.put("block", block);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella lettura carta", e);
            return null;
        }
    }
    
    /**
     * Scrive dati su una carta NFC
     */
    public boolean writeCard(int cardType, int sector, int block, String data, String key) {
        try {
            if (deviceHandle == -1) {
                return false;
            }
            
            byte[] keyBytes = hexStringToByteArray(key);
            byte[] dataBytes = hexStringToByteArray(data);
            
            // Autenticazione - FIRMA CORRETTA: rimosso il parametro extra
            int authResult = call_comPro.lc_authentication(deviceHandle, (byte)0x60, (byte)sector, keyBytes);
            if (authResult != 0) {
                Log.e(TAG, "Errore nell'autenticazione: " + authResult);
                return false;
            }
            
            // Scrittura
            int writeResult = call_comPro.lc_write(deviceHandle, (byte)block, dataBytes);
            if (writeResult != 0) {
                Log.e(TAG, "Errore nella scrittura: " + writeResult);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella scrittura carta", e);
            return false;
        }
    }
    
    /**
     * Legge tag NDEF
     */
    public JSONObject readNDEF() {
        try {
            if (deviceHandle == -1) {
                return null;
            }
            
            byte[] ndefData = new byte[1024];
            byte[] dataSize = new byte[4];
            
            // Implementazione specifica per lettura NDEF
            // Questo dipende dal tipo specifico di tag NDEF
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("message", "Implementazione NDEF da completare");
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella lettura NDEF", e);
            return null;
        }
    }
    
    /**
     * Scrive tag NDEF
     */
    public boolean writeNDEF(String message) {
        try {
            if (deviceHandle == -1) {
                return false;
            }
            
            // Implementazione specifica per scrittura NDEF
            // Questo dipende dal tipo specifico di tag NDEF
            
            Log.d(TAG, "Scrittura NDEF: " + message);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella scrittura NDEF", e);
            return false;
        }
    }
    
    /**
     * Controlla il LED
     */
    public boolean controlLED(boolean on, int duration) {
        try {
            if (deviceHandle == -1) {
                return false;
            }
            
            // Controllo LED - FIRMA CORRETTA: (int icdev, int iLED, int on_off)
            int result = call_comPro.lc_led(deviceHandle, 1, on ? 1 : 0); // 1=LED rosso, 2=LED verde
            
            if (duration > 0 && on) {
                // Lampeggio per la durata specificata
                Thread.sleep(duration);
                call_comPro.lc_led(deviceHandle, 1, 0);
            }
            
            return result == 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nel controllo LED", e);
            return false;
        }
    }
    
    /**
     * Fa suonare il buzzer
     */
    public boolean beep(int duration) {
        try {
            if (deviceHandle == -1) {
                return false;
            }
            
            int result = call_comPro.lc_beep(deviceHandle, duration);
            return result == 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nel buzzer", e);
            return false;
        }
    }
    
    /**
     * Legge tag UHF
     */
    public JSONObject readUHF() {
        try {
            if (deviceHandle == -1) {
                throw new Exception("Lettore non inizializzato");
            }
            
            byte[] epcData = new byte[255];
            byte[] epcSize = new byte[4];
            
            // Usa la firma corretta con scanTime
            int result = call_comPro.lc_uhf_inventory(deviceHandle, (short)200, epcData, epcSize);
            
            if (result == 0) {
                JSONObject uhfInfo = new JSONObject();
                
                StringBuilder epcHex = new StringBuilder();
                for (int i = 0; i < epcSize[0]; i++) {
                    epcHex.append(String.format("%02X", epcData[i] & 0xFF));
                }
                
                uhfInfo.put("found", true);
                uhfInfo.put("epc", epcHex.toString());
                uhfInfo.put("epcSize", epcSize[0] & 0xFF);
                return uhfInfo;
            } else {
                JSONObject uhfInfo = new JSONObject();
                uhfInfo.put("found", false);
                uhfInfo.put("error", "Nessun tag UHF trovato");
                return uhfInfo;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella lettura UHF: " + e.getMessage());
            try {
                JSONObject errorInfo = new JSONObject();
                errorInfo.put("found", false);
                errorInfo.put("error", e.getMessage());
                return errorInfo;
            } catch (JSONException je) {
                return new JSONObject();
            }
        }
    }
    
    /**
     * Scrive tag UHF
     */
    public boolean writeUHF(String data, int bank, int address) {
        try {
            if (deviceHandle == -1) {
                return false;
            }
            
            byte[] dataBytes = hexStringToByteArray(data);
            byte[] password = new byte[4]; // Password di default (tutti zeri)
            
            // FIRMA CORRETTA: (int icdev, byte bTagBank, int addrWord, int lenWord, byte[] pData, byte[] pPwd)
            int result = call_comPro.lc_uhf_writeTag(deviceHandle, (byte)bank, address, dataBytes.length / 2, dataBytes, password);
            
            return result == 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nella scrittura UHF", e);
            return false;
        }
    }
    
    // Metodi di utilità
    
    private String getCardTypeName(byte cardType) {
        switch (cardType) {
            case 0x00: return "Mifare Classic 1K";
            case 0x01: return "Mifare Classic 4K";
            case 0x02: return "Mifare Plus 2K";
            case 0x03: return "Mifare Plus 4K";
            case 0x04: return "Ultralight";
            case 0x05: return "Ultralight-C";
            case 0x06: return "NTAG203";
            case 0x07: return "NTAG210";
            case 0x08: return "NTAG212";
            case 0x09: return "NTAG213";
            case 0x0A: return "NTAG215";
            case 0x0B: return "NTAG216";
            case 0x11: return "Mifare DESFire";
            default: return "Sconosciuto (" + String.format("0x%02X", cardType & 0xFF) + ")";
        }
    }
    
    private byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[0];
        }
        
        int len = hex.length();
        byte[] data = new byte[len / 2];
        
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i + 1), 16));
        }
        
        return data;
    }
    
    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
}