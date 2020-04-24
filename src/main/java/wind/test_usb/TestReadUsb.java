package wind.test_usb;

import gnu.io.*;
import wind.Until.ArrayUtils;
import wind.Interface.Read_usb;
import wind.Until.ShowUtils;
import wind.Until.KafkaUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

public class TestReadUsb implements Read_usb {

    public static String portName;
    public static int baudrate;
    public static ArrayList<String> resu;


    public TestReadUsb(String portName, int baudrate) {
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
            out.close();
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
    public byte[] readFromPort(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = {};
        try {
            in = serialPort.getInputStream();
            // 缓冲区大小为一个字节
            byte[] readBuffer = new byte[1];
            int bytesNum = in.read(readBuffer, 0, 1);

            while (bytesNum > 0) {
                bytes = ArrayUtils.concat(bytes, readBuffer);
                bytesNum = in.read(readBuffer);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {

                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    @Override
    public void addListener(SerialPort serialPort, KafkaUtil ka1) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            // 给串口添加监听器
            serialPort.addEventListener(new SerialPortListener(() -> {

                String msg = null;
                String[] msg1 = null;
                byte[] data = null;
                int scuss = 0;
                try {
                    if (serialPort == null) {
                        ShowUtils.errorMessage("串口对象为空，监听失败！");
                    } else {
                        // 读取串口数据
                        data = readFromPort(serialPort);
                        //设置字符集
                        msg = new String(data,"UTF-8");

                        //定义分隔符，这边测试用换行符，实际某些字段中会存在换行符导致字段错乱
                        msg1 = msg.split("\n");
                     /*  for (int i=0;i<msg1.length;i++){

                           System.out.println(msg1[i]);

                       }*/
                        //批量插入kafka
                        scuss = ka1.insertTopic("test1", "1", msg1);
                        if (scuss == 1) {
                            sendToPort(serialPort, ("成功eeeeeeeee").getBytes());
                            //清理list，防止占用内存
                            msg1 = null;
                        } else {
                            sendToPort(serialPort, ("失败").getBytes());
                            //清理list，防止占用内存
                            msg1 = null;
                        }
                    }
                } catch (Exception e) {
                    ShowUtils.errorMessage(e.toString());
                    // 发生读取错误时显示错误信息后退出系统

                    System.exit(0);
                }
            }));
            // 设置当有数据到达时唤醒监听接收线程
            serialPort.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
            serialPort.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    /**
     * 串口监听
     */
    public static class SerialPortListener implements SerialPortEventListener {

        private DataAvailableListener mDataAvailableListener;

        public SerialPortListener(DataAvailableListener mDataAvailableListener) {
            this.mDataAvailableListener = mDataAvailableListener;
        }

        public void serialEvent(SerialPortEvent serialPortEvent) {
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE: // 1.串口存在有效数据
                    if (mDataAvailableListener != null) {
                        mDataAvailableListener.dataAvailable();
                    }
                    break;

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2.输出缓冲区已清空
                    break;

                case SerialPortEvent.CTS: // 3.清除待发送数据
                    break;

                case SerialPortEvent.DSR: // 4.待发送数据准备好了
                    break;

                case SerialPortEvent.RI: // 5.振铃指示
                    break;

                case SerialPortEvent.CD: // 6.载波检测
                    break;

                case SerialPortEvent.OE: // 7.溢位（溢出）错误
                    break;

                case SerialPortEvent.PE: // 8.奇偶校验错误
                    break;

                case SerialPortEvent.FE: // 9.帧错误
                    break;

                case SerialPortEvent.BI: // 10.通讯中断
                    ShowUtils.errorMessage("与串口设备通讯中断");
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 串口存在有效数据监听
     */
    public interface DataAvailableListener {

        void dataAvailable();
    }

    public static void main(String[] args) {
        KafkaUtil kaf = new KafkaUtil();
        TestReadUsb rt = new TestReadUsb("COM3", 115200);
        SerialPort serialPort1 = rt.openPort();
        rt.addListener(serialPort1, kaf);

    }


}
