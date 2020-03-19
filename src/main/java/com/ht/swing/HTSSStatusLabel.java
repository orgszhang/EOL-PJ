package com.ht.swing;

import javax.swing.*;
import java.awt.*;

public class HTSSStatusLabel extends HTSSLabel {
    public HTSSStatusLabel() {
        super("--");
        this.setForeground(Color.black);
    }

    public void setOKStatus() {
        this.setText("OK");
        this.setForeground(Color.GREEN);
    }

    public void setErrorStatus() {
        this.setText("ERR");
        this.setForeground(Color.RED);
    }
}
