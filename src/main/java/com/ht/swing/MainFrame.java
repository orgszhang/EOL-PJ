package com.ht.swing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import javax.swing.*;
import java.awt.*;




/**
 * 开工界面
 * <p>
 * https://coolsymbol.com/
 */

public class MainFrame extends JFrame {
    private static final Log logger = LogFactory.getLog(MainFrame.class);

    private JFrame mainFrame;

    public MainFrame() {
        logger.info("Init Main Frame ... ");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mainFrame = new JFrame(UIConstant.APP_NAME);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(screenSize);

        Image image = null;
        try {
            image = Toolkit.getDefaultToolkit().getImage(".\\libs\\htlogo.png");
        } catch (Exception e) {
            logger.error("Cannot find logo image ...", e);
        }
        mainFrame.setIconImage(image);

        PanelsEOL pvt = new PanelsEOL();
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(pvt);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        stopWatch.stop();
        logger.info("Init KickOff down in " + stopWatch.getTotalTimeMillis() / 1000.0 + " seconds.");
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}
