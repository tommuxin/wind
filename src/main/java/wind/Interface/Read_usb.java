package wind.Interface;

import gnu.io.SerialPort;
import wind.Until.KafkaUtil;

import java.io.IOException;
import java.util.ArrayList;

public interface Read_usb {

    /**
     * 打开串口
     *
     * @param portName
     *            端口名称
     * @param baudrate
     *            波特率
     * @return 串口对象
     * @throws PortInUseException
     *             串口已被占用
     */


    public SerialPort openPort();

    /**
     * 关闭串口
     *
     * @param serialport
     *            待关闭的串口对象
     */
    public  void closePort(SerialPort serialPort);


    /**
     * 从串口读取数据
     *
     * @param serialPort
     *            当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    /**
     * 从串口读取数据
     *
     * @param serialPort
     *            当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    public byte[] readFromPort(SerialPort serialPort) throws IOException;
    /**
     * 串口发送数据
     *
     * @param port
     *            串口对象
     * @param order
     *            需要发送的数据字节流
     */


    public void sendToPort(SerialPort serialPort, byte[] order);
    /**
     * 添加监听器，m默认
     *
     * @count 只要监听到数据，立马开始提交数据程序
     *
     * @param
     *
     */
    public void addListener(SerialPort serialPort, KafkaUtil ka1,String topic_name);


}
