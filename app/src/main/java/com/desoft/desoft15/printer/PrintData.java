package com.desoft.desoft15.printer;

public class PrintData {
    private final PrintType type;
    private final String texto;
    private final PrinterAlignment alignment;
    private final PrinterTextStyle size;
    private final int feedline;
    private final boolean bold;

    public PrintData(PrintType type, String texto, PrinterAlignment alignment, PrinterTextStyle size, int feedline, boolean bold) {
        this.type = type;
        this.texto = texto;
        this.alignment = alignment;
        this.size = size;
        this.feedline = feedline;
        this.bold = bold;
    }

    public PrintType getType() {
        return type;
    }

    public String getTexto() {
        return texto;
    }

    public PrinterAlignment getAlignment() {
        return alignment;
    }

    public PrinterTextStyle getSize() {
        return size;
    }

    public int getFeedline() {
        return feedline;
    }

    public boolean isBold() {
        return bold;
    }
}

