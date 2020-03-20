package com.ht.swing;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.comm.NetPortListener;
import com.ht.entity.Devices;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.repository.DevicesRepo;
import com.ht.utils.TestConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
    ThreadLocal<String> eolStatus = ThreadLocal.withInitial(() -> "BUSY");
    KeySightManager manager = new KeySightManager();

    // Buttons
    JButton resetButton = new HTSSButton(UIConstant.RESET_BUTTON);
    JButton testStartButton = new HTSSButton(UIConstant.NETPORT_OPEN);
    JButton deviceButton = new HTSSButton(UIConstant.RESET_BUTTON);

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
    JTextField textFieldTemperature = new HTSSResultField();

    // Labels
    JLabel labelResultOne = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelResultTwo = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelQRCode = new HTSSLabel(UIConstant.LABEL_TO_GENERATE);
    // 网口
    NetPortListener mainPLCListener;
    NetPortListener laserListener;

    // 串口
    private JTextArea mDataView = new JTextArea();

    private volatile boolean ready = true;

    public PanelsEOL() {
        initMainPanel();
        /*    this.initData();*/
        this.add(mainPanel);
        mainPanel.setVisible(true);

        resetButton.addActionListener(this);
        testStartButton.addActionListener(this);
        deviceButton.addActionListener(this);
        /*     serialActionListener();*/
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
        layout.setConstraints(titlePanel, gbc);//设置组件

        JTabbedPane jTabbedpane = new JTabbedPane();
        JPanel jpanelFirst = createPanelFirst();
        jTabbedpane.addTab("EOL测试", null, jpanelFirst, "first");

        JPanel jpanelSecond = createPanelSecond();
        jTabbedpane.addTab("测试日志", null, jpanelSecond, "second");

        JPanel jpanelThird = createPanelThird();
        jTabbedpane.addTab("设备管理", null, jpanelThird, "third");

        jTabbedpane.setFont(UIConstant.TEXT_FONT);
        mainPanel.add(jTabbedpane);
        gbc.gridx = 0;
        layout.setConstraints(jTabbedpane, gbc);//设置组件

        /************* 版权 *************/
        JPanel lbPanel = new JPanel();
        JLabel proLabel = new JLabel("上海禾他汽车科技有限公司 版权所有 ©2019-2099");
        proLabel.setFont(UIConstant.COPYRIGHT_FONT);
        proLabel.setPreferredSize(new Dimension(800, 25));
        proLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lbPanel.add(proLabel);
        mainPanel.add(lbPanel);
        gbc.gridx = 0;
        layout.setConstraints(lbPanel, gbc);//设置组件
    }

    private JPanel createTitlePanel() {
        /************ 标题 ************/
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());

        JLabel iconLabel = new JLabel();
        ImageIcon icon = new ImageIcon("libs\\htlogo.png");
        icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        iconLabel.setIcon(icon);
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel(UIConstant.APP_NAME);
        titleLabel.setPreferredSize(new Dimension(740, 80));
        titleLabel.setFont(UIConstant.TITLE_FONT);
        titleLabel.setHorizontalAlignment(0);
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

        /************ 传入信息区域 ************/
        JPanel partDataPanel = createDataTransferInPanel();
        jpanelFirst.add(partDataPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(partDataPanel, gbcFirst);

        /************ 检测结果区域 ************/
        JPanel testResultPanel = createTestResultPanel();
        jpanelFirst.add(testResultPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(testResultPanel, gbcFirst);

        /************ 二维码区域 ************/
        JPanel qrPanel = createQRPanel();
        jpanelFirst.add(qrPanel);
        gbcFirst.gridx = 0;
        layoutFirst.setConstraints(qrPanel, gbcFirst);

        return jpanelFirst;
    }

    public JPanel createDataTransferInPanel() {
        JPanel partDataPanel = new JPanel();
        partDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "传入信息", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        partDataPanel.setPreferredSize(new Dimension(800, 80));
        GridLayout partDataLayout = new GridLayout(1, 2, 5, 5);
        partDataPanel.setLayout(partDataLayout);

        /*----- 虚拟零件号 ----*/

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        // panel1.add(Box.createRigidArea(new Dimension(15, 15)));
        JLabel label1 = new HTSSLabel("虚拟零件号：");
        label1.setHorizontalAlignment(4);
        label1.setPreferredSize(new Dimension(120, 30));
        panel1.add(label1);
        panel1.add(visualPartNumber);
        visualPartNumber.setEnabled(false);
        visualPartNumber.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel1);

        /*----- 分流器二维码 ----*/
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JLabel label2 = new HTSSLabel("分流器二维码：");
        label2.setHorizontalAlignment(4);
        label2.setPreferredSize(new Dimension(120, 30));
        panel2.add(label2);
        panel2.add(textFieldResistorsID);
        textFieldResistorsID.setEnabled(false);
        textFieldResistorsID.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel2);

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
        testResultPanel.setPreferredSize(new Dimension(800, 280));

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
                "检测成功，将自动分配二维码", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.AREA_FONT));

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

        JLabel dlr = new HTSSLabel("二维码");
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

    private JPanel createPanelSecond() {
        JPanel jpanelSecond = new JPanel();
        GridBagLayout layoutSecond = new GridBagLayout();
        jpanelSecond.setLayout(layoutSecond);
        jpanelSecond.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbcSecond = new GridBagConstraints(); //定义一个GridBagConstraints
        gbcSecond.fill = GridBagConstraints.BOTH;
        gbcSecond.insets = new Insets(5, 5, 5, 5);

        /************ 初始化测试环境 ************/
        JPanel testPortsPanel = createTestPanel();
        jpanelSecond.add(testPortsPanel);
        gbcSecond.gridx = 0;
        gbcSecond.gridy = 0;
        gbcSecond.gridwidth = 1;
        gbcSecond.gridheight = 1;
        layoutSecond.setConstraints(testPortsPanel, gbcSecond);//设置组件


        /************ 状态显示 ***********/
        mDataView.setFocusable(false);
        mDataView.setFont(UIConstant.TEXT_FONT);
        mDataView.setLineWrap(true);
        JScrollPane jsp2 = new JScrollPane(mDataView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp2.setPreferredSize(new Dimension(800, 410));
        jpanelSecond.add(jsp2);

        gbcSecond.gridx = 0;
        gbcSecond.gridy = 1;
        gbcSecond.gridwidth = 1;
        gbcSecond.gridheight = 4;
        layoutSecond.setConstraints(jsp2, gbcSecond);//设置组件

        return jpanelSecond;
    }

    private JPanel createTestPanel() {
        JPanel testInitPanel = new JPanel();
        testInitPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "参数设置", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        testInitPanel.setPreferredSize(new Dimension(800, 125));
        testInitPanel.setLayout(new GridLayout(2, 2, 5, 5));

        JPanel mJPanel2 = new JPanel();
        mJPanel2.setLayout(new FlowLayout());
        JLabel l5 = new HTSSLabel("焊接后标定温度（°C）：");
        l5.setHorizontalAlignment(4);
        l5.setPreferredSize(new Dimension(150, 30));
        mJPanel2.add(l5);
        mJPanel2.add(textFieldTemp);
        textFieldTemp.setText(String.valueOf(UIConstant.TEST_TEMP));
        textFieldTemp.setPreferredSize(UIConstant.INPUT_DIMENSION);
        testInitPanel.add(mJPanel2);

        testStartButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panel = new JPanel();
        b1Panel.add(testStartButton);
        testInitPanel.add(b1Panel);

        testInitPanel.add(new JLabel());

        resetButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panelr = new JPanel();
        b1Panelr.add(resetButton);
        testInitPanel.add(b1Panelr);

        return testInitPanel;
    }

    private JPanel createPanelThird() {
        JPanel jpanelThird = new JPanel();
        GridBagLayout layoutThird = new GridBagLayout();
        jpanelThird.setLayout(layoutThird);
        jpanelThird.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbcThird = new GridBagConstraints(); //定义一个GridBagConstraints
        gbcThird.fill = GridBagConstraints.BOTH;
        gbcThird.insets = new Insets(5, 5, 5, 5);

        /************ 初始化测试环境 ************/
        JPanel devicesPanel = createDevicesPanel();
        jpanelThird.add(devicesPanel);
        gbcThird.gridx = 0;
        gbcThird.gridy = 0;
        gbcThird.gridwidth = 1;
        gbcThird.gridheight = 1;
        layoutThird.setConstraints(devicesPanel, gbcThird);//设置组件


        /*JPanel jp1 = new JPanel();
        deviceButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        jp1.add(deviceButton);
        jpanelThird.add(jp1);
        gbcThird.gridx = 0;
        gbcThird.gridy = 1;
        gbcThird.gridwidth = 1;
        gbcThird.gridheight = 1;
        layoutThird.setConstraints(jp1, gbcThird);//设置组件*/

        return jpanelThird;
    }

    private JPanel createDevicesPanel() {
        JPanel devicesPanel = new JPanel();
        GridLayout gridLayoutL = new GridLayout(9, 4, 5, 5);
        devicesPanel.setLayout(gridLayoutL);
        devicesPanel.setPreferredSize(new Dimension(800, 500));

        /*devicesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "设备地址", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));*/

        List<Devices> deviceList = null;
        try {
            DevicesRepo repo = SpringContext.getBean(DevicesRepo.class);
            deviceList = repo.findAll();
        } catch (Exception e) {
            logger.error(e);
        }

        //row 1
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
        JLabel l4 = new HTSSLabel("状态");
        JPanel p4 = new JPanel(new BorderLayout());
        l4.setFont(UIConstant.TEXT_FONT);
        p4.setBackground(UIConstant.BGCOLOR_GRAY);
        p4.add(l4);
        devicesPanel.add(p4);

        devicesPanel.add(new HTSSLabel("本机"));
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ip = inetAddress.toString();
            String localhost = StringUtils.substringAfterLast(ip, "/");
            devicesPanel.add(new HTSSLabel(localhost));
        } catch (Exception e) {
            devicesPanel.add(new HTSSLabel("localhost"));
        }
        devicesPanel.add(new JLabel());
        devicesPanel.add(new HTSSStatusLabel());

        deviceList.forEach(item -> {
            devicesPanel.add(new HTSSLabel(item.getDevice()));
            devicesPanel.add(new HTSSLabel(item.getIpAddress()));

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

            devicesPanel.add(new HTSSStatusLabel());
        });

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

            FrameReset();
        } else if (actionCommand.equals(UIConstant.NETPORT_OPEN)) {
            FrameReset();
            logger.info("测试开始...");
            if (!checkInput()) return;

            // 打开主控PLC通信端口
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("textFieldTemp", textFieldTemp);
            jsonObject.put("visualPartNumber", visualPartNumber);
            jsonObject.put("textFieldResistorsID", textFieldResistorsID);
            mainPLCListener = new NetPortListener(Integer.parseInt(textFieldMainPLCPort.getText()), jsonObject);
            mainPLCListener.start();
            mDataView.append(formatInfo("主控PLC通信端口已打开，可以接收数据......" + getStatus()));

            // TODO: 打开激光打码机通信端口
            laserListener = new NetPortListener(Integer.parseInt(textFieldLaserPort.getText()), null);
            laserListener.start();
            mDataView.append(formatInfo("激光打码机通信端口已打开，可以接收数据......" + getStatus()));

            // TODO: 初始化电源和测试设备

            // 按钮设为"结束测试"
            testStartButton.setText(UIConstant.NETPORT_CLOSE);
        } else if (actionCommand.equals(UIConstant.NETPORT_CLOSE)) {
            // 关闭主控PLC通信端口
            mainPLCListener.closePort();
            mDataView.append(formatInfo("主控PLC通信端口已关闭......" + getStatus()));

            // 关闭激光打码机通信端口
            laserListener.closePort();
            mDataView.append(formatInfo("主控PLC通信端口已关闭......" + getStatus()));

            // TODO: 关闭电源，关闭测试设备

            // 按钮设为"开始测试"
            testStartButton.setText(UIConstant.NETPORT_OPEN);
        } else {
            logger.error("something wrong boy..." + actionCommand);
        }
    }

    private String formatInfo(String s) {
        String result = "";
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result = result + f.format(new Date())
                + " - " + s + "\r\n";
        return result;
    }

    private void Test() {
        String vp = visualPartNumber.getText();
        String factory = null;
        if (vp.startsWith("D")) {
            factory = TestConstant.SVW;
        } else if (vp.startsWith("G")) {
            factory = TestConstant.FAW;
        }
        if (factory == null) return;
        String cirTemp = textFieldTemp.getText();
        String id = textFieldResistorsID.getText();
        ProRecords part = manager.testThePart(factory, Double.parseDouble(cirTemp), id);

        //TODO: 拿到结果, 回显
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        textFieldRt_R25.setText(nf.format(part.getR25()));
        textFieldRw_R16.setText(nf.format(part.getR16()));
        textFieldRntc_NTCRValue.setText(nf.format(part.getRntc()));
        textFieldTemperature.setText(nf.format(part.getTntc()));
        if (part.getProCode() == null || "".equals(part.getProCode())) {
            labelQRCode.setText("无二维码");
        } else {
            labelQRCode.setText(part.getProCode());
        }

        if ((part.getR25() < 78.25 && part.getR25() > 71.75)) {
            labelResultOne.setText("合格");
            labelResultOne.setForeground(Color.green);

        } else {
            labelResultOne.setText("不合格");
            labelResultOne.setForeground(Color.red);
        }

        if (Math.abs(part.getTntc() - Double.parseDouble(cirTemp)) <= 3) {
            labelResultTwo.setText("合格");
            labelResultTwo.setForeground(Color.green);
        } else {
            labelResultTwo.setText("不合格");
            labelResultTwo.setForeground(Color.red);
        }

        mDataView.append(formatInfo("测试结束......"));
        // logger.info("result testResultVo is: " + part);
        testStartButton.setEnabled(true);
    }

    private void FrameReset() {
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

    private boolean checkInput() {
        String mainPort = textFieldMainPLCPort.getText();
        try {
            Integer i = Integer.parseInt(mainPort);
            // mDataView.append("网络端口" + mainPort + "准备打开......" + "\r\n");
        } catch (Exception exp) {
            mDataView.append(formatInfo("主控通信端口" + mainPort + "输入有误，请重新输入！"));
            return false;
        }

        String laserPort = textFieldMainPLCPort.getText();
        try {
            Integer i = Integer.parseInt(laserPort);
            // mDataView.append("网络端口" + laserPort + "准备打开......" + "\r\n");
        } catch (Exception exp) {
            mDataView.append(formatInfo("激光打码机通信端口" + laserPort + "输入有误，请重新输入！"));
            return false;
        }

        String cirTemp = textFieldTemp.getText();
        try {
            double d = Double.parseDouble(cirTemp);
            if (d < 15) {
                mDataView.append(formatInfo("环境温度" + cirTemp + "<15°C，过低！"));
                return false;
            } else if (d > 35) {
                mDataView.append(formatInfo("环境温度" + cirTemp + ">35°C，过高！"));
                return false;
            }
        } catch (Exception exp) {
            mDataView.append(formatInfo("环境温度值输入错误！"));
            return false;
        }

        String vPartStart = textFieldeolStatus.getText();
        if (1 == 1) {
            mDataView.append(formatInfo("状态:" + eolStatus.get()));
        }

        return true;
    }
}
