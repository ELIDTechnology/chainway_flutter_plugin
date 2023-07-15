package com.jgen.chainway.chainway_plugin;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Translator {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public int getFlags(List<String> options) {
        if (options == null) {
            options = new ArrayList<>();
        }

        int flags = 0;

        if (options.contains("iso14443")) {
            flags = flags | NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B;
        }

        if (options.contains("iso15693")) {
            flags = flags | NfcAdapter.FLAG_READER_NFC_V;
        }

        if (options.contains("iso18092")) {
            flags = flags | NfcAdapter.FLAG_READER_NFC_F;
        }

        return flags;
    }

    public static Map<String, Object> getTagMap(Tag tag) {
        Map<String, Object> data = new HashMap<>();

        for (String tech : tag.getTechList()) {
            String techName = tech.toLowerCase().split("\\.")[(tech.split("\\.")).length - 1];

            if (NfcA.class.getName().equals(tech)) {
                NfcA nfcA = NfcA.get(tag);
                data.put(techName, new HashMap<String, Object>() {{
                    put("identifier", tag.getId());
                    put("atqa", nfcA.getAtqa());
                    put("maxTransceiveLength", nfcA.getMaxTransceiveLength());
                    put("sak", nfcA.getSak());
                    put("timeout", nfcA.getTimeout());
                }});
            } else if (NfcB.class.getName().equals(tech)) {
                // handle NfcB
            }
        }

        return data;
    }

    public NdefMessage getNdefMessage(Map<String, Object> arg) {
        List<Map<String, Object>> records = (List<Map<String, Object>>) arg.get("records");
        NdefRecord[] ndefRecords = new NdefRecord[records.size()];

        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> record = records.get(i);
            ndefRecords[i] = new NdefRecord(
                    ((Integer) record.get("typeNameFormat")).shortValue(),
                    (byte[]) record.get("type"),
                    (byte[]) record.get("identifier"),
                    (byte[]) record.get("payload")
            );
        }

        return new NdefMessage(ndefRecords);
    }

    public Map<String, Object> getNdefMessageMap(NdefMessage ndefMessage) {
        NdefRecord[] records = ndefMessage.getRecords();
        List<Map<String, Object>> recordMaps = new ArrayList<>();

        for (NdefRecord record : records) {
            recordMaps.add(new HashMap<String, Object>() {{
                put("typeNameFormat", record.getTnf());
                put("type", record.getType());
                put("identifier", record.getId());
                put("payload", record.getPayload());
            }});
        }

        return new HashMap<String, Object>() {{
            put("records", recordMaps);
        }};
    }

}
