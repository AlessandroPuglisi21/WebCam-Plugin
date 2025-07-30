package com.nfcreader.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class NFCReaderPlugin extends CordovaPlugin {
    
    private static final String TAG = "NFCReaderPlugin";
    private NFCReaderManager nfcManager;
    
    @Override
    public void initialize(org.apache.cordova.CordovaInterface cordova, org.apache.cordova.CordovaWebView webView) {
        super.initialize(cordova, webView);
        nfcManager = new NFCReaderManager();
        Log.d(TAG, "Plugin inizializzato");
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        try {
            if ("init".equals(action)) {
                return initReader(args, callbackContext);
            } else if ("disconnect".equals(action)) {
                return disconnect(callbackContext);
            } else if ("getVersion".equals(action)) {
                return getVersion(callbackContext);
            } else if ("identifyCard".equals(action)) {
                return identifyCard(callbackContext);
            } else if ("readCard".equals(action)) {
                return readCard(args, callbackContext);
            } else if ("writeCard".equals(action)) {
                return writeCard(args, callbackContext);
            } else if ("readNDEF".equals(action)) {
                return readNDEF(callbackContext);
            } else if ("writeNDEF".equals(action)) {
                return writeNDEF(args, callbackContext);
            } else if ("controlLED".equals(action)) {
                return controlLED(args, callbackContext);
            } else if ("beep".equals(action)) {
                return beep(args, callbackContext);
            } else if ("readUHF".equals(action)) {
                return readUHF(callbackContext);
            } else if ("writeUHF".equals(action)) {
                return writeUHF(args, callbackContext);
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Errore nell'esecuzione dell'azione: " + action, e);
            callbackContext.error("Errore: " + e.getMessage());
            return true;
        }
    }
    
    private boolean initReader(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int portType = options.optInt("portType", 2); // Default USB
                    String devicePath = options.optString("devicePath", "");
                    int baudRate = options.optInt("baudRate", 115200);
                    
                    boolean result = nfcManager.initReader(portType, devicePath, baudRate);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("message", "Lettore NFC inizializzato con successo");
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Impossibile inizializzare il lettore NFC");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nell'inizializzazione", e);
                    callbackContext.error("Errore nell'inizializzazione: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean disconnect(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = nfcManager.disconnect();
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("message", "Lettore NFC disconnesso");
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nella disconnessione");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella disconnessione", e);
                    callbackContext.error("Errore nella disconnessione: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean getVersion(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String version = nfcManager.getVersion();
                    
                    if (version != null) {
                        JSONObject response = new JSONObject();
                        response.put("version", version);
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Impossibile ottenere la versione");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nel recupero versione", e);
                    callbackContext.error("Errore nel recupero versione: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean identifyCard(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject cardInfo = nfcManager.identifyCard();
                    
                    if (cardInfo != null) {
                        callbackContext.success(cardInfo);
                    } else {
                        callbackContext.error("Nessuna carta rilevata");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nell'identificazione carta", e);
                    callbackContext.error("Errore nell'identificazione: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean readCard(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int cardType = options.optInt("cardType", 0);
                    int sector = options.optInt("sector", 0);
                    int block = options.optInt("block", 0);
                    String key = options.optString("key", "FFFFFFFFFFFF");
                    
                    JSONObject result = nfcManager.readCard(cardType, sector, block, key);
                    
                    if (result != null) {
                        callbackContext.success(result);
                    } else {
                        callbackContext.error("Errore nella lettura della carta");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella lettura carta", e);
                    callbackContext.error("Errore nella lettura: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean writeCard(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int cardType = options.optInt("cardType", 0);
                    int sector = options.optInt("sector", 0);
                    int block = options.optInt("block", 0);
                    String data = options.optString("data", "");
                    String key = options.optString("key", "FFFFFFFFFFFF");
                    
                    boolean result = nfcManager.writeCard(cardType, sector, block, data, key);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("message", "Scrittura completata con successo");
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nella scrittura della carta");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella scrittura carta", e);
                    callbackContext.error("Errore nella scrittura: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean readNDEF(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject ndefData = nfcManager.readNDEF();
                    
                    if (ndefData != null) {
                        callbackContext.success(ndefData);
                    } else {
                        callbackContext.error("Errore nella lettura NDEF");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella lettura NDEF", e);
                    callbackContext.error("Errore nella lettura NDEF: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean writeNDEF(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = options.optString("message", "");
                    
                    boolean result = nfcManager.writeNDEF(message);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("message", "NDEF scritto con successo");
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nella scrittura NDEF");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella scrittura NDEF", e);
                    callbackContext.error("Errore nella scrittura NDEF: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean controlLED(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean on = options.optBoolean("on", true);
                    int duration = options.optInt("duration", 1000);
                    
                    boolean result = nfcManager.controlLED(on, duration);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nel controllo LED");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nel controllo LED", e);
                    callbackContext.error("Errore nel controllo LED: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean beep(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int duration = options.optInt("duration", 500);
                    
                    boolean result = nfcManager.beep(duration);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nel buzzer");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nel buzzer", e);
                    callbackContext.error("Errore nel buzzer: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean readUHF(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject uhfData = nfcManager.readUHF();
                    
                    if (uhfData != null) {
                        callbackContext.success(uhfData);
                    } else {
                        callbackContext.error("Errore nella lettura UHF");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella lettura UHF", e);
                    callbackContext.error("Errore nella lettura UHF: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    private boolean writeUHF(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = options.optString("data", "");
                    int bank = options.optInt("bank", 1);
                    int address = options.optInt("address", 0);
                    
                    boolean result = nfcManager.writeUHF(data, bank, address);
                    
                    if (result) {
                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("message", "UHF scritto con successo");
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Errore nella scrittura UHF");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Errore nella scrittura UHF", e);
                    callbackContext.error("Errore nella scrittura UHF: " + e.getMessage());
                }
            }
        });
        
        return true;
    }
    
    @Override
    public void onDestroy() {
        if (nfcManager != null) {
            nfcManager.disconnect();
        }
        super.onDestroy();
    }
}