package com.ht.swing;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.comm.NetPortListener;
import com.ht.entity.Devices;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.jna.TcpClient;
import com.ht.printer.PrinterListener;
import com.ht.repository.DevicesRepo;
import com.ht.utils.DateUtil;
import com.ht.utils.TestConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

/**
 * 2个panel(面板) , 切换
 * 20191104 新需求"
 * 1. 开工界面去掉个数;
 * 2. 将新建批次号带入到 测试界面;
 */


public class PanelsEOL extends JPanel implements ActionListener {
    private static final Log logger = LogFactory.getLog(PanelsEOL.class);
    public static JPanel mainPanel = new JPanel();
    ThreadLocal<String> eolStatus = ThreadLocal.withInitial(() -> "Reday");

    // Buttons
    JButton resetButton = new HTSSButton(UIConstant.RESET_BUTTON);
    JButton deviceManageButton = new HTSSButton(UIConstant.DEVICES_OPEN);
    JButton manualTestButton = new HTSSButton(UIConstant.MANUAL_TEST);
    JButton plcButton = new HTSSButton(UIConstant.PLC_OPEN);

    // Input Fields
    JTextField textFieldeolStatus = new HTSSInputField();  //
    JTextField visualPartNumber = new HTSSInputField();  //
    JTextField textFieldTemp = new HTSSInputField();  //
    JTextField textFieldMainPLCPort = new HTSSInputField();  //
    JTextField textFieldLaserPort = new HTSSInputField();
    JTextField textFieldResistorsID = new HTSSInputField();
    JTextField textFieldRt_R25 = new HTSSResultField();  // Rt
    JTextField textFieldRw_R16 = new HTSSResultField();  // Rw
    JTextField textFieldRntc_NTCRValue = new HTSSResultField();  // Rntc
    JTextField textFieldTemperature = new HTSSResultField(); //换算温度

    // Labels
    JLabel labelResultOne = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelResultTwo = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelQRCode = new HTSSLabel(UIConstant.LABEL_TO_GENERATE);

    JCheckBox checkBox01 = new JCheckBox("打印二维码");
    JRadioButton radioBtn01 = new JRadioButton("生产模式");
    JRadioButton radioBtn02 = new JRadioButton("自检模式");
    // JRadioButton radioBtn03 = new JRadioButton("调试模式");
    JRadioButton radioBtn04 = new JRadioButton(TestConstant.SVW);
    JRadioButton radioBtn05 = new JRadioButton(TestConstant.FAW);
    JRadioButton radioBtn06 = new JRadioButton("无需二维码");
    KeySightManager manager = new KeySightManager();

    // 网口
    NetPortListener mainPLCListener;
    PrinterListener printerListener;
    ServerSocket printSeverSocket = null;
    // Socket printSocket = null;
    private JTextArea mDataView = new JTextArea();

    private volatile boolean ready = true;

    public PanelsEOL() {
        initMainPanel();
        /*    this.initData();*/
        this.add(mainPanel);
        mainPanel.setVisible(true);

        resetButton.addActionListener(this);
        deviceManageButton.addActionListener(this);
        manualTestButton.addActionListener(this);
        plcButton.addActionListener(this);
    }

    public synchronized boolean getStatus() {
        return ready;
    }

    public synchronized void setStatus(boolean status) {
        this.ready = status;
    }

    private void initMainPanel() {
        GridBagLayout layout = new GridBagLayout();
        mainPanel.setLayout(layout);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbc = new GridBagConstraints(); //定义一个GridBagConstraints
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        layout.setConstraints(titlePanel, gbc);//设置组件

        /* *********** 初始化测试环境 *********** */
        JPanel devicesPanel = createDevicesPanel();
        mainPanel.add(devicesPanel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(devicesPanel, gbc);//设置组件

        JPanel jpanelFirst = createPanelFirst();
        mainPanel.add(jpanelFirst);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(jpanelFirst, gbc);//设置组件

        /* *********** 状态显示 ********** */
        JPanel jstatusPanel = new JPanel();
        jstatusPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        mDataView.setFocusable(false);
        mDataView.setFont(UIConstant.TEXT_FONT);
        mDataView.setLineWrap(true);
        JScrollPane jsp2 = new JScrollPane(mDataView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp2.setPreferredSize(new Dimension(420, 690));
        jstatusPanel.add(jsp2);
        mainPanel.add(jstatusPanel);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(jstatusPanel, gbc);//设置组件


        /* ************ 版权 ************ */
        JPanel lbPanel = new JPanel();
        JLabel proLabel = new JLabel("上海禾他汽车科技有限公司 版权所有 ©2019-2099");
        proLabel.setFont(UIConstant.COPYRIGHT_FONT);
        proLabel.setPreferredSize(new Dimension(UIConstant.SCREEN_WIDTH, 25));
        proLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lbPanel.add(proLabel);
        mainPanel.add(lbPanel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        layout.setConstraints(lbPanel, gbc);//设置组件

        // 初始化进入自检模式
        checkBox01.setSelected(false);
        checkBox01.setEnabled(true);
        manualTestButton.setEnabled(true);
        plcButton.setEnabled(false);
        visualPartNumber.setEnabled(false);
        visualPartNumber.setText("");
        textFieldResistorsID.setEnabled(true);
        radioBtn04.setVisible(true);
        radioBtn05.setVisible(true);
        radioBtn06.setVisible(true);
        radioBtn06.setSelected(true);
    }


    private JPanel createTitlePanel() {
        /* *********** 标题 *********** */
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());

        JLabel iconLabel = new JLabel();
        ImageIcon icon = new ImageIcon("libs\\htlogo.png");
        icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        iconLabel.setIcon(icon);
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel(UIConstant.APP_NAME);
        titleLabel.setPreferredSize(new Dimension(UIConstant.SCREEN_WIDTH - 100, 80));
        titleLabel.setFont(UIConstant.TITLE_FONT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel);

        return titlePanel;
    }

    private JPanel createPanelFirst() {
        JPanel jpanelFirst = new JPanel();
        GridBagLayout layoutFirst = new GridBagLayout();
        jpanelFirst.setLayout(layoutFirst);
        jpanelFirst.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbcFirst = new GridBagConstraints(); //定义一个GridBagConstraints
        gbcFirst.fill = GridBagConstraints.BOTH;
        gbcFirst.insets = new Insets(5, 5, 5, 5);

        /* *********** 传入信息区域 *********** */
        JPanel partDataPanel = createDataTransferInPanel();
        jpanelFirst.add(partDataPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(partDataPanel, gbcFirst);

        /* *********** 检测结果区域 *********** */
        JPanel testResultPanel = createTestResultPanel();
        jpanelFirst.add(testResultPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(testResultPanel, gbcFirst);

        /* *********** 二维码区域 *********** */
        JPanel qrPanel = createQRPanel();
        jpanelFirst.add(qrPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(qrPanel, gbcFirst);

        return jpanelFirst;
    }

    public JPanel createDataTransferInPanel() {
        JPanel partDataPanel = new JPanel();
        partDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "检测参数", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        partDataPanel.setPreferredSize(new Dimension(UIConstant.SCREEN_WIDTH, 220));
        GridLayout partDataLayout = new GridLayout(4, 2, 5, 5);
        partDataPanel.setLayout(partDataLayout);

        JPanel mJPanel2 = new JPanel();
        mJPanel2.setLayout(new FlowLayout());
        JLabel l5 = new HTSSLabel("环境温度（°C）：");
        l5.setHorizontalAlignment(SwingConstants.RIGHT);
        l5.setPreferredSize(new Dimension(120, 30));
        mJPanel2.add(l5);
        mJPanel2.add(textFieldTemp);
        textFieldTemp.setText(String.valueOf(UIConstant.TEST_TEMP));
        textFieldTemp.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(mJPanel2);

        partDataPanel.add(new JLabel());

        JPanel jrbPanel = new JPanel();
        // 创建两个单选按钮
        radioBtn01.setFont(UIConstant.TEXT_FONT);
        radioBtn02.setFont(UIConstant.TEXT_FONT);
        // radioBtn03.setFont(UIConstant.TEXT_FONT);

        // 创建按钮组，把两个单选按钮添加到该组
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(radioBtn01);
        btnGroup.add(radioBtn02);
        // btnGroup.add(radioBtn03);

        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changEvent) {
                AbstractButton aButton = (AbstractButton) changEvent.getSource();
                ButtonModel aModel = aButton.getModel();
                // if (aModel.isArmed()) return;
                if (aModel.isPressed()) {
                    if (radioBtn01.isSelected()) { // 生产模式
                        checkBox01.setSelected(true);
                        checkBox01.setEnabled(false);
                        manualTestButton.setEnabled(false);
                        plcButton.setEnabled(true);
                        visualPartNumber.setEnabled(false);
                        visualPartNumber.setText("");
                        textFieldResistorsID.setEnabled(false);
                        radioBtn04.setVisible(false);
                        radioBtn05.setVisible(false);
                        radioBtn06.setVisible(false);
                        radioBtn06.setSelected(true);
                    } else if (radioBtn02.isSelected()) { // 自检模式
                        checkBox01.setSelected(false);
                        checkBox01.setEnabled(true);
                        manualTestButton.setEnabled(true);
                        plcButton.setEnabled(false);
                        visualPartNumber.setEnabled(false);
                        visualPartNumber.setText("");
                        textFieldResistorsID.setEnabled(true);
                        radioBtn04.setVisible(true);
                        radioBtn05.setVisible(true);
                        radioBtn06.setVisible(true);
                        radioBtn06.setSelected(true);
                    }
                }
            }
        };

        radioBtn01.addChangeListener(changeListener);
        radioBtn02.addChangeListener(changeListener);
        // radioBtn03.addChangeListener(changeListener);

        // 设置第一个单选按钮选中
        radioBtn02.setSelected(true);

        jrbPanel.add(radioBtn02);
        jrbPanel.add(radioBtn01);
        // jrbPanel.add(radioBtn03);
        partDataPanel.add(jrbPanel);

        radioBtn04.setFont(UIConstant.COPYRIGHT_FONT);
        radioBtn05.setFont(UIConstant.COPYRIGHT_FONT);
        radioBtn06.setFont(UIConstant.COPYRIGHT_FONT);

        ButtonGroup btnGroup1 = new ButtonGroup();
        btnGroup1.add(radioBtn04);
        btnGroup1.add(radioBtn05);
        btnGroup1.add(radioBtn06);
        JPanel jrbPanel1 = new JPanel();
        radioBtn06.setSelected(true);

        jrbPanel1.add(radioBtn04);
        jrbPanel1.add(radioBtn05);
        jrbPanel1.add(radioBtn06);
        partDataPanel.add(jrbPanel1);

        /*----- 虚拟零件号 ----*/
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        // panel1.add(Box.createRigidArea(new Dimension(15, 15)));
        JLabel label1 = new HTSSLabel("虚拟零件号：");
        label1.setHorizontalAlignment(SwingConstants.RIGHT);
        label1.setPreferredSize(new Dimension(120, 30));
        panel1.add(label1);
        panel1.add(visualPartNumber);
        visualPartNumber.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel1);

        JPanel jcbPanel = new JPanel();

        jcbPanel.add(checkBox01);
        partDataPanel.add(jcbPanel);



        /*----- 分流器二维码 ----*/
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JLabel label2 = new HTSSLabel("分流器二维码：");
        label2.setHorizontalAlignment(SwingConstants.RIGHT);
        label2.setPreferredSize(new Dimension(120, 30));
        panel2.add(label2);
        panel2.add(textFieldResistorsID);
        textFieldResistorsID.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel2);

        JPanel jmtPanel = new JPanel();
        manualTestButton.setPreferredSize(new Dimension(150, 30));
        jmtPanel.add(manualTestButton);
        partDataPanel.add(jmtPanel);

        return partDataPanel;
    }

    public void showTransferInData(String vPartNumber, String qr) {
        visualPartNumber.setText(vPartNumber);
        textFieldResistorsID.setText(qr);
    }

    private JPanel createTestResultPanel() {
        JPanel testResultPanel = new JPanel();
        GridLayout gridLayoutL = new GridLayout(5, 4, 5, 5);
        testResultPanel.setLayout(gridLayoutL);
        testResultPanel.setBackground(UIConstant.BGCOLOR_BLUE);
        testResultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "检测结果", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.AREA_FONT));
        testResultPanel.setPreferredSize(new Dimension(UIConstant.SCREEN_WIDTH, 280));

        //row 1
        testResultPanel.add(new JLabel());
        testResultPanel.add(new HTSSLabel("下限 LL"));
        testResultPanel.add(new HTSSLabel("上限 UL"));
        testResultPanel.add(new HTSSLabel("测试结果"));

        //row 2
        testResultPanel.add(new HTSSLabel("电阻Rt"));
        testResultPanel.add(new HTSSLabel("78.75"));
        testResultPanel.add(new HTSSLabel("71.25"));
        JPanel tbPanel1 = new JPanel();
        tbPanel1.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldRt_R25.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        tbPanel1.add(textFieldRt_R25);
        testResultPanel.add(tbPanel1);

        //row 3
        testResultPanel.add(new HTSSLabel("电阻Rw"));
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        JPanel tbPanel2 = new JPanel();
        tbPanel2.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldRw_R16.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        tbPanel2.add(textFieldRw_R16);
        testResultPanel.add(tbPanel2);

        //row 4
        testResultPanel.add(new HTSSLabel("电阻Rntc"));
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        JPanel tbPanel3 = new JPanel();
        tbPanel3.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldRntc_NTCRValue.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        tbPanel3.add(textFieldRntc_NTCRValue);
        testResultPanel.add(tbPanel3);

        //row 5
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        testResultPanel.add(new HTSSLabel("换算温度"));
        JPanel tbPanel4 = new JPanel();
        tbPanel4.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldTemperature.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        tbPanel4.add(textFieldTemperature);
        testResultPanel.add(tbPanel4);

        return testResultPanel;
    }

    private JPanel createQRPanel() {
        JPanel qrPanel = new JPanel();
        qrPanel.setPreferredSize(new Dimension(450, 180));
        GridBagLayout rLayout = new GridBagLayout();
        qrPanel.setLayout(rLayout);
        qrPanel.setBackground(UIConstant.BGCOLOR_GRAY);
        qrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "检测判定", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.AREA_FONT));

        GridBagConstraints gbcr = new GridBagConstraints(); //定义一个GridBagConstraints
        gbcr.fill = GridBagConstraints.BOTH;
        gbcr.insets = new Insets(10, 5, 10, 5);

        JLabel dl1 = new HTSSLabel("分流器电阻检测结果");
        qrPanel.add(dl1);
        qrPanel.add(labelResultOne);
        labelResultOne.setFont(UIConstant.TEXT_FONT);
        JLabel dl2 = new HTSSLabel("NTC电阻检测结果");
        qrPanel.add(dl2);
        qrPanel.add(labelResultTwo);
        labelResultTwo.setPreferredSize(new Dimension(200, 30));
        labelResultTwo.setFont(UIConstant.TEXT_FONT);

        JLabel dlr = new HTSSLabel("检测成功，将自动分配二维码");
        qrPanel.add(dlr);
        qrPanel.add(labelQRCode);
        labelQRCode.setFont(UIConstant.TEXT_FONT);

        gbcr.weightx = 0.5;
        gbcr.gridwidth = 1;
        rLayout.setConstraints(dl1, gbcr);
        gbcr.gridwidth = 0;
        rLayout.setConstraints(labelResultOne, gbcr);
        gbcr.gridwidth = 1;
        rLayout.setConstraints(dl2, gbcr);
        gbcr.gridwidth = 0;
        rLayout.setConstraints(labelResultTwo, gbcr);
        gbcr.gridwidth = 1;
        rLayout.setConstraints(dl1, gbcr);
        gbcr.gridwidth = 0;
        rLayout.setConstraints(labelQRCode, gbcr);

        return qrPanel;
    }

    private JPanel createDevicesPanel() {
        JPanel devicesPanel = new JPanel();
        devicesPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        GridLayout gridLayoutL = new GridLayout(14, 3, 5, 5);
        devicesPanel.setLayout(gridLayoutL);
        devicesPanel.setPreferredSize(new Dimension(420, 520));

        /*devicesPanel.add(new JLabel());
        devicesPanel.add(new JLabel());
        devicesPanel.add(new JLabel());*/

        List<Devices> deviceList = null;
        try {
            DevicesRepo repo = SpringContext.getBean(DevicesRepo.class);
            deviceList = repo.findAll();
        } catch (Exception e) {
            logger.error(e);
        }

        JLabel l1 = new HTSSLabel("设备");
        JPanel p1 = new JPanel(new BorderLayout());
        l1.setFont(UIConstant.TEXT_FONT);
        p1.setBackground(UIConstant.BGCOLOR_GRAY);
        p1.add(l1);
        devicesPanel.add(p1);
        JLabel l2 = new HTSSLabel("地址");
        JPanel p2 = new JPanel(new BorderLayout());
        l2.setFont(UIConstant.TEXT_FONT);
        p2.setBackground(UIConstant.BGCOLOR_GRAY);
        p2.add(l2);
        devicesPanel.add(p2);
        JLabel l3 = new HTSSLabel("端口");
        JPanel p3 = new JPanel(new BorderLayout());
        l3.setFont(UIConstant.TEXT_FONT);
        p3.setBackground(UIConstant.BGCOLOR_GRAY);
        p3.add(l3);
        devicesPanel.add(p3);

        devicesPanel.add(new HTSSLabel("本机"));
        String localhost = "localhost";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ip = inetAddress.toString();
            localhost = StringUtils.substringAfterLast(ip, "/");
            devicesPanel.add(new HTSSLabel(localhost));
        } catch (Exception e) {
            devicesPanel.add(new HTSSLabel(localhost));
        }
        devicesPanel.add(new JLabel());
        // devicesPanel.add(new HTSSStatusLabel());

        String finalLocalhost = localhost;
        deviceList.forEach(item -> {
            devicesPanel.add(new HTSSLabel(item.getDevice()));

            if ("localhost".equals(item.getIpAddress())) {
                devicesPanel.add(new HTSSLabel(finalLocalhost));
            } else {
                devicesPanel.add(new HTSSLabel(item.getIpAddress()));
            }

            if ("MainPLC".equals(item.getDevice())) {
                JPanel jp1 = new JPanel(new GridBagLayout());
                textFieldMainPLCPort.setPreferredSize(UIConstant.INPUT_DIMENSION);
                textFieldMainPLCPort.setHorizontalAlignment(0);
                textFieldMainPLCPort.setText(item.getPortNumber());
                jp1.add(textFieldMainPLCPort);
                devicesPanel.add(jp1);
            } else if ("LaserMarking".equals(item.getDevice())) {
                JPanel jp2 = new JPanel(new GridBagLayout());
                textFieldLaserPort.setPreferredSize(UIConstant.INPUT_DIMENSION);
                textFieldLaserPort.setHorizontalAlignment(0);
                textFieldLaserPort.setText(item.getPortNumber());
                jp2.add(textFieldLaserPort);
                devicesPanel.add(jp2);
            } else {
                devicesPanel.add(new HTSSLabel(item.getPortNumber()));
            }
        });

        devicesPanel.add(new JLabel());
        devicesPanel.add(new JLabel());
        devicesPanel.add(new JLabel());

        devicesPanel.add(new JLabel());

        deviceManageButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panel = new JPanel();
        b1Panel.add(deviceManageButton);
        devicesPanel.add(b1Panel);

        devicesPanel.add(new JLabel());

        devicesPanel.add(new JLabel());
        plcButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panelp = new JPanel();
        b1Panelp.add(plcButton);
        devicesPanel.add(b1Panelp);
        devicesPanel.add(new JLabel());

        devicesPanel.add(new JLabel());

        resetButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panelr = new JPanel();
        b1Panelr.add(resetButton);
        devicesPanel.add(b1Panelr);

        devicesPanel.add(new JLabel());

        return devicesPanel;
    }



    /**
     * 按钮监听事件
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        final String actionCommand = e.getActionCommand();

        if (actionCommand.equals(UIConstant.RESET_BUTTON)) {
            // TODO: 应该先停止所有测试，然后再清空相应字段，重置设备和按钮

            mDataView.setText(UIConstant.EMPTY_STRING);
            visualPartNumber.setText(UIConstant.EMPTY_STRING);
            textFieldResistorsID.setText(UIConstant.EMPTY_STRING);

            frameReset();
        } else if (actionCommand.equals(UIConstant.DEVICES_OPEN)) {
            frameReset();

            String laserPort = textFieldLaserPort.getText();
            try {
                Integer i = Integer.parseInt(laserPort);
            } catch (Exception exp) {
                mDataView.append(DateUtil.formatInfo("激光打码机通信端口" + laserPort + "输入有误，请重新输入！"));
                return;
            }

            logger.info("连接测试设备......");
            // 初始化电源和测试设备
            manager.initDevices();
            mDataView.append(DateUtil.formatInfo("已连接测试设备......"));

            // 连接电源
            TcpClient client = new TcpClient();
            client.remoteCtl(true);
            mDataView.append(DateUtil.formatInfo("电源已接通......" + getStatus()));

            // 打开激光打码机通信端口
            try {
                printSeverSocket = new ServerSocket(Integer.parseInt(textFieldLaserPort.getText()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            printerListener = new PrinterListener(printSeverSocket);
            printerListener.setStatus(mDataView);
            printerListener.start();
            mDataView.append(DateUtil.formatInfo("激光打码机通信端口已打开，可以接收数据......" + getStatus()));

            // 按钮设为"结束测试"
            deviceManageButton.setText(UIConstant.DEVICES_CLOSE);
        } else if (actionCommand.equals(UIConstant.DEVICES_CLOSE)) {
            // 关闭激光打码机通信端口
            printerListener.closePort();
            mDataView.append(DateUtil.formatInfo("激光打码机通信端口已关闭......" + getStatus()));

            // 关闭电源
            TcpClient client = new TcpClient();
            client.remoteCtl(false);
            mDataView.append(DateUtil.formatInfo("电源已断开......" + getStatus()));

            // 关闭测试设备
            manager.closeDivices();
            mDataView.append(DateUtil.formatInfo("测试设备已断开......" + getStatus()));

            // 按钮设为"开始测试"
            deviceManageButton.setText(UIConstant.DEVICES_OPEN);
        } else if (actionCommand.equals(UIConstant.PLC_OPEN)) {
            String mainPort = textFieldMainPLCPort.getText();
            try {
                Integer i = Integer.parseInt(mainPort);
            } catch (Exception exp) {
                mDataView.append(DateUtil.formatInfo("主控通信端口" + mainPort + "输入有误，请重新输入！"));
                return;
            }

            // 打开主控PLC通信端口
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("textFieldTemp", textFieldTemp);
            jsonObject.put("visualPartNumber", visualPartNumber);
            jsonObject.put("textFieldResistorsID", textFieldResistorsID);
            jsonObject.put("textFieldRt_R25", textFieldRt_R25);
            jsonObject.put("textFieldRw_R16", textFieldRw_R16);
            jsonObject.put("textFieldRntc_NTCRValue", textFieldRntc_NTCRValue);
            jsonObject.put("textFieldTemperature", textFieldTemperature);
            jsonObject.put("labelResultOne", labelResultOne);
            jsonObject.put("labelResultTwo", labelResultTwo);
            jsonObject.put("labelQRCode", labelQRCode);
            jsonObject.put("mDataView", mDataView);
            jsonObject.put("eolStatus", eolStatus);
            mainPLCListener = new NetPortListener(Integer.parseInt(textFieldMainPLCPort.getText()), jsonObject, printSeverSocket,manager);
            mainPLCListener.start();
            mDataView.append(DateUtil.formatInfo("主控PLC通信端口已打开，可以接收数据......" + getStatus()));
        } else if (actionCommand.equals(UIConstant.PLC_CLOSE)) {
            // 关闭主控PLC通信端口
            mainPLCListener.closePort();
            mDataView.append(DateUtil.formatInfo("主控PLC通信端口已关闭......" + getStatus()));

            // 按钮设为"开始测试"
            deviceManageButton.setText(UIConstant.PLC_OPEN);
        } else if (actionCommand.equals(UIConstant.MANUAL_TEST)) {
            if (UIConstant.DEVICES_OPEN.equals(deviceManageButton.getText())) {
                JOptionPane.showMessageDialog(this, "端口未开！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String resistorID = textFieldResistorsID.getText();
            if ("".equals(resistorID)) {
                mDataView.append(DateUtil.formatInfo("分流器二维码输入有误，请重新输入！"));
                return;
            }

            String cirTemp = textFieldTemp.getText();
            try {
                double d = Double.parseDouble(cirTemp);
                if (d < 15) {
                    mDataView.append(DateUtil.formatInfo("环境温度" + cirTemp + "<15°C，过低！"));
                    return;
                } else if (d > 35) {
                    mDataView.append(DateUtil.formatInfo("环境温度" + cirTemp + ">35°C，过高！"));
                    return;
                }
            } catch (Exception exp) {
                mDataView.append(DateUtil.formatInfo("环境温度值输入错误！"));
                return;
            }

            visualPartNumber.setText(DateUtil.createVitualPartNumber());

            logger.info("测试开始...");

            // 开电源
            TcpClient client = new TcpClient();
            mDataView.append(new Date() + " - 测试开始 ...\r\n");
            client.open();

            String qrcode = "NA";
            if (radioBtn04.isSelected()) qrcode = TestConstant.SVW;
            else if (radioBtn05.isSelected()) qrcode = TestConstant.FAW;

            ProRecords result = manager.testThePart(visualPartNumber.getText(), Double.parseDouble(cirTemp), resistorID, qrcode, mDataView, null, null);

            // 关电源
            client.close();
            mDataView.append(new Date() + " - 测试结束 ...\r\n");

            NumberFormat ddf = NumberFormat.getNumberInstance();
            ddf.setMaximumFractionDigits(4);
            textFieldRt_R25.setText(ddf.format(result.getR25()));
            textFieldRw_R16.setText(ddf.format(result.getR16()));
            textFieldRntc_NTCRValue.setText(ddf.format(result.getRntc()));
            textFieldTemperature.setText(ddf.format(result.getTntc()));

            if ((result.getR25() < 78.25 && result.getR25() > 71.75)
                    && (result.getR16() < 78.25 && result.getR16() > 71.75)) {
                labelResultOne.setText("合格");
                labelResultOne.setForeground(Color.green);
            } else {
                labelResultOne.setText("不合格");
                labelResultOne.setForeground(Color.red);
            }

            if (Math.abs(result.getTntc() - Double.parseDouble(cirTemp)) <= 3) {
                labelResultTwo.setText("合格");
                labelResultTwo.setForeground(Color.green);
            } else {
                labelResultTwo.setText("不合格");
                labelResultTwo.setForeground(Color.red);
            }

            if (checkBox01.isSelected()) {
                printerListener.sendMessage(qrcode);
            }

            logger.info("result testResultVo is: " + result);
        } else {
            logger.error("something wrong boy..." + actionCommand);
        }
    }

    private void frameReset() {
        logger.debug("信息重置...");

        textFieldRt_R25.setText(UIConstant.EMPTY_STRING);
        textFieldRw_R16.setText(UIConstant.EMPTY_STRING);
        textFieldRntc_NTCRValue.setText(UIConstant.EMPTY_STRING);
        textFieldTemperature.setText(UIConstant.EMPTY_STRING);

        labelResultOne.setText(UIConstant.LABEL_TO_TEST);
        labelResultOne.setForeground(Color.black);
        labelResultTwo.setText(UIConstant.LABEL_TO_TEST);
        labelResultTwo.setForeground(Color.black);
        labelQRCode.setText(UIConstant.LABEL_TO_GENERATE);
    }
}
