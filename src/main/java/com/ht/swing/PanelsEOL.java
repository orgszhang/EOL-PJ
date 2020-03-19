package com.ht.swing;


import com.alibaba.fastjson.JSONObject;
import com.ht.Socket.EolServer;
import com.ht.comm.NetPortListener;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;

import com.ht.utils.ByteUtils;
import com.ht.utils.ShowUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.server.PortInUseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
    public static JPanel mainPanel = new JPanel();
    ThreadLocal<String> eolStatus = ThreadLocal.withInitial(() -> "BUSY");


    private static final Log logger = LogFactory.getLog(PanelsEOL.class);
    KeySightManager manager = new KeySightManager();


    // Buttons
    JButton resetButton = new HTSSButton(UIConstant.RESET_BUTTON);
    JButton testStartButton = new HTSSButton(UIConstant.NETPORT_OPEN);
    JButton deviceButton = new HTSSButton(UIConstant.LABEL_DEVICE_CONFIG);

    // Input Fields
    JTextField textFieldeolStatus = new HTSSInputField();  //
    JTextField visualPartNumber = new HTSSInputField();  //
    JTextField textFieldTemp = new HTSSInputField();  //
    JTextField textFieldNetPort = new HTSSInputField();  // 分流器
    JTextField textFieldResistorsID = new HTSSInputField();
    JTextField textFieldRt_R25 = new HTSSResultField();  // Rt
    JTextField textFieldRw_R16 = new HTSSResultField();  // Rw
    JTextField textFieldRntc_NTCRValue = new HTSSResultField();  // Rntc
    JTextField textFieldTemperature = new HTSSResultField();

    // Labels
    JLabel labelResultOne = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelResultTwo = new HTSSLabel(UIConstant.LABEL_TO_TEST);
    JLabel labelQRCode = new HTSSLabel(UIConstant.LABEL_TO_GENERATE);

    // 串口
    private JTextArea mDataView = new JTextArea();
    private JScrollPane mScrollDataView = new JScrollPane(mDataView);

    private JComboBox mCommChoice = new JComboBox();
    private JComboBox mBaudrateChoice = new JComboBox();
    private ButtonGroup mDataChoice = new ButtonGroup();
    private JRadioButton mDataASCIIChoice = new JRadioButton("ASCII", true);
    private JRadioButton mDataHexChoice = new JRadioButton("Hex");

    private JTextArea mDataInput = new JTextArea();
    private JButton initSerialButton = new HTSSButton("打开串口");
    private JButton sendDataButton = new HTSSButton("发送数据");

    // 串口列表
    private List<String> mCommList = null;
/*    // 串口对象
    private SerialPort mSerialport;*/

    // 网口
    NetPortListener portListener;

    public PanelsEOL(String code,String qc) {
        initMainPanel(code,qc);
        /*    this.initData();*/
        this.add(mainPanel);
        mainPanel.setVisible(true);

        resetButton.addActionListener(this);
        testStartButton.addActionListener(this);
        deviceButton.addActionListener(this);
        /*     serialActionListener();*/
    }

/*    public static void main(String[] args) {
        PanelsEOL pvt = new PanelsEOL();

        JFrame frame = new JFrame(UIConstant.APP_NAME);
        frame.setResizable(false);
        Container contentPane = frame.getContentPane();
        contentPane.add(pvt);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }*/

    private volatile boolean ready = true;

    public synchronized void setStatus(boolean status) {
        this.ready = status;
    }

    public synchronized boolean getStatus() {
        return ready;
    }

    private void initMainPanel(String code,String qc) {
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
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        // gbc.weightx = 1;
        // gbc.weighty = 0.2;
        layout.setConstraints(titlePanel, gbc);//设置组件

/************ 串口区域 ************/

        JPanel devicesPanel = createDevicesPanel();
        mainPanel.add(devicesPanel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        // gbc.weightx = 0.6;
        // gbc.weighty = 0.15;
        layout.setConstraints(devicesPanel, gbc);//设置组件


/************ 初始化测试环境 ************/

        JPanel testInitPanel = createNetPortPanel();
        mainPanel.add(testInitPanel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        // gbc.weightx = 0.6;
        // gbc.weighty = 0.15;
        layout.setConstraints(testInitPanel, gbc);//设置组件

        /*
         *********** 传入信息区域 ***********
         */

        JPanel partDataPanel = createDataTransferInPanel(code,qc);
        mainPanel.add(partDataPanel);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        // gbc.weighty = 0.1;
        layout.setConstraints(partDataPanel, gbc);

        /*
         *********** 检测结果区域 ***********
         */

        JPanel testResultPanel = createTestResultPanel();
        mainPanel.add(testResultPanel);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.35;
        layout.setConstraints(testResultPanel, gbc);//设置组件

        /*
         *********** 二维码区域 ***********
         */

        JPanel qrPanel = createQRPanel();
        mainPanel.add(qrPanel);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.3;
        layout.setConstraints(qrPanel, gbc);//设置组件

        /*
         *********** 状态显示 **********
         */

        mDataView.setFocusable(false);
        mDataView.setFont(UIConstant.TEXT_FONT);
        mScrollDataView.setBounds(10, 10, 500, 350);
        // mScrollDataView.setPreferredSize(new Dimension(500, 350));
        mainPanel.add(mScrollDataView);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        // gbc.weightx = 0.5;
        // gbc.weighty = 0.3;
        layout.setConstraints(mScrollDataView, gbc);//设置组件

        /*
         ************ 版权 ************
         */

        JPanel lbPanel = new JPanel();
        JLabel proLabel = new JLabel("上海禾他汽车科技有限公司 版权所有 ©2019-2099");
        proLabel.setFont(UIConstant.COPYRIGHT_FONT);
        proLabel.setPreferredSize(new Dimension(1350, 25));
        proLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lbPanel.add(proLabel);
        mainPanel.add(lbPanel);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        // gbc.weightx = 0.5;
        // gbc.weighty = 0.3;
        layout.setConstraints(lbPanel, gbc);//设置组件
    }

    private JPanel createTitlePanel() {

/************ 标题 ************/

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());

        JLabel iconLabel = new JLabel();
        ImageIcon icon = new ImageIcon("src/main/resources/htlogo.png");
        icon.setImage(icon.getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT));
        iconLabel.setIcon(icon);
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel(UIConstant.APP_NAME);
        titleLabel.setPreferredSize(new Dimension(1290, 120));
        titleLabel.setFont(UIConstant.TITLE_FONT);
        titleLabel.setHorizontalAlignment(0);
        titlePanel.add(titleLabel);

        return titlePanel;
    }

    private JPanel createDevicesPanel() {
        JPanel devicesPanel = new JPanel();
        GridLayout gridLayoutL = new GridLayout(6, 3, 5, 5);
        devicesPanel.setLayout(gridLayoutL);
        // devicesPanel.setBackground(UIConstant.BGCOLOR_BLUE);
        devicesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "测试设备", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.AREA_FONT));

        //row 1
        devicesPanel.add(new HTSSLabel("设备"));
        devicesPanel.add(new HTSSLabel("IP地址"));
        devicesPanel.add(new HTSSLabel("端口"));

        //row 2
        devicesPanel.add(new HTSSLabel("电流"));
        devicesPanel.add(new HTSSLabel("169.254.210.1"));
        devicesPanel.add(new JLabel());

        //row 3
        devicesPanel.add(new HTSSLabel("电压I"));
        devicesPanel.add(new HTSSLabel("169.254.210.2"));
        devicesPanel.add(new JLabel());

        //row 4
        devicesPanel.add(new HTSSLabel("电压II"));
        devicesPanel.add(new HTSSLabel("169.254.210.3"));
        devicesPanel.add(new JLabel());

        //row 5
        devicesPanel.add(new HTSSLabel("电阻"));
        devicesPanel.add(new HTSSLabel("169.254.210.4"));
        devicesPanel.add(new JLabel());

        //row 6
        devicesPanel.add(new HTSSLabel("激光打标"));
        devicesPanel.add(new HTSSLabel("169.254.210.7"));
        devicesPanel.add(new HTSSLabel("6666"));

        return devicesPanel;
    }

    public JPanel createDataTransferInPanel(String code ,String qc) {
        JPanel partDataPanel = new JPanel();
        partDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "传入信息", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        GridLayout partDataLayout = new GridLayout(2, 2, 5, 5);
        partDataPanel.setLayout(partDataLayout);

        /*----- 虚拟零件号 ----*/

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        // panel1.add(Box.createRigidArea(new Dimension(15, 15)));
        JLabel label1 = new HTSSLabel("虚拟零件号：");
        label1.setHorizontalAlignment(4);
        label1.setPreferredSize(new Dimension(100, 30));
        panel1.add(label1);
        panel1.add(visualPartNumber);
        panel1.add(textFieldeolStatus);
        visualPartNumber.setEnabled(false);
        visualPartNumber.setText(code);
        visualPartNumber.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel1);

        partDataPanel.add(new JLabel());

        /*----- 分流器二维码 ----*/

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JLabel label2 = new HTSSLabel("分流器二维码：");
        label2.setHorizontalAlignment(4);
        label2.setPreferredSize(new Dimension(100, 30));
        panel2.add(label2);
        panel2.add(textFieldResistorsID);
        textFieldResistorsID.setEnabled(false);
        textFieldResistorsID.setText(qc);
        textFieldResistorsID.setPreferredSize(UIConstant.INPUT_LONGDIMENSION);
        partDataPanel.add(panel2);
        resetButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b3Panel = new JPanel();
        b3Panel.add(resetButton);
        partDataPanel.add(b3Panel);

        return partDataPanel;
    }

    private JPanel createSerialPortPanel() {
        JPanel mSerialPortPanel = new JPanel();
        mSerialPortPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "1. 串口设置和调试", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        GridBagLayout layout = new GridBagLayout();
        mSerialPortPanel.setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints(); //定义一个GridBagConstraints
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel panel1 = new JPanel();

        JLabel label1 = new HTSSLabel("串口：");
        label1.setHorizontalAlignment(4);
        label1.setPreferredSize(new Dimension(50, 25));
        panel1.add(label1);
        mCommChoice.setPreferredSize(UIConstant.INPUT_DIMENSION);
        mCommChoice.setFocusable(false);
        panel1.add(mCommChoice);
        mSerialPortPanel.add(panel1);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.3;
        layout.setConstraints(panel1, gbc);//设置组件

        JPanel panel2 = new JPanel();
        JLabel label2 = new HTSSLabel("波特率：");
        label2.setHorizontalAlignment(4);
        label2.setPreferredSize(new Dimension(50, 30));
        panel2.add(label2);
        mBaudrateChoice.setPreferredSize(UIConstant.INPUT_DIMENSION);
        mBaudrateChoice.setFocusable(false);
        panel2.add(mBaudrateChoice);
        mSerialPortPanel.add(panel2);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(panel2, gbc);//设置组件

        JPanel panelx = new JPanel();
        panelx.setLayout(new FlowLayout());
        panelx.setPreferredSize(UIConstant.INPUT_DIMENSION);
        mDataChoice.add(mDataASCIIChoice);
        mDataChoice.add(mDataHexChoice);
        mDataASCIIChoice.setHorizontalAlignment(4);
        panelx.add(mDataASCIIChoice);
        panelx.add(mDataHexChoice);
        mSerialPortPanel.add(panelx);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(panelx, gbc);//设置组件

        JPanel panely = new JPanel(new FlowLayout());
        // panely.setLayout(new BoxLayout(panely, BoxLayout.X_AXIS));
        // panely.add(Box.createRigidArea(new Dimension(15, 15)));
        panely.setPreferredSize(new Dimension(500, 40));
        initSerialButton.setFocusable(false);
        initSerialButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        initSerialButton.setVerticalAlignment(0);
        panely.add(initSerialButton);
        panely.add(Box.createRigidArea(new Dimension(15, 15)));
        sendDataButton.setFocusable(false);
        sendDataButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        panely.add(sendDataButton);
        mSerialPortPanel.add(panely);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        layout.setConstraints(panely, gbc);//设置组件

        mDataInput.setLineWrap(true);
        mDataInput.setPreferredSize(new Dimension(150, 50));
        mDataInput.setWrapStyleWord(true);
        mSerialPortPanel.add(mDataInput);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        layout.setConstraints(mDataInput, gbc);//设置组件

        return mSerialPortPanel;
    }

    private JPanel createNetPortPanel() {
        JPanel testInitPanel = new JPanel();
        testInitPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "参数设置", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.TEXT_FONT));
        testInitPanel.setPreferredSize(new Dimension(350, 130));
        // testInitPanel.setLayout(new BoxLayout(testInitPanel, BoxLayout.Y_AXIS));
        testInitPanel.setLayout(new GridLayout(2, 2, 5, 5));
        // testInitPanel.add(Box.createRigidArea(new Dimension(15, 15)));

        JPanel netPortLabel = new JPanel();
        netPortLabel.setLayout(new FlowLayout());
        JLabel label7 = new HTSSLabel("主控通信端口：");
        label7.setHorizontalAlignment(4);
        label7.setPreferredSize(new Dimension(120, 30));
        netPortLabel.add(label7);
        textFieldNetPort.setPreferredSize(UIConstant.INPUT_DIMENSION);
        netPortLabel.add(textFieldNetPort);
        textFieldNetPort.setText("8888");
        testInitPanel.add(netPortLabel);

        testStartButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b1Panel = new JPanel();
        b1Panel.add(testStartButton);
        testInitPanel.add(b1Panel);

        JPanel mJPanel2 = new JPanel();
        mJPanel2.setLayout(new FlowLayout());
        JLabel l5 = new HTSSLabel("环境温度（°C）：");
        l5.setHorizontalAlignment(4);
        l5.setPreferredSize(new Dimension(120, 30));
        mJPanel2.add(l5);
        mJPanel2.add(textFieldTemp);
        textFieldTemp.setPreferredSize(UIConstant.INPUT_DIMENSION);
        testInitPanel.add(mJPanel2);

        deviceButton.setPreferredSize(UIConstant.BUTTON_DIMENSION);
        JPanel b3Panel = new JPanel();
        b3Panel.add(deviceButton);
        testInitPanel.add(b3Panel);

        return testInitPanel;
    }

    private JPanel createTestResultPanel() {
        JPanel testResultPanel = new JPanel();
        GridLayout gridLayoutL = new GridLayout(5, 4, 5, 5);
        testResultPanel.setLayout(gridLayoutL);
        testResultPanel.setBackground(UIConstant.BGCOLOR_BLUE);
        testResultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "检测结果", TitledBorder.LEFT, TitledBorder.CENTER, UIConstant.AREA_FONT));

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
        textFieldRt_R25.setPreferredSize(new Dimension(150, 30));
        tbPanel1.add(textFieldRt_R25);
        testResultPanel.add(tbPanel1);

        //row 3
        testResultPanel.add(new HTSSLabel("电阻Rw"));
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        JPanel tbPanel2 = new JPanel();
        tbPanel2.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldRw_R16.setPreferredSize(new Dimension(150, 30));
        tbPanel2.add(textFieldRw_R16);
        testResultPanel.add(tbPanel2);

        //row 4
        testResultPanel.add(new HTSSLabel("电阻Rntc"));
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        JPanel tbPanel3 = new JPanel();
        tbPanel3.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldRntc_NTCRValue.setPreferredSize(new Dimension(150, 30));
        tbPanel3.add(textFieldRntc_NTCRValue);
        testResultPanel.add(tbPanel3);

        //row 5
        testResultPanel.add(new JLabel());
        testResultPanel.add(new JLabel());
        testResultPanel.add(new HTSSLabel("换算温度"));
        JPanel tbPanel4 = new JPanel();
        tbPanel4.setBackground(UIConstant.BGCOLOR_BLUE);
        textFieldTemperature.setPreferredSize(new Dimension(150, 30));
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

    /**
     * 按钮监听事件
     */


    @Override
    public void actionPerformed(ActionEvent e) {
        final String actionCommand = e.getActionCommand();
        if (actionCommand.equals(UIConstant.RESET_BUTTON)) {
            mDataView.setText(UIConstant.EMPTY_STRING);
            textFieldResistorsID.setText(UIConstant.EMPTY_STRING);
            testStartButton.setEnabled(true);
            FrameReset();
        } else if (actionCommand.equals(UIConstant.NETPORT_OPEN)) {
            FrameReset();
            logger.info("测试开始...");
            if (!checkInput()) return;
            testStartButton.setText(UIConstant.NETPORT_CLOSE);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("textFieldTemp",textFieldTemp);
            jsonObject.put("visualPartNumber",visualPartNumber);
            jsonObject.put("textFieldResistorsID",textFieldResistorsID);
            portListener = new NetPortListener(Integer.parseInt(textFieldNetPort.getText()),jsonObject);
            portListener.start();
            mDataView.append(new Date() + "：网口已打开，可以接收数据......" + getStatus() + "\r\n");

        } else if (actionCommand.equals(UIConstant.NETPORT_CLOSE)) {
            portListener.closePort();
            mDataView.append(new Date() + "：网口已关闭......" + getStatus() + "\r\n");
            testStartButton.setText(UIConstant.NETPORT_OPEN);
        } else if (actionCommand.equals(UIConstant.LABEL_DEVICE_CONFIG)) {
            String devices = "I: 169.254.210.1" + "\r\n"
                    + "V25: 169.254.210.2" + "\r\n"
                    + "V16: 169.254.210.3" + "\r\n"
                    + "R: 169.254.210.4" + "\r\n"
                    + "Laser: 169.254.210.7:6666";
            JOptionPane.showMessageDialog(this, devices, "设备配置", JOptionPane.INFORMATION_MESSAGE);
        } else {
            logger.error("something wrong boy..." + actionCommand);
        }
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

        mDataView.append("测试结束......" + new Date() + "\r\n");
        // logger.info("result testResultVo is: " + part);
        testStartButton.setEnabled(true);
    }


    /*private void serialActionListener() {
        // 串口
        mCommChoice.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                mCommList = SerialPortManager.findPorts();
                // 检查是否有可用串口，有则加入选项中
                if (mCommList == null || mCommList.size() < 1) {
                    ShowUtils.warningMessage("没有搜索到有效串口！");
                } else {
                    int index = mCommChoice.getSelectedIndex();
                    mCommChoice.removeAllItems();
                    for (String s : mCommList) {
                        mCommChoice.addItem(s);
                    }
                    mCommChoice.setSelectedIndex(index);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // NO OP
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // NO OP
            }
        });

        // 打开|关闭串口
        initSerialButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("打开串口".equals(initSerialButton.getText()) && mSerialport == null) {
                    openSerialPort(e);
                } else {
                    closeSerialPort(e);
                }
            }
        });

        // 发送数据
        sendDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendData(e);
            }
        });
    }

    private void openSerialPort(ActionEvent evt) {
        // 获取串口名称
        String commName = (String) mCommChoice.getSelectedItem();
        // 获取波特率，默认为9600
        int baudrate = 9600;
        String bps = (String) mBaudrateChoice.getSelectedItem();
        baudrate = Integer.parseInt(bps);

        // 检查串口名称是否获取正确
        if (commName == null || commName.equals("")) {
            ShowUtils.warningMessage("没有搜索到有效串口！");
        } else {
            try {
                mSerialport = SerialPortManager.openPort(commName, baudrate);
                if (mSerialport != null) {
                    mDataView.setText("串口已打开" + "\r\n");
                    initSerialButton.setText("关闭串口");
                }
            } catch (PortInUseException e) {
                ShowUtils.warningMessage("串口已被占用！");
            }
        }

        // 添加串口监听
        SerialPortManager.addListener(mSerialport, new SerialPortManager.DataAvailableListener() {

            @Override
            public void dataAvailable() {
                byte[] data = null;
                try {
                    if (mSerialport == null) {
                        ShowUtils.errorMessage("串口对象为空，监听失败！");
                    } else {
                        // 读取串口数据
                        data = SerialPortManager.readFromPort(mSerialport);

                        // 以字符串的形式接收数据
                        if (mDataASCIIChoice.isSelected()) {
                            mDataView.append(new String(data) + "\r\n");
                        }

                        // 以十六进制的形式接收数据
                        if (mDataHexChoice.isSelected()) {
                            mDataView.append(ByteUtils.byteArrayToHexString(data) + "\r\n");
                        }
                    }
                } catch (Exception e) {
                    ShowUtils.errorMessage(e.toString());
                    // 发生读取错误时显示错误信息后退出系统
                    System.exit(0);
                }
            }
        });
    }

    private void closeSerialPort(ActionEvent evt) {
        SerialPortManager.closePort(mSerialport);
        mDataView.append("串口已关闭" + "\r\n");
        initSerialButton.setText("打开串口");
        mSerialport = null;
    }*/

    /*   private void sendData(ActionEvent evt) {
           // 待发送数据
           String data = mDataInput.getText();

           if (mSerialport == null) {
               ShowUtils.warningMessage("请先打开串口！");
               return;
           }

           if ("".equals(data) || data == null) {
               ShowUtils.warningMessage("请输入要发送的数据！");
               return;
           }

           // 以字符串的形式发送数据
           if (mDataASCIIChoice.isSelected()) {
               SerialPortManager.sendToPort(mSerialport, data.getBytes());
           }

           // 以十六进制的形式发送数据
           if (mDataHexChoice.isSelected()) {
               SerialPortManager.sendToPort(mSerialport, ByteUtils.hexStr2Byte(data));
           }
       }

       private void initData() {
           mCommList = SerialPortManager.findPorts();
           // 检查是否有可用串口，有则加入选项中
           if (mCommList == null || mCommList.size() < 1) {
               // ShowUtils.warningMessage("没有搜索到有效串口！");
           } else {
               for (String s : mCommList) {
                   mCommChoice.addItem(s);
               }
           }

           mBaudrateChoice.addItem("9600");
           mBaudrateChoice.addItem("19200");
           mBaudrateChoice.addItem("38400");
           mBaudrateChoice.addItem("57600");
           mBaudrateChoice.addItem("115200");
       }
   */
    private void FrameReset() {
        logger.debug("信息重置...");

        textFieldRt_R25.setText(UIConstant.EMPTY_STRING);
        textFieldRw_R16.setText(UIConstant.EMPTY_STRING);
        textFieldRntc_NTCRValue.setText(UIConstant.EMPTY_STRING);
        textFieldTemperature.setText(UIConstant.EMPTY_STRING);

        // resistorResult.setText(UIConstant.LABEL_TO_GENERATE);
        labelResultOne.setText(UIConstant.LABEL_TO_TEST);
        labelResultOne.setForeground(Color.black);
        labelResultTwo.setText(UIConstant.LABEL_TO_TEST);
        labelResultTwo.setForeground(Color.black);
        labelQRCode.setText(UIConstant.LABEL_TO_GENERATE);
    }

    private boolean checkInput() {
        String netPort = textFieldNetPort.getText();
        try {
            Integer i = Integer.parseInt(netPort);
            mDataView.append("网络端口" + netPort + "准备打开......" + "\r\n");
        } catch (Exception exp) {
            mDataView.append("网络端口" + netPort + "输入有误，请重新输入！" + "\r\n");
            return false;
        }


        String cirTemp = textFieldTemp.getText();
        try {
            double d = Double.parseDouble(cirTemp);
            if (d < 15) {
                mDataView.append("Error: 环境温度" + cirTemp + "<15°C，过低！" + "\r\n");
                return false;
            } else if (d > 35) {
                mDataView.append("Error: 环境温度" + cirTemp + ">35°C，过高！" + "\r\n");
                return false;
            }
        } catch (Exception exp) {
            mDataView.append("Error: 环境温度值输入错误！" + "\r\n");
            return false;
        }



        String vPartStart = textFieldeolStatus.getText();
        if(1==1) {
            mDataView.append("状态:" + eolStatus.get()+ "\r\n");
        }

   /*     String vPartNumber = visualPartNumber.getText();
        if (vPartNumber == null || UIConstant.EMPTY_STRING.equals(vPartNumber)) {
            mDataView.append("Error: 虚拟零件号传入为空，请检查！" + "\r\n");
            return false;
        } else if (vPartNumber.length() != 11) {
            mDataView.append("Error: 虚拟零件号" + vPartNumber + "长度不够，请检查！" + "\r\n");
            return false;
        }*/

  /*      String resistorID = textFieldResistorsID.getText();
        if (resistorID == null || UIConstant.EMPTY_STRING.equals(resistorID)) {
            mDataView.append("Error: 分流器二维码传入为空，请检查！" + "\r\n");
            return false;
        } else if (resistorID.length() < 11 || resistorID.length() > 12) {
            mDataView.append("Error: 分流器二维码" + resistorID + "长度不够，请检查！" + "\r\n");
            return false;
        }*/


        String portStatus = initSerialButton.getText();
        if (!"打开串口".equals(portStatus)) {
            mDataView.append("Error: 串口没有打开，请检查！" + "\r\n");
            return false;
        }


        return true;
    }
}
