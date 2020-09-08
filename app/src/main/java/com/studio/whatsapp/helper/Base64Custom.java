package com.studio.whatsapp.helper;

import android.util.Base64;

public class Base64Custom {

    public static String codificar(String txt) {
        return Base64.encodeToString(txt.getBytes(), Base64.DEFAULT).replaceAll("\\n|\\r", "");
    }

    public static String decodificar(String txt) {
        return new String(Base64.decode(txt, Base64.DEFAULT));
    }

}
