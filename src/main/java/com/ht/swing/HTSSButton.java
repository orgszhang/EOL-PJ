package com.ht.swing;

import javax.swing.*;
import java.awt.*;

public class HTSSButton extends JButton {
    public HTSSButton(String text) {
        Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
        this.setText(text);
        this.setFont(titleFont);
        this.setHorizontalAlignment(JLabel.CENTER);
    }
}
