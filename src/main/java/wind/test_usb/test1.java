package wind.test_usb;

import gnu.io.*;
import wind.Until.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

public class test1 {

           public static String portName;
        public static int baudrate;
        public static ArrayList<String> resu;
        public static int count1 = 0;

        public static String re_msg = "第一次发送";

    public test1(String portName, int baudrate){
            this.portName = portName;
            this.baudrate = baudrate;
        }
    public static final ArrayList<String> findPorts() {
        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList<String>();
        // 将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;
    }
     public SerialPort openPort() {
        try {
            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            // 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // 设置一下串口的波特率等参数
                    // 数据位：8
                    // 停止位：1
                    // 校验位：None
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {
                    e.printStackTrace();
                }
                return serialPort;
            }
        } catch (NoSuchPortException | PortInUseException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    public void sendToPort(SerialPort serialPort, byte[] order) throws IOException {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }
    public static void main(String[] args) throws IOException {
  /*      test1 usb_write = new test1("/dev/ttyUSB0", 115200);
///dev/ttyUSB0

        SerialPort serialPort1 = usb_write.openPort();


        for (int i = 0; i < 10; i++) {

           usb_write.sendToPort(serialPort1, (("tom" + i) + "," + i + "," + i  + ",tom"+"-----------"+"cehis"+"deee"+"-------------\n").getBytes());

        }

serialPort1.close();*/
StringBuffer yy=new StringBuffer();
yy.append("nimei");
yy.append(23);
System.out.println(yy.toString().length());
    }

}