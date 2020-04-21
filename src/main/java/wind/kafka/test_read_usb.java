package wind.kafka;

import gnu.io.*;
import wind.Until.ArrayUtils;
import wind.Interface.Read_usb;
import wind.Until.ShowUtils;
import wind.Until.KafkaUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

public class test_read_usb implements Read_usb {

    public static String portName;
    public static int baudrate;
    public static ArrayList<String> resu;


    public test_read_usb(String portName, int baudrate) {
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
    public byte[] readFromPort(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = {};
        try {
            in = serialPort.getInputStream();
            // 缓冲区大小为一个字节
            byte[] readBuffer = new byte[1];
            int bytesNum = in.read(readBuffer);
            while (bytesNum > 0) {
                bytes = ArrayUtils.concat(bytes, readBuffer);
                bytesNum = in.read(readBuffer);
            }
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
    public void addListener(SerialPort serialPort, KafkaUtil ka1,int count) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            // 给串口添加监听器
            serialPort.addEventListener(new SerialPortListener(() -> {
                byte[] data = null;
                String msg = null;


                try {
                    if (serialPort == null) {
                        ShowUtils.errorMessage("串口对象为空，监听失败！");
                    } else {
                        // 读取串口数据
                        data = readFromPort(serialPort);
                        msg = new String(data);


                        //ka1.insertTopic("test1", msg);
                        System.out.println(msg + "\r" + result.size());
                        //将数据流先暂存至list中，做简单缓存
                        result.add(msg);
                        //count数达到一定量级写入kafka
                        if (result.size() > count) {
                            ka1.insertTopic("test1", result);
                            //清理list，防止占用内存
                            result.clear();
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
        test_read_usb rt = new test_read_usb("COM3", 115200);
        SerialPort serialPort1 = rt.openPort();
        rt.addListener(serialPort1, kaf,3);

    }


}
