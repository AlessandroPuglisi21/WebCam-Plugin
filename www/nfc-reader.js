var exec = require('cordova/exec');

var NFCReader = {
    
    // Costanti per i tipi di carta
    CARD_TYPES: {
        AUTO_IDENTIFY: 0x00,
        M1: 0x20,
        ULTRALIGHT: 0x21,
        DESFIRE: 0x22,
        CTL_CPU: 0x23,
        MF_PLUS: 0x24,
        ICODE: 0x25,
        TYPEB: 0x26,
        FM1208: 0x27,
        NDEF: 0x80,
        SSC: 0x81,
        PBOC_PAN: 0x82,
        UHF: 0x90
    },
    
    // Costanti per i tipi di porta
    PORT_TYPES: {
        SERIAL: 1,
        USB: 2,
        BLUETOOTH: 3,
        TCPIP_CLIENT: 4,
        TCPIP_SERVER: 5
    },
    
    // Costanti per le interfacce
    INTERFACES: {
        CONTACT: 0,
        CONTACTLESS: 100
    },
    
    /**
     * Inizializza la connessione al lettore NFC
     * @param {Object} options - Opzioni di connessione
     * @param {number} options.portType - Tipo di porta (USB, SERIAL, etc.)
     * @param {string} options.devicePath - Percorso del dispositivo (per SERIAL/BLUETOOTH)
     * @param {number} options.baudRate - Velocità di comunicazione
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    init: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'init', [options]);
    },
    
    /**
     * Disconnette il lettore NFC
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    disconnect: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'disconnect', []);
    },
    
    /**
     * Ottiene la versione del firmware del dispositivo
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    getVersion: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'getVersion', []);
    },
    
    /**
     * Cerca e identifica automaticamente una carta NFC
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    identifyCard: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'identifyCard', []);
    },
    
    /**
     * Legge dati da una carta NFC
     * @param {Object} options - Opzioni di lettura
     * @param {number} options.cardType - Tipo di carta
     * @param {number} options.sector - Settore da leggere (per Mifare)
     * @param {number} options.block - Blocco da leggere
     * @param {string} options.key - Chiave di autenticazione (hex)
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    readCard: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'readCard', [options]);
    },
    
    /**
     * Scrive dati su una carta NFC
     * @param {Object} options - Opzioni di scrittura
     * @param {number} options.cardType - Tipo di carta
     * @param {number} options.sector - Settore da scrivere (per Mifare)
     * @param {number} options.block - Blocco da scrivere
     * @param {string} options.data - Dati da scrivere (hex)
     * @param {string} options.key - Chiave di autenticazione (hex)
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    writeCard: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'writeCard', [options]);
    },
    
    /**
     * Legge tag NDEF
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    readNDEF: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'readNDEF', []);
    },
    
    /**
     * Scrive tag NDEF
     * @param {Object} options - Opzioni di scrittura NDEF
     * @param {string} options.message - Messaggio NDEF da scrivere
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    writeNDEF: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'writeNDEF', [options]);
    },
    
    /**
     * Controlla il LED del lettore
     * @param {Object} options - Opzioni LED
     * @param {boolean} options.on - Accende/spegne il LED
     * @param {number} options.duration - Durata in millisecondi (per lampeggio)
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    controlLED: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'controlLED', [options]);
    },
    
    /**
     * Fa suonare il buzzer del lettore
     * @param {Object} options - Opzioni buzzer
     * @param {number} options.duration - Durata del suono in millisecondi
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    beep: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'beep', [options]);
    },
    
    /**
     * Legge tag UHF
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    readUHF: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'readUHF', []);
    },
    
    /**
     * Scrive tag UHF
     * @param {Object} options - Opzioni di scrittura UHF
     * @param {string} options.data - Dati da scrivere
     * @param {number} options.bank - Bank di memoria
     * @param {number} options.address - Indirizzo di scrittura
     * @param {Function} successCallback - Callback di successo
     * @param {Function} errorCallback - Callback di errore
     */
    writeUHF: function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'NFCReader', 'writeUHF', [options]);
    }
};

module.exports = NFCReader;