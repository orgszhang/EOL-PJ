package com.ht.swing;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.comm.NetPortListener;
import com.ht.dc.TcpClient;
import com.ht.entity.Devices;
import com.ht.entity.EolStatus;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.jna.TestThread;
import com.ht.printer.PrinterListener;
import com.ht.repository.DevicesRepo;
import com.ht.utils.DateUtil;
import com.ht.utils.TestConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.StringUtil;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    // Buttons
    JButton resetButton = new HTSSButton(UIConstant.RESET_BUTTON);
    JButton deviceManageButton = new HTSSButton(UIConstant.DEVICES_OPEN);
    JButton manualTestButton = new HTSSButton(UIConstant.MANUAL_TEST);
    JButton plcButton = new HTSSButton(UIConstant.PLC_OPEN);

    // Input Fields
    // JTextField textFieldeolStatus = new HTSSInputField();  //
    JTextField visualPartNumber = new HTSSInputField();  //
    JTextField textFieldTemp = new HTSSInputField();  //
    // JTextField textFieldMainPLCPort = new HTSSInputField();  //
    // JTextField textFieldLaserPort = new HTSSInputField();
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
    // ServerSocket printSeverSocket = null;
    // String ipMainPLC;

    private JTextArea mDataView = new JTextArea();
    private volatile boolean ready = true;

    public PanelsEOL() {
        initMainPanel();
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
        mDataView.setFont(UIConstant.COPYRIGHT_FONT);
        mDataView.setLineWrap(true);
        JScrollPane jsp2 = new JScrollPane(mDataView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp2.setPreferredSize(new Dimension(UIConstant.SIDE_WIDTH, 690));
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
        proLabel.setPreferredSize(new Dimension(UIConstant.MAIN_WIDTH, 25));
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
        titleLabel.setPreferredSize(new Dimension(UIConstant.MAIN_WIDTH - 100, 80));
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
        partDataPanel.setPreferredSize(new Dimension(UIConstant.MAIN_WIDTH, 220));
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

        // 设置第一个单选按钮选中
        radioBtn02.setSelected(true);

        jrbPanel.add(radioBtn02);
        jrbPanel.add(radioBtn01);
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
        testResultPanel.setPreferredSize(new Dimension(UIConstant.MAIN_WIDTH, 280));

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
        GridLayout gridLayoutL = new GridLayout(10, 3, 5, 5);
        devicesPanel.setLayout(gridLayoutL);
        devicesPanel.setPreferredSize(new Dimension(UIConstant.SIDE_WIDTH, 520));

        JLabel l1 = new HTSSLabel("设备");
        JPanel p1 = new JPanel(new BorderLayout());
        l1.setFont(UIConstant.TEXT_FONT);
        l1.setForeground(UIConstant.BGCOLOR_BLUE);
        p1.add(l1);
        devicesPanel.add(p1);
        JLabel l2 = new HTSSLabel("地址");
        JPanel p2 = new JPanel(new BorderLayout());
        l2.setFont(UIConstant.TEXT_FONT);
        l2.setForeground(UIConstant.BGCOLOR_BLUE);
        p2.add(l2);
        devicesPanel.add(p2);
        JLabel l3 = new HTSSLabel("端口");
        JPanel p3 = new JPanel(new BorderLayout());
        l3.setFont(UIConstant.TEXT_FONT);
        l3.setForeground(UIConstant.BGCOLOR_BLUE);
        p3.add(l3);
        devicesPanel.add(p3);

        devicesPanel.add(new HTSSLabel("主控"));
        devicesPanel.add(new HTSSLabel("192.168.10.80"));
        devicesPanel.add(new HTSSLabel("8088"));

        devicesPanel.add(new HTSSLabel("-"));
        devicesPanel.add(new HTSSLabel("-"));
        devicesPanel.add(new HTSSLabel("-"));

        devicesPanel.add(new HTSSLabel("电源"));
        devicesPanel.add(new HTSSLabel("169.254.210.22"));
        devicesPanel.add(new HTSSLabel("5025"));

        devicesPanel.add(new HTSSLabel("测试设备"));
        devicesPanel.add(new HTSSLabel("169.254.210.X"));
        devicesPanel.add(new HTSSLabel("-"));

        devicesPanel.add(new HTSSLabel("打码机"));
        devicesPanel.add(new HTSSLabel("169.254.210.66"));
        devicesPanel.add(new HTSSLabel("8082"));

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

            // 初始化电源和测试设备
            logger.info("连接测试设备......");
            manager.initDevices(mDataView);
            mDataView.append(DateUtil.formatInfo("测试设备已连接......"));

            // 连接电源
            TcpClient client = new TcpClient();
            client.remoteCtl(true);
            mDataView.append(DateUtil.formatInfo("电源已接通......"));

            // 打开激光打码机通信端口
            printerListener = PrinterListener.getInstance(mDataView);
            mDataView.append(DateUtil.formatInfo("打码机通信端口已打开......"));

            // 按钮设为"结束测试"
            deviceManageButton.setText(UIConstant.DEVICES_CLOSE);
        } else if (actionCommand.equals(UIConstant.DEVICES_CLOSE)) {
            // 关闭激光打码机通信端口
            printerListener.closePort();
            mDataView.append(DateUtil.formatInfo("打码机通信端口已关闭......"));

            // 关闭电源
            TcpClient client = new TcpClient();
            client.remoteCtl(false);
            mDataView.append(DateUtil.formatInfo("电源已断开......"));

            // 关闭测试设备
            manager.closeDivices();
            mDataView.append(DateUtil.formatInfo("测试设备已断开......"));

            // 按钮设为"开始测试"
            deviceManageButton.setText(UIConstant.DEVICES_OPEN);
        } else if (actionCommand.equals(UIConstant.PLC_OPEN)) {
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

            mainPLCListener = new NetPortListener(manager, jsonObject);
            // mainPLCListener.start();
            mDataView.append(DateUtil.formatInfo("主控PLC通信端口已打开....."));

            // 按钮设为"开始测试"
            plcButton.setText(UIConstant.PLC_CLOSE);
        } else if (actionCommand.equals(UIConstant.PLC_CLOSE)) {
            // 关闭主控PLC通信端口
            mDataView.append(DateUtil.formatInfo("主控PLC通信端口已关闭......"));
            try {
                mainPLCListener.close();
            } catch (Exception exp) {
                logger.error("主控PLC通信端口关闭错误：" + exp);
            }

            // 按钮设为"开始测试"
            plcButton.setText(UIConstant.PLC_OPEN);
        } else if (actionCommand.equals(UIConstant.MANUAL_TEST)) {
            if (UIConstant.DEVICES_OPEN.equals(deviceManageButton.getText())) {
                JOptionPane.showMessageDialog(this, "端口未开！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String resistorID = textFieldResistorsID.getText();
            if (StringUtils.isEmpty(resistorID)) {
                JOptionPane.showMessageDialog(this, "分流器二维码输入有误，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String cirTemp = textFieldTemp.getText();
            try {
                double d = Double.parseDouble(cirTemp);
                if (d < 15) {
                    JOptionPane.showMessageDialog(this, "环境温度" + cirTemp + "<15°C，过低！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if (d > 35) {
                    JOptionPane.showMessageDialog(this, "环境温度" + cirTemp + ">35°C，过高！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception exp) {
                JOptionPane.showMessageDialog(this, "环境温度值输入错误！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int flag = 0;
            if (radioBtn04.isSelected()) {
                flag = 1;
            } else if (radioBtn05.isSelected()) {
                flag = 2;
            }
            visualPartNumber.setText(DateUtil.createVitualPartNumber(flag));

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

            logger.info("测试开始...[" + visualPartNumber.getText() + ", " + textFieldResistorsID.getText() + "]");
            boolean print = false;
            if (checkBox01.isSelected()) {
                print = true;
            }
            TestThread thread = new TestThread(manager, jsonObject, /*null,*/ false, print);
            thread.start();
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
