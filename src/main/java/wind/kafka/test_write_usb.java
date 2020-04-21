package wind.kafka;

import gnu.io.*;
import wind.Interface.Write_usb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class test_write_usb implements Write_usb {

    public static String portName;
    public static int baudrate;
    public static ArrayList<String> resu;


    public test_write_usb(String portName, int baudrate) {
        this.portName = portName;
        this.baudrate = baudrate;
    }


    @Override
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

    @Override
    public void sendToPort(SerialPort serialPort, byte[] order) {
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

    @Override
    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }


    public static void main(String[] args) {
        test_write_usb usb_write = new test_write_usb("COM3", 115200);
        SerialPort serialPort1 = usb_write.openPort();
        for (int i = 0; i < 10; i++) {

            usb_write.sendToPort(serialPort1, (("tom" + i) + "," + 10 + i + "," + i + 1 + "\n").getBytes());
        }
        usb_write.closePort(serialPort1);

    }
}
