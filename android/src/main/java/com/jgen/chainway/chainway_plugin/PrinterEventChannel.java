package com.jgen.chainway.chainway_plugin;

import com.rscja.deviceapi.Printer;

import io.flutter.plugin.common.EventChannel;

class PrinterEventChannel implements EventChannel.StreamHandler {
    private final EventChannel eventChannel;

    private Printer mPrinter;

    PrinterEventChannel(EventChannel eventChannel, Printer printer) {
        this.eventChannel = eventChannel;
this.mPrinter = printer;
        this.eventChannel.setStreamHandler(this);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        mPrinter.setPrinterStatusCallBack(printerStatus -> events.success(printerStatus.name()));

    }

    @Override
    public void onCancel(Object arguments) {
        // Stop sending this type of event

    }
}