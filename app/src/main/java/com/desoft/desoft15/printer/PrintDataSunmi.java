package com.desoft.desoft15.printer;

import java.util.List;

public class PrintDataSunmi {
    private final PrintType type;
    private final String texto;
    private final int alignment;
    private final double size;
    private final int feedline;
    private final boolean bold;
    private final boolean underline;
    private final List<String> columnText;
    private final List<Integer> columnAlign;

    public PrintDataSunmi(PrintType type, String texto, int alignment, double size, int feedline, boolean bold, boolean underline, List<String> columnText, List<Integer> columnAlign) {
        this.type = type;
        this.texto = texto;
        this.alignment = alignment;
        this.size = size;
        this.feedline = feedline;
        this.bold = bold;
        this.underline = underline;
        this.columnText = columnText;
        this.columnAlign = columnAlign;
    }

    public PrintType getType() {
        return type;
    }

    public String getTexto() {
        return texto;
    }

    public int getAlignment() {
        return alignment;
    }

    public double getSize() {
        return size;
    }

    public int getFeedline() {
        return feedline;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isUnderline() {
        return underline;
    }

    public List<String> getColumnText() {
        return columnText;
    }

    public List<Integer> getColumnAlign() {
        return columnAlign;
    }
}
