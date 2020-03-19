package com.ht.swing;

import javax.swing.*;
import java.awt.*;

public class HTSSLabel extends JLabel {

    public HTSSLabel(String text) {
        Font titleFont = new Font("微软雅黑", Font.BOLD, 12);
        this.setText(text);
        this.setFont(titleFont);
        this.setHorizontalAlignment(JLabel.CENTER);
    }
}
