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

    switch (call.method){
      case "barcode_init":
        if(barcodeDecoder.isOpen()){
          result.success(true);
        }else{
          barcodeDecoder.open(context);
          Log.e(TAG,"open()==========================:"+ barcodeDecoder.open(context));
          result.success(true);
        }
      case "barcode_scan":
        start();
      case "barcode_stop_scan":
        stopScan();
      case "barcode_close":
        close();
      case "printer_init":
        mPrinter.init(0);
        mPrinter.setPrintLeftMargin(50);
        mPrinter.setPrintRightMargin(50);
        mPrinter.setPrintRowSpacing(33);
        result.success(true);
      case "printer_speed":
        Integer arg = call.argument("speed");
        mPrinter.setPrintSpeed(arg);
      case "print_qr_code":
        String barcode_details = call.argument("barcode_details");
        Bitmap bitmap=generateBitmap(barcode_details,320,320);
        mPrinter.print(bitmap);
      case "print_receipt":
        String receipt_details = call.argument("receipt_details");
        mPrinter.print(receipt_details);
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
      if(barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS){
        events.success(barcodeEntity.getBarcodeData());
      }else{
        events.success(null);
      }
    });

    printer.setPrinterStatusCallBack(new Printer.PrinterStatusCallBack() {
      @Override
      public void message(Printer.PrinterStatus printerStatus) {
        switch (printerStatus) {
          case NORMAL://正常:

            break;
          case OVERPRESSURE://过压

            break;
          case LACKOFPAPER://缺纸

            break;
          case OVERHEATING://过热

            break;
          case PRESSUREAXISOPEN://压轴打开

            break;
          case PAPERSTUCK://卡纸

            break;
          case SLICINGERROR://切片错误

            break;
          case PAPERFINISH://打印机纸将尽

            break;
          case CANCELPAPER://打印机用户未取纸

            break;
          case LEISURE:

            break;
          case UNLEISURED:


        }

      }});}


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
