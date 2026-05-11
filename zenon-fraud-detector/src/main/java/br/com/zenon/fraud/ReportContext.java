package br.com.zenon.fraud;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public record ReportContext(ResourceBundle bundle, NumberFormat currency, NumberFormat integer) {
    public static ReportContext of(Locale locale) {
        return new ReportContext(
                ResourceBundle.getBundle("report", locale),
                NumberFormat.getCurrencyInstance(locale),
                NumberFormat.getIntegerInstance(locale)
        );
    }
}
