package com.jgen.chainway.chainway_plugin;

import static android.Manifest.permission.NFC;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultRegistryOwner;
import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.deviceapi.Printer;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.deviceapi.exception.PrinterBarcodeInvalidException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/** ChainwayPlugin */
public class ChainwayPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware{
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;

  BarcodeDecoder barcodeDecoder= BarcodeFactory.getInstance().getBarcodeDecoder();
  private NfcAdapter nfcAdapter;
  private Context context;
  private Printer mPrinter;
  private Intent mIntent = null;
  String TAG="CHAINWAY_FLUTTER";
  private Activity activity;
  private MethodChannel.Result pendingResult;

  private ActivityResultLauncher<Intent> resultLauncher;

  private static final int REQUEST_CODE = 1234; // choose any number
  private static final int NFC_PERMISSION_REQ_CODE = 1001;
  private static final int NFC_READER_REQ_CODE = 1002;
  private PendingIntent pendingIntent;
  private ActivityPluginBinding activityPluginBinding;
  private Map<String, Tag> tags = new HashMap<>();
  ImageView iv2D;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "chainway");
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "chainway_stream");
    channel.setMethodCallHandler(this);
    eventChannel.setStreamHandler(this);
    context = flutterPluginBinding.getApplicationContext();

    try {
      mPrinter = Printer.getInstance();

    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    Log.e(TAG,"method "+ call.method);
    if(call.method.equals("barcode_init")){
      if(barcodeDecoder.isOpen()){
        result.success(true);
      }else{
        barcodeDecoder.open(context);
        Log.e(TAG,"open()==========================:"+ barcodeDecoder.open(context));
        result.success(true);
      }
    }
     if(call.method.equals("scan")){
       Log.e(TAG,"SCANNING"+ barcodeDecoder);
        start();
    }
     if(call.method.equals("barcode_stop_scan")){
      stopScan();
    }
    if(call.method.equals("barcode_close")){
      close();
    }

    if(call.method.equals("init_printer")){

      mPrinter.init(0);
//      mPrinter.clearCache();//清
//      mPrinter.free();
//      mPrinter.setPrintGrayLevel(3);// 空缓存区
//      mPrinter.setPrintSpeed(4);// 空

      Log.e(TAG,"isPowerOn"+   mPrinter.isPowerOn());
    }

    if(call.method.equals("print_speed")){
      Integer arg = call.argument("speed");
      Log.e(TAG,"speed"+ arg);
      mPrinter.setPrintSpeed(arg);
    }
    if(call.method.equals("print_qr_code")){
      mPrinter.setPrintLeftMargin(50);
      mPrinter.setPrintRightMargin(50);
      mPrinter.setPrintRowSpacing(33);
      Bitmap bitmap=generateBitmap("HELLO WORLD",320,320);
      Log.e(TAG,"bitmap"+ bitmap);

      mPrinter.print(bitmap);
    }

    if(call.method.equals("print_receipt")){
      mPrinter.setPrintLeftMargin(50);
      mPrinter.setPrintRightMargin(50);
      mPrinter.setPrintRowSpacing(33);
      mPrinter.print("STORE NAME\n");
      mPrinter.print("Address\n");
      mPrinter.print("Phone number\n");
      mPrinter.print("-------------------------------\n\n");

      // Set alignment to left

      mPrinter.print("Item Description      Price Qty\n");
      mPrinter.print("-------------------------------\n");
      mPrinter.print("Item 1                $10   2\n");
      mPrinter.print("Item 2                $5    5\n");

      // Print total, tax, and other details
      mPrinter.print("-------------------------------\n");

      mPrinter.print("Total:                      $50\n");
      mPrinter.print("Tax:                          $5\n");
      mPrinter.print("-------------------------------\n");
      mPrinter.print("Grand Total:                 $55\n");
    }

  }


  private Bitmap generateBitmap(String content, int width, int height) {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    Map<EncodeHintType, String> hints = new HashMap<>();
    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
    try {
      BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
      int[] pixels = new int[width * height];
      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
          if (encode.get(j, i)) {
            pixels[i * width + j] = 0x00000000;
          } else {
            pixels[i * width + j] = 0xffffffff;
          }
        }
      }
      return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
    } catch (WriterException e) {
      e.printStackTrace();
    }
    return null;
  }



//  private void handleNfcStartSession(MethodCall call, MethodChannel.Result result) {
//    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//      result.error("unavailable", "Requires API level 19.", null);
//    } else {
//      NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
//      if(adapter == null) {
//        result.error("unavailable", "NFC is not available for device.", null);
//        return;
//      }
//      adapter.enableReaderMode(activity, tag -> {
//        final String handle = UUID.randomUUID().toString();
//        tags.put(handle, tag);
//        activity.runOnUiThread(() -> {
//          HashMap<String, Object> tagMap = (HashMap<String, Object>) new Translator().getTagMap(tag);
//          Log.e(TAG,"->"+   tagMap);
//          tagMap.put("handle", handle);
//          channel.invokeMethod("onDiscovered", tagMap);
//
//        });
//      }, new Translator().getFlags((List<String>) call.argument("pollingOptions")), null);
//      result.success(null);
//    }
//  }





  private void start(){
    barcodeDecoder.startScan();
  }

  private void stopScan(){
    barcodeDecoder.stopScan();
  }
  private void close(){
    barcodeDecoder.close();
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }



  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {

    eventSink = events;
    barcodeDecoder.setDecodeCallback(barcodeEntity -> {
      Log.e(TAG,"BarcodeDecoder==========================:"+barcodeEntity.getResultCode());
      if(barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS){

        Log.e(TAG,"data==========================:"+barcodeEntity.getBarcodeData());

        events.success(barcodeEntity.getBarcodeData());
      }else{
        events.success(null);
      }
    });


  }


  @Override
  public void onCancel(Object arguments) {
    eventSink = null;
    barcodeDecoder.close();
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }



  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }


}
