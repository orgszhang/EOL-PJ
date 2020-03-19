package com.ht.swing;

import com.ht.Socket.EolServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;


/**
 * 开工界面
 * <p>
 * https://coolsymbol.com/
 */

public class MainFrame {
    private static final Log logger = LogFactory.getLog(MainFrame.class);

    private JFrame mainFrame;

    public MainFrame() {
        logger.info("Init Main Frame ... ");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mainFrame = new JFrame(UIConstant.APP_NAME);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(screenSize);

        try {
            InputStream logoImageInputStream = MainFrame.class.getResourceAsStream("/htlogo.png");
            mainFrame.setIconImage(ImageIO.read(logoImageInputStream));
        } catch (Exception e) {
            logger.error("Cannot find logo image ...", e);
        }

        // PanelsVDB pvt = new PanelsVDB();
        // PanelsV3 pvt = new PanelsV3();
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
