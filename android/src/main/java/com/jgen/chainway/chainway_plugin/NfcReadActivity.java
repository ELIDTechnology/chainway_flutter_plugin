package com.jgen.chainway.chainway_plugin;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class NfcReadActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.e("EWQOPEIWOIEW","eeeeeeee "+action);
        Log.e("EWQOPEIWOIEW","eeeeeeee "+NfcAdapter.ACTION_TAG_DISCOVERED.equals(action));
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                MifareClassic mifareClassic = MifareClassic.get(tag);

                Log.e("EWQOPEIWOIEW","eeeeeeee "+mifareClassic.getTag());
                if (mifareClassic != null) {

                    Intent data = new Intent();
                    data.putExtra("tagData","test");
                    Log.e("EWQOPEIWOIEW","TAG DATA "+data.getExtras().toString());
                    setResult(RESULT_OK, data);
                    finish();
//                    try {
//                        mifareClassic.connect();
//                        int sectorCount = mifareClassic.getSectorCount();
//                        boolean auth;
//                        StringBuilder tagData = new StringBuilder();
//                        for (int j = 0; j < sectorCount; j++) {
//                            try {
//                                int sectorIndex = mifareClassic.blockToSector(j);
//                                auth = mifareClassic,(sectorIndex, MifareClassic.TYPE_CLASSIC);
//                                if (auth) {
//                                    byte[] data = mifareClassic.readBlock(j);
//                                    tagData.append(bytesToHex(data));
//                                } else {
//                                    tagData.append("Authentication failed");
//                                }
//                            } catch (TagLostException e) {
//                                // If the tag was lost, break the loop and inform the user.
//                                tagData.append("Tag was lost.");
//                                break;
//                            }
//                        }
//                        mifareClassic.close();
//
//                        Intent data = new Intent();
//                        data.putExtra("tagData", tagData.toString());
//                        Log.e("EWQOPEIWOIEW","TAG DATA "+data.getExtras().toString());
//                        setResult(RESULT_OK, data);
//
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
                }
            }

        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}