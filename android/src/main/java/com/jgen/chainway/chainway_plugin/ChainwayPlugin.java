package com.jgen.chainway.chainway_plugin;

import static android.Manifest.permission.NFC;

import static com.rscja.deviceapi.Printer.PrinterStatus.PAPERFINISH;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
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

import java.io.ByteArrayOutputStream;
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

import org.json.JSONException;
import org.json.JSONObject;

/** ChainwayPlugin */
public class ChainwayPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware{
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel eventChannelPrinter;

  BarcodeDecoder barcodeDecoder= BarcodeFactory.getInstance().getBarcodeDecoder();
  private Context context;
  private Printer mPrinter;
  String TAG="CHAINWAY_FLUTTER";
  private Map<String, Tag> tags = new HashMap<>();
  ImageView iv2D;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "chainway");
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "chainway_stream");
    eventChannelPrinter = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "chainway_printer");

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
          result.success(true);
        }
        break;
      case "barcode_scan":
        start();
        break;
      case "barcode_stop_scan":
        stopScan();
        break;
      case "barcode_close":
        close();
        break;
      case "printer_init":
        mPrinter.init(0);
      new PrinterEventChannel(eventChannelPrinter, mPrinter);

        result.success(true);
        break;
      case "printer_speed":
        Integer arg = call.argument("speed");
        mPrinter.setPrintSpeed(arg);
        break;
      case "print_qr_code":
        String barcode_details = call.argument("print_qr_code");
        Log.e("EWQEOWPQ", barcode_details);
//        Bitmap bitmap=generateBitmap(barcode_details.toString(),320,320);
//        mPrinter.print(bitmap);
        break;
      case "print_bitmap":
        byte[] byteArray = call.argument("header");
        String body = call.argument("body");
        String qr = call.argument("qr");
        String footer = call.argument("footer");
        Bitmap a = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Matrix matrix = new Matrix();
        int width = a.getWidth();
        int height = a.getHeight();
        int newWidth = 384;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight =scaleWidth;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap headerBitmap = Bitmap.createBitmap(a, 0, 0, width,height, matrix, true);
        Bitmap qrCode = generateBitmap(qr.toString(),384,300);
        mPrinter.clearCache();
        mPrinter.setPrintSpeed(5);
        mPrinter.setPrintRowSpacing(2);
        mPrinter.print(headerBitmap);
        mPrinter.print(body.trim());
        mPrinter.print(qrCode);
        mPrinter.print(footer);

        result.success("DONE");
        break;
      case "print_test":
        byte[] byteArrays = call.argument("test");
        Bitmap b = BitmapFactory.decodeByteArray(byteArrays, 0, byteArrays.length);
        Matrix matrixb = new Matrix();
        int widthb = b.getWidth();
        int heightb = b.getHeight();
        int newWidthb = 384;
        float scaleWidthb = ((float) newWidthb) / widthb;
        float scaleHeightb =scaleWidthb;
        matrixb.postScale(newWidthb, scaleHeightb);
        Bitmap test = Bitmap.createBitmap(b, 0, 0, widthb,heightb, matrixb, true);
        mPrinter.clearCache();
        mPrinter.print(test);
        result.success("DONE");
        break;
      case "print_receipt":
        String receipt_details = call.argument("receipt_details");
        mPrinter.clearCache();
        mPrinter.setPrintSpeed(5);
        mPrinter.setPrintRowSpacing(10);
        mPrinter.print(receipt_details);
        result.success("DONE");
        break;
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

      Bitmap qrBitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);

      // Calculate the margin to center the QR code
      int paperWidth = 384;
      int marginLeft = (paperWidth - width) / 2;
      int marginTop = 1; // Adjust this value to reduce space at the top

      // Create a new bitmap with the desired size
      Bitmap finalBitmap = Bitmap.createBitmap(paperWidth, height + marginTop, Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(finalBitmap);
      canvas.drawColor(Color.WHITE); // Fill with white
      canvas.drawBitmap(qrBitmap, marginLeft, marginTop, null); // Draw QR code at the center

      return finalBitmap;

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
    barcodeDecoder.setDecodeCallback(barcodeEntity -> {
      if(barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS){
        events.success(barcodeEntity.getBarcodeData());
      }else{
        events.error("01", "BARCODE FAILURE", "BARCODE FAILURE");
      }
    });

//    mPrinter.setPrinterStatusCallBack(printerStatus -> {
//      switch (printerStatus) {
//        case PAPERFINISH:
//          events.success("PAPERFINISH");
//        case NORMAL:
//          events.success("NORMAL");
//        case LACKOFPAPER:
//          events.success("LACK OF PAPER");
//
//
//
//      }
//    });




}


  @Override
  public void onCancel(Object arguments) {

    barcodeDecoder.close();
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {

  }



  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity(){
  }


}
