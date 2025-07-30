# Cordova NFC Reader Plugin

Plugin Apache Cordova per l'integrazione di lettori NFC esterni utilizzando l'SDK "lc".

## Installazione

```bash
cordova plugin add cordova-plugin-nfc-reader
```

## Utilizzo

### Inizializzazione

```javascript
// Inizializza il lettore NFC via USB
NFCReader.init({
    portType: NFCReader.PORT_TYPES.USB,
    baudRate: 115200
}, function(success) {
    console.log('Lettore NFC inizializzato:', success);
}, function(error) {
    console.error('Errore inizializzazione:', error);
});
```

### Identificazione automatica carta

```javascript
NFCReader.identifyCard(function(cardInfo) {
    if (cardInfo.found) {
        console.log('Carta trovata:', cardInfo);
        console.log('SNR:', cardInfo.snr);
        console.log('Tipo:', cardInfo.cardType);
    } else {
        console.log('Nessuna carta rilevata');
    }
}, function(error) {
    console.error('Errore:', error);
});
```

### Lettura carta Mifare

```javascript
NFCReader.readCard({
    cardType: NFCReader.CARD_TYPES.M1,
    sector: 1,
    block: 4,
    key: 'FFFFFFFFFFFF'
}, function(data) {
    console.log('Dati letti:', data.data);
}, function(error) {
    console.error('Errore lettura:', error);
});
```

### Scrittura carta Mifare

```javascript
NFCReader.writeCard({
    cardType: NFCReader.CARD_TYPES.M1,
    sector: 1,
    block: 4,
    data: '00112233445566778899AABBCCDDEEFF',
    key: 'FFFFFFFFFFFF'
}, function(success) {
    console.log('Scrittura completata:', success);
}, function(error) {
    console.error('Errore scrittura:', error);
});
```

### Controllo LED e Buzzer

```javascript
// Accende il LED per 2 secondi
NFCReader.controlLED({
    on: true,
    duration: 2000
}, function(success) {
    console.log('LED controllato');
}, function(error) {
    console.error('Errore LED:', error);
});

// Fa suonare il buzzer per 500ms
NFCReader.beep({
    duration: 500
}, function(success) {
    console.log('Buzzer attivato');
}, function(error) {
    console.error('Errore buzzer:', error);
});
```

### Lettura tag UHF

```javascript
NFCReader.readUHF(function(uhfData) {
    if (uhfData.found) {
        console.log('Tag UHF trovato:', uhfData.epc);
    } else {
        console.log('Nessun tag UHF rilevato');
    }
}, function(error) {
    console.error('Errore UHF:', error);
});
```

## API Reference

### Costanti

- `NFCReader.CARD_TYPES`: Tipi di carta supportati
- `NFCReader.PORT_TYPES`: Tipi di connessione
- `NFCReader.INTERFACES`: Interfacce di comunicazione

### Metodi

- `init(options, success, error)`: Inizializza il lettore
- `disconnect(success, error)`: Disconnette il lettore
- `getVersion(success, error)`: Ottiene la versione firmware
- `identifyCard(success, error)`: Identifica automaticamente una carta
- `readCard(options, success, error)`: Legge dati da una carta
- `writeCard(options, success, error)`: Scrive dati su una carta
- `readNDEF(success, error)`: Legge tag NDEF
- `writeNDEF(options, success, error)`: Scrive tag NDEF
- `controlLED(options, success, error)`: Controlla il LED
- `beep(options, success, error)`: Attiva il buzzer
- `readUHF(success, error)`: Legge tag UHF
- `writeUHF(options, success, error)`: Scrive tag UHF

## Requisiti

- Cordova >= 7.0.0
- Android >= API 21
- Lettore NFC compatibile con SDK "lc"

## Licenza

MIT