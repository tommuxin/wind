package wind.test_usb;

import gnu.io.*;
import wind.Interface.Write_usb;
import wind.Until.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class TestWriteUsb implements Write_usb {

    public static String portName;
    public static int baudrate;
    public static ArrayList<String> resu;
    public static int count1=0;

    public static String re_msg="第一次发送";

    public TestWriteUsb(String portName, int baudrate) {
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
    public String sendToPort(SerialPort serialPort, byte[] order) {
        OutputStream out = null;
        InputStream in = null;
        byte[] bytes = {};
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
            out.close();
            Thread.sleep(500);
            in=serialPort.getInputStream();//读取返回值
            byte[] readBuffer1 = new byte[1024];

            int bytesNum = in.read(readBuffer1);
            while (bytesNum >0) {
                bytes = ArrayUtils.concat(bytes, readBuffer1);
                bytesNum = in.read(readBuffer1);
                re_msg=new String(bytes);
            }
            in.close();
            System.out.println("已被消费："+re_msg );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
        return  re_msg;
    }

    @Override
    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }


    public static void main(String[] args) {
        TestWriteUsb usb_write = new TestWriteUsb("COM3", 115200);
        SerialPort serialPort1 = usb_write.openPort();
String re1=null;
        for (int i = 0; i < 10; i++) {

          re1=   usb_write.sendToPort(serialPort1, (("tom" + i) + "," + i + "," + i  + ",tom"+"-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------你好---------------------------------------------------"+"cehis"+"deee"+"-------------\n").getBytes());

        }
     System.out.println(re1);
        usb_write.closePort(serialPort1);

    }
}
