package com.ht.swing;

import javax.swing.*;
import java.awt.*;

public class HTSSResultField extends JTextField {
    public HTSSResultField() {
        Font titleFont = new Font("微软雅黑", Font.BOLD, 12);
        this.setFont(titleFont);
        this.setHorizontalAlignment(JTextField.CENTER);
        this.setEditable(false);  // Rt
    }
}
